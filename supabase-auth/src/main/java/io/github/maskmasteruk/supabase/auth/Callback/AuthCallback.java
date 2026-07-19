package io.github.maskmasteruk.supabase.auth.Callback;

import io.github.maskmasteruk.supabase.auth.Object.SupabaseUser;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Interface for authentication result callbacks.
 * <p>
 * This callback is used for operations that result in a successful user authentication,
 * returning a {@link SupabaseUser} object upon completion.
 * <p>
 * <b>Invocation Timing:</b> Invoked once the network request to the Supabase Auth server
 * completes and the response is successfully parsed into a session.
 * <p>
 * <b>Threading:</b> By default, these methods are invoked on a background thread.
 * If UI updates are required, ensure they are performed on the main thread (e.g., using {@code runOnUiThread}).
 * <p>
 * <b>Lifecycle Expectations:</b> Callbacks should check the state of the calling Activity or Fragment
 * before performing UI operations to avoid leaks or crashes.
 *
 * @since 1.0.0
 */
public interface AuthCallback {
    /**
     * Called when the authentication operation is successful.
     *
     * @param supabaseUser The authenticated {@link SupabaseUser} object containing the profile details.
     */
    void onSuccess(SupabaseUser supabaseUser);

    /**
     * Called when the authentication operation fails due to network issues,
     * invalid credentials, or server-side errors.
     *
     * @param supabaseError The {@link SupabaseError} object describing the failure.
     */
    void onError(SupabaseError supabaseError);
}

