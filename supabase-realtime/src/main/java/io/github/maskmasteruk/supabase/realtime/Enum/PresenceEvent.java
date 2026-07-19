package io.github.maskmasteruk.supabase.realtime.Enum;

/**
 * Represents the types of presence events that can occur in a channel.
 *
 * <p>Presence allows you to track users or devices that are currently "present" in a channel.
 * These events notify you when the state of presence changes.</p>
 *
 * <pre>{@code
 * presence.on(PresenceEvent.JOIN, (payload) -> {
 *     System.out.println("User joined!");
 * });
 * }</pre>
 */
public enum PresenceEvent {
    /** Indicates the initial state or a full refresh of the presence list. */
    SYNC("sync"),

    /** Indicates a new user or device has joined the channel. */
    JOIN("join"),

    /** Indicates a user or device has left the channel. */
    LEAVE("leave");

    private final String eventValue;

    /**
     * Internal constructor for PresenceEvent.
     *
     * @param eventValue The string representation used in the protocol.
     */
    PresenceEvent(String eventValue) {
        this.eventValue = eventValue;
    }

    /**
     * Gets the string value of the event.
     *
     * @return The protocol string value.
     */
    public String getEventValue() {
        return eventValue;
    }

    /**
     * Converts a string value into its corresponding {@link PresenceEvent} enum.
     *
     * @param value The string value to convert.
     * @return The matching {@link PresenceEvent}.
     * @throws IllegalArgumentException if the value does not match any known event.
     *
     * <pre>{@code
     * PresenceEvent event = PresenceEvent.fromString("join");
     * }</pre>
     */
    public static PresenceEvent fromString(String value) {
        for (PresenceEvent event : PresenceEvent.values()) {
            if (event.eventValue.equalsIgnoreCase(value)) {
                return event;
            }
        }
        throw new IllegalArgumentException("Unknown presence event type: " + value);
    }
}
