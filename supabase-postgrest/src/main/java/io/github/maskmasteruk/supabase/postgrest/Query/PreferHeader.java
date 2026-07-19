package io.github.maskmasteruk.supabase.postgrest.Query;

/**
 * Enum representing 'Prefer' header options for PostgREST requests.
 *
 * <p>Control various aspects of the request execution and response,
 * such as whether to return the affected records, how to handle conflicts,
 * and how to treat missing fields.</p>
 */
public enum PreferHeader {

    // Return behavior
    /** Preference to return the representation of the resource in the response body. */
    RETURN_REPRESENTATION("return=representation"),
    /** Preference to return minimal information (no response body). */
    RETURN_MINIMAL("return=minimal"),
    /** Preference to return only headers (like Location). */
    RETURN_HEADERS_ONLY("return=headers-only"),

    // Upsert resolution
    /** Preference to merge duplicates during an upsert operation. */
    RESOLUTION_MERGE_DUPLICATES("resolution=merge-duplicates"),
    /** Preference to ignore duplicates during an upsert operation. */
    RESOLUTION_IGNORE_DUPLICATES("resolution=ignore-duplicates"),

    // Missing fields
    /** Preference to use default values for missing fields in an insert/update. */
    MISSING_DEFAULT("missing=default"),

    // Handling
    /** Preference to enforce strict handling (e.g., error if more rows affected than max-affected). */
    HANDLING_STRICT("handling=strict"),

    // Timezone
    /** Prefix for specifying the time zone. */
    TIMEZONE("timezone");

    private final String value;

    /**
     * Constructs a PreferHeader.
     * @param value The header value.
     */
    PreferHeader(String value) {
        this.value = value;
    }

    /**
     * Returns the header value string (e.g., "return=representation").
     * @return The header value.
     */
    public String getValue() {
        return value;
    }
}
