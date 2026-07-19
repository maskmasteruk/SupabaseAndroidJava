package io.github.maskmasteruk.supabase.postgrest.Query;

/**
 * Enum representing join types for PostgREST embedded resource queries.
 */
public enum Join {
    /** Default join (typically Left Join in PostgREST). */
    DEFAULT(null),
    /** Inner join. */
    INNER("inner");

    private final String value;

    /**
     * Constructs a Join type.
     * @param value The PostgREST string representation of the join.
     */
    Join(String value) {
        this.value = value;
    }

    /**
     * Gets the string value of the join type.
     * @return The join value.
     */
    public String getValue() {
        return value;
    }
}
