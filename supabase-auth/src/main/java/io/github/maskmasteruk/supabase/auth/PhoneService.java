package io.github.maskmasteruk.supabase.auth;

import static java.net.HttpURLConnection.HTTP_OK;

import android.content.Context;

import io.github.maskmasteruk.supabase.auth.Callback.OnCompleteCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnVerifyCallback;
import io.github.maskmasteruk.supabase.auth.Enums.PhoneChannel;
import io.github.maskmasteruk.supabase.auth.Enums.ResendType;
import io.github.maskmasteruk.supabase.auth.Enums.VerifyType;
import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.core.Runnables;
import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;

/**
 * Internal service class for handling phone-based authentication operations.
 * <p>
 * This class provides methods to:
 * <ul>
 *     <li>Send OTP codes via SMS or WhatsApp</li>
 *     <li>Verify phone OTP codes</li>
 *     <li>Resend phone verification triggers</li>
 * </ul>
 * <p>
 * <b>Architectural Responsibility:</b> Interfaces with the Supabase Auth API for phone-related endpoints.
 * <p>
 * <b>Thread Safety:</b> Thread-safe singleton. Operations are executed asynchronously.
 *
 * @since 1.0.0
 */
class PhoneService {
    private static volatile PhoneService instance;
    private final Helper helper;

    /**
     * Private constructor for PhoneService.
     *
     * @param context The Android context.
     */
    private PhoneService(Context context) {
        helper = Helper.getInstance(context);
    }

    /**
     * Returns the singleton instance of PhoneService.
     *
     * @param context The Android context.
     * @return The {@link PhoneService} singleton instance.
     */
    public static PhoneService getInstance(Context context) {
        if (instance == null) {
            synchronized (PhoneService.class) {
                if (instance == null) {
                    instance = new PhoneService(context);
                }
            }
        }
        return instance;
    }

    /**
     * Sends a one-time password (OTP) to a user's phone number.
     *
     * @param phone              The phone number in E.164 format.
     * @param channel            The delivery channel (e.g., SMS, WhatsApp).
     * @param onCompleteCallback Callback to notify success or failure.
     * @throws SupabaseError If the phone number or channel is null.
     */
    public void sendOtpToPhone(String phone, PhoneChannel channel, OnCompleteCallback onCompleteCallback) {
        if (phone == null) {
            throw new SupabaseError("Given phone is null.");
        }
        if (channel == null) {
            throw new SupabaseError("Given channel is null.");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.OTP);


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("phone", phone);
            jsonObjectStringBuilder.append("channel", channel.getValue());
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
     * Verifies the OTP sent to a phone number.
     *
     * @param phone            The phone number.
     * @param otp              The one-time password to verify.
     * @param onVerifyCallback Callback to notify verification result.
     * @throws SupabaseError If the phone number or OTP is null.
     */
    public void verifyPhoneOTP(String phone, String otp, OnVerifyCallback onVerifyCallback) {
        if (phone == null) {
            throw new SupabaseError("Given phone is null.");
        }
        if (otp == null) {
            throw new SupabaseError("Given otp is null.");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.VERIFY);


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("phone", phone);
            jsonObjectStringBuilder.append("token", otp);
            jsonObjectStringBuilder.append("type", VerifyType.SMS.getValue());
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
     * Resends a phone number change confirmation trigger.
     *
     * @param phone              The new phone number.
     * @param onCompleteCallback Callback to notify success or failure.
     * @throws SupabaseError If the phone number is null.
     */
    public void resendPhoneChangeConfirmation(String phone, OnCompleteCallback onCompleteCallback) {
        if (phone == null) {
            throw new SupabaseError("Given phone is null.");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.RESEND);


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("phone", phone);
            jsonObjectStringBuilder.append("type", ResendType.PHONE_CHANGE.getValue());
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
     * Resends the initial phone verification SMS.
     *
     * @param phone              The phone number to verify.
     * @param onCompleteCallback Callback to notify success or failure.
     * @throws SupabaseError If the phone number is null.
     */
    public void resendPhoneVerification(String phone, OnCompleteCallback onCompleteCallback) {
        if (phone == null) {
            throw new SupabaseError("Given phone is null.");
        }
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.RESEND);


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("phone", phone);
            jsonObjectStringBuilder.append("type", ResendType.SMS.getValue());
            String inputJson = jsonObjectStringBuilder.build();
            Response result = new RequestHandler().post(authUrlBuilder.build(), inputJson);

            if (result.getCode() == HTTP_OK) {
                onCompleteCallback.onSuccess();
            } else {
                helper.generateError(result, onCompleteCallback);
            }
        });
    }

}

