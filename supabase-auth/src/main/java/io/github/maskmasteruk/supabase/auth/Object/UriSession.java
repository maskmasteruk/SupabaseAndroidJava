package io.github.maskmasteruk.supabase.auth.Object;

import android.net.Uri;
import android.net.UrlQuerySanitizer;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Represents a Supabase authentication session parsed from a redirection URI fragment.
 * <p>
 * This class is used to extract session details (access token, refresh token, etc.)
 * from the URI fragment after a successful OAuth or Magic Link redirection.
 * <p>
 * <b>Architectural Responsibility:</b> Utility model for parsing session state from URIs.
 *
 * @since 1.0.0
 */
public class UriSession {

    private String accessToken;
    private String tokenType;
    private int expiresIn;
    private long expiresAt;
    private String refreshToken;
    private String type;
    private String sb;

    /**
     * Default constructor for UriSession.
     */
    public UriSession() {
    }

    /**
     * Constructs a UriSession by parsing the fragment of the given URI.
     *
     * @param uri The URI containing session parameters in its fragment.
     * @throws SupabaseError If required session parameters are missing from the URI.
     */
    public UriSession(Uri uri) {
        String fragment = uri.getFragment();

        UrlQuerySanitizer sanitizer = new UrlQuerySanitizer();
        sanitizer.setAllowUnregisteredParamaters(true);
        sanitizer.parseQuery(fragment);

        if (!sanitizer.hasParameter("refresh_token")) {
            throw new SupabaseError("Refresh token not found in the given Uri");
        }

        if (!sanitizer.hasParameter("access_token")) {
            throw new SupabaseError("Access token not found in the given Uri");
        }

        if (!sanitizer.hasParameter("type")) {
            throw new SupabaseError("type not found in the given Uri");
        }

        accessToken = sanitizer.getValue("access_token");
        tokenType = sanitizer.getValue("token_type");
        expiresIn = Integer.parseInt(sanitizer.getValue("expires_in"));
        expiresAt = Long.parseLong(sanitizer.getValue("expires_at"));
        refreshToken = sanitizer.getValue("refresh_token");
        type = sanitizer.getValue("type");
        sb = sanitizer.getValue("sb");

    }

    /**
     * Gets the access token extracted from the URI.
     *
     * @return The access token string.
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets the access token.
     *
     * @param accessToken The access token string.
     * @return The current {@link UriSession} instance for chaining.
     */
    public UriSession setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    /**
     * Gets the token type, typically "bearer".
     *
     * @return The token type string.
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Sets the token type.
     *
     * @param tokenType The token type.
     * @return The current {@link UriSession} instance for chaining.
     */
    public UriSession setTokenType(String tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    /**
     * Gets the expiration duration in seconds.
     *
     * @return The expiration duration.
     */
    public int getExpiresIn() {
        return expiresIn;
    }

    /**
     * Sets the expiration duration.
     *
     * @param expiresIn The duration in seconds.
     * @return The current {@link UriSession} instance for chaining.
     */
    public UriSession setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }

    /**
     * Gets the absolute Unix timestamp (in seconds) when the token expires.
     *
     * @return The expiration timestamp.
     */
    public long getExpiresAt() {
        return expiresAt;
    }

    /**
     * Sets the expiration timestamp.
     *
     * @param expiresAt The Unix timestamp.
     * @return The current {@link UriSession} instance for chaining.
     */
    public UriSession setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    /**
     * Gets the refresh token extracted from the URI.
     *
     * @return The refresh token string.
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Sets the refresh token.
     *
     * @param refreshToken The refresh token string.
     * @return The current {@link UriSession} instance for chaining.
     */
    public UriSession setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    /**
     * Gets the type of the authentication flow (e.g., "recovery", "signup", "magiclink").
     *
     * @return The flow type string.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the flow type.
     *
     * @param type The flow type string.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the internal state value (if applicable).
     *
     * @return The sb value string.
     */
    public String getSb() {
        return sb;
    }

    /**
     * Sets the sb value.
     *
     * @param sb The sb value string.
     */
    public void setSb(String sb) {
        this.sb = sb;
    }

    @Override
    public String toString() {
        return "UriSession{" +
                "accessToken='" + accessToken + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", expiresAt=" + expiresAt +
                ", refreshToken='" + refreshToken + '\'' +
                ", type=" + type +
                ", sb=" + sb +
                '}';
    }

    /**
     * Serializes the UriSession object to a {@link JSONObject}.
     *
     * @return The serialized JSON object.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            json.put("access_token", accessToken);
            json.put("token_type", tokenType);
            json.put("expires_in", expiresIn);
            json.put("expires_at", expiresAt);
            json.put("refresh_token", refreshToken);
            json.put("type", type);
            json.put("sb", sb);

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return json;
    }
}

