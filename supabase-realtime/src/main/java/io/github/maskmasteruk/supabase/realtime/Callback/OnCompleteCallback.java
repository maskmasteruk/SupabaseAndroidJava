package io.github.maskmasteruk.supabase.realtime.Callback;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Callback interface for operations that complete with a result or fail with an error.
 *
 * <p>Commonly used for one-time operations like joining a channel or sending a single message.</p>
 *
 * @param <T> The type of the result on success.
 *
 * <pre>{@code
 * channel.join(new OnCompleteCallback<String>() {
 *     @Override
 *     public void OnSuccess(String message) {
 *         System.out.println("Joined channel: " + message);
 *     }
 *
 *     @Override
 *     public void onError(SupabaseError error) {
 *         System.err.println("Failed to join: " + error.getMessage());
 *     }
 * });
 * }</pre>
 */
public interface OnCompleteCallback<T> {
    /**
     * Invoked when the operation completes successfully.
     *
     * @param t The result of the operation.
     */
    void OnSuccess(T t);

    /**
     * Invoked when the operation fails.
     *
     * @param supabaseError The error that caused the failure.
     */
    void onError(SupabaseError supabaseError);
}
