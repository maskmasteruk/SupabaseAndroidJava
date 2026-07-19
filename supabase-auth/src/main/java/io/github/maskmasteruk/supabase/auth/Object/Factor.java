package io.github.maskmasteruk.supabase.auth.Object;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a Multi-Factor Authentication (MFA) factor.
 * <p>
 * An MFA factor is a verification method enrolled for a user account, such as
 * a Time-based One-Time Password (TOTP) authenticator application or an
 * SMS-based phone number. A factor can be enrolled, verified, challenged,
 * and used during multi-factor authentication.
 *
 * @since 1.0.0
 */
public class Factor {

    private String id;
    private String createdAt;
    private String updatedAt;
    private String status;
    private String friendlyName;
    private String factorType;
    private String phone;
    private String lastChallengedAt;


    /**
     * Default constructor for Factor.
     */
    public Factor() {
    }

    /**
     * Constructs a Factor object from a JSON representation.
     *
     * @param json The {@link JSONObject} containing factor data.
     */
    public Factor(JSONObject json) {

        id = json.optString("id");
        createdAt = json.optString("created_at");
        updatedAt = json.optString("updated_at");
        status = json.optString("status");
        friendlyName = json.optString("friendly_name");
        factorType = json.optString("factor_type");
        phone = json.optString("phone");

        if (json.isNull("last_challenged_at")) {
            lastChallengedAt = null;
        } else {
            lastChallengedAt = json.optString("last_challenged_at");
        }
    }

    /**
     * Gets the unique factor ID.
     *
     * @return The factor ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the factor ID.
     *
     * @param id The factor ID.
     * @return The current {@link Factor} instance for chaining.
     */
    public Factor setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the timestamp when the factor was created.
     *
     * @return The creation timestamp.
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt The creation timestamp.
     * @return The current {@link Factor} instance for chaining.
     */
    public Factor setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Gets the timestamp when the factor was last updated.
     *
     * @return The update timestamp.
     */
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the update timestamp.
     *
     * @param updatedAt The update timestamp.
     * @return The current {@link Factor} instance for chaining.
     */
    public Factor setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    /**
     * Gets the current verification status of the factor.
     *
     * @return The factor status.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the verification status of the factor.
     *
     * @param status The factor status.
     * @return The current {@link Factor} instance for chaining.
     */
    public Factor setStatus(String status) {
        this.status = status;
        return this;
    }

    /**
     * Gets the user-friendly name of the factor.
     *
     * @return The friendly name.
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * Sets the user-friendly name of the factor.
     *
     * @param friendlyName The friendly name.
     * @return The current {@link Factor} instance for chaining.
     */
    public Factor setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
        return this;
    }

    /**
     * Gets the type of MFA factor (e.g., "totp", "phone").
     *
     * @return The factor type.
     */
    public String getFactorType() {
        return factorType;
    }

    /**
     * Sets the type of MFA factor.
     *
     * @param factorType The factor type.
     * @return The current {@link Factor} instance for chaining.
     */
    public Factor setFactorType(String factorType) {
        this.factorType = factorType;
        return this;
    }

    /**
     * Gets the phone number associated with the factor, if applicable.
     *
     * @return The phone number.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number associated with the factor.
     *
     * @param phone The phone number.
     * @return The current {@link Factor} instance for chaining.
     */
    public Factor setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    /**
     * Gets the timestamp when the factor was last challenged.
     *
     * @return The last challenged timestamp, or {@code null} if never challenged.
     */
    public String getLastChallengedAt() {
        return lastChallengedAt;
    }

    /**
     * Sets the timestamp when the factor was last challenged.
     *
     * @param lastChallengedAt The last challenged timestamp.
     * @return The current {@link Factor} instance for chaining.
     */
    public Factor setLastChallengedAt(String lastChallengedAt) {
        this.lastChallengedAt = lastChallengedAt;
        return this;
    }

    /**
     * Checks whether this factor has been verified.
     *
     * @return {@code true} if the factor is verified; {@code false} otherwise.
     */
    public boolean isVerified() {
        return status.equals("verified");
    }

    @Override
    public String toString() {
        return "Factor{" +
                "id='" + id + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", status='" + status + '\'' +
                ", isVerified=" + isVerified() +
                ", friendlyName='" + friendlyName + '\'' +
                ", factorType='" + factorType + '\'' +
                ", phone='" + phone + '\'' +
                ", lastChallengedAt='" + lastChallengedAt + '\'' +
                '}';
    }

    /**
     * Converts this factor to its JSON representation.
     *
     * @return A {@link JSONObject} containing the factor data.
     * @throws JSONException If an error occurs while constructing the JSON object.
     */
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();

        json.put("id", id);
        json.put("created_at", createdAt);
        json.put("updated_at", updatedAt);
        json.put("status", status);
        json.put("friendly_name", friendlyName);
        json.put("factor_type", factorType);
        json.put("phone", phone);
        json.put("last_challenged_at", lastChallengedAt);

        return json;
    }
}
