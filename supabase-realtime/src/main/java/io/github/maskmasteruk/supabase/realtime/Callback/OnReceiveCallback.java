package io.github.maskmasteruk.supabase.realtime.Callback;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Generic callback interface for receiving asynchronous data or errors.
 *
 * <p>This interface is used throughout the realtime library to handle the results
 * of asynchronous operations such as message reception or state updates.</p>
 *
 * @param <T> The type of data being received.
 *
 * <pre>{@code
 * OnReceiveCallback<String> callback = new OnReceiveCallback<String>() {
 *     @Override
 *     public void onReceive(String data) {
 *         System.out.println("Received: " + data);
 *     }
 *
 *     @Override
 *     public void onError(SupabaseError error) {
 *         System.err.println("Error: " + error.getMessage());
 *     }
 * };
 * }</pre>
 */
public interface OnReceiveCallback<T> {
    /**
     * Invoked when data is successfully received.
     *
     * <p>This method is typically called on a background thread when a message
     * arrives over the websocket.</p>
     *
     * @param t The received data.
     */
    void onReceive(T t);

    /**
     * Invoked when an error occurs during the operation or message reception.
     *
     * @param supabaseError The error details.
     */
    void onError(SupabaseError supabaseError);
}
