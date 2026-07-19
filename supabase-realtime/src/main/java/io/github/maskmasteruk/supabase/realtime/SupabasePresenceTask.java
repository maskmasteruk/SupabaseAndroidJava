package io.github.maskmasteruk.supabase.realtime;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.realtime.Callback.OnCompleteCallback;
import io.github.maskmasteruk.supabase.realtime.Enum.PresenceTypes;

/**
 * Represents an asynchronous task to track or untrack presence in a realtime channel.
 *
 * <p>Presence allows you to see who is online and share state (like status or cursor
 * position) with other users in the same channel.</p>
 *
 * <pre>{@code
 * channel.presenceTrack(new JSONObject().put("status", "online"))
 *        .addOnCompleteCallback(new OnCompleteCallback<Void>() {
 *            @Override
 *            public void OnSuccess(Void result) {
 *                System.out.println("Presence tracked!");
 *            }
 *            // ...
 *        });
 * }</pre>
 */
public class SupabasePresenceTask {
    private final ArrayList<OnCompleteCallback<Void>> onCompleteCallbacks;

    /**
     * Internal constructor for SupabasePresenceTask.
     *
     * <p>Initiates the presence update process immediately upon creation.</p>
     *
     * @param senderExecutorService Executor to handle sending the message.
     * @param outputStream The socket's output stream.
     * @param topic The channel topic.
     * @param type The type of presence action (TRACK or UNTRACK).
     * @param jsonObject The presence metadata to share.
     * @param channelId The join reference ID.
     * @param refInteger The message reference ID.
     */
    public SupabasePresenceTask(ExecutorService senderExecutorService, OutputStream outputStream, String topic, PresenceTypes type, JSONObject jsonObject, Long channelId, int refInteger) {
        onCompleteCallbacks = new ArrayList<>();
        presenceTrack(senderExecutorService, outputStream, topic, type, jsonObject, channelId, refInteger);
    }

    /**
     * Adds a callback to be invoked when the presence update is sent.
     *
     * @param onCompleteCallback The callback to add.
     * @return This task instance for chaining.
     */
    public SupabasePresenceTask addOnCompleteCallback(OnCompleteCallback<Void> onCompleteCallback) {
        onCompleteCallbacks.add(onCompleteCallback);
        return this;
    }

    /**
     * Internal method to trigger error callbacks.
     *
     * @param supabaseError The error that occurred.
     */
    public void onError(SupabaseError supabaseError) {
        onCompleteCallbacks.forEach(tOnCompleteCallback -> tOnCompleteCallback.onError(supabaseError));
    }

    /**
     * Internal method to trigger success callbacks.
     */
    public void onSent() {
        onCompleteCallbacks.forEach(tOnCompleteCallback -> tOnCompleteCallback.OnSuccess(null));
    }

    /**
     * Executes the presence update request on a background thread.
     */
    private void presenceTrack(ExecutorService senderExecutorService, OutputStream outputStream, String topic, PresenceTypes type, JSONObject jsonObject, Long channelId, int refInteger) {
        senderExecutorService.execute(() -> {
            EventBuilder.PresenceRequest presenceRequest = new EventBuilder.PresenceRequest(topic, type, jsonObject, refInteger, channelId);
            try {
                Websocket.writeAsMaskedFrame(outputStream, presenceRequest.toJsonString());
                onSent();
            } catch (IOException e) {
                onError(new SupabaseError(e));
            }
        });
    }
}
