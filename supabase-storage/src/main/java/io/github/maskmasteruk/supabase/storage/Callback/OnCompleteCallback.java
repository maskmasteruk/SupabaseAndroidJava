package io.github.maskmasteruk.supabase.storage.Callback;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Interface for operation completion callbacks.
 * <p>
 * Purpose: This callback is used for operations that do not return a result object upon success,
 * such as deleting or updating a bucket.
 * </p>
 * <p>
 * Intended implementations: Typically implemented as anonymous classes by the library user.
 * </p>
 * <p>
 * Usage:
 * <pre>
 * storage.deleteBucket("my-bucket", new OnCompleteCallback() {
 *     &#64;Override
 *     public void onSuccess() {
 *         // Handle success
 *     }
 *     &#64;Override
 *     public void onError(SupabaseError error) {
 *         // Handle error
 *     }
 * });
 * </pre>
 * </p>
 */
public interface OnCompleteCallback {
    /**
     * Called when the operation completes successfully.
     */
    void onSuccess();

    /**
     * Called when the operation fails.
     *
     * @param supabaseError The error details.
     */
    void onError(SupabaseError supabaseError);
}
