package io.github.maskmasteruk.supabase.realtime;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import io.github.maskmasteruk.supabase.core.Callback.OnSupabaseConfigChangeCallback;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.core.Supabase;
import io.github.maskmasteruk.supabase.realtime.Callback.OnCompleteCallback;
import io.github.maskmasteruk.supabase.realtime.Callback.OnRealtimeCallback;
import io.github.maskmasteruk.supabase.realtime.Enum.PhoenixEvent;
import io.github.maskmasteruk.supabase.realtime.Enum.PostgresChangeEvent;
import io.github.maskmasteruk.supabase.realtime.Enum.PresenceEvent;
import io.github.maskmasteruk.supabase.realtime.Enum.PresenceTypes;

/**
 * Represents a logical connection to a specific topic in Supabase Realtime.
 *
 * <p>A channel is used to broadcast messages, track presence, and listen to
 * database changes. It manages its own heartbeat to keep the connection alive
 * and handles authentication token refreshes.</p>
 *
 * <p>Channels are created via {@link SupabaseRealtime#joinChannel(String, boolean, boolean, boolean, boolean, ArrayList)}.</p>
 *
 * @param <T> The type of data handled by this channel.
 *
 * <pre>{@code
 * SupabaseChannel<Void> channel = realtime.channel("room-1");
 *
 * channel.addOnRealtimeCallback(new OnRealtimeCallback<Void>() {
 *     @Override
 *     public void onConnected() {
 *         System.out.println("Joined channel!");
 *     }
 *     // ...
 * });
 *
 * channel.subscribe();
 * }</pre>
 */
public class SupabaseChannel<T> {

    private static final int heartbeatPeriodInSeconds = 30;

    AtomicInteger refInteger = new AtomicInteger(0);
    private final String topic;
    private final Long channelId;
    private final ArrayList<OnRealtimeCallback<T>> onRealtimeCallbacks;
    private WebsocketResult websocketResult;

    private final ScheduledExecutorService heartBeatExecutorService;
    private final ExecutorService executorService;
    private final ExecutorService senderExecutorService;
    private ExecutorService receiverExecutorService;
    private SupabaseRealtimeListenerTask supabaseRealtimeListenerTask;

    private final AtomicReference<ScheduledFuture<?>> heartBeatAtomicReference = new AtomicReference<>(null);

    private final AtomicReference<String> accessTokenReference = new AtomicReference<>(Supabase.getInstance().getSupabaseConfig().getBearer());

    private boolean closed = false;

    OnSupabaseConfigChangeCallback supabaseConfigChangeCallback;


    /**
     * Internal constructor for SupabaseChannel.
     *
     * @param channelId Unique ID for the channel.
     * @param topic The topic name.
     * @param executorService Executor for background tasks.
     */
    public SupabaseChannel(Long channelId, String topic, ExecutorService executorService) {
        this.channelId = channelId;
        this.topic = topic;
        this.executorService = executorService;
        senderExecutorService = Executors.newSingleThreadExecutor();
        heartBeatExecutorService = Executors.newSingleThreadScheduledExecutor();
        onRealtimeCallbacks = new ArrayList<>();
    }

    /**
     * Adds a callback to listen for channel lifecycle events.
     *
     * @param onRealtimeCallback The callback to add.
     * @return This channel instance for chaining.
     *
     * <pre>{@code
     * channel.addOnRealtimeCallback(callback);
     * }</pre>
     */
    public SupabaseChannel<T> addOnRealtimeCallback(OnRealtimeCallback<T> onRealtimeCallback) {
        onRealtimeCallbacks.add(onRealtimeCallback);
        return this;
    }

    /**
     * Internal method to trigger error callbacks.
     *
     * @param supabaseError The error that occurred.
     */
    public void onError(SupabaseError supabaseError) {
        onRealtimeCallbacks.forEach(tOnRealtimeCallback -> tOnRealtimeCallback.onError(supabaseError));
    }

    /**
     * Internal method to trigger connected callbacks.
     */
    public void onConnected() {
        onRealtimeCallbacks.forEach(OnRealtimeCallback::onConnected);
    }

    /**
     * Starts the periodic heartbeat to keep the websocket connection alive.
     */
    public void sendPeriodicHeartbeat() {
        if (heartBeatAtomicReference.get() == null) {
            heartBeatAtomicReference.set(heartBeatExecutorService.scheduleWithFixedDelay(() -> {
                try {
                    Websocket.writeAsMaskedFrame(websocketResult.socket.getOutputStream(), new EventBuilder.HeartbeatRequest(getReference()).toJsonString());
                } catch (IOException e) {
                    heartBeatAtomicReference.get().cancel(false);
                }
            }, heartbeatPeriodInSeconds, heartbeatPeriodInSeconds, TimeUnit.SECONDS));
        }
    }

    /**
     * Closes the channel and shuts down all associated executor services.
     */
    public void close() {
        executorService.shutdownNow();
        senderExecutorService.shutdownNow();
        if (heartBeatAtomicReference.get() != null) {
            heartBeatAtomicReference.get().cancel(true);
        }
        heartBeatExecutorService.shutdownNow();
        stopListening();

        if (supabaseConfigChangeCallback != null) {
            Supabase.getInstance().removeOnSupabaseConfigChangeCallbacks(supabaseConfigChangeCallback);
            supabaseConfigChangeCallback = null;
        }
        try {
            if (executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                onClose();
            }
        } catch (InterruptedException ignored) {

        }
    }

    /**
     * Checks if the channel is closed.
     *
     * @return true if the channel is closed.
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Internal method to trigger close callbacks.
     */
    public void onClose() {
        closed = true;
        onRealtimeCallbacks.forEach(OnRealtimeCallback::onClose);
    }

    /**
     * Broadcasts a message to all subscribers of this channel.
     *
     * @param event The event name.
     * @param payload The message payload.
     * @return A task representing the broadcast operation.
     * @throws SupabaseError if the channel is not connected.
     *
     * <pre>{@code
     * channel.broadcast("chat", new JSONObject().put("msg", "hello"));
     * }</pre>
     */
    public SupabaseBroadcastTask broadcast(String event, JSONObject payload) {
        if (websocketResult == null) {
            throw new SupabaseError("Cannot broadcast message: channel is not connected. Connect to the channel before broadcasting.");
        }
        return new SupabaseBroadcastTask(senderExecutorService, websocketResult.outputStream, topic, event, payload, channelId, refInteger.getAndIncrement());
    }

    /**
     * Leaves the channel gracefully.
     *
     * @return A task representing the leave operation.
     * @throws SupabaseError if the channel is not connected.
     *
     * <pre>{@code
     * channel.leave();
     * }</pre>
     */
    public SupabaseLeaveTask leave() {
        if (websocketResult == null) {
            throw new SupabaseError("Cannot leave channel: channel is not connected. Connect to the channel before leaving.");
        }
        SupabaseLeaveTask supabaseLeaveTask = new SupabaseLeaveTask(senderExecutorService, websocketResult.outputStream, topic, refInteger.getAndIncrement(), channelId);
        supabaseLeaveTask.addOnCompleteCallback(new OnCompleteCallback<>() {
            @Override
            public void OnSuccess(Void unused) {
                close();
            }

            @Override
            public void onError(SupabaseError supabaseError) {

            }
        });

        return supabaseLeaveTask;
    }

    /**
     * Starts tracking presence for the current client in this channel.
     *
     * @param payload Metadata about the client (e.g., user ID, status).
     * @return A task representing the presence tracking operation.
     *
     * <pre>{@code
     * channel.presenceTrack(new JSONObject().put("user", "Alice"));
     * }</pre>
     */
    public SupabasePresenceTask presenceTrack(JSONObject payload) {
        return new SupabasePresenceTask(senderExecutorService, websocketResult.outputStream, topic, PresenceTypes.TRACK, payload, channelId, refInteger.getAndIncrement());
    }

    /**
     * Stops tracking presence for the current client.
     *
     * @return A task representing the presence untracking operation.
     *
     * <pre>{@code
     * channel.presenceUnTrack();
     * }</pre>
     */
    public SupabasePresenceTask presenceUnTrack() {
        return new SupabasePresenceTask(senderExecutorService, websocketResult.outputStream, topic, PresenceTypes.UNTRACK, null, channelId, refInteger.getAndIncrement());
    }

    /**
     * Starts listening to all messages on this channel.
     *
     * @return A task that handles incoming messages.
     * @throws SupabaseError if a listener is already registered.
     *
     * <pre>{@code
     * channel.listen().onReceive(message -> {
     *     System.out.println("Received: " + message);
     * });
     * }</pre>
     */
    public SupabaseRealtimeListenerTask listen() {
        if (receiverExecutorService != null) {
            throw new SupabaseError("A listener is already registered. Remove the existing listener before adding a new one.");
        }
        receiverExecutorService = Executors.newSingleThreadExecutor();
        supabaseRealtimeListenerTask = new SupabaseRealtimeListenerTask(receiverExecutorService, websocketResult.inputStream);
        return supabaseRealtimeListenerTask;
    }

    /**
     * Starts listening to specific events on this channel.
     *
     * @param events List of event names to listen for.
     * @return A task that handles incoming messages for the specified events.
     * @throws SupabaseError if a listener is already registered.
     */
    public SupabaseRealtimeListenerTask listen(ArrayList<String> events) {
        if (receiverExecutorService != null) {
            throw new SupabaseError("A listener is already registered. Remove the existing listener before adding a new one.");
        }
        receiverExecutorService = Executors.newSingleThreadExecutor();
        supabaseRealtimeListenerTask = new SupabaseRealtimeListenerTask(receiverExecutorService, websocketResult.inputStream, events);
        return supabaseRealtimeListenerTask;
    }

    /**
     * Starts listening to events defined by various enum types or strings.
     *
     * @param objects Event identifiers (can be {@link PhoenixEvent}, {@link PresenceEvent},
     *                {@link PostgresChangeEvent}, or String).
     * @return A task that handles incoming messages.
     */
    public SupabaseRealtimeListenerTask listen(Object... objects) {
        ArrayList<String> events = new ArrayList<>();
        for (Object object : objects) {
            if (object instanceof PhoenixEvent) {
                events.add(((PhoenixEvent) object).getEventValue());
            } else if (object instanceof PresenceEvent) {
                events.add(((PresenceEvent) object).getEventValue());
            } else if (object instanceof PostgresChangeEvent) {
                events.add(((PostgresChangeEvent) object).getEventValue());
            } else {
                events.add(object.toString());
            }
        }
        return listen(events);
    }

    /**
     * Starts listening to specific Phoenix protocol events.
     *
     * @param phoenixEvents The events to listen for.
     * @return A task that handles incoming messages.
     */
    public SupabaseRealtimeListenerTask listen(PhoenixEvent... phoenixEvents) {
        return listen(
                new ArrayList<>(Arrays.stream(phoenixEvents).map(PhoenixEvent::getEventValue).collect(Collectors.toList()))
        );
    }

    /**
     * Starts listening to specific Postgres change events.
     *
     * @param postgresChangeEvents The database events to listen for.
     * @return A task that handles incoming messages.
     */
    public SupabaseRealtimeListenerTask listen(PostgresChangeEvent... postgresChangeEvents) {
        return listen(
                new ArrayList<>(Arrays.stream(postgresChangeEvents).map(PostgresChangeEvent::getEventValue).collect(Collectors.toList()))
        );
    }

    /**
     * Starts listening to specific Presence events.
     *
     * @param presenceEvents The presence events to listen for.
     * @return A task that handles incoming messages.
     */
    public SupabaseRealtimeListenerTask listen(PresenceEvent... presenceEvents) {
        return listen(
                new ArrayList<>(Arrays.stream(presenceEvents).map(PresenceEvent::getEventValue).collect(Collectors.toList()))
        );
    }

    /**
     * Stops the active listener and shuts down the receiver executor.
     */
    public void stopListening() {
        if (supabaseRealtimeListenerTask != null) {
            supabaseRealtimeListenerTask.terminate();
        }
        receiverExecutorService = null;
    }

    /**
     * Gets and increments the message reference ID.
     *
     * @return The next reference ID.
     */
    public int getReference() {
        return refInteger.getAndIncrement();
    }

    /**
     * Gets the unique channel ID.
     *
     * @return The channel ID.
     */
    public Long getChannelId() {
        return channelId;
    }

    /**
     * Gets the current access token used by this channel.
     *
     * @return The access token string.
     */
    public String getAccessToken() {
        return accessTokenReference.get();
    }

    /**
     * Internal method to set the websocket connection result.
     *
     * @param websocketResult The connection result.
     */
    public void setWebsocketResult(WebsocketResult websocketResult) {
        this.websocketResult = websocketResult;
    }

    /**
     * Starts listening for global Supabase configuration changes (e.g., auth token updates).
     * When the token changes, it automatically sends an ACCESS_TOKEN update to the server.
     */
    void listenToSupabaseConfigChange() {
        supabaseConfigChangeCallback = () -> {
            String bearer = Supabase.getInstance().getSupabaseConfig().getBearer();
            if (bearer == null) {
                close();
            } else if (!bearer.equals(accessTokenReference.get())) {
                accessTokenReference.set(bearer);
                Executors.newSingleThreadScheduledExecutor().execute(() -> {
                    try {
                        Websocket.writeAsMaskedFrame(websocketResult.socket.getOutputStream(), new EventBuilder.AccessTokenRequest(topic, bearer, getReference()).toJsonString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        };
        Supabase.getInstance().addOnSupabaseConfigChangeCallbacks(supabaseConfigChangeCallback);
    }
}
