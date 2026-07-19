package io.github.maskmasteruk.supabase.auth;

import static java.net.HttpURLConnection.HTTP_OK;

import android.content.Context;
import android.net.Uri;

import org.json.JSONObject;

import java.util.Map;

import io.github.maskmasteruk.supabase.auth.Callback.AuthCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnCompleteCallback;
import io.github.maskmasteruk.supabase.auth.Enums.VerifyType;
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
 * Internal service class for handling email and password authentication operations.
 * <p>
 * This class provides methods to:
 * <ul>
 *     <li>Sign up users with email and password</li>
 *     <li>Sign in users with email and password</li>
 *     <li>Reset passwords via OTP or callback URIs</li>
 *     <li>Handle email update callbacks</li>
 * </ul>
 * <p>
 * <b>Architectural Responsibility:</b> Interfaces with the Supabase Auth API for identity-related endpoints.
 * <p>
 * <b>Thread Safety:</b> Thread-safe singleton. Operations are executed asynchronously.
 *
 * @since 1.0.0
 */
class EmailPasswordService {
    private static volatile EmailPasswordService instance;
    private final Context context;
    private final Helper helper;

    /**
     * Private constructor for EmailPasswordService.
     *
     * @param context The Android context.
     */
    private EmailPasswordService(Context context) {
        helper = Helper.getInstance(context);
        this.context = context.getApplicationContext();
    }

    /**
     * Returns the singleton instance of EmailPasswordService.
     *
     * @param context The Android context.
     * @return The {@link EmailPasswordService} singleton instance.
     */
    public static EmailPasswordService getInstance(Context context) {
        if (instance == null) {
            synchronized (EmailPasswordService.class) {
                if (instance == null) {
                    instance = new EmailPasswordService(context);
                }
            }
        }
        return instance;
    }

    /**
     * Signs up a new user with an email and password.
     *
     * @param email        The user's email address.
     * @param password     The user's password.
     * @param userData     Optional metadata to store with the user.
     * @param authCallback Callback for success or failure.
     */
    public void signUpWithEmailAndPassword(String email, String password, Map<String, Object> userData, AuthCallback authCallback) {
        helper.checkIfUserSessionAlreadyExistsAndThrow();
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.SIGNUP);

            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder()
                    .append("email", email)
                    .append("password", password);
            if (userData != null && !userData.isEmpty()) {
                jsonObjectStringBuilder.append("data", JsonUtils.toJsonObject(userData));
            }
            String inputJson = jsonObjectStringBuilder.build();

            Response result = new RequestHandler().post(authUrlBuilder.build(), inputJson);
            helper.handleAuthResponse(authCallback, result);

        });
    }

    /**
     * Signs in an existing user with email and password.
     *
     * @param email        The user's email address.
     * @param password     The user's password.
     * @param authCallback Callback for success or failure.
     */
    public void signInWithEmailAndPassword(String email, String password, AuthCallback authCallback) {
        helper.checkIfUserSessionAlreadyExistsAndThrow();
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.SIGN_IN);
            authUrlBuilder.appendQueryParam("grant_type", "password");


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder()
                    .append("email", email)
                    .append("password", password);
            String inputJson = jsonObjectStringBuilder.build();

            Response result = new RequestHandler().post(authUrlBuilder.build(), inputJson);
            helper.handleAuthResponse(authCallback, result);

        });
    }

    /**
     * Handles the callback URI for an email update request.
     *
     * @param uri          The URI from the intent.
     * @param authCallback Callback for success or failure.
     */
    public void handleUpdateEmailCallback(Uri uri, AuthCallback authCallback) {
        UriError uriError = new UriError(uri);
        if (uriError.isError()) {
            helper.generateError(uriError, authCallback);
            return;
        }

        UriSession uriSession = new UriSession(uri);
        helper.refreshSession(uriSession.getRefreshToken(), authCallback);
    }

    /**
     * Resets a user's password using an email and an OTP.
     *
     * @param email              The user's email address.
     * @param password           The new password.
     * @param otp                The one-time password received.
     * @param onCompleteCallback Callback for success or failure.
     * @throws SupabaseError If email or password is null.
     */
    public void resetPasswordWithOtp(String email, String password, String otp, OnCompleteCallback onCompleteCallback) {
        if (email == null) {
            throw new SupabaseError("Given email is null.");
        }
        if (password == null) {
            throw new SupabaseError("Given password is null.");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.VERIFY);


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("email", email);
            jsonObjectStringBuilder.append("token", otp);
            jsonObjectStringBuilder.append("type", VerifyType.RECOVERY.getValue());
            String inputJson = jsonObjectStringBuilder.build();

            Response result = new RequestHandler().post(authUrlBuilder.build(), inputJson.toString());

            if (result.getCode() == HTTP_OK) {
                SessionData.getInstance(context).setSessionData(result.getResponseJSON());
                updatePassword(password, new AuthCallback() {
                    @Override
                    public void onSuccess(SupabaseUser supabaseUser) {
                        onCompleteCallback.onSuccess();
                    }

                    @Override
                    public void onError(SupabaseError supabaseError) {
                        onCompleteCallback.onError(supabaseError);
                    }
                });
            } else {
                helper.generateError(result, onCompleteCallback);
            }
        });
    }

    /**
     * Resets the current user's password using an OTP.
     *
     * @param password           The new password.
     * @param otp                The one-time password received.
     * @param onCompleteCallback Callback for success or failure.
     * @throws SupabaseError If the password is null.
     */
    public void resetPasswordWithOtp(String password, String otp, OnCompleteCallback onCompleteCallback) {
        if (password == null) {
            throw new SupabaseError("Given password is null.");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.USER_DETAILS);


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("password", password);
            jsonObjectStringBuilder.append("nonce", otp);
            String inputJson = jsonObjectStringBuilder.build();

            Response result = new RequestHandler().put(authUrlBuilder.build(), inputJson.toString());
            if (result.getCode() == HTTP_OK) {
                onCompleteCallback.onSuccess();
            } else {
                helper.generateError(result, onCompleteCallback);
            }
        });
    }

    /**
     * Resets a user's password using a session from a callback URI.
     *
     * @param uri                The URI containing the recovery session.
     * @param password           The new password.
     * @param onCompleteCallback Callback for success or failure.
     * @throws SupabaseError If the password is null or the URI is invalid.
     */
    public void resetPasswordWithCallback(Uri uri, String password, OnCompleteCallback onCompleteCallback) {
        if (password == null) {
            throw new SupabaseError("Given password is null.");
        }

        UriError uriError = new UriError(uri);
        if (uriError.isError()) {
            helper.generateError(uriError, onCompleteCallback);
            return;
        }
        Runnables.getExecutorService().execute(() -> {
            UriSession uriSession = new UriSession(uri);

            if (!uriSession.getType().equals("recovery")) {
                throw new SupabaseError("Invalid Uri to reset password");
            }

            Session session = SessionData.getInstance(context).getSessionData();
            session.setAccessToken(uriSession.getAccessToken());
            session.setRefreshToken(uriSession.getRefreshToken());
            session.setExpiresAt(uriSession.getExpiresAt());
            session.setExpiresIn(uriSession.getExpiresIn());
            session.setTokenType(uriSession.getTokenType());

            SessionData.getInstance(context).setSessionData(session.toJson());
            updatePassword(password, new AuthCallback() {
                @Override
                public void onSuccess(SupabaseUser supabaseUser) {
                    onCompleteCallback.onSuccess();
                }

                @Override
                public void onError(SupabaseError supabaseError) {
                    onCompleteCallback.onError(supabaseError);
                }
            });
        });
    }

    /**
     * Updates the password for the currently authenticated user.
     *
     * @param password     The new password.
     * @param authCallback Callback for success or failure.
     * @throws SupabaseError If the password is null or empty.
     */
    private void updatePassword(String password, AuthCallback authCallback) {
        if (password == null || password.isEmpty()) {
            throw new SupabaseError("Password cant be null or empty");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.USER_DETAILS);

            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("password", password);
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
}

