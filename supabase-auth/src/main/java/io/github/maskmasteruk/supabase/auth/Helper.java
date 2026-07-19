package io.github.maskmasteruk.supabase.auth;

import static java.net.HttpURLConnection.HTTP_OK;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.maskmasteruk.supabase.auth.Callback.AuthCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnCompleteCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnFactorCreatedCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnGetFactorChallengeCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnGetFactorsCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnVerifyCallback;
import io.github.maskmasteruk.supabase.auth.Object.Session;
import io.github.maskmasteruk.supabase.auth.Object.SupabaseUser;
import io.github.maskmasteruk.supabase.auth.Object.UriError;
import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.core.Runnables;
import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;

/**
 * A shared internal utility class that provides common functionality across all auth services.
 * <p>
 * This class handles:
 * <ul>
 *     <li>URL construction for auth endpoints</li>
 *     <li>Session refresh and validation</li>
 *     <li>Common error generation and handling</li>
 *     <li>Managing the PKCE code verifier cache</li>
 *     <li>User session state checks</li>
 * </ul>
 * <p>
 * <b>Architectural Responsibility:</b> Acts as a bridge between services and the core network layer,
 * providing shared logic to avoid duplication.
 * <p>
 * <b>Thread Safety:</b> Thread-safe singleton.
 *
 * @since 1.0.0
 */
class Helper {
    private static volatile Helper instance;
    private final Context context;

    /**
     * Cache for PKCE code verifiers.
     */
    public InMemoryCodeVerifyCache codeVerifyCache = new InMemoryCodeVerifyCache();

    /**
     * Private constructor for Helper.
     *
     * @param context The Android context.
     */
    private Helper(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Returns the singleton instance of Helper.
     *
     * @param context The Android context.
     * @return The {@link Helper} singleton instance.
     */
    public static Helper getInstance(Context context) {
        if (instance == null) {
            synchronized (Helper.class) {
                if (instance == null) {
                    instance = new Helper(context);
                }
            }
        }
        return instance;
    }

    /**
     * Constructs a {@link UrlBuilder} initialized with the base auth URL and version.
     *
     * @return A {@link UrlBuilder} instance.
     */
    public UrlBuilder getBaseAuthUrlBuilder() {
        return new UrlBuilder().appendPath(AUTH_END_POINTS.AUTH).appendPath(AUTH_END_POINTS.VERSION);
    }

    /**
     * Retrieves the current user from the session data.
     *
     * @return The current {@link SupabaseUser}, or {@code null} if no session exists.
     */
    public SupabaseUser getCurrentUser() {
        Session sessionData = SessionData.getInstance(context).getSessionData();
        return sessionData == null ? null : sessionData.getUser();
    }

    /**
     * Asynchronously refreshes the session using the provided refresh token.
     *
     * @param refreshToken The refresh token.
     * @param authCallback Callback to notify success or failure.
     */
    public void refreshSession(String refreshToken, AuthCallback authCallback) {
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.TOKEN);
            authUrlBuilder.appendQueryParam("grant_type", "refresh_token");

            String inputJson = new JsonUtils.JsonObjectStringBuilder().append("refresh_token", refreshToken).build();

            Response result = new RequestHandler().post(authUrlBuilder.build(), inputJson);
            JSONObject resultJson = result.getResponseJSON();

            if (result.getCode() == HTTP_OK) {
                SessionData.getInstance(context).setSessionData(resultJson);
                if (authCallback != null) {
                    authCallback.onSuccess(new Session(resultJson).getUser());
                }
            } else {
                SessionData.getInstance(context).clearSessionData();
                if (authCallback != null) {
                    generateError(result, authCallback);
                }
            }

        });
    }

    /**
     * Refreshes the session without a callback.
     *
     * @param refreshToken The refresh token.
     */
    public void refreshSession(String refreshToken) {
        refreshSession(refreshToken, null);
    }

    /**
     * Checks if the current session is valid (i.e., not expired).
     * Automatically triggers a session refresh if the token has expired.
     */
    public void checkIfSessionIsValid() {
        Session sessionData = SessionData.getInstance(context).getSessionData();

        if (sessionData == null) {
            return;
        }
        if (System.currentTimeMillis() / 1000 >= sessionData.getExpiresAt()) {
            refreshSession(sessionData.getRefreshToken());
        }

    }


    /**
     * Checks if an active user session already exists and throws an error if it does.
     * Typically used before sign-in or sign-up operations.
     *
     * @throws SupabaseError If an active session exists.
     */
    public void checkIfUserSessionAlreadyExistsAndThrow() {
        if (getCurrentUser() != null) {
            throw new SupabaseError("An active user session already exists.");
        }
    }

    /**
     * Handles the common logic for processing an authentication response from the server.
     *
     * @param authCallback Callback to notify success or failure.
     * @param result       The {@link Response} from the network request.
     */
    public void handleAuthResponse(AuthCallback authCallback, Response result) {
        JSONObject resultJson = result.getResponseJSON();
        if (result.getCode() == HTTP_OK) {
            SessionData.getInstance(context).setSessionData(resultJson);
            Session session = new Session(resultJson);
            authCallback.onSuccess(session.getUser());
        } else {
            generateError(result, authCallback);
        }
    }

    /**
     * Generates and dispatches a {@link SupabaseError} based on the server response.
     *
     * @param response The network response containing error information.
     * @param object   The callback object (AuthCallback, OnCompleteCallback, or OnVerifyCallback).
     */
    public void generateError(Response response, Object object) {
        SupabaseError supabaseError;

        try {
            JSONObject resultJson = response.getResponseJSON();
            if (resultJson.has("error_code") && resultJson.getString("error_code").equals("user_not_found")) {
                SessionData.getInstance(context).clearSessionData();
            }
            if (resultJson.has("error_code") && io.github.maskmasteruk.supabase.core.VALUES.CONSTANTS.AUTH_ERROR_MESSAGES.containsKey(resultJson.getString("error_code"))) {
                supabaseError = new SupabaseError(io.github.maskmasteruk.supabase.core.VALUES.CONSTANTS.AUTH_ERROR_MESSAGES.get(resultJson.getString("error_code")));
            } else if (resultJson.has("msg")) {
                supabaseError = new SupabaseError(resultJson.getString("msg"));
            } else {
                supabaseError = new SupabaseError(response.getResponse());
            }

        } catch (JSONException e) {
            supabaseError = new SupabaseError(e);
        }

        if (object == null) {
            Log.e("SupabaseAuthError", supabaseError.getErrorMessage());
        } else if (object instanceof AuthCallback) {
            ((AuthCallback) object).onError(supabaseError);
        } else if (object instanceof OnCompleteCallback) {
            ((OnCompleteCallback) object).onError(supabaseError);
        } else if (object instanceof OnGetFactorsCallback) {
            ((OnGetFactorsCallback) object).onError(supabaseError);
        } else if (object instanceof OnFactorCreatedCallback) {
            ((OnFactorCreatedCallback) object).onError(supabaseError);
        } else if (object instanceof OnGetFactorChallengeCallback) {
            ((OnGetFactorChallengeCallback) object).onError(supabaseError);
        } else if (object instanceof OnVerifyCallback) {
            ((OnVerifyCallback) object).onInvalid(supabaseError);
        } else {
            Log.e("SupabaseAuthError", supabaseError.getErrorMessage());
        }
    }

    /**
     * Generates and dispatches a {@link SupabaseError} based on errors parsed from a URI fragment.
     *
     * @param uriError The error details extracted from the URI.
     * @param object   The callback object.
     */
    public void generateError(UriError uriError, Object object) {
        SupabaseError supabaseError;

        if (io.github.maskmasteruk.supabase.core.VALUES.CONSTANTS.AUTH_ERROR_MESSAGES.containsKey(uriError.getErrorCode())) {
            supabaseError = new SupabaseError(io.github.maskmasteruk.supabase.core.VALUES.CONSTANTS.AUTH_ERROR_MESSAGES.get(uriError.getErrorCode()));
        } else {
            supabaseError = new SupabaseError(uriError.getErrorDescription());
        }

        if (object == null) {
            throw supabaseError;
        } else if (object instanceof AuthCallback) {
            ((AuthCallback) object).onError(supabaseError);
        } else if (object instanceof OnCompleteCallback) {
            ((OnCompleteCallback) object).onError(supabaseError);
        } else if (object instanceof OnVerifyCallback) {
            ((OnVerifyCallback) object).onInvalid(supabaseError);
        } else {
            throw supabaseError;
        }
    }

}

