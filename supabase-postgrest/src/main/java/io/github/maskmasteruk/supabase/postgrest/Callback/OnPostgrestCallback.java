package io.github.maskmasteruk.supabase.postgrest.Callback;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.postgrest.Object.PostgrestResult;

/**
 * Interface definition for a callback to be invoked when a PostgREST request is completed.
 *
 * <p>This interface provides methods to handle both successful and failed PostgREST operations.
 * It is commonly used in asynchronous calls to the Supabase PostgREST API.</p>
 */
public interface OnPostgrestCallback {
    /**
     * Called when the PostgREST request completes successfully.
     *
     * @param postgrestResult The result of the PostgREST operation, containing response data and metadata.
     */
    void onSuccess(PostgrestResult postgrestResult);

    /**
     * Called when the PostgREST request fails.
     *
     * @param supabaseError The error object containing details about the failure (e.g., status code, message).
     */
    void onFailure(SupabaseError supabaseError);
}
