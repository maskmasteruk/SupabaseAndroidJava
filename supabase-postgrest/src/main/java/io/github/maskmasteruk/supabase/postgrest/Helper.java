package io.github.maskmasteruk.supabase.postgrest;

import android.util.Log;

import org.json.JSONException;

import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.postgrest.Callback.OnPostgrestCallback;

/**
 * Internal helper class for PostgREST operations.
 *
 * <p>Provides common utilities for URL construction and error generation from network responses.</p>
 */
class Helper {
    private static volatile Helper instance;

    private Helper() {
    }

    /**
     * Gets the singleton instance of {@link Helper}.
     *
     * @return The {@link Helper} instance.
     */
    public static Helper getInstance() {
        if (instance == null) {
            synchronized (Helper.class) {
                if (instance == null) {
                    instance = new Helper();
                }
            }
        }
        return instance;
    }

    /**
     * Constructs the base URL for PostgREST requests (rest/v1).
     *
     * @return A {@link UrlBuilder} initialized with the base PostgREST path.
     */
    public UrlBuilder getBaseRestUrlBuilder() {
        return new UrlBuilder().appendPath(POSTGREST_END_POINTS.REST).appendPath(POSTGREST_END_POINTS.VERSION);
    }


    /**
     * Generates a {@link SupabaseError} from a network response and notifies the callback.
     *
     * @param response The network response containing error details.
     * @param object The callback object (expected to be an {@link OnPostgrestCallback}).
     * @throws RuntimeException if there's an error parsing the error response JSON.
     */
    public void generateError(Response response, Object object) {
        SupabaseError supabaseError;

        try {
            supabaseError = new SupabaseError(response.getResponseJSON().toString(4));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if (object == null) {
            Log.e("SupabasePostgrestError", supabaseError.getErrorMessage());
        } else if (object instanceof OnPostgrestCallback) {
            ((OnPostgrestCallback) object).onFailure(supabaseError);
        }else {
            Log.e("SupabasePostgrestError", supabaseError.getErrorMessage());
        }

    }
}
