package io.github.maskmasteruk.supabase.storage.Callback;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.storage.Bucket;

/**
 * Callback interface for retrieving a single bucket.
 * <p>
 * Purpose: Receives the {@link Bucket} object upon successful retrieval.
 * </p>
 */
public interface OnGetBucket {
    /**
     * Called when the bucket is successfully retrieved.
     * @param bucket The retrieved bucket object.
     */
    void onSuccess(Bucket bucket);

    /**
     * Called when retrieval fails.
     * @param supabaseError The error details.
     */
    void onFailure(SupabaseError supabaseError);
}
