package io.github.maskmasteruk.supabase.realtime.Enum;

/**
 * Represents the standard events used in the Phoenix protocol for realtime communication.
 *
 * <p>Phoenix protocol events are internal events used to manage the websocket connection,
 * channel lifecycles, and core realtime features like broadcast and presence.</p>
 *
 * <p>Typical usage involves checking the event type of an incoming message to determine
 * how to process the payload.</p>
 *
 * <pre>{@code
 * PhoenixEvent event = PhoenixEvent.fromString("phx_join");
 * if (event == PhoenixEvent.JOIN) {
 *     // Handle join success
 * }
 * }</pre>
 */
public enum PhoenixEvent {
    /** Sent by the client to join a channel. */
    JOIN("phx_join"),

    /** Sent by the client to leave a channel. */
    LEAVE("phx_leave"),

    /** Indicates an error occurred within the Phoenix protocol. */
    ERROR("phx_error"),

    /** Indicates the channel or connection was closed. */
    CLOSE("phx_close"),

    /** A reply from the server to a client-initiated message. */
    REPLY("phx_reply"),

    /** Internal heartbeat event to keep the connection alive. */
    HEARTBEAT("heartbeat"),

    /** Event for sending or refreshing access tokens. */
    ACCESS_TOKEN("access_token"),

    /** Represents a broadcast event from other clients. */
    BROADCAST("broadcast"),

    /** Internal system-level events. */
    SYSTEM("system"),

    /** Events related to database changes via Postgres. */
    POSTGRES_CHANGES("postgres_changes"),

    /** General presence events. */
    PRESENCE("presence"),

    /** Specific event indicating a change in presence state (joins/leaves). */
    PRESENCE_DIFF("presence_diff"),

    /** Event providing the full current state of presence in a channel. */
    PRESENCE_STATE("presence_state");

    private final String eventValue;

    /**
     * Internal constructor for PhoenixEvent.
     *
     * @param eventValue The string representation used in the protocol.
     */
    PhoenixEvent(String eventValue) {
        this.eventValue = eventValue;
    }

    /**
     * Gets the string value of the event as used in the Phoenix protocol.
     *
     * @return The protocol string value.
     */
    public String getEventValue() {
        return eventValue;
    }

    /**
     * Converts a protocol string value into its corresponding {@link PhoenixEvent} enum.
     *
     * @param value The string value to convert.
     * @return The matching {@link PhoenixEvent}.
     * @throws IllegalArgumentException if the value does not match any known event.
     *
     * <pre>{@code
     * PhoenixEvent event = PhoenixEvent.fromString("phx_reply");
     * }</pre>
     */
    public static PhoenixEvent fromString(String value) {
        for (PhoenixEvent event : PhoenixEvent.values()) {
            if (event.eventValue.equalsIgnoreCase(value)) {
                return event;
            }
        }
        throw new IllegalArgumentException("Unknown phoenix protocol event: " + value);
    }
}
