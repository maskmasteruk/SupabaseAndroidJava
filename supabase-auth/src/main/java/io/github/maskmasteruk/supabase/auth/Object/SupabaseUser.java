package io.github.maskmasteruk.supabase.auth.Object;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a Supabase user profile.
 * <p>
 * This model contains detailed user information including their unique identifier,
 * email, phone, metadata, and linked identities from third-party providers.
 * <p>
 * <b>Architectural Responsibility:</b> Data model for user identity information.
 * <p>
 * <b>Lifecycle:</b> Maintained within a {@link Session} and updated when user details are modified.
 * <p>
 * <b>Serialization:</b> Can be serialized to and from JSON.
 *
 * @since 1.0.0
 */
public class SupabaseUser {

    private String id;
    private String aud;
    private String role;
    private String email;
    private String emailConfirmedAt;
    private String phone;
    private String confirmedAt;
    private String lastSignInAt;

    private Map<String, Object> appMetadata;
    private Map<String, Object> userMetadata;

    private List<Identity> identities;
    private List<Factor> factors;

    private String createdAt;
    private String updatedAt;
    private boolean isAnonymous;

    /**
     * Default constructor for SupabaseUser.
     */
    public SupabaseUser() {
    }

    /**
     * Constructs a SupabaseUser object from a JSON representation.
     *
     * @param json The {@link JSONObject} containing user data.
     */
    public SupabaseUser(JSONObject json) {

        id = json.optString("id");
        aud = json.optString("aud");
        role = json.optString("role");
        email = json.optString("email");
        emailConfirmedAt = json.optString("email_confirmed_at");
        phone = json.optString("phone");
        confirmedAt = json.optString("confirmed_at");
        lastSignInAt = json.optString("last_sign_in_at");

        createdAt = json.optString("created_at");
        updatedAt = json.optString("updated_at");

        isAnonymous = json.optBoolean("is_anonymous");

        JSONObject app = json.optJSONObject("app_metadata");

        if (app != null) {
            appMetadata = new HashMap<>();

            JSONArray names = app.names();

            if (names != null) {
                for (int i = 0; i < names.length(); i++) {

                    String key = names.optString(i);

                    appMetadata.put(key, app.opt(key));
                }
            }
        }

        JSONObject meta = json.optJSONObject("user_metadata");

        if (meta != null) {

            userMetadata = new HashMap<>();

            JSONArray names = meta.names();

            if (names != null) {

                for (int i = 0; i < names.length(); i++) {

                    String key = names.optString(i);

                    userMetadata.put(key, meta.opt(key));
                }
            }
        }

        JSONArray identitiesArray = json.optJSONArray("identities");

        if (identitiesArray != null) {

            identities = new ArrayList<>();

            for (int i = 0; i < identitiesArray.length(); i++) {

                JSONObject identityJson = identitiesArray.optJSONObject(i);

                if (identityJson == null)
                    continue;

                Identity identity =
                        new Identity()
                                .setIdentityId(identityJson.optString("identity_id"))
                                .setId(identityJson.optString("id"))
                                .setUserId(identityJson.optString("user_id"))
                                .setProvider(identityJson.optString("provider"))
                                .setEmail(identityJson.optString("email"))
                                .setCreatedAt(identityJson.optString("created_at"))
                                .setUpdatedAt(identityJson.optString("updated_at"))
                                .setLastSignInAt(identityJson.optString("last_sign_in_at"));

                JSONObject identityData =
                        identityJson.optJSONObject("identity_data");

                if (identityData != null) {

                    HashMap<String, Object> map =
                            new HashMap<>();

                    JSONArray keys = identityData.names();

                    if (keys != null) {

                        for (int j = 0; j < keys.length(); j++) {

                            String key = keys.optString(j);

                            map.put(key, identityData.opt(key));
                        }
                    }

                    identity.setIdentityData(map);
                }

                identities.add(identity);
            }
        }

        JSONArray factorsArray = json.optJSONArray("factors");

        if (factorsArray != null) {
            factors = new ArrayList<>();
            for (int i = 0; i < factorsArray.length(); i++) {
                JSONObject factorJson = factorsArray.optJSONObject(i);

                if (factorJson == null)
                    continue;

                factors.add(new Factor(factorJson));
            }
        }
    }

    /**
     * Gets the unique user ID (UUID).
     *
     * @return The user ID string.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the user ID.
     *
     * @param id The UUID string.
     * @return The current {@link SupabaseUser} instance for chaining.
     */
    public SupabaseUser setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the audience, typically "authenticated".
     *
     * @return The audience string.
     */
    public String getAud() {
        return aud;
    }

    /**
     * Sets the audience.
     *
     * @param aud The audience string.
     * @return The current {@link SupabaseUser} instance for chaining.
     */
    public SupabaseUser setAud(String aud) {
        this.aud = aud;
        return this;
    }

    /**
     * Gets the user's role.
     *
     * @return The role string.
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the user's role.
     *
     * @param role The role string.
     * @return The current {@link SupabaseUser} instance for chaining.
     */
    public SupabaseUser setRole(String role) {
        this.role = role;
        return this;
    }

    /**
     * Gets the user's email address.
     *
     * @return The email string.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the user's email address.
     *
     * @param email The email string.
     * @return The current {@link SupabaseUser} instance for chaining.
     */
    public SupabaseUser setEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     * Gets the timestamp when the email was confirmed.
     *
     * @return Confirmation timestamp string.
     */
    public String getEmailConfirmedAt() {
        return emailConfirmedAt;
    }

    /**
     * Sets the email confirmation timestamp.
     *
     * @param emailConfirmedAt The timestamp string.
     * @return The current {@link SupabaseUser} instance for chaining.
     */
    public SupabaseUser setEmailConfirmedAt(String emailConfirmedAt) {
        this.emailConfirmedAt = emailConfirmedAt;
        return this;
    }

    /**
     * Gets the user's phone number.
     *
     * @return The phone number string.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the user's phone number.
     *
     * @param phone The phone number string.
     * @return The current {@link SupabaseUser} instance for chaining.
     */
    public SupabaseUser setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    /**
     * Gets the timestamp when the user account was confirmed.
     *
     * @return Confirmation timestamp string.
     */
    public String getConfirmedAt() {
        return confirmedAt;
    }

    /**
     * Sets the confirmation timestamp.
     *
     * @param confirmedAt The timestamp string.
     * @return The current {@link SupabaseUser} instance for chaining.
     */
    public SupabaseUser setConfirmedAt(String confirmedAt) {
        this.confirmedAt = confirmedAt;
        return this;
    }

    /**
     * Gets the timestamp of the last sign-in.
     *
     * @return Last sign-in timestamp string.
     */
    public String getLastSignInAt() {
        return lastSignInAt;
    }

    /**
     * Sets the last sign-in timestamp.
     *
     * @param lastSignInAt The timestamp string.
     * @return The current {@link SupabaseUser} instance for chaining.
     */
    public SupabaseUser setLastSignInAt(String lastSignInAt) {
        this.lastSignInAt = lastSignInAt;
        return this;
    }

    /**
     * Gets the application metadata for the user.
     * This data is managed by the application.
     *
     * @return A map of application metadata.
     */
    public Map<String, Object> getAppMetadata() {
        return appMetadata;
    }

    /**
     * Sets the application metadata.
     *
     * @param appMetadata The metadata map.
     * @return The current {@link SupabaseUser} instance for chaining.
     */
    public SupabaseUser setAppMetadata(Map<String, Object> appMetadata) {
        this.appMetadata = appMetadata;
        return this;
    }

    /**
     * Gets the user-specific metadata.
     * This data can be updated by the user.
     *
     * @return A map of user metadata.
     */
    public Map<String, Object> getUserMetadata() {
        return userMetadata;
    }

    /**
     * Sets the user metadata.
     *
     * @param userMetadata The metadata map.
     * @return The current {@link SupabaseUser} instance for chaining.
     */
    public SupabaseUser setUserMetadata(Map<String, Object> userMetadata) {
        this.userMetadata = userMetadata;
        return this;
    }

    /**
     * Gets the list of identities linked to this user (e.g., Google, GitHub).
     *
     * @return A list of {@link Identity} objects.
     */
    public List<Identity> getIdentities() {
        return identities;
    }

    /**
     * Sets the user's identities.
     *
     * @param identities The list of identities.
     * @return The current {@link SupabaseUser} instance for chaining.
     */
    public SupabaseUser setIdentities(List<Identity> identities) {
        this.identities = identities;
        return this;
    }

    /**
     * Gets the timestamp when the user was created.
     *
     * @return Creation timestamp string.
     */
    public String getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt The timestamp string.
     * @return The current {@link SupabaseUser} instance for chaining.
     */
    public SupabaseUser setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Gets the timestamp when the user profile was last updated.
     *
     * @return Update timestamp string.
     */
    public String getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the update timestamp.
     *
     * @param updatedAt The timestamp string.
     * @return The current {@link SupabaseUser} instance for chaining.
     */
    public SupabaseUser setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    /**
     * Indicates whether the user is currently anonymous.
     *
     * @return {@code true} if anonymous, {@code false} otherwise.
     */
    public boolean isAnonymous() {
        return isAnonymous;
    }

    /**
     * Sets the anonymous status.
     *
     * @param anonymous The anonymous status.
     * @return The current {@link SupabaseUser} instance for chaining.
     */
    public SupabaseUser setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
        return this;
    }

    /**
     * Gets the enrolled MFA factors.
     *
     * @return The list of enrolled factors.
     */
    public List<Factor> getFactors() {
        return factors;
    }

    /**
     * Sets the enrolled MFA factors.
     *
     * @param factors The list of enrolled factors.
     * @return The current {@link SupabaseUser} instance for chaining.
     */
    public SupabaseUser setFactors(List<Factor> factors) {
        this.factors = factors;
        return this;
    }

    @Override
    public String toString() {
        return "SupabaseUser{" +
                "id='" + id + '\'' +
                ", aud='" + aud + '\'' +
                ", role='" + role + '\'' +
                ", email='" + email + '\'' +
                ", emailConfirmedAt='" + emailConfirmedAt + '\'' +
                ", phone='" + phone + '\'' +
                ", confirmedAt='" + confirmedAt + '\'' +
                ", lastSignInAt='" + lastSignInAt + '\'' +
                ", appMetadata=" + appMetadata +
                ", userMetadata=" + userMetadata +
                ", identities=" + identities +
                ", factors=" + factors +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", isAnonymous=" + isAnonymous +
                '}';
    }

    /**
     * Serializes this SupabaseUser object to a {@link JSONObject}.
     *
     * @return The serialized JSON object.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            json.put("id", id);
            json.put("aud", aud);
            json.put("role", role);
            json.put("email", email);
            json.put("email_confirmed_at", emailConfirmedAt);
            json.put("phone", phone);
            json.put("confirmed_at", confirmedAt);
            json.put("last_sign_in_at", lastSignInAt);
            json.put("created_at", createdAt);
            json.put("updated_at", updatedAt);
            json.put("is_anonymous", isAnonymous);

            if (appMetadata != null) {
                JSONObject app = new JSONObject();

                for (Map.Entry<String, Object> entry : appMetadata.entrySet()) {
                    app.put(entry.getKey(), entry.getValue());
                }

                json.put("app_metadata", app);
            }

            if (userMetadata != null) {
                JSONObject meta = new JSONObject();

                for (Map.Entry<String, Object> entry : userMetadata.entrySet()) {
                    meta.put(entry.getKey(), entry.getValue());
                }

                json.put("user_metadata", meta);
            }

            if (identities != null) {
                JSONArray array = new JSONArray();

                for (Identity identity : identities) {

                    JSONObject obj = new JSONObject();

                    obj.put("identity_id", identity.getIdentityId());
                    obj.put("id", identity.getId());
                    obj.put("user_id", identity.getUserId());
                    obj.put("provider", identity.getProvider());
                    obj.put("email", identity.getEmail());
                    obj.put("created_at", identity.getCreatedAt());
                    obj.put("updated_at", identity.getUpdatedAt());
                    obj.put("last_sign_in_at", identity.getLastSignInAt());

                    if (identity.getIdentityData() != null) {
                        JSONObject identityData = new JSONObject();

                        for (Map.Entry<String, Object> entry : identity.getIdentityData().entrySet()) {
                            identityData.put(entry.getKey(), entry.getValue());
                        }

                        obj.put("identity_data", identityData);
                    }

                    array.put(obj);
                }

                json.put("identities", array);
            }

            if (factors != null) {
                JSONArray array = new JSONArray();
                for (Factor factor : factors) {
                    array.put(factor.toJson());
                }
                json.put("factors", array);
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return json;
    }

    /**
     * Represents a user identity linked to a third-party provider.
     */
    public static class Identity {

        private String identityId;
        private String id;
        private String userId;

        private Map<String, Object> identityData;

        private String provider;
        private String lastSignInAt;
        private String createdAt;
        private String updatedAt;
        private String email;

        /**
         * Default constructor for Identity.
         */
        public Identity() {
        }

        /**
         * Gets the unique identity ID.
         *
         * @return The identity ID string.
         */
        public String getIdentityId() {
            return identityId;
        }

        /**
         * Sets the identity ID.
         *
         * @param identityId The identity ID string.
         * @return The current {@link Identity} instance for chaining.
         */
        public Identity setIdentityId(String identityId) {
            this.identityId = identityId;
            return this;
        }

        /**
         * Gets the internal ID.
         *
         * @return The ID string.
         */
        public String getId() {
            return id;
        }

        /**
         * Sets the internal ID.
         *
         * @param id The ID string.
         * @return The current {@link Identity} instance for chaining.
         */
        public Identity setId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Gets the user ID associated with this identity.
         *
         * @return The user ID string.
         */
        public String getUserId() {
            return userId;
        }

        /**
         * Sets the user ID.
         *
         * @param userId The user ID string.
         * @return The current {@link Identity} instance for chaining.
         */
        public Identity setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        /**
         * Gets the provider-specific data for this identity.
         *
         * @return A map of identity data.
         */
        public Map<String, Object> getIdentityData() {
            return identityData;
        }

        /**
         * Sets the identity data.
         *
         * @param identityData The data map.
         * @return The current {@link Identity} instance for chaining.
         */
        public Identity setIdentityData(Map<String, Object> identityData) {
            this.identityData = identityData;
            return this;
        }

        /**
         * Gets the name of the identity provider (e.g., "google", "github").
         *
         * @return The provider name string.
         */
        public String getProvider() {
            return provider;
        }

        /**
         * Sets the provider name.
         *
         * @param provider The provider name.
         * @return The current {@link Identity} instance for chaining.
         */
        public Identity setProvider(String provider) {
            this.provider = provider;
            return this;
        }

        /**
         * Gets the timestamp of the last sign-in via this identity.
         *
         * @return Last sign-in timestamp string.
         */
        public String getLastSignInAt() {
            return lastSignInAt;
        }

        /**
         * Sets the last sign-in timestamp.
         *
         * @param lastSignInAt The timestamp string.
         * @return The current {@link Identity} instance for chaining.
         */
        public Identity setLastSignInAt(String lastSignInAt) {
            this.lastSignInAt = lastSignInAt;
            return this;
        }

        /**
         * Gets the timestamp when this identity was linked.
         *
         * @return Creation timestamp string.
         */
        public String getCreatedAt() {
            return createdAt;
        }

        /**
         * Sets the creation timestamp.
         *
         * @param createdAt The timestamp string.
         * @return The current {@link Identity} instance for chaining.
         */
        public Identity setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /**
         * Gets the timestamp when this identity was last updated.
         *
         * @return Update timestamp string.
         */
        public String getUpdatedAt() {
            return updatedAt;
        }

        /**
         * Sets the update timestamp.
         *
         * @param updatedAt The timestamp string.
         * @return The current {@link Identity} instance for chaining.
         */
        public Identity setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        /**
         * Gets the email address associated with this identity.
         *
         * @return The email address string.
         */
        public String getEmail() {
            return email;
        }

        /**
         * Sets the identity email.
         *
         * @param email The email address string.
         * @return The current {@link Identity} instance for chaining.
         */
        public Identity setEmail(String email) {
            this.email = email;
            return this;
        }

        @Override
        public String toString() {
            return "Identity{" +
                    "identityId='" + identityId + '\'' +
                    ", id='" + id + '\'' +
                    ", userId='" + userId + '\'' +
                    ", identityData=" + identityData +
                    ", provider='" + provider + '\'' +
                    ", lastSignInAt='" + lastSignInAt + '\'' +
                    ", createdAt='" + createdAt + '\'' +
                    ", updatedAt='" + updatedAt + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }
}
