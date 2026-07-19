package io.github.maskmasteruk.supabase.storage;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;


/**
 * Manages persistence for resumable upload tasks.
 * <p>
 * Purpose: This class uses Android {@link SharedPreferences} to store the upload URLs (links)
 * for resumable uploads, allowing them to be resumed even after the app is restarted.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Storing resumable task links mapped to a unique hash key of the file/reference.</li>
 *     <li>Managing expiration of task links (default 24 hours).</li>
 *     <li>Providing methods to clear or remove specific task info.</li>
 * </ul>
 * </p>
 */
class ResumableTasksInfo {
    private static final String PREF_NAME = "SUPABASE-STORAGE-RESUMABLE-TASK-INFO";
    private static final String TIMING_LABEL = "_TIMING";
    private SharedPreferences sharedPreferences;
    private static ResumableTasksInfo resumableTasksInfo;

    /**
     * Package-private constructor.
     *
     * @param context The Android context.
     */
    ResumableTasksInfo(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    }

    /**
     * Returns the singleton instance of {@code ResumableTasksInfo}.
     *
     * @param context The Android context.
     * @return The singleton instance.
     */
    public static ResumableTasksInfo getInstance(Context context) {
        synchronized (ResumableTasksInfo.class) {
            if (resumableTasksInfo == null) {
                resumableTasksInfo = new ResumableTasksInfo(context);
            }
            return resumableTasksInfo;
        }
    }

    /**
     * Retrieves the resumable upload link for a given hash key.
     * Checks if the link has expired (older than 24 hours).
     *
     * @param hashKey The hash key associated with the upload.
     * @return The upload link, or {@code null} if not found or expired.
     */
    public String getResumableTasksInfo(String hashKey) {
        String string = sharedPreferences.getString(hashKey, null);
        if (string != null) {
            if (TimeUnit.MILLISECONDS.toHours(Math.abs(System.currentTimeMillis() - sharedPreferences.getLong(hashKey + TIMING_LABEL, -1))) > 24) {
                remove(hashKey);
                return null;
            }
        }
        return string;
    }

    /**
     * Persists a resumable upload link and the current timestamp.
     *
     * @param hashKey            The hash key.
     * @param resumableTaskLink The upload URL.
     */
    public void setResumableTasksInfo(String hashKey, String resumableTaskLink) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(hashKey, resumableTaskLink);
        editor.putLong(hashKey + TIMING_LABEL, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Clears all stored resumable task information.
     */
    public void clearResumableTasksInfo() {
        sharedPreferences.edit().clear().apply();
    }

    /**
     * Removes information for a specific task.
     *
     * @param hashKey The hash key to remove.
     */
    public void remove(String hashKey) {
        sharedPreferences.edit().remove(hashKey).apply();
    }
}
