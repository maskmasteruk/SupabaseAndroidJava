package io.github.maskmasteruk.supabase.realtime;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.realtime.Callback.OnCompleteCallback;

/**
 * Represents an asynchronous task to broadcast a message to a realtime channel.
 *
 * <p>Broadcast allows you to send low-latency messages to other clients subscribed
 * to the same channel topic.</p>
 *
 * <pre>{@code
 * channel.broadcast("cursor_move", new JSONObject().put("x", 100).put("y", 200))
 *        .addOnCompleteCallback(new OnCompleteCallback<JSONObject>() {
 *            @Override
 *            public void OnSuccess(JSONObject payload) {
 *                System.out.println("Message broadcasted!");
 *            }
 *            // ...
 *        });
 * }</pre>
 */
public class SupabaseBroadcastTask {
    private final ArrayList<OnCompleteCallback<JSONObject>> onCompleteCallbacks;

    /**
     * Internal constructor for SupabaseBroadcastTask.
     *
     * <p>Initiates the broadcast process immediately upon creation.</p>
     *
     * @param senderExecutorService Executor to handle sending the message.
     * @param outputStream The socket's output stream.
     * @param topic The channel topic.
     * @param event The event name for the broadcast.
     * @param jsonObject The payload to broadcast.
     * @param channelId The join reference ID.
     * @param refInteger The message reference ID.
     */
    public SupabaseBroadcastTask(ExecutorService senderExecutorService, OutputStream outputStream, String topic, String event, JSONObject jsonObject, Long channelId, int refInteger) {
        onCompleteCallbacks = new ArrayList<>();
        broadcast(senderExecutorService, outputStream, topic, event, jsonObject, channelId, refInteger);
    }

    /**
     * Adds a callback to be invoked when the broadcast message is sent.
     *
     * @param onCompleteCallback The callback to add.
     * @return This task instance for chaining.
     */
    public SupabaseBroadcastTask addOnCompleteCallback(OnCompleteCallback<JSONObject> onCompleteCallback) {
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
     *
     * @param jsonObject The broadcasted payload.
     */
    public void onSent(JSONObject jsonObject) {
        onCompleteCallbacks.forEach(tOnCompleteCallback -> tOnCompleteCallback.OnSuccess(jsonObject));
    }

    /**
     * Executes the broadcast request on a background thread.
     */
    private void broadcast(ExecutorService senderExecutorService, OutputStream outputStream, String topic, String event, JSONObject jsonObject, Long channelId, int refInteger) {
        senderExecutorService.execute(() -> {
            EventBuilder.BroadcastRequest broadcastRequest = new EventBuilder.BroadcastRequest(topic, event, jsonObject, refInteger, channelId);
            try {
                Websocket.writeAsMaskedFrame(outputStream, broadcastRequest.toJsonString());
                onSent(jsonObject);
            } catch (IOException e) {
                onError(new SupabaseError(e));
            }
        });
    }
}
