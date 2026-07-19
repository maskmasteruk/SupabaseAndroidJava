package io.github.maskmasteruk.supabase.core.Config;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Holds the configuration for the Supabase SDK.
 *
 * Responsibilities:
 * - Storing project URL and API keys.
 * - Managing the current authentication token.
 * - Validating the type of API key being used.
 *
 * Usage:
 * SupabaseConfig config = new SupabaseConfig("https://xyz.supabase.co", "sb_publishable_...");
 *
 * Thread Safety:
 * Not thread-safe. Synchronization should be handled by the caller.
 */
public class SupabaseConfig {
    /**
     * The URL of the Supabase project.
     */
    private String projectUrl;

    /**
     * The public API key (anon key) for the Supabase project.
     */
    private String projectPublishableKey;

    /**
     * The service role key for the Supabase project. Use with caution.
     */
    private String projectServiceRoleKey;

    /**
     * The access token of the currently authenticated user.
     */
    private String userAccessToken;

    /**
     * Flag to allow keys that don't start with the standard publishable prefix.
     */
    private boolean allowOtherKeys = false;


    /**
     * Default constructor for SupabaseConfig.
     */
    public SupabaseConfig() {
    }

    /**
     * Creates a SupabaseConfig with the project URL and publishable key.
     *
     * @param projectUrl             The project URL.
     * @param projectPublishableKey The public API key.
     */
    public SupabaseConfig(String projectUrl, String projectPublishableKey) {
        setProjectUrl(projectUrl);
        setProjectPublishableKey(projectPublishableKey);
    }

    /**
     * Gets the project URL.
     *
     * @return The project URL.
     */
    public String getProjectUrl() {
        return projectUrl;
    }

    /**
     * Sets the project URL.
     *
     * @param projectUrl The project URL to set.
     * @return This SupabaseConfig instance for chaining.
     */
    public SupabaseConfig setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
        return this;
    }

    /**
     * Gets the project publishable key.
     *
     * @return The publishable key.
     */
    public String getProjectPublishableKey() {
        return projectPublishableKey;
    }

    /**
     * Sets the project publishable key. Validates that it starts with "sb_publishable_" unless allowOtherKeys is true.
     *
     * @param projectPublishableKey The publishable key to set.
     * @return This SupabaseConfig instance for chaining.
     * @throws SupabaseError If the key format is invalid and other keys are not allowed.
     */
    public SupabaseConfig setProjectPublishableKey(String projectPublishableKey) {
        if (!projectPublishableKey.startsWith("sb_publishable_")) {
            if (!allowOtherKeys) {
                throw new SupabaseError("Only publishable keys are accepted by default. If you intentionally want to use a service role or another non-publishable key, explicitly opt in by calling new SupabaseConfig().setAllowOtherKeys(true) before providing the key.");
            }
        }
        this.projectPublishableKey = projectPublishableKey;
        return this;
    }

    /**
     * Gets the project service role key.
     *
     * @return The service role key.
     */
    public String getProjectServiceRoleKey() {
        return projectServiceRoleKey;
    }

    /**
     * Sets the project service role key.
     *
     * @param projectServiceRoleKey The service role key to set.
     * @return This SupabaseConfig instance for chaining.
     */
    public SupabaseConfig setProjectServiceRoleKey(String projectServiceRoleKey) {
        this.projectServiceRoleKey = projectServiceRoleKey;
        return this;
    }

    /**
     * Sets whether to allow non-publishable keys in setProjectPublishableKey.
     *
     * @param allow true to allow other keys.
     * @return This SupabaseConfig instance for chaining.
     */
    public SupabaseConfig setAllowOtherKeys(boolean allow) {
        this.allowOtherKeys = allow;
        return this;
    }

    /**
     * Returns the current Bearer token.
     * If a user is logged in, returns the userAccessToken; otherwise, returns the projectPublishableKey.
     *
     * @return The token to be used in the Authorization header.
     */
    public String getBearer() {
        return userAccessToken != null ? userAccessToken : projectPublishableKey;
    }

    /**
     * Sets the user access token.
     *
     * @param userAccessToken The user's JWT.
     * @return This SupabaseConfig instance for chaining.
     */
    public SupabaseConfig setBearer(String userAccessToken) {
        this.userAccessToken = userAccessToken;
        return this;
    }

}
