package io.github.maskmasteruk.supabase.realtime.Enum;

/**
 * Defines the types of presence actions a client can perform.
 *
 * <p>Used when interacting with the Presence feature to either start or stop
 * tracking the current client's state in a channel.</p>
 *
 * <pre>{@code
 * // Used internally by the Presence implementation
 * }</pre>
 */
public enum PresenceTypes {
    /** Action to start tracking the client's presence. */
    TRACK("track"),

    /** Action to stop tracking the client's presence. */
    UNTRACK("untrack");

    private final String value;

    /**
     * Internal constructor for PresenceTypes.
     *
     * @param value The action string value.
     */
    PresenceTypes(String value) {
        this.value = value;
    }

    /**
     * Gets the string value of the presence action.
     *
     * @return The action string value.
     */
    public String getValue() {
        return value;
    }
}
