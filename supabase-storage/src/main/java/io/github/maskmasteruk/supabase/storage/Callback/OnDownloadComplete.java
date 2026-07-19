package io.github.maskmasteruk.supabase.storage.Callback;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Callback interface for download operations.
 * <p>
 * Purpose: Provides notifications when a file download has finished successfully or failed.
 * </p>
 */
public interface OnDownloadComplete {
    /** Called when the download is complete. */
    void onSuccess();
    /**
     * Called when the download fails.
     * @param supabaseError The error details.
     */
    void onFailure(SupabaseError supabaseError);
}
