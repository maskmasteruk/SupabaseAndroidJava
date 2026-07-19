package io.github.maskmasteruk.supabase.auth.Object;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a Multi-Factor Authentication (MFA) challenge.
 * <p>
 * An MFA challenge is created when a user initiates verification of an enrolled
 * MFA factor. The challenge contains a unique identifier, the factor type, and
 * an expiration time, and must be completed by providing a valid verification
 * code before it expires.
 *
 * @since 1.0.0
 */
public class FactorChallenge {

    private String id;
    private String type;
    private long expiresAt;

    /**
     * Default constructor for Challenge.
     */
    public FactorChallenge() {
    }

    /**
     * Constructs a Challenge object from a JSON representation.
     *
     * @param json The {@link JSONObject} containing challenge data.
     */
    public FactorChallenge(JSONObject json) {
        id = json.optString("id");
        type = json.optString("type");
        expiresAt = json.optLong("expires_at");
    }

    /**
     * Gets the unique challenge ID.
     *
     * @return The challenge ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the challenge ID.
     *
     * @param id The challenge ID.
     * @return The current {@link FactorChallenge} instance for chaining.
     */
    public FactorChallenge setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the challenge type.
     *
     * @return The challenge type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the challenge type.
     *
     * @param type The challenge type.
     * @return The current {@link FactorChallenge} instance for chaining.
     */
    public FactorChallenge setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Gets the UNIX timestamp at which this challenge expires.
     *
     * @return The expiration timestamp in seconds since the Unix epoch.
     */
    public long getExpiresAt() {
        return expiresAt;
    }

    /**
     * Sets the UNIX timestamp at which this challenge expires.
     *
     * @param expiresAt The expiration timestamp in seconds since the Unix epoch.
     * @return The current {@link FactorChallenge} instance for chaining.
     */
    public FactorChallenge setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    /**
     * Converts this challenge to its JSON representation.
     *
     * @return A {@link JSONObject} containing the challenge data.
     * @throws JSONException If an error occurs while constructing the JSON object.
     */
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("id", id);
        json.put("type", type);
        json.put("expires_at", expiresAt);

        return json;
    }

    @Override
    public String toString() {
        return "Challenge{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
