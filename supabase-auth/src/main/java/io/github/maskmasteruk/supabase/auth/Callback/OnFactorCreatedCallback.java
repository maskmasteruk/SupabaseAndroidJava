package io.github.maskmasteruk.supabase.auth.Callback;

import io.github.maskmasteruk.supabase.auth.Object.NewFactor;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Interface for MFA factor enrollment result callbacks.
 * <p>
 * This callback is used for operations that enroll a new Multi-Factor
 * Authentication (MFA) factor, such as registering a TOTP authenticator
 * application or a phone-based factor.
 * <p>
 * <b>Invocation Timing:</b> Invoked after the enrollment request is processed by
 * the Supabase server.
 * <p>
 * <b>Threading:</b> Invoked on a background thread. UI updates must be handled on
 * the main thread.
 *
 * @since 1.0.0
 */
public interface OnFactorCreatedCallback {

    /**
     * Called when the MFA factor is successfully enrolled.
     *
     * @param newFactor The newly enrolled MFA factor.
     */
    void onSuccess(NewFactor newFactor);

    /**
     * Called when the enrollment request fails.
     *
     * @param supabaseError The error describing the failure.
     */
    void onError(SupabaseError supabaseError);
}