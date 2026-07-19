package io.github.maskmasteruk.supabase.auth.admin;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.maskmasteruk.supabase.auth.Callback.AuthCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnCompleteCallback;
import io.github.maskmasteruk.supabase.auth.Callback.OnVerifyCallback;
import io.github.maskmasteruk.supabase.auth.admin.Callback.OnGetLinkCallback;
import io.github.maskmasteruk.supabase.auth.admin.Callback.OnGetUsersCallback;
import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;

/**
 * Internal helper class for common administrative authentication tasks.
 */
class Helper {
    private static volatile Helper instance;
    private final Context context;


    private Helper(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Returns the singleton instance of {@link Helper}.
     *
     * @param context The application context.
     * @return The {@link Helper} instance.
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
     * Creates a {@link UrlBuilder} initialized with the base Auth API path.
     *
     * @return A {@link UrlBuilder} for the Auth API.
     */
    public UrlBuilder getBaseAuthUrlBuilder() {
        return new UrlBuilder().appendPath(ADMIN_AUTH_END_POINTS.AUTH).appendPath(ADMIN_AUTH_END_POINTS.VERSION);
    }

    /**
     * Creates a {@link UrlBuilder} initialized with the base Admin Auth API path.
     *
     * @return A {@link UrlBuilder} for the Admin Auth API.
     */
    public UrlBuilder getBaseAdminAuthUrlBuilder() {
        return getBaseAuthUrlBuilder().appendPath(ADMIN_AUTH_END_POINTS.ADMIN);
    }

    /**
     * Parses an error response from the Supabase Auth API and notifies the provided callback.
     *
     * @param response The {@link Response} containing the error data.
     * @param object   The callback object to notify.
     */
    public void generateError(Response response, Object object) {
        SupabaseError supabaseError;

        try {
            JSONObject resultJson = JsonUtils.getJsonObject(response.getResponse());
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
            throw supabaseError;
        } else if (object instanceof AuthCallback) {
            ((AuthCallback) object).onError(supabaseError);
        } else if (object instanceof OnCompleteCallback) {
            ((OnCompleteCallback) object).onError(supabaseError);
        } else if (object instanceof OnGetUsersCallback) {
            ((OnGetUsersCallback) object).onError(supabaseError);
        } else if (object instanceof OnGetLinkCallback) {
            ((OnGetLinkCallback) object).onError(supabaseError);
        } else if (object instanceof OnVerifyCallback) {
            ((OnVerifyCallback) object).onInvalid(supabaseError);
        } else {
            throw supabaseError;
        }
    }


}

