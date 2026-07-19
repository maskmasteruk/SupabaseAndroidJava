package io.github.maskmasteruk.supabase.core.Callback;

/**
 * Interface for receiving progress updates during long-running operations.
 *
 * Responsibilities:
 * - Providing a standard way to report progress (e.g., file uploads or downloads).
 *
 * Usage:
 * storage.uploadFile(path, new OnProgressCallback() {
 *     @Override
 *     public void onProgress(long progress) {
 *         // Update UI progress bar
 *     }
 * });
 */
public interface OnProgressCallback {
    /**
     * Called when progress is made in an operation.
     *
     * @param progress The number of bytes transferred or a percentage, depending on the implementation.
     */
    void onProgress(long progress);
}
