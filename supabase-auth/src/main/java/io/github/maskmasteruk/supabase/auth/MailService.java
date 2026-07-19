package io.github.maskmasteruk.supabase.auth;

import static java.net.HttpURLConnection.HTTP_OK;

import android.content.Context;

import java.util.Map;

import io.github.maskmasteruk.supabase.auth.Callback.OnCompleteCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnVerifyCallback;
import io.github.maskmasteruk.supabase.auth.Enums.ResendType;
import io.github.maskmasteruk.supabase.auth.Enums.VerifyType;
import io.github.maskmasteruk.supabase.auth.Object.Session;
import io.github.maskmasteruk.supabase.auth.Object.SupabaseUser;
import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.core.Runnables;
import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;

/**
 * Internal service class for handling email-based authentication operations.
 * <p>
 * This class provides methods to:
 * <ul>
 *     <li>Send login and signup links (Magic Links)</li>
 *     <li>Send password reset emails</li>
 *     <li>Handle email update verifications</li>
 *     <li>Verify email OTP codes</li>
 *     <li>Resend email verification triggers</li>
 * </ul>
 * <p>
 * <b>Architectural Responsibility:</b> Interfaces with the Supabase Auth API for email-related endpoints.
 * <p>
 * <b>Thread Safety:</b> Thread-safe singleton. Operations are executed asynchronously.
 *
 * @since 1.0.0
 */
class MailService {
    private static volatile MailService instance;
    private final Context context;
    private final Helper helper;

    /**
     * Private constructor for MailService.
     *
     * @param context The Android context.
     */
    private MailService(Context context) {
        helper = Helper.getInstance(context);
        this.context = context.getApplicationContext();
    }

    /**
     * Returns the singleton instance of MailService.
     *
     * @param context The Android context.
     * @return The {@link MailService} singleton instance.
     */
    public static MailService getInstance(Context context) {
        if (instance == null) {
            synchronized (MailService.class) {
                if (instance == null) {
                    instance = new MailService(context);
                }
            }
        }
        return instance;
    }

    /**
     * Sends a Magic Link login email to the specified address.
     *
     * @param email              The user's email address.
     * @param redirectTo         Optional URL to redirect to after clicking the link.
     * @param onCompleteCallback Callback to notify success or failure.
     * @throws SupabaseError If the email is null.
     */
    public void sendLoginLinkToEmail(String email, String redirectTo, OnCompleteCallback onCompleteCallback) {
        if (email == null) {
            throw new SupabaseError("Given email is null.");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.OTP);
            if (redirectTo != null) {
                authUrlBuilder.appendQueryParam("redirect_to", redirectTo);
            }


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("email", email);
            jsonObjectStringBuilder.append("create_user", false);
            String inputJson = jsonObjectStringBuilder.build();

            Response result = new RequestHandler().post(authUrlBuilder.build(), inputJson);

            if (result.getCode() == HTTP_OK) {
                onCompleteCallback.onSuccess();
            } else {
                helper.generateError(result, onCompleteCallback);
            }
        });

    }

    /**
     * Sends a Magic Link signup email to the specified address.
     *
     * @param email              The user's email address.
     * @param redirectTo         Optional URL to redirect to.
     * @param userData           Optional metadata to store with the new user.
     * @param onCompleteCallback Callback to notify success or failure.
     * @throws SupabaseError If the email is null.
     */
    public void sendSignupLinkToEmail(String email, String redirectTo, Map<String, Object> userData, OnCompleteCallback onCompleteCallback) {
        if (email == null) {
            throw new SupabaseError("Given email is null.");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.OTP);
            if (redirectTo != null) {
                authUrlBuilder.appendQueryParam("redirect_to", redirectTo);
            }


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("email", email);
            if (userData != null && !userData.isEmpty()) {
                jsonObjectStringBuilder.append("data", JsonUtils.toJsonObject(userData));
            }
            jsonObjectStringBuilder.append("create_user", true);
            String inputJson = jsonObjectStringBuilder.build();
            Response result = new RequestHandler().post(authUrlBuilder.build(), inputJson);

            if (result.getCode() == HTTP_OK) {
                onCompleteCallback.onSuccess();
            } else {
                helper.generateError(result, onCompleteCallback);
            }
        });

    }

    /**
     * Sends a password recovery email to the user.
     *
     * @param email              The user's email address.
     * @param redirectTo         Optional URL to redirect to.
     * @param onCompleteCallback Callback to notify success or failure.
     * @throws SupabaseError If the email is null.
     */
    public void sendPasswordResetMail(String email, String redirectTo, OnCompleteCallback onCompleteCallback) {
        if (email == null) {
            throw new SupabaseError("Given email is null.");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.RECOVER);
            if (redirectTo != null) {
                authUrlBuilder.appendQueryParam("redirect_to", redirectTo);
            }

            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("email", email);
            String inputJson = jsonObjectStringBuilder.build();
            Response result = new RequestHandler().post(authUrlBuilder.build(), inputJson);

            if (result.getCode() == HTTP_OK) {
                onCompleteCallback.onSuccess();
            } else {
                helper.generateError(result, onCompleteCallback);
            }
        });

    }

    /**
     * Sends a verification email to confirm a change to the user's email address.
     *
     * @param newEmail           The new email address.
     * @param redirectTo         Optional URL to redirect to.
     * @param onCompleteCallback Callback to notify success or failure.
     * @throws SupabaseError If the email is null.
     */
    public void sendEmailUpdateVerificationMail(String newEmail, String redirectTo, OnCompleteCallback onCompleteCallback) {
        if (newEmail == null) {
            throw new SupabaseError("Given email is null.");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.USER_DETAILS);
            if (redirectTo != null) {
                authUrlBuilder.appendQueryParam("redirect_to", redirectTo);
            }


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("email", newEmail);
            String inputJson = jsonObjectStringBuilder.build();
            Response result = new RequestHandler().put(authUrlBuilder.build(), inputJson);

            if (result.getCode() == HTTP_OK) {
                SessionData sessionData = SessionData.getInstance(context);
                Session session = sessionData.getSessionData().setUser(new SupabaseUser(result.getResponseJSON()));
                sessionData.setSessionData(session.toJson());
                onCompleteCallback.onSuccess();
            } else {
                helper.generateError(result, onCompleteCallback);
            }
        });

    }

    /**
     * Verifies an OTP code sent via email.
     *
     * @param email            The user's email address.
     * @param otp              The one-time password to verify.
     * @param onVerifyCallback Callback to notify verification result.
     * @throws SupabaseError If the email or OTP is null.
     */
    public void verifyEmailOTP(String email, String otp, OnVerifyCallback onVerifyCallback) {
        if (email == null) {
            throw new SupabaseError("Given email is null.");
        }
        if (otp == null) {
            throw new SupabaseError("Given otp is null.");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.VERIFY);


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("email", email);
            jsonObjectStringBuilder.append("token", otp);
            jsonObjectStringBuilder.append("type", VerifyType.EMAIL.getValue());
            String inputJson = jsonObjectStringBuilder.build();

            Response result = new RequestHandler().post(authUrlBuilder.build(), inputJson);

            if (result.getCode() == HTTP_OK) {
                onVerifyCallback.onValid();
            } else {
                helper.generateError(result, onVerifyCallback);
            }
        });
    }

    /**
     * Resends the initial signup verification email.
     *
     * @param email              The user's email address.
     * @param onCompleteCallback Callback to notify success or failure.
     * @throws SupabaseError If the email is null.
     */
    public void resendSignUpEmail(String email, OnCompleteCallback onCompleteCallback) {
        if (email == null) {
            throw new SupabaseError("Given email is null.");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.RESEND);


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("email", email);
            jsonObjectStringBuilder.append("type", ResendType.SIGNUP.getValue());
            String inputJson = jsonObjectStringBuilder.build();
            Response result = new RequestHandler().post(authUrlBuilder.build(), inputJson);

            if (result.getCode() == HTTP_OK) {
                onCompleteCallback.onSuccess();
            } else {
                helper.generateError(result, onCompleteCallback);
            }
        });
    }

    /**
     * Resends the email change confirmation message.
     *
     * @param email              The new email address.
     * @param onCompleteCallback Callback to notify success or failure.
     * @throws SupabaseError If the email is null.
     */
    public void resendEmailChangeConfirmation(String email, OnCompleteCallback onCompleteCallback) {
        if (email == null) {
            throw new SupabaseError("Given email is null.");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.RESEND);


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("email", email);
            jsonObjectStringBuilder.append("type", ResendType.EMAIL_CHANGE.getValue());
            String inputJson = jsonObjectStringBuilder.build();

            Response result = new RequestHandler().post(authUrlBuilder.build(), inputJson.toString());

            if (result.getCode() == HTTP_OK) {
                onCompleteCallback.onSuccess();
            } else {
                helper.generateError(result, onCompleteCallback);
            }
        });
    }

}

