package io.github.maskmasteruk.supabase.auth.admin;

import static java.net.HttpURLConnection.HTTP_OK;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import io.github.maskmasteruk.supabase.auth.Callback.OnCompleteCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnGetFactorsCallback;
import io.github.maskmasteruk.supabase.auth.Object.Factor;
import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Runnables;

/**
 * Service class for administrative Multi-Factor Authentication (MFA) operations.
 * <p>
 * This service provides methods to manage MFA factors for any user using
 * administrative privileges.
 */
public class MFAService {
    private static volatile MFAService instance;
    private final Helper helper;


    private MFAService(Context context) {
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
     * Retrieves all MFA factors for a specific user.
     *
     * @param userID The unique ID of the user.
     */
    public void listFactors(String userID, OnGetFactorsCallback onGetFactorsCallback) {
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAdminAuthUrlBuilder();
            authUrlBuilder.appendPath(ADMIN_AUTH_END_POINTS.USERS);
            authUrlBuilder.appendPath(userID);
            authUrlBuilder.appendPath(ADMIN_AUTH_END_POINTS.FACTORS);

            Response result = new RequestHandler().getServiceRole(authUrlBuilder.build());

            if (result.getCode() == HTTP_OK) {
                JSONArray responseJSONArray = result.getResponseJSONArray();
                List<Factor> factors = new ArrayList<>();
                for (int i = 0; i < responseJSONArray.length(); i++) {
                    try {
                        factors.add(new Factor(responseJSONArray.getJSONObject(i)));
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
                onGetFactorsCallback.onSuccess(factors);
            } else {
                helper.generateError(result, onGetFactorsCallback);
            }
        });
    }


    /**
     * Deletes an MFA factor.
     *
     * @param factorID           The ID of the factor to delete.
     * @param onCompleteCallback Callback to handle the result.
     */
    public void deleteFactor(String userID, String factorID, OnCompleteCallback onCompleteCallback) {
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder authUrlBuilder = helper.getBaseAdminAuthUrlBuilder();
            authUrlBuilder.appendPath(ADMIN_AUTH_END_POINTS.USERS);
            authUrlBuilder.appendPath(userID);
            authUrlBuilder.appendPath(ADMIN_AUTH_END_POINTS.FACTORS);
            authUrlBuilder.appendPath(factorID);


            Response result = new RequestHandler().deleteServiceRole(authUrlBuilder.build(), null);

            if (result.getCode() == HTTP_OK) {
                onCompleteCallback.onSuccess();
            } else {
                helper.generateError(result, onCompleteCallback);
            }
        });
    }

}

