package io.github.maskmasteruk.supabase.postgrest.Query;

import java.util.HashMap;

/**
 * Configuration for PostgREST requests.
 *
 * <p>This class allows configuring additional options for a PostgREST query, such as
 * count preferences (exact, estimated, planned) and explain data for query analysis.</p>
 */
public class PostgrestConfig {
    /** Data for the 'Explain' header to analyze query performance. */
    ExplainData explainData;
    /** Whether to request an exact count of rows. */
    private boolean getExactCount = false;
    /** Whether to request an estimated count of rows. */
    private boolean getEstimatedCount = false;
    /** Whether to request a planned count of rows. */
    private boolean getPlannedCount = false;

    /**
     * Sets the preference to get an exact count of rows matching the query.
     *
     * <p>Adds 'Prefer: count=exact' to the request headers.</p>
     *
     * @return This {@link PostgrestConfig} instance for chaining.
     */
    public PostgrestConfig getExactCount() {
        getExactCount = true;
        getEstimatedCount = false;
        getPlannedCount = false;
        return this;
    }

    /**
     * Sets the preference to get an estimated count of rows matching the query.
     *
     * <p>Adds 'Prefer: count=estimated' to the request headers.</p>
     *
     * @return This {@link PostgrestConfig} instance for chaining.
     */
    public PostgrestConfig getEstimatedCount() {
        getExactCount = false;
        getEstimatedCount = true;
        getPlannedCount = false;
        return this;
    }

    /**
     * Sets the preference to get a planned count of rows matching the query.
     *
     * <p>Adds 'Prefer: count=planned' to the request headers.</p>
     *
     * @return This {@link PostgrestConfig} instance for chaining.
     */
    public PostgrestConfig getPlannedCount() {
        getExactCount = false;
        getEstimatedCount = false;
        getPlannedCount = true;
        return this;
    }

    /**
     * Sets the explain data to analyze the query.
     *
     * @param explainData The explain configuration.
     * @return This {@link PostgrestConfig} instance for chaining.
     */
    public PostgrestConfig setExplainData(ExplainData explainData) {
        this.explainData = explainData;
        return this;
    }

    /**
     * Checks if any type of count has been requested.
     *
     * @return {@code true} if exact, estimated, or planned count is requested, {@code false} otherwise.
     */
    public boolean hasRequestedAnyCount() {
        return getExactCount || getPlannedCount || getEstimatedCount;
    }

    /**
     * Builds the headers based on the current configuration.
     *
     * @param mediaType The media type to use for the 'Accept' header if explain data is present.
     * @return A {@link HashMap} containing the constructed headers.
     */
    public HashMap<String, String> buildHeaders(String mediaType) {
        HashMap<String, String> headers = new HashMap<>();
        if (getExactCount) {
            headers.put("Prefer", "count=exact");
        }
        if (getEstimatedCount) {
            headers.put("Prefer", "count=estimated");
        }
        if (getPlannedCount) {
            headers.put("Prefer", "count=planned");
        }

        if (explainData != null) {
            headers.put("Accept", explainData.get(mediaType == null ? new AcceptHeader().Json(false).get() : mediaType));
        }
        return headers;
    }
}
