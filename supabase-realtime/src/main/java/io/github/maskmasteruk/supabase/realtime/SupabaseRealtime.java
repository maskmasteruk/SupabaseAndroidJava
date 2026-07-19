package io.github.maskmasteruk.supabase.realtime;

import android.net.Uri;
import android.util.Base64;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLSocket;

import io.github.maskmasteruk.supabase.core.Network.HttpMethod;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.core.Supabase;

/**
 * The main entry point for Supabase Realtime functionality.
 *
 * <p>This class manages the lifecycle of realtime channels and handles the
 * underlying websocket connection to the Supabase Realtime server.
 * It follows the Singleton pattern.</p>
 *
 * <p>To use Realtime, get an instance and join a channel:</p>
 *
 * <pre>{@code
 * SupabaseRealtime realtime = SupabaseRealtime.getInstance();
 * SupabaseChannel<JSONObject> channel = realtime.joinChannel(
 *     "my_room",
 *     true,
 *     true,
 *     true,
 *     false,
 *     null
 * );
 * }</pre>
 */
public class SupabaseRealtime {
    private static volatile SupabaseRealtime instance;
    private final Uri projectUrl = Uri.parse(Supabase.getInstance().getSupabaseConfig().getProjectUrl());
    private final String host = projectUrl.getHost();
    private final int port = 443;
    private final ArrayList<SupabaseChannel<?>> supabaseChannels;

    /**
     * Private constructor for Singleton.
     */
    private SupabaseRealtime() {
        supabaseChannels = new ArrayList<>();
    }

    /**
     * Returns the singleton instance of {@code SupabaseRealtime}.
     *
     * @return The {@code SupabaseRealtime} instance.
     */
    public static SupabaseRealtime getInstance() {
        if (instance == null) {
            synchronized (SupabaseRealtime.class) {
                if (instance == null) {
                    instance = new SupabaseRealtime();
                }
            }
        }
        return instance;
    }

    /**
     * Joins a realtime channel with the specified configuration.
     *
     * <p>This method initiates the websocket connection (if not already connected)
     * and sends a join request for the given channel topic.</p>
     *
     * @param channelName The name of the channel to join (e.g., "chat").
     * @param broadcastAck Whether to request acknowledgments for broadcast messages.
     * @param broadcastSelf Whether to receive broadcasts sent by this client.
     * @param presence Whether to enable presence features for this channel.
     * @param isPrivate Whether the channel requires authentication.
     * @param postgresChanges Optional list of database change subscriptions.
     * @return A {@link SupabaseChannel} instance representing the joined channel.
     *
     * <pre>{@code
     * ArrayList<PostgresChange> changes = new ArrayList<>();
     * changes.add(new PostgresChange(PostgresChangeEvent.ALL, "public", "users", null));
     *
     * SupabaseChannel<JSONObject> channel = SupabaseRealtime.getInstance().joinChannel(
     *     "updates",
     *     false,
     *     true,
     *     true,
     *     true,
     *     changes
     * );
     * }</pre>
     */
    public SupabaseChannel<JSONObject> joinChannel(String channelName, boolean broadcastAck, boolean broadcastSelf, boolean presence, boolean isPrivate, ArrayList<PostgresChange> postgresChanges) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        String topic = "realtime:" + channelName;

        SupabaseChannel<JSONObject> supabaseChannel = new SupabaseChannel<>(System.currentTimeMillis(), topic, executorService);
        executorService.execute(() -> {
            try {
                WebsocketResult websocketResult = connectToWebsocket();

                if (!websocketResult.response.contains("HTTP/1.1 101 Switching Protocols")) {
                    supabaseChannel.onError(new SupabaseError("Unable to connect to the web socket. \nRESPONSE: " + websocketResult.response));
                    supabaseChannel.onClose();
                    return;
                }

                supabaseChannel.setWebsocketResult(websocketResult);

                EventBuilder.JoinRequest.Builder joinRequest = new EventBuilder.JoinRequest.Builder();
                joinRequest
                        .topic(topic)
                        .isPrivate(isPrivate)
                        .broadcastAck(broadcastAck)
                        .broadcastSelf(broadcastSelf)
                        .presenceEnabled(presence);

                if (postgresChanges != null) {
                    joinRequest.setPostgresChange(postgresChanges);
                }

                if (isPrivate || supabaseChannel.getAccessToken() != null) {
                    if (supabaseChannel.getAccessToken() == null) {
                        supabaseChannel.onError(new SupabaseError("Invalid Auth for Websocket connection in private channel"));
                        return;
                    }
                    joinRequest.accessToken(supabaseChannel.getAccessToken());
                    supabaseChannel.listenToSupabaseConfigChange();
                }

                joinRequest.ref(supabaseChannel.getReference());
                joinRequest.join_ref(supabaseChannel.getChannelId());

                Websocket.writeAsMaskedFrame(websocketResult.outputStream, joinRequest.build().toJsonString());

                supabaseChannel.sendPeriodicHeartbeat();

                supabaseChannel.onConnected();
            } catch (IOException e) {
                supabaseChannel.onError(new SupabaseError(e));
            }
        });
        supabaseChannels.add(supabaseChannel);
        return supabaseChannel;
    }

    /**
     * Closes all active realtime channels and shuts down their connections.
     *
     * <pre>{@code
     * SupabaseRealtime.getInstance().closeAllChannel();
     * }</pre>
     */
    public void closeAllChannel() {
        supabaseChannels.forEach(supabaseChannel -> {
            if (!supabaseChannel.isClosed()) {
                supabaseChannel.close();
            }
        });
    }

    /**
     * Internal method to establish a websocket connection to the Supabase server.
     *
     * @return A {@link WebsocketResult} containing the socket and initial response.
     * @throws IOException if a network error occurs.
     */
    @NonNull
    private WebsocketResult connectToWebsocket() throws IOException {
        SSLSocket socket = Websocket.createSocket(host, port);
        byte[] random = new byte[16];
        new SecureRandom().nextBytes(random);

        String key = Base64.encodeToString(random, Base64.NO_WRAP);
        Uri.Builder uriBuilder = Uri.parse("").buildUpon();
        uriBuilder.appendPath(REALTIME_END_POINTS.REALTIME);
        uriBuilder.appendPath(REALTIME_END_POINTS.VERSION);
        uriBuilder.appendPath(REALTIME_END_POINTS.WEBSOCKET);
        uriBuilder.appendQueryParameter("apikey", Supabase.getInstance().getSupabaseConfig().getProjectPublishableKey());

        String request = SupabaseWebsocket.buildWebsocketRequest(HttpMethod.GET, uriBuilder.build().toString(), key, host);

        OutputStream outputStream = socket.getOutputStream();
        InputStream inputStream = socket.getInputStream();
        Websocket.writeText(outputStream, request);
        String response = Websocket.readText(inputStream);
        return new WebsocketResult(socket, outputStream, inputStream, response);
    }

}
