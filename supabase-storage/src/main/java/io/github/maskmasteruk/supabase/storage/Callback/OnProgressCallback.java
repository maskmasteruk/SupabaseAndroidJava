package io.github.maskmasteruk.supabase.storage.Callback;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Callback interface for tracking operation progress.
 * <p>
 * Purpose: Provides progress updates, success notification, and error handling
 * for long-running operations like file transfers.
 * </p>
 */
public interface OnProgressCallback {
    /**
     * Called periodically with progress updates.
     * @param progress The progress percentage (0-100).
     */
    void onProgress(int progress);

    /** Called when the operation completes successfully. */
    void onSuccess();

    /**
     * Called when the operation fails.
     * @param supabaseError The error details.
     */
    void onError(SupabaseError supabaseError);
}
