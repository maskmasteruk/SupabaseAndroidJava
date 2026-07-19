package io.github.maskmasteruk.supabase.auth;

import static java.net.HttpURLConnection.HTTP_OK;

import android.content.Context;

import io.github.maskmasteruk.supabase.auth.Callback.OnCompleteCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnFactorCreatedCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnGetFactorChallengeCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnGetFactorsCallback;
import io.github.maskmasteruk.supabase.auth.Enums.FactorType;
import io.github.maskmasteruk.supabase.auth.Object.FactorChallenge;
import io.github.maskmasteruk.supabase.auth.Object.NewFactor;
import io.github.maskmasteruk.supabase.auth.Object.SupabaseUser;
import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Runnables;
import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;

/**
 * Service class for handling Multi-Factor Authentication (MFA) operations in Supabase.
 * <p>
 * This service provides methods for managing MFA factors, including adding,
 * challenging, verifying, and deleting factors.
 */
public class MFAService {

    private static volatile MFAService instance;
    private final Helper helper;
    private final Context context;

    private MFAService(Context context) {
        this.context = context;
        helper = Helper.getInstance(context);
    }

    /**
     * Returns the singleton instance of {@link MFAService}.
     *
     * @param context The application context.
     * @return The {@link MFAService} instance.
     */
    public static MFAService getInstance(Context context) {
        if (instance == null) {
            synchronized (MFAService.class) {
                if (instance == null) {
                    instance = new MFAService(context);
                }
            }
        }
        return instance;
    }


    /**
     * Verifies an MFA challenge using a verification code.
     *
     * @param factorID           The ID of the MFA factor being verified.
     * @param factorChallenge    The challenge object obtained from {@link #challengeFactor(String, OnGetFactorChallengeCallback)}.
     * @param code               The verification code provided by the user.
     * @param onCompleteCallback Callback to handle the result of the verification.
     */
    public void verifyChallengeFactor(String factorID, FactorChallenge factorChallenge, String code, OnCompleteCallback onCompleteCallback) {
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.FACTORS);
            authUrlBuilder.appendPath(factorID);
            authUrlBuilder.appendPath(AUTH_END_POINTS.VERIFY);


            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder()
                    .append("challenge_id", factorChallenge.getId())
                    .append("code", code);

            Response result = new RequestHandler().post(authUrlBuilder.build(), jsonObjectStringBuilder.build());

            if (result.getCode() == HTTP_OK) {
                SessionData.getInstance(context).setSessionData(result.getResponseJSON());
                onCompleteCallback.onSuccess();
            } else {
                helper.generateError(result, onCompleteCallback);
            }
        });
    }

    /**
     * Initiates a challenge for an MFA factor.
     *
     * @param factorID                    The ID of the MFA factor to challenge.
     * @param onGetFactorChallengeCallback Callback to handle the resulting {@link FactorChallenge}.
     */
    public void challengeFactor(String factorID, OnGetFactorChallengeCallback onGetFactorChallengeCallback) {
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.FACTORS);
            authUrlBuilder.appendPath(factorID);
            authUrlBuilder.appendPath(AUTH_END_POINTS.CHALLENGE);


            Response result = new RequestHandler().post(authUrlBuilder.build(), null);

            if (result.getCode() == HTTP_OK) {
                onGetFactorChallengeCallback.onSuccess(new FactorChallenge(result.getResponseJSON()));
            } else {
                helper.generateError(result, onGetFactorChallengeCallback);
            }
        });
    }

    /**
     * Adds a new MFA factor for the current user.
     *
     * @param factorType              The type of factor to add (e.g., TOTP).
     * @param issuer                  The issuer name (e.g., "Supabase").
     * @param name                    A user-friendly name for the factor.
     * @param onFactorCreatedCallback Callback to handle the created factor data.
     */
    public void addFactor(FactorType factorType, String issuer, String name, OnFactorCreatedCallback onFactorCreatedCallback) {
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.FACTORS);

            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder()
                    .append("factor_type", factorType.getValue())
                    .append("issuer", issuer)
                    .append("friendly_name", name);

            Response result = new RequestHandler().post(authUrlBuilder.build(), jsonObjectStringBuilder.build());

            if (result.getCode() == HTTP_OK) {
                onFactorCreatedCallback.onSuccess(new NewFactor(result.getResponseJSON()));
            } else {
                helper.generateError(result, onFactorCreatedCallback);
            }
        });
    }

    /**
     * Deletes an MFA factor.
     *
     * @param factorID           The ID of the factor to delete.
     * @param onCompleteCallback Callback to handle the result.
     */
    public void deleteFactor(String factorID, OnCompleteCallback onCompleteCallback) {
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.FACTORS);
            authUrlBuilder.appendPath(factorID);


            Response result = new RequestHandler().delete(authUrlBuilder.build(), null);

            if (result.getCode() == HTTP_OK) {
                onCompleteCallback.onSuccess();
            } else {
                helper.generateError(result, onCompleteCallback);
            }
        });
    }

    /**
     * Lists all MFA factors enrolled for the current user.
     *
     * @param onGetFactorsCallback Callback to handle the list of factors.
     */
    public void listAllFactors(OnGetFactorsCallback onGetFactorsCallback) {
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAuthUrlBuilder();
            authUrlBuilder.appendPath(AUTH_END_POINTS.USER_DETAILS);

            Response result = new RequestHandler().get(authUrlBuilder.build());

            if (result.getCode() == HTTP_OK) {
                onGetFactorsCallback.onSuccess(new SupabaseUser(result.getResponseJSON()).getFactors());
            } else {
                helper.generateError(result, onGetFactorsCallback);
            }
        });

    }

}
