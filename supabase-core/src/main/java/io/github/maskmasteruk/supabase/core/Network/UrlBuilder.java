package io.github.maskmasteruk.supabase.core.Network;

import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.maskmasteruk.supabase.core.Supabase;

/**
 * Helper class for constructing Supabase API URLs.
 *
 * Responsibilities:
 * - Building URLs based on the base project URL.
 * - Managing path segments and query parameters.
 * - Ensuring proper URL encoding via Android's Uri.Builder.
 *
 * Usage:
 * String url = new UrlBuilder()
 *     .appendPath("rest")
 *     .appendPath("v1")
 *     .appendQueryParam("select", "*")
 *     .build();
 *
 * Thread Safety:
 * Not thread-safe. Use a new instance for each URL construction.
 */
public class UrlBuilder {
    /**
     * Map of query parameters to be appended to the URL.
     */
    private Map<String, String> queryParams = new HashMap<>();

    /**
     * List of path segments to be appended to the URL.
     */
    private List<String> path = new ArrayList<>();

    /**
     * Reference to the Supabase instance to retrieve the base URL.
     */
    private Supabase supabase;

    /**
     * Creates a new UrlBuilder.
     * Initializes the Supabase reference.
     */
    public UrlBuilder() {
        supabase = Supabase.getInstance();
    }

    /**
     * Appends a query parameter to the URL.
     *
     * @param key   The parameter name.
     * @param value The parameter value.
     * @return This UrlBuilder instance for chaining.
     */
    public UrlBuilder appendQueryParam(String key, String value) {
        queryParams.put(key, value);
        return this;
    }

    /**
     * Appends a path segment to the URL.
     *
     * @param value The path segment.
     * @return This UrlBuilder instance for chaining.
     */
    public UrlBuilder appendPath(String value) {
        path.add(value);
        return this;
    }

    /**
     * Builds the final URL string.
     * Starts with the project URL from SupabaseConfig and appends paths and query params.
     *
     * @return The complete URL string.
     */
    public String build() {
        Uri.Builder uri = Uri.parse(supabase.getSupabaseConfig().getProjectUrl()).buildUpon();
        for (String s : path) {
            uri.appendPath(s);
        }
        for (Map.Entry<String, String> queryParam : queryParams.entrySet()) {
            uri.appendQueryParameter(queryParam.getKey(), queryParam.getValue());
        }

        return uri.build().toString();
    }
}
