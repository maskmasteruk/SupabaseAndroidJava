package io.github.maskmasteruk.supabase.auth;

import static java.net.HttpURLConnection.HTTP_OK;

import android.content.Context;
import android.net.Uri;

import org.json.JSONObject;

import java.util.Map;

import io.github.maskmasteruk.supabase.auth.Callback.AuthCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnCompleteCallback;
import io.github.maskmasteruk.supabase.auth.Object.Session;
import io.github.maskmasteruk.supabase.auth.Object.SupabaseUser;
import io.github.maskmasteruk.supabase.auth.Object.UriError;
import io.github.maskmasteruk.supabase.auth.Object.UriSession;
import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.core.Runnables;
import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;

/**
 * Internal service class for handling general authentication and user management operations.
 * <p>
 * This class provides methods to:
 * <ul>
 *     <li>Sign out users and clear local sessions</li>
 *     <li>Reauthenticate the current session</li>
 *     <li>Update user metadata</li>
 *     <li>Handle Magic Link logins</li>
 * </ul>
 * <p>
 * <b>Architectural Responsibility:</b> Interfaces with core Supabase Auth endpoints for session and user management.
 * <p>
 * <b>Thread Safety:</b> Thread-safe singleton. Operations are executed asynchronously.
 *
 * @since 1.0.0
 */
class AuthService {
    private static volatile AuthService instance;
    private final Context context;
    private final Helper helper;

    /**
     * Private constructor for AuthService.
     *
     * @param context The Android context.
     */
    private AuthService(Context context) {
        helper = Helper.getInstance(context);
        this.context = context.getApplicationContext();
    }

    /**
     * Returns the singleton instance of AuthService.
     *
     * @param context The Android context.
     * @return The {@link AuthService} singleton instance.
     */
    public static AuthService getInstance(Context context) {
        if (instance == null) {
            synchronized (AuthService.class) {
                if (instance == null) {
                    instance = new AuthService(context);
                }
            }
        }
        return instance;
    }

    /**
     * Reauthenticates the current user session with the server.
     *
     * @param onCompleteCallback Callback to notify success or failure.
     */
    public void reauthenticate(OnCompleteCallback onCompleteCallback) {
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.REAUTHENTICATE);

            Response result = new RequestHandler().get(authUrlBuilder.build());
            if (result.getCode() == HTTP_OK) {
                if (onCompleteCallback != null) {
                    onCompleteCallback.onSuccess();
                }
            } else {
                helper.generateError(result, onCompleteCallback);
            }
        });
    }

    /**
     * Logs in the user using a Magic Link URI.
     *
     * @param uri          The URI containing the session data.
     * @param authCallback Callback to notify success or failure.
     */
    public void loginWithMagicLink(Uri uri, AuthCallback authCallback) {
        UriError uriError = new UriError(uri);
        if (uriError.isError()) {
            helper.generateError(uriError, authCallback);
            return;
        }

        UriSession uriSession = new UriSession(uri);
        helper.refreshSession(uriSession.getRefreshToken(), authCallback);
    }

    /**
     * Updates the user metadata for the currently authenticated user.
     *
     * @param userData     A map containing user metadata to update.
     * @param authCallback Callback to notify success or failure.
     * @throws SupabaseError If userData is null or empty.
     */
    public void updateUserData(Map<String, Object> userData, AuthCallback authCallback) {
        if (userData == null || userData.isEmpty()) {
            throw new SupabaseError("User Data cant be null or empty.");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.USER_DETAILS);


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("data", JsonUtils.toJsonObject(userData));
            String inputJson = jsonObjectStringBuilder.build();

            Response result = new RequestHandler().put(authUrlBuilder.build(), inputJson);
            JSONObject resultJson = result.getResponseJSON();
            if (result.getCode() == HTTP_OK) {
                SessionData sessionData = SessionData.getInstance(context);
                Session session = sessionData.getSessionData().setUser(new SupabaseUser(resultJson));
                sessionData.setSessionData(session.toJson());
                authCallback.onSuccess(helper.getCurrentUser());
            } else {
                helper.generateError(result, authCallback);
            }

        });
    }

    /**
     * Signs out the current user by notifying the server and clearing local session data.
     *
     * @param onCompleteCallback Callback to notify success or failure.
     * @throws SupabaseError If no active user session exists.
     */
    public void signOut(OnCompleteCallback onCompleteCallback) {
        if (helper.getCurrentUser() == null) {
            throw new SupabaseError("There is no active user session to sign out.");
        }

        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.LOGOUT);

            Response result = new RequestHandler().post(authUrlBuilder.build(), null);
            SessionData.getInstance(context).clearSessionData();
            if (onCompleteCallback != null) {
                onCompleteCallback.onSuccess();
            }
        });
    }

    /**
     * Signs out the current user without a callback.
     */
    public void signOut() {
        signOut(null);
    }
}

