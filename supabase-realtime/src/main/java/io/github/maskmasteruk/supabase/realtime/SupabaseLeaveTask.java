package io.github.maskmasteruk.supabase.realtime;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.realtime.Callback.OnCompleteCallback;

/**
 * Represents an asynchronous task to leave a realtime channel.
 *
 * <p>This task handles the sending of the leave request over the websocket and
 * provides callbacks for success or failure.</p>
 *
 * <pre>{@code
 * channel.leave().addOnCompleteCallback(new OnCompleteCallback<Void>() {
 *     @Override
 *     public void OnSuccess(Void result) {
 *         System.out.println("Left channel successfully");
 *     }
 *
 *     @Override
 *     public void onError(SupabaseError error) {
 *         System.err.println("Failed to leave channel: " + error.getMessage());
 *     }
 * });
 * }</pre>
 */
public class SupabaseLeaveTask {
    private final ArrayList<OnCompleteCallback<Void>> onCompleteCallbacks;

    /**
     * Internal constructor for SupabaseLeaveTask.
     *
     * <p>Initiates the leave process immediately upon creation.</p>
     *
     * @param senderExecutorService Executor to handle sending the message.
     * @param outputStream The socket's output stream.
     * @param topic The topic to leave.
     * @param refInteger The message reference ID.
     * @param channelId The join reference ID.
     */
    public SupabaseLeaveTask(ExecutorService senderExecutorService, OutputStream outputStream, String topic, int refInteger, Long channelId) {
        onCompleteCallbacks = new ArrayList<>();
        leave(senderExecutorService, outputStream, topic, refInteger, channelId);
    }

    /**
     * Adds a callback to be invoked when the leave operation completes.
     *
     * @param onCompleteCallback The callback to add.
     * @return This task instance for chaining.
     */
    public SupabaseLeaveTask addOnCompleteCallback(OnCompleteCallback<Void> onCompleteCallback) {
        onCompleteCallbacks.add(onCompleteCallback);
        return this;
    }

    /**
     * Internal method to trigger error callbacks.
     *
     * @param supabaseError The error that occurred.
     */
    private void onError(SupabaseError supabaseError) {
        onCompleteCallbacks.forEach(tOnCompleteCallback -> tOnCompleteCallback.onError(supabaseError));
    }

    /**
     * Internal method to trigger success callbacks.
     */
    private void onSuccess() {
        onCompleteCallbacks.forEach(tOnCompleteCallback -> tOnCompleteCallback.OnSuccess(null));
    }

    /**
     * Executes the leave request on a background thread.
     */
    private void leave(ExecutorService senderExecutorService, OutputStream outputStream, String topic, int refInteger, Long channelId) {
        senderExecutorService.execute(() -> {
            EventBuilder.LeaveRequest leaveRequest = new EventBuilder.LeaveRequest(topic, refInteger, channelId);
            try {
                Websocket.writeAsMaskedFrame(outputStream, leaveRequest.toJsonString());
                onSuccess();
            } catch (IOException e) {
                onError(new SupabaseError(e));
            }
        });
    }
}
