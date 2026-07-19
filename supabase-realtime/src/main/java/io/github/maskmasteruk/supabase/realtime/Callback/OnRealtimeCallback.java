package io.github.maskmasteruk.supabase.realtime.Callback;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Callback interface for monitoring the lifecycle of a Realtime connection.
 *
 * <p>Implement this to receive notifications about connection status changes,
 * such as successful connections, closures, or errors.</p>
 *
 * @param <T> Reserved for future use.
 *
 * <pre>{@code
 * realtime.addCallback(new OnRealtimeCallback<Void>() {
 *     @Override
 *     public void onConnected() {
 *         System.out.println("Connected to Supabase Realtime!");
 *     }
 *
 *     @Override
 *     public void onClose() {
 *         System.out.println("Connection closed.");
 *     }
 *
 *     @Override
 *     public void onError(SupabaseError error) {
 *         System.err.println("Connection error: " + error.getMessage());
 *     }
 * });
 * }</pre>
 */
public interface OnRealtimeCallback<T> {
    /**
     * Invoked when the websocket connection is successfully established and authenticated.
     */
    void onConnected();

    /**
     * Invoked when the websocket connection is closed.
     */
    void onClose();

    /**
     * Invoked when an error occurs with the underlying connection.
     *
     * @param supabaseError The error details.
     */
    void onError(SupabaseError supabaseError);
}
