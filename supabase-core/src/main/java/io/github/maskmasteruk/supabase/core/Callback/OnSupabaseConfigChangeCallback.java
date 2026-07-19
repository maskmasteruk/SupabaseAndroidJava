package io.github.maskmasteruk.supabase.core.Callback;

/**
 * Interface for receiving notifications when the global Supabase configuration changes.
 *
 * Responsibilities:
 * - Notifying internal components (like Auth or Storage) when the access token or other config properties are updated.
 *
 * Usage:
 * Supabase.getInstance().addOnSupabaseConfigChangeCallbacks(() -> {
 *     // React to config change
 * });
 */
public interface OnSupabaseConfigChangeCallback {
    /**
     * Called when the Supabase configuration has been modified.
     */
    void onChange();
}
