package io.github.maskmasteruk.supabase.storage.Listeners;


import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Listener interface for operation failures.
 * <p>
 * Purpose: Provides notification when a task fails.
 * </p>
 */
public interface OnFailureListener {
    /**
     * Called when the operation fails.
     * @param supabaseError The error details.
     */
    void onFailure(SupabaseError supabaseError);
}
