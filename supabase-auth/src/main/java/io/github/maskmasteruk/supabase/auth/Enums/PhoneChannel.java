package io.github.maskmasteruk.supabase.auth.Enums;

/**
 * Defines the delivery channel for phone-based one-time passwords (OTP).
 */
public enum PhoneChannel {
    /**
     * Delivery via standard SMS.
     */
    SMS("sms"),

    /**
     * Delivery via the WhatsApp messaging platform.
     */
    WHATSAPP("whatsapp");
    private final String value;

    PhoneChannel(String value) {
        this.value = value;
    }

    /**
     * Returns the string value of the channel expected by the Supabase API.
     *
     * @return The channel string.
     */
    public String getValue() {
        return value;
    }
}


