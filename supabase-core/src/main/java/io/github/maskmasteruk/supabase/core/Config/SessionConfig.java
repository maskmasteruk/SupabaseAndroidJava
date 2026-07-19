package io.github.maskmasteruk.supabase.core.Config;

/**
 * Represents the session configuration, specifically authentication tokens.
 *
 * Responsibilities:
 * - Holding the current access token (JWT) and refresh token.
 * - Providing a data structure for session persistence or transfer.
 *
 * Usage:
 * SessionConfig session = new SessionConfig(accessToken, refreshToken);
 *
 * Thread Safety:
 * Not thread-safe. Synchronization should be handled by the caller if shared across threads.
 */
public class SessionConfig {
    /**
     * The JWT access token used for authenticated requests.
     */
    private String accessToken;

    /**
     * The refresh token used to obtain a new access token.
     */
    private String refreshToken;


    /**
     * Default constructor for SessionConfig.
     */
    public SessionConfig() {
    }

    /**
     * Creates a SessionConfig with the specified tokens.
     *
     * @param accessToken  The access token.
     * @param refreshToken The refresh token.
     */
    public SessionConfig(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    /**
     * Gets the current access token.
     *
     * @return The access token.
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets the access token.
     *
     * @param accessToken The access token to set.
     * @return This SessionConfig instance for chaining.
     */
    public SessionConfig setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    /**
     * Gets the current refresh token.
     *
     * @return The refresh token.
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Sets the refresh token.
     *
     * @param refreshToken The refresh token to set.
     * @return This SessionConfig instance for chaining.
     */
    public SessionConfig setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }
}
