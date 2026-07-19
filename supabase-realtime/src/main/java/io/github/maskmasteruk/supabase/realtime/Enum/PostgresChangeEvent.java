package io.github.maskmasteruk.supabase.realtime.Enum;

/**
 * Represents the types of database changes that can be subscribed to via Postgres CDC.
 *
 * <p>When subscribing to database changes, you can filter by specific event types
 * like INSERT, UPDATE, or DELETE, or listen to ALL events.</p>
 *
 * <pre>{@code
 * channel.on(PostgresChangeEvent.INSERT, (payload) -> {
 *     System.out.println("New row inserted: " + payload);
 * });
 * }</pre>
 */
public enum PostgresChangeEvent {
    /** Listen to all database events (INSERT, UPDATE, and DELETE). */
    ALL("*"),

    /** Listen specifically to INSERT events. */
    INSERT("INSERT"),

    /** Listen specifically to UPDATE events. */
    UPDATE("UPDATE"),

    /** Listen specifically to DELETE events. */
    DELETE("DELETE");

    private final String eventValue;

    /**
     * Internal constructor for PostgresChangeEvent.
     *
     * @param eventValue The database action string.
     */
    PostgresChangeEvent(String eventValue) {
        this.eventValue = eventValue;
    }

    /**
     * Gets the string value of the database event.
     *
     * @return The database action string.
     */
    public String getEventValue() {
        return eventValue;
    }

    /**
     * Converts a raw string value from the server into its corresponding {@link PostgresChangeEvent} enum.
     *
     * @param value The raw event string.
     * @return The matching {@link PostgresChangeEvent}.
     * @throws IllegalArgumentException if the value does not match any known event.
     *
     * <pre>{@code
     * PostgresChangeEvent event = PostgresChangeEvent.fromString("UPDATE");
     * }</pre>
     */
    public static PostgresChangeEvent fromString(String value) {
        for (PostgresChangeEvent event : PostgresChangeEvent.values()) {
            if (event.eventValue.equalsIgnoreCase(value)) {
                return event;
            }
        }
        throw new IllegalArgumentException("Unknown postgres change event type: " + value);
    }
}