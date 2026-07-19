package io.github.maskmasteruk.supabase.storage.Listeners;

/**
 * Listener interface for progress updates.
 * <p>
 * Purpose: Provides periodic updates on the progress of a long-running operation.
 * </p>
 */
public interface OnProgressListener {
    /**
     * Called when progress is updated.
     * @param progress The progress percentage (0-100).
     */
    void onProgress(int progress);
}
