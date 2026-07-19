package io.github.maskmasteruk.supabase.auth.Object;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Supabase authentication session.
 * <p>
 * This model encapsulates all the data returned after a successful authentication,
 * including the access token, refresh token, and user profile information.
 * <p>
 * <b>Architectural Responsibility:</b> Data model for authentication state.
 * <p>
 * <b>Lifecycle:</b> Created upon successful login/signup and updated during token refresh.
 * <p>
 * <b>Serialization:</b> Can be serialized to and from JSON for local persistence.
 *
 * @since 1.0.0
 */
public class Session {

    private String accessToken;
    private String tokenType;
    private int expiresIn;
    private long expiresAt;
    private String refreshToken;
    private SupabaseUser user;
    private WeakPassword weakPassword;

    /**
     * Default constructor for Session.
     */
    public Session() {
    }

    /**
     * Constructs a Session object from a JSON representation.
     *
     * @param json The {@link JSONObject} containing session data.
     */
    public Session(JSONObject json) {

        accessToken = json.optString("access_token");
        tokenType = json.optString("token_type");
        expiresIn = json.optInt("expires_in");
        expiresAt = json.optLong("expires_at");
        refreshToken = json.optString("refresh_token");

        JSONObject userJson = json.optJSONObject("user");
        if (userJson != null) {
            user = new SupabaseUser(userJson);
        }

        JSONObject weakPasswordJson = json.optJSONObject("weak_password");
        if (weakPasswordJson != null) {
            weakPassword = new WeakPassword(weakPasswordJson);
        }
    }

    /**
     * Gets the JWT access token.
     * This token should be included in the header of authenticated requests.
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
     * @return The current {@link Session} instance for chaining.
     */
    public Session setAccessToken(String accessToken) {
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
     * @return The current {@link Session} instance for chaining.
     */
    public Session setTokenType(String tokenType) {
        this.tokenType = tokenType;
        return this;
    }

    /**
     * Gets the duration in seconds until the access token expires.
     *
     * @return The expiration duration in seconds.
     */
    public int getExpiresIn() {
        return expiresIn;
    }

    /**
     * Sets the expiration duration.
     *
     * @param expiresIn The duration in seconds.
     * @return The current {@link Session} instance for chaining.
     */
    public Session setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }

    /**
     * Gets the absolute Unix timestamp (in seconds) when the access token expires.
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
     * @return The current {@link Session} instance for chaining.
     */
    public Session setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    /**
     * Gets the refresh token, which is used to obtain a new access token when the current one expires.
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
     * @return The current {@link Session} instance for chaining.
     */
    public Session setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    /**
     * Gets the user object associated with this session.
     *
     * @return The {@link SupabaseUser} object, or {@code null} if not available.
     */
    public SupabaseUser getUser() {
        return user;
    }

    /**
     * Sets the user object.
     *
     * @param user The {@link SupabaseUser} instance.
     * @return The current {@link Session} instance for chaining.
     */
    public Session setUser(SupabaseUser user) {
        this.user = user;
        return this;
    }

    /**
     * Gets weak password validation details if the user signed up with a weak password.
     *
     * @return A {@link WeakPassword} object, or {@code null} if the password is valid.
     */
    public WeakPassword getWeakPassword() {
        return weakPassword;
    }

    /**
     * Sets the weak password details.
     *
     * @param weakPassword The {@link WeakPassword} instance.
     * @return The current {@link Session} instance for chaining.
     */
    public Session setWeakPassword(WeakPassword weakPassword) {
        this.weakPassword = weakPassword;
        return this;
    }

    @Override
    public String toString() {
        return "Session{" +
                "accessToken='" + accessToken + '\'' +
                ", tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", expiresAt=" + expiresAt +
                ", refreshToken='" + refreshToken + '\'' +
                ", user=" + user +
                ", weakPassword=" + weakPassword +
                '}';
    }

    /**
     * Serializes this Session object to a {@link JSONObject}.
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

            if (user != null) {
                json.put("user", user.toJson());
            }

            if (weakPassword != null) {
                json.put("weak_password", weakPassword.toJson());
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return json;
    }

    /**
     * Represents information regarding weak password validation.
     * <p>
     * Includes a descriptive message and specific reasons why the password is considered weak.
     */
    public static class WeakPassword {

        private String message;
        private List<String> reasons;

        /**
         * Default constructor for WeakPassword.
         */
        public WeakPassword() {
        }

        /**
         * Constructs a WeakPassword object from a JSON representation.
         *
         * @param json The {@link JSONObject} containing weak password info.
         */
        public WeakPassword(JSONObject json) {

            message = json.optString("message");

            JSONArray array = json.optJSONArray("reasons");

            if (array != null) {
                reasons = new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {
                    reasons.add(array.optString(i));
                }
            }
        }

        /**
         * Gets the descriptive message about the weak password.
         *
         * @return The message string.
         */
        public String getMessage() {
            return message;
        }

        /**
         * Sets the message.
         *
         * @param message The message string.
         * @return The current {@link WeakPassword} instance for chaining.
         */
        public WeakPassword setMessage(String message) {
            this.message = message;
            return this;
        }

        /**
         * Gets the list of specific reasons why the password is weak.
         *
         * @return A list of reason strings.
         */
        public List<String> getReasons() {
            return reasons;
        }

        /**
         * Sets the list of reasons.
         *
         * @param reasons The list of reasons.
         * @return The current {@link WeakPassword} instance for chaining.
         */
        public WeakPassword setReasons(List<String> reasons) {
            this.reasons = reasons;
            return this;
        }


        @Override
        public String toString() {
            return "WeakPassword{" +
                    "message='" + message + '\'' +
                    ", reasons=" + reasons +
                    '}';
        }

        /**
         * Serializes the WeakPassword object to a {@link JSONObject}.
         *
         * @return The serialized JSON object.
         */
        public JSONObject toJson() {
            JSONObject json = new JSONObject();

            try {
                json.put("message", message);

                if (reasons != null) {
                    JSONArray array = new JSONArray();

                    for (String reason : reasons) {
                        array.put(reason);
                    }

                    json.put("reasons", array);
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            return json;
        }
    }
}

