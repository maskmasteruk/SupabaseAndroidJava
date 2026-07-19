package io.github.maskmasteruk.supabase.auth;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import io.github.maskmasteruk.supabase.auth.Object.Session;
import io.github.maskmasteruk.supabase.core.Supabase;
import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;

/**
 * Internal manager for the persistence and lifecycle of Supabase authentication session data.
 * <p>
 * This class uses {@link SharedPreferences} to store session details securely and
 * automatically updates the Bearer token in the {@link Supabase} core instance.
 * <p>
 * <b>Architectural Responsibility:</b> Local session persistence and synchronization with core.
 * <p>
 * <b>Lifecycle:</b> Singleton instance that manages the "logged-in" state of the application.
 * <p>
 * <b>Thread Safety:</b> Thread-safe singleton implementation.
 *
 * @since 1.0.0
 */
class SessionData {
    private static final String PREF_NAME = "SUPABASE-AUTH";
    private static final String SESSION_KEY = "SESSION";
    private static SessionData sessionData;
    private SharedPreferences sharedPreferences;

    /**
     * Initializes the SessionData instance and restores the session if it exists.
     * Automatically sets the Bearer token in the {@link Supabase} client.
     *
     * @param context The Android context.
     */
    public SessionData(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        Session session = getSessionData();
        if (session != null) {
            Supabase.getInstance().setBearer(session.getAccessToken());
        } else {
            Supabase.getInstance().setBearer(null);
        }
    }

    /**
     * Returns the singleton instance of SessionData.
     *
     * @param context The Android context.
     * @return The {@link SessionData} singleton.
     */
    public static SessionData getInstance(Context context) {
        synchronized (SessionData.class) {
            if (sessionData == null) {
                sessionData = new SessionData(context);
            }
            return sessionData;
        }
    }

    /**
     * Retrieves the currently saved session from local storage.
     *
     * @return The {@link Session} object, or {@code null} if no session is saved.
     */
    public Session getSessionData() {
        String string = sharedPreferences.getString(SESSION_KEY, null);
        if (string == null) {
            return null;
        }

        return new Session(JsonUtils.getJsonObject(string));
    }

    /**
     * Persists the given session data and updates the global Bearer token.
     *
     * @param jsonObject The {@link JSONObject} containing session data from the server.
     */
    public void setSessionData(JSONObject jsonObject) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SESSION_KEY, jsonObject.toString());
        Session session = new Session(jsonObject);
        Supabase.getInstance().setBearer(session.getAccessToken());
        editor.apply();
    }

    /**
     * Clears the saved session from local storage and resets the global Bearer token.
     */
    public void clearSessionData() {
        sharedPreferences.edit().clear().apply();
        Supabase.getInstance().setBearer(null);
    }
}


