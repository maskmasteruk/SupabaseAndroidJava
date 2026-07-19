package io.github.maskmasteruk.supabase.postgrest;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Runnables;
import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;
import io.github.maskmasteruk.supabase.postgrest.Callback.OnPostgrestCallback;
import io.github.maskmasteruk.supabase.postgrest.Object.PostgrestResult;
import io.github.maskmasteruk.supabase.postgrest.Query.PostgrestConfig;
import io.github.maskmasteruk.supabase.postgrest.Query.UpdateQueryBuilder;
import io.github.maskmasteruk.supabase.postgrest.Query.UpsertQueryBuilder;

/**
 * Service class for handling row update (UPDATE, UPSERT) operations in PostgREST.
 *
 * <p>This class provides methods to update existing rows or upsert rows in a table.
 * It handles URL construction, JSON body building, and asynchronous request execution.</p>
 */
class UpdateService {
    private static volatile UpdateService instance;
    private final Helper helper;

    private UpdateService() {
        helper = Helper.getInstance();
    }

    /**
     * Gets the singleton instance of {@link UpdateService}.
     *
     * @return The {@link UpdateService} instance.
     */
    public static UpdateService getInstance() {
        if (instance == null) {
            synchronized (UpdateService.class) {
                if (instance == null) {
                    instance = new UpdateService();
                }
            }
        }
        return instance;
    }

    /**
     * Updates rows in the specified table.
     *
     * @param tableName Name of the table.
     * @param values Map of column names and new values.
     * @param updateQueryBuilder Builder containing filters for the update.
     * @param postgrestConfig Optional configuration for the request.
     * @param onPostgrestCallback Callback to handle success or failure.
     */
    public void update(String tableName, HashMap<String, Object> values, UpdateQueryBuilder updateQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        HashMap<String, String> queryParams = updateQueryBuilder != null ? updateQueryBuilder.buildQueryParams() : new HashMap<>();
        HashMap<String, String> headers = updateQueryBuilder != null ? updateQueryBuilder.buildHeaders() : new HashMap<>();
        if (postgrestConfig != null) {
            headers.putAll(postgrestConfig.buildHeaders(headers.getOrDefault("Accept", null)));
        }
        headers.putAll(RequestHandler.baseHeaders);

        Runnables.getExecutorService().execute(() -> {
            UrlBuilder urlBuilder = helper.getBaseRestUrlBuilder();
            urlBuilder.appendPath(tableName);
            queryParams.forEach(urlBuilder::appendQueryParam);

            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            values.forEach(jsonObjectStringBuilder::append);

            Response response = new RequestHandler().patch(urlBuilder.build(), jsonObjectStringBuilder.build(), headers);
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                onPostgrestCallback.onSuccess(new PostgrestResult(tableName, updateQueryBuilder, postgrestConfig, response));
            } else {
                helper.generateError(response, onPostgrestCallback);
            }
        });
    }

    /**
     * Updates multiple rows in the specified table.
     *
     * @param tableName Name of the table.
     * @param values List of maps, each containing column names and new values.
     * @param updateQueryBuilder Builder containing filters.
     * @param postgrestConfig Optional configuration.
     * @param onPostgrestCallback Callback for results.
     */
    public void update(String tableName, ArrayList<HashMap<String, Object>> values, UpdateQueryBuilder updateQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        HashMap<String, String> queryParams = updateQueryBuilder != null ? updateQueryBuilder.buildQueryParams() : new HashMap<>();
        HashMap<String, String> headers = updateQueryBuilder != null ? updateQueryBuilder.buildHeaders() : new HashMap<>();
        if (postgrestConfig != null) {
            headers.putAll(postgrestConfig.buildHeaders(headers.getOrDefault("Accept", null)));
        }
        headers.putAll(RequestHandler.baseHeaders);

        Runnables.getExecutorService().execute(() -> {
            UrlBuilder urlBuilder = helper.getBaseRestUrlBuilder();
            urlBuilder.appendPath(tableName);
            queryParams.forEach(urlBuilder::appendQueryParam);

            JsonUtils.JsonArrayStringBuilder jsonArrayStringBuilder = new JsonUtils.JsonArrayStringBuilder();
            values.forEach(value -> {
                JsonUtils.JsonObjectBuilder jsonObjectBuilder = new JsonUtils.JsonObjectBuilder();
                value.forEach(jsonObjectBuilder::append);
                jsonArrayStringBuilder.append(jsonObjectBuilder.build());
            });

            Response response = new RequestHandler().patch(urlBuilder.build(), jsonArrayStringBuilder.build(), headers);
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                onPostgrestCallback.onSuccess(new PostgrestResult(tableName, updateQueryBuilder, postgrestConfig, response));
            } else {
                helper.generateError(response, onPostgrestCallback);
            }
        });
    }

    /**
     * Performs an UPSERT operation (typically replaces the entire row).
     *
     * @param tableName Name of the table.
     * @param values Map of values for the row.
     * @param upsertQueryBuilder Builder for configuration.
     * @param postgrestConfig Optional configuration.
     * @param onPostgrestCallback Callback for results.
     */
    public void upsert(String tableName, HashMap<String, Object> values, UpsertQueryBuilder upsertQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        HashMap<String, String> queryParams = upsertQueryBuilder != null ? upsertQueryBuilder.buildQueryParams() : new HashMap<>();
        HashMap<String, String> headers = upsertQueryBuilder != null ? upsertQueryBuilder.buildHeaders() : new HashMap<>();
        if (postgrestConfig != null) {
            headers.putAll(postgrestConfig.buildHeaders(headers.getOrDefault("Accept", null)));
        }
        headers.putAll(RequestHandler.baseHeaders);

        Runnables.getExecutorService().execute(() -> {
            UrlBuilder urlBuilder = helper.getBaseRestUrlBuilder();
            urlBuilder.appendPath(tableName);
            queryParams.forEach(urlBuilder::appendQueryParam);

            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            values.forEach(jsonObjectStringBuilder::append);

            Response response = new RequestHandler().put(urlBuilder.build(), jsonObjectStringBuilder.build(), headers);
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                onPostgrestCallback.onSuccess(new PostgrestResult(tableName, upsertQueryBuilder, postgrestConfig, response));
            } else {
                helper.generateError(response, onPostgrestCallback);
            }
        });
    }

    /**
     * Replaces an entire row (synonym for upsert).
     *
     * @param tableName Name of the table.
     * @param values Map of values.
     * @param upsertQueryBuilder Builder for configuration.
     * @param postgrestConfig Optional configuration.
     * @param onPostgrestCallback Callback for results.
     */
    public void replace(String tableName, HashMap<String, Object> values, UpsertQueryBuilder upsertQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        upsert(tableName, values, upsertQueryBuilder, postgrestConfig, onPostgrestCallback);
    }
}
