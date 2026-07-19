package io.github.maskmasteruk.supabase.auth.admin.Enums;

/**
 * Defines the types of authentication links that can be generated administratively.
 */
public enum LinkType {
    /**
     * Link for magic link authentication.
     */
    MAGICLINK("magiclink"),
    /**
     * Link for inviting a new user.
     */
    INVITE("invite"),
    /**
     * Link for password recovery.
     */
    RECOVERY("recovery"),
    /**
     * Link sent to the current email address for an email change.
     */
    EMAIL_CHANGE_CURRENT("email_change_current"),
    /**
     * Link sent to the new email address for an email change.
     */
    EMAIL_CHANGE_NEW("email_change_new"),
    /**
     * Link for user signup confirmation.
     */
    SIGNUP("signup");

    private final String value;

    LinkType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
