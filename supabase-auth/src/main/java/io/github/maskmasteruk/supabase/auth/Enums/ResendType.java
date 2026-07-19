package io.github.maskmasteruk.supabase.auth.Enums;

/**
 * Defines the types of verification triggers that can be resent.
 */
public enum ResendType {
    /**
     * Resend the email change confirmation.
     */
    EMAIL_CHANGE("email_change"),

    /**
     * Resend the SMS OTP.
     */
    SMS("sms"),

    /**
     * Resend the initial signup verification email.
     */
    SIGNUP("signup"),

    /**
     * Resend the phone change confirmation.
     */
    PHONE_CHANGE("phone_change");

    private final String value;

    ResendType(String value) {
        this.value = value;
    }

    /**
     * Returns the string value expected by the Supabase API.
     *
     * @return The resend type string.
     */
    public String getValue() {
        return value;
    }
}

