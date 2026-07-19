package io.github.maskmasteruk.supabase.core;

import java.util.ArrayList;

import io.github.maskmasteruk.supabase.core.Callback.OnSupabaseConfigChangeCallback;
import io.github.maskmasteruk.supabase.core.Config.SupabaseConfig;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * The main entry point for the Supabase SDK.
 *
 * Responsibilities:
 * - SDK initialization and configuration management.
 * - Global singleton instance management.
 * - Managing configuration change listeners.
 * - Managing authentication tokens (Bearer tokens).
 *
 * Usage:
 * Initialize the SDK once at application start:
 * Supabase.initialize(new SupabaseConfig(url, key));
 *
 * Access the instance:
 * Supabase supabase = Supabase.getInstance();
 *
 * Thread Safety:
 * Initialization is synchronized and thread-safe. Instance access assumes proper initialization.
 */
public class Supabase {
    /**
     * Singleton instance of the Supabase class.
     */
    private static Supabase instance;

    /**
     * Current configuration for the Supabase SDK.
     */
    private final SupabaseConfig supabaseConfig;

    /**
     * List of callbacks to be notified when the configuration changes.
     */
    private final ArrayList<OnSupabaseConfigChangeCallback> onSupabaseConfigChangeCallbacks;

    /**
     * Private constructor for the Supabase singleton.
     *
     * @param supabaseConfig The initial configuration to use.
     */
    private Supabase(SupabaseConfig supabaseConfig) {
        this.supabaseConfig = supabaseConfig;
        onSupabaseConfigChangeCallbacks = new ArrayList<>();
    }

    /**
     * Initializes the Supabase SDK with the provided configuration.
     * This should be called once, typically in the Application class.
     *
     * @param supabaseConfig The configuration containing Supabase URL and API Key.
     *
     * Example:
     * SupabaseConfig config = new SupabaseConfig("https://xyz.supabase.co", "apikey");
     * Supabase.initialize(config);
     */
    public static synchronized void initialize(SupabaseConfig supabaseConfig) {
        if (instance != null) {
            return;
        }
        instance = new Supabase(supabaseConfig);
    }

    /**
     * Returns the singleton instance of the Supabase class.
     *
     * @return The Supabase instance.
     * @throws SupabaseError If the SDK has not been initialized yet.
     *
     * Example:
     * Supabase supabase = Supabase.getInstance();
     */
    public static Supabase getInstance() {
        if (instance == null) {
            throw new SupabaseError("Supabase is not Initialized");
        }
        return instance;
    }

    /**
     * Adds a callback to be notified when the configuration (like the Bearer token) changes.
     *
     * @param onSupabaseConfigChangeCallback The callback to add.
     */
    public void addOnSupabaseConfigChangeCallbacks(OnSupabaseConfigChangeCallback onSupabaseConfigChangeCallback) {
        onSupabaseConfigChangeCallbacks.add(onSupabaseConfigChangeCallback);
    }

    /**
     * Removes a configuration change callback.
     *
     * @param onSupabaseConfigChangeCallback The callback to remove.
     * @return true if the callback was successfully removed.
     */
    public boolean removeOnSupabaseConfigChangeCallbacks(OnSupabaseConfigChangeCallback onSupabaseConfigChangeCallback) {
        return onSupabaseConfigChangeCallbacks.remove(onSupabaseConfigChangeCallback);
    }

    /**
     * Sets the Bearer token (JWT) for authentication and notifies all registered listeners.
     *
     * @param userAccessToken The JWT to use for subsequent requests.
     */
    public void setBearer(String userAccessToken) {
        supabaseConfig.setBearer(userAccessToken);
        notifyOnSupabaseConfigChangeCallbacks();
    }

    /**
     * Notifies all registered listeners that the configuration has changed.
     */
    private void notifyOnSupabaseConfigChangeCallbacks() {
        onSupabaseConfigChangeCallbacks.forEach(OnSupabaseConfigChangeCallback::onChange);
    }

    /**
     * Gets the current Supabase configuration.
     *
     * @return The SupabaseConfig instance.
     */
    public SupabaseConfig getSupabaseConfig() {
        return supabaseConfig;
    }
}
