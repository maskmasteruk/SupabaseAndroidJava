package io.github.maskmasteruk.supabase.storage.Listeners;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.storage.Tasks.Task;

/**
 * Interface for operation completion listeners.
 * <p>
 * Purpose: This listener is used to be notified when a {@link Task} finishes,
 * whether it succeeded or failed.
 * </p>
 * <p>
 * Intended implementations: Implemented by users to handle task completion.
 * </p>
 *
 * @param <T> The result type of the task.
 */
public interface OnCompleteListener<T> {
    /**
     * Called when the operation completes successfully.
     * @param t The result of the operation.
     */
    void onSuccess(T t);

    /**
     * Called when the operation fails.
     *
     * @param supabaseError The {@link SupabaseError} describing the failure.
     */
    void onError(SupabaseError supabaseError);
}
