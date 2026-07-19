package io.github.maskmasteruk.supabase.auth.Enums;

/**
 * Defines the types of verification being performed.
 * <p>
 * This enum is used to specify the context of a verification request, such as
 * email verification, SMS verification, or password recovery.
 */
public enum VerifyType {
    /**
     * Verification via email.
     */
    EMAIL("email"),

    /**
     * Verification via SMS.
     */
    SMS("sms"),

    /**
     * Verification for password recovery.
     */
    RECOVERY("recovery"),

    /**
     * Verification via an invite link.
     */
    INVITE("invite"),

    /**
     * Verification for an email address change.
     */
    EMAIL_CHANGE("email_change"),

    /**
     * Verification for reauthentication.
     */
    REAUTHENTICATION("reauthentication"),

    /**
     * Verification via a Magic Link.
     */
    MAGICLINK("magiclink");

    private final String value;

    VerifyType(String value) {
        this.value = value;
    }

    /**
     * Returns the string value of the verification type expected by the Supabase API.
     *
     * @return The verification type string.
     */
    public String getValue() {
        return value;
    }
}

