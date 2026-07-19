package io.github.maskmasteruk.supabase.auth.admin.Callback;

import java.util.List;

import io.github.maskmasteruk.supabase.auth.Object.SupabaseUser;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Callback interface for receiving a list of users from administrative operations.
 */
public interface OnGetUsersCallback {
    /**
     * Called when the list of users is successfully retrieved.
     *
     * @param supabaseUsers The list of {@link SupabaseUser} objects.
     */
    void onSuccess(List<SupabaseUser> supabaseUsers);

    /**
     * Called when an error occurs during the request.
     *
     * @param supabaseError The {@link SupabaseError} containing error details.
     */
    void onError(SupabaseError supabaseError);
}
