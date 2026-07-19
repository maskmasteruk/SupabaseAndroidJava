package io.github.maskmasteruk.supabase.auth.Object;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents the response returned after enrolling a new Multi-Factor
 * Authentication (MFA) factor.
 * <p>
 * This class contains the details required to complete enrollment of a new MFA
 * factor, such as the factor identifier, type, friendly name, and TOTP
 * configuration (if applicable). For TOTP factors, the response includes the
 * shared secret, provisioning URI, and QR code information used to configure an
 * authenticator application.
 *
 * @since 1.0.0
 */
public class NewFactor {

    private String id;
    private String type;
    private String friendlyName;
    private Totp totp;

    /**
     * Default constructor.
     */
    public NewFactor() {
    }

    /**
     * Constructs a Factor object from a JSON representation.
     *
     * @param json The {@link JSONObject} containing factor data.
     */
    public NewFactor(JSONObject json) {
        id = json.optString("id");
        type = json.optString("type");
        friendlyName = json.optString("friendly_name");

        JSONObject totpJson = json.optJSONObject("totp");
        if (totpJson != null) {
            totp = new Totp(totpJson);
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
     * @return The current {@link NewFactor} instance for chaining.
     */
    public NewFactor setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets the factor type (e.g., "totp").
     *
     * @return The factor type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the factor type.
     *
     * @param type The factor type.
     * @return The current {@link NewFactor} instance for chaining.
     */
    public NewFactor setType(String type) {
        this.type = type;
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
     * @return The current {@link NewFactor} instance for chaining.
     */
    public NewFactor setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
        return this;
    }

    /**
     * Gets the TOTP configuration for the factor, if applicable.
     *
     * @return The {@link Totp} configuration.
     */
    public Totp getTotp() {
        return totp;
    }

    /**
     * Sets the TOTP configuration for the factor.
     *
     * @param totp The {@link Totp} configuration.
     * @return The current {@link NewFactor} instance for chaining.
     */
    public NewFactor setTotp(Totp totp) {
        this.totp = totp;
        return this;
    }


    @Override
    public String toString() {
        return "Factor{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", friendlyName='" + friendlyName + '\'' +
                ", totp=" + totp +
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
        json.put("type", type);
        json.put("friendly_name", friendlyName);

        if (totp != null) {
            json.put("totp", totp.toJson());
        }

        return json;
    }

    /**
     * Represents the TOTP enrollment information.
     */
    public static class Totp {

        private String qrCode;
        private String secret;
        private String uri;

        /**
         * Default constructor.
         */
        public Totp() {
        }

        /**
         * Constructs a Totp object from a JSON representation.
         *
         * @param json The {@link JSONObject} containing TOTP data.
         */
        public Totp(JSONObject json) {
            qrCode = json.optString("qr_code");
            secret = json.optString("secret");
            uri = json.optString("uri");
        }

        /**
         * Gets the QR code as a base64-encoded string.
         *
         * @return The QR code.
         */
        public String getQrCode() {
            return qrCode;
        }

        /**
         * Sets the QR code.
         *
         * @param qrCode The QR code.
         * @return The current {@link Totp} instance for chaining.
         */
        public Totp setQrCode(String qrCode) {
            this.qrCode = qrCode;
            return this;
        }

        /**
         * Gets the shared TOTP secret.
         *
         * @return The TOTP secret.
         */
        public String getSecret() {
            return secret;
        }

        /**
         * Sets the shared TOTP secret.
         *
         * @param secret The TOTP secret.
         */
        public Totp setSecret(String secret) {
            this.secret = secret;
            return this;
        }

        /**
         * Gets the provisioning URI for configuring an authenticator app.
         *
         * @return The provisioning URI.
         */
        public String getUri() {
            return uri;
        }

        /**
         * Sets the provisioning URI.
         *
         * @param uri The provisioning URI.
         * @return The current {@link Totp} instance for chaining.
         */
        public Totp setUri(String uri) {
            this.uri = uri;
            return this;
        }


        @Override
        public String toString() {
            return "Totp{" +
                    "qrCode='" + qrCode.substring(0, Math.min(qrCode.length(), 20)) + "....'" +
                    ", secret='" + secret + '\'' +
                    ", uri='" + uri + '\'' +
                    '}';
        }

        /**
         * Converts this TOTP configuration to its JSON representation.
         *
         * @return A {@link JSONObject} containing the TOTP data.
         * @throws JSONException If an error occurs while constructing the JSON object.
         */
        public JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();

            json.put("qr_code", qrCode);
            json.put("secret", secret);
            json.put("uri", uri);

            return json;
        }
    }
}