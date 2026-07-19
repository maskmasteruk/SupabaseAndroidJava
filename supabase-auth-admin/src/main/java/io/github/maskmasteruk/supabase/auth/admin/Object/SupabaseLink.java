package io.github.maskmasteruk.supabase.auth.admin.Object;

import org.json.JSONObject;

/**
 * Represents a generated authentication link and associated tokens.
 */
public class SupabaseLink {

    private String actionLink;
    private String emailOtp;
    private String hashedToken;
    private String verificationType;
    private String redirectTo;

    /**
     * Default constructor.
     */
    public SupabaseLink() {
    }

    /**
     * Constructs a {@code SupabaseLink} from a JSON object.
     *
     * @param json The JSON object containing link details.
     */
    public SupabaseLink(JSONObject json) {
        this.actionLink = json.optString("action_link", "");
        this.emailOtp = json.optString("email_otp", "");
        this.hashedToken = json.optString("hashed_token", "");
        this.verificationType = json.optString("verification_type", "");
        this.redirectTo = json.optString("redirect_to", "");
    }

    /**
     * Constructs a {@code SupabaseLink} with specified details.
     *
     * @param actionLink       The full URL for the action.
     * @param emailOtp         The OTP sent via email.
     * @param hashedToken      The hashed version of the token.
     * @param verificationType The type of verification (e.g., signup, recovery).
     * @param redirectTo       The URL to redirect to after verification.
     */
    public SupabaseLink(String actionLink, String emailOtp, String hashedToken,
                        String verificationType, String redirectTo) {
        this.actionLink = actionLink;
        this.emailOtp = emailOtp;
        this.hashedToken = hashedToken;
        this.verificationType = verificationType;
        this.redirectTo = redirectTo;
    }

    /**
     * Returns the full action link URL.
     *
     * @return The action link.
     */
    public String getActionLink() {
        return actionLink;
    }

    /**
     * Sets the action link URL.
     *
     * @param actionLink The action link to set.
     * @return This instance for chaining.
     */
    public SupabaseLink setActionLink(String actionLink) {
        this.actionLink = actionLink;
        return this;
    }

    /**
     * Returns the email OTP.
     *
     * @return The email OTP.
     */
    public String getEmailOtp() {
        return emailOtp;
    }

    /**
     * Sets the email OTP.
     *
     * @param emailOtp The email OTP to set.
     * @return This instance for chaining.
     */
    public SupabaseLink setEmailOtp(String emailOtp) {
        this.emailOtp = emailOtp;
        return this;
    }

    /**
     * Returns the hashed token.
     *
     * @return The hashed token.
     */
    public String getHashedToken() {
        return hashedToken;
    }

    /**
     * Sets the hashed token.
     *
     * @param hashedToken The hashed token to set.
     * @return This instance for chaining.
     */
    public SupabaseLink setHashedToken(String hashedToken) {
        this.hashedToken = hashedToken;
        return this;
    }

    /**
     * Returns the verification type.
     *
     * @return The verification type.
     */
    public String getVerificationType() {
        return verificationType;
    }

    /**
     * Sets the verification type.
     *
     * @param verificationType The verification type to set.
     * @return This instance for chaining.
     */
    public SupabaseLink setVerificationType(String verificationType) {
        this.verificationType = verificationType;
        return this;
    }

    /**
     * Returns the redirect URL.
     *
     * @return The redirect URL.
     */
    public String getRedirectTo() {
        return redirectTo;
    }

    /**
     * Sets the redirect URL.
     *
     * @param redirectTo The redirect URL to set.
     * @return This instance for chaining.
     */
    public SupabaseLink setRedirectTo(String redirectTo) {
        this.redirectTo = redirectTo;
        return this;
    }

    @Override
    public String toString() {
        return "SupabaseLink{" +
                "actionLink='" + actionLink + '\'' +
                ", emailOtp='" + emailOtp + '\'' +
                ", hashedToken='" + hashedToken + '\'' +
                ", verificationType='" + verificationType + '\'' +
                ", redirectTo='" + redirectTo + '\'' +
                '}';
    }
}
