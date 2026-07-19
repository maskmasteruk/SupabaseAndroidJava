package io.github.maskmasteruk.supabase.storage.Listeners;

/**
 * Listener interface for successful operation completion.
 * <p>
 * Purpose: Provides notification when a task succeeds and returns a result.
 * </p>
 *
 * @param <T> The type of the result object.
 */
public interface OnSuccessListener<T> {
    /**
     * Called when the operation succeeds.
     * @param t The result of the operation.
     */
    void onSuccess(T t);
}
