package io.github.maskmasteruk.supabase.postgrest.Query;

/**
 * Helper class for constructing the 'Accept' header for PostgREST requests.
 *
 * <p>This class allows specifying the desired response format, such as JSON,
 * CSV, GeoJSON, or a single object.</p>
 */
public class AcceptHeader {
    /** The constructed Accept header value. */
    private String accept;

    /**
     * Default constructor for AcceptHeader.
     */
    public AcceptHeader() {
    }

    /**
     * Configures the header to request a single object instead of an array.
     *
     * @param stripNulls Whether to strip null fields from the JSON response.
     * @return This {@link AcceptHeader} instance for chaining.
     */
    public AcceptHeader Single(boolean stripNulls) {
        accept = "application/vnd.pgrst.object+json" + (stripNulls ? ";nulls=stripped" : "");
        return this;
    }

    /**
     * Configures the header to request a JSON array.
     *
     * @param stripNulls Whether to strip null fields from the JSON response.
     * @return This {@link AcceptHeader} instance for chaining.
     */
    public AcceptHeader Json(boolean stripNulls) {
        accept = stripNulls ? "application/vnd.pgrst.array+json;nulls=stripped" : "application/json";
        return this;
    }

    /**
     * Configures the header to request CSV format.
     *
     * @return This {@link AcceptHeader} instance for chaining.
     */
    public AcceptHeader CSV() {
        accept = "text/csv";
        return this;
    }

    /**
     * Configures the header to request GeoJSON format.
     *
     * @return This {@link AcceptHeader} instance for chaining.
     */
    public AcceptHeader GeoJson() {
        accept = "application/geo+json";
        return this;
    }

    /**
     * Gets the constructed Accept header value.
     * @return The header value string.
     */
    public String get() {
        return accept;
    }
}
