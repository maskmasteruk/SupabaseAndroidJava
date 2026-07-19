package io.github.maskmasteruk.supabase.auth.Callback;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Interface for OTP or token verification result callbacks.
 * <p>
 * This callback is used for operations that verify a one-time password (OTP) or a verification token.
 * <p>
 * <b>Invocation Timing:</b> Invoked after the verification request is processed by the Supabase server.
 * <p>
 * <b>Threading:</b> Invoked on a background thread. UI updates must be handled on the main thread.
 *
 * @since 1.0.0
 */
public interface OnVerifyCallback {
    /**
     * Called when the verification is successful and the token/OTP is valid.
     */
    void onValid();

    /**
     * Called when the verification fails because the token/OTP is invalid or has expired.
     *
     * @param supabaseError The {@link SupabaseError} describing the failure reasons.
     */
    void onInvalid(SupabaseError supabaseError);
}

