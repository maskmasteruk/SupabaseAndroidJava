package io.github.maskmasteruk.supabase.auth.Callback;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Interface for general operation completion callbacks.
 * <p>
 * This callback is used for operations that do not return a specific object upon success,
 * such as sending a reset email, signing out, or resending verification codes.
 * <p>
 * <b>Invocation Timing:</b> Invoked once the requested operation has been acknowledged as successful or failed.
 * <p>
 * <b>Threading:</b> Invoked on a background thread. UI updates must be handled on the main thread.
 *
 * @since 1.0.0
 */
public interface OnCompleteCallback {
    /**
     * Called when the operation completes successfully.
     */
    void onSuccess();

    /**
     * Called when the operation fails due to network errors or validation issues.
     *
     * @param supabaseError The {@link SupabaseError} describing the failure.
     */
    void onError(SupabaseError supabaseError);
}

