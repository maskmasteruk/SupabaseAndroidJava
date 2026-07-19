package io.github.maskmasteruk.supabase.auth.Callback;

import io.github.maskmasteruk.supabase.auth.Object.FactorChallenge;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Interface for MFA challenge retrieval result callbacks.
 * <p>
 * This callback is used for operations that create or retrieve a Multi-Factor
 * Authentication (MFA) challenge, such as initiating a TOTP or phone verification
 * challenge.
 * <p>
 * <b>Invocation Timing:</b> Invoked after the challenge request is processed by the
 * Supabase server.
 * <p>
 * <b>Threading:</b> Invoked on a background thread. UI updates must be handled on
 * the main thread.
 *
 * @since 1.0.0
 */
public interface OnGetFactorChallengeCallback {

    /**
     * Called when the MFA challenge is successfully retrieved.
     *
     * @param factorChallenge The retrieved MFA challenge.
     */
    void onSuccess(FactorChallenge factorChallenge);

    /**
     * Called when the request fails.
     *
     * @param supabaseError The error describing the failure.
     */
    void onError(SupabaseError supabaseError);
}
