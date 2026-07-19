package io.github.maskmasteruk.supabase.storage.Enum;

/**
 * Columns by which Supabase objects can be sorted when listing.
 */
public enum ObjectSortBy {
    /** Sort by object name. */
    NAME("name"),
    /** Sort by creation timestamp. */
    CREATED_AT("created_at"),
    /** Sort by last update timestamp. */
    UPDATED_AT("updated_at");

    private final String value;

    /**
     * Constructor for ObjectSortBy enum.
     *
     * @param value The column name used by the API.
     */
    ObjectSortBy(String value) {
        this.value = value;
    }

    /** @return The API-compatible column name. */
    public String getValue() {
        return value;
    }
}
