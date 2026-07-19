package io.github.maskmasteruk.supabase.auth.admin.Callback;

import io.github.maskmasteruk.supabase.auth.admin.Object.SupabaseLink;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Callback interface for receiving a generated authentication link.
 */
public interface OnGetLinkCallback {
    /**
     * Called when the link is successfully generated.
     *
     * @param supabaseLink The generated {@link SupabaseLink}.
     */
    void onSuccess(SupabaseLink supabaseLink);

    /**
     * Called when an error occurs during link generation.
     *
     * @param supabaseError The {@link SupabaseError} containing error details.
     */
    void onError(SupabaseError supabaseError);
}
