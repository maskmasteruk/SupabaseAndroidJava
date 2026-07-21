package io.github.maskmasteruk.supabase.storage.Callback;

import java.util.ArrayList;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.storage.Bucket;

/**
 * Callback interface for retrieving multiple buckets.
 * <p>
 * Purpose: Receives a list of {@link Bucket} objects upon successful retrieval.
 * </p>
 */
public interface OnGetBuckets {
    /**
     * Called when buckets are successfully retrieved.
     * @param buckets The list of retrieved buckets.
     */
    void onSuccess(ArrayList<Bucket> buckets);

    /**
     * Called when retrieval fails.
     * @param supabaseError The error details.
     */
    void onFailure(SupabaseError supabaseError);
}
