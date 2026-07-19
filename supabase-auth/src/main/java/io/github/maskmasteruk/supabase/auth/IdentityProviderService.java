package io.github.maskmasteruk.supabase.auth;

import android.content.Context;
import android.net.Uri;

import io.github.maskmasteruk.supabase.auth.Callback.AuthCallback;
import io.github.maskmasteruk.supabase.auth.Enums.AuthProviders;
import io.github.maskmasteruk.supabase.auth.Object.UriError;
import io.github.maskmasteruk.supabase.auth.Object.UriSession;
import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.core.Runnables;
import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;

/**
 * Internal service class for handling OAuth and ID Token authentication operations.
 * <p>
 * This class provides methods to:
 * <ul>
 *     <li>Sign in using ID tokens from third-party providers (Google, Apple, etc.)</li>
 *     <li>Generate OAuth authorization URLs with PKCE support</li>
 *     <li>Handle OAuth redirection and token exchange</li>
 * </ul>
 * <p>
 * <b>Architectural Responsibility:</b> Interfaces with the Supabase Auth API for external identity providers.
 * <p>
 * <b>Thread Safety:</b> Thread-safe singleton. Operations are executed asynchronously.
 *
 * @since 1.0.0
 */
class IdentityProviderService {
    private static volatile IdentityProviderService instance;
    private final Helper helper;

    /**
     * Private constructor for IdentityProviderService.
     *
     * @param context The Android context.
     */
    private IdentityProviderService(Context context) {
        helper = Helper.getInstance(context);
    }

    /**
     * Returns the singleton instance of IdentityProviderService.
     *
     * @param context The Android context.
     * @return The {@link IdentityProviderService} singleton instance.
     */
    public static IdentityProviderService getInstance(Context context) {
        if (instance == null) {
            synchronized (IdentityProviderService.class) {
                if (instance == null) {
                    instance = new IdentityProviderService(context);
                }
            }
        }
        return instance;
    }


    /**
     * Signs in a user using an ID Token from an external provider.
     *
     * @param provider     The ID token provider (e.g., Google, Azure).
     * @param idToken      The ID token string.
     * @param authCallback Callback for success or failure.
     */
    public void signInWithIdToken(AuthProviders.IDTokenProviders provider, String idToken, AuthCallback authCallback) {
        helper.checkIfUserSessionAlreadyExistsAndThrow();
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.SIGN_IN);
            authUrlBuilder.appendQueryParam("grant_type", "id_token");


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder()
                    .append("provider", provider.getValue())
                    .append("id_token", idToken);
            String inputJson = jsonObjectStringBuilder.build();

            Response result = new RequestHandler().post(authUrlBuilder.build(), inputJson);
            helper.handleAuthResponse(authCallback, result);

        });
    }

    /**
     * Generates an OAuth authorization URL for the specified provider.
     * Optionally enables PKCE code verification.
     *
     * @param provider         The OAuth provider (e.g., GitHub, Discord).
     * @param enableCodeVerify Whether to generate and cache a PKCE code verifier.
     * @param redirectTo       Optional URL to redirect to after successful sign-in.
     * @return The authorization URL string.
     */
    public String getSignInUrlForAuthProvider(AuthProviders.OAuthProviders provider, boolean enableCodeVerify, String redirectTo) {
        helper.checkIfUserSessionAlreadyExistsAndThrow();
        UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
        authUrlBuilder.appendPath(AUTH_END_POINTS.OAUTH_AUTHORIZATION);
        authUrlBuilder.appendQueryParam("provider", provider.getValue());
        authUrlBuilder.appendQueryParam("redirect_to", redirectTo);
        if (enableCodeVerify) {
            String verifier = PkceUtils.generateCodeVerifier();
            String challenge = PkceUtils.generateCodeChallenge(verifier);
            authUrlBuilder.appendQueryParam("code_challenge", challenge);
            authUrlBuilder.appendQueryParam("code_challenge_method", "S256");
            helper.codeVerifyCache.save(verifier);
        }

        return authUrlBuilder.build();
    }

    /**
     * Completes the OAuth sign-in flow by exchanging the code or refreshing the session.
     *
     * @param uri                 The redirection URI received from the provider.
     * @param isCodeVerifyEnabled Whether PKCE code verification was used.
     * @param authCallback        Callback for success or failure.
     * @throws SupabaseError If the URI is null or required parameters are missing.
     */
    public void signInWithAuthProvider(Uri uri, boolean isCodeVerifyEnabled, AuthCallback authCallback) {
        helper.checkIfUserSessionAlreadyExistsAndThrow();
        if (uri == null) {
            throw new SupabaseError("Given uri is null.");
        }

        UriError uriError = new UriError(uri);
        if (uriError.isError()) {
            helper.generateError(uriError, authCallback);
            return;
        }

        if (isCodeVerifyEnabled) {
            if (uri.getQueryParameter("code") == null) {
                throw new SupabaseError("Auth Code not found in the given Uri");
            }
            Runnables.getExecutorService().execute(() -> {
                String authCode = uri.getQueryParameter("code");
                if (authCode == null) {
                    throw new SupabaseError("Invalid Auth Code.");
                }
                String codeVerifier = helper.codeVerifyCache.load();
                if (codeVerifier == null) {
                    throw new SupabaseError("Invalid Code Verifier.");
                }

                UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
                authUrlBuilder.appendPath(AUTH_END_POINTS.TOKEN);
                authUrlBuilder.appendQueryParam("grant_type", "pkce");


                JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder()
                        .append("auth_code", authCode)
                        .append("code_verifier", codeVerifier);
                String inputJson = jsonObjectStringBuilder.build();


                Response result = new RequestHandler().post(authUrlBuilder.build(), inputJson);
                helper.codeVerifyCache.delete();

                helper.handleAuthResponse(authCallback, result);
            });
        } else {
            UriSession uriSession = new UriSession(uri);
            helper.refreshSession(uriSession.getRefreshToken(), authCallback);
        }
    }
}

