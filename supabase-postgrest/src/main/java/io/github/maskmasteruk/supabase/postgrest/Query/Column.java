package io.github.maskmasteruk.supabase.postgrest.Query;

/**
 * Enum representing special column identifiers in PostgREST queries.
 */
public enum Column {
    /** Represents all columns in a table. */
    ALL("*");

    private final String value;

    /**
     * Constructs a Column with the specified string value.
     * @param value The PostgREST string representation of the column identifier.
     */
    Column(String value) {
        this.value = value;
    }

    /**
     * Gets the string value of the column identifier.
     * @return The string value.
     */
    public String getValue() {
        return value;
    }
}

