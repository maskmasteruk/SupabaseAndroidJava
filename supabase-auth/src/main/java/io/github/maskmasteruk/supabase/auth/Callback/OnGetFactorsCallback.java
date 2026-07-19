package io.github.maskmasteruk.supabase.auth.Callback;

import java.util.List;

import io.github.maskmasteruk.supabase.auth.Object.Factor;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Interface for MFA factor retrieval result callbacks.
 * <p>
 * This callback is used for operations that retrieve the Multi-Factor
 * Authentication (MFA) factors enrolled for a user.
 * <p>
 * <b>Invocation Timing:</b> Invoked after the retrieval request is processed by
 * the Supabase server.
 * <p>
 * <b>Threading:</b> Invoked on a background thread. UI updates must be handled on
 * the main thread.
 *
 * @since 1.0.0
 */
public interface OnGetFactorsCallback {

    /**
     * Called when the enrolled MFA factors are successfully retrieved.
     *
     * @param factors The list of enrolled MFA factors.
     */
    void onSuccess(List<Factor> factors);

    /**
     * Called when the request fails.
     *
     * @param supabaseError The error describing the failure.
     */
    void onError(SupabaseError supabaseError);
}