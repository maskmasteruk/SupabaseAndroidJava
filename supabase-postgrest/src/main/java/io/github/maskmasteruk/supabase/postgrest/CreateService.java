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
import io.github.maskmasteruk.supabase.postgrest.Query.InsertQueryBuilder;
import io.github.maskmasteruk.supabase.postgrest.Query.PostgrestConfig;

/**
 * Service class for handling row creation (INSERT) operations in PostgREST.
 *
 * <p>This class provides methods to insert single or multiple rows into a table.
 * It handles URL construction, JSON body building, and asynchronous request execution.</p>
 */
class CreateService {
    private static volatile CreateService instance;
    private final Helper helper;

    private CreateService() {
        helper = Helper.getInstance();
    }

    /**
     * Gets the singleton instance of {@link CreateService}.
     *
     * @return The {@link CreateService} instance.
     */
    public static CreateService getInstance() {
        if (instance == null) {
            synchronized (CreateService.class) {
                if (instance == null) {
                    instance = new CreateService();
                }
            }
        }
        return instance;
    }

    /**
     * Inserts a single row into the specified table.
     *
     * @param tableName Name of the table.
     * @param values Map of column names to values for the new row.
     * @param insertQueryBuilder Optional builder for configuring the insert (e.g., returning data).
     * @param postgrestConfig Optional configuration for the request (e.g., count).
     * @param onPostgrestCallback Callback to handle success or failure.
     */
    public void insert(String tableName, HashMap<String, Object> values, InsertQueryBuilder insertQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        HashMap<String, String> queryParams = insertQueryBuilder != null ? insertQueryBuilder.buildQueryParams() : new HashMap<>();
        HashMap<String, String> headers = insertQueryBuilder != null ? insertQueryBuilder.buildHeaders() : new HashMap<>();
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

            Response response = new RequestHandler().post(urlBuilder.build(), jsonObjectStringBuilder.build(), headers);
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                onPostgrestCallback.onSuccess(new PostgrestResult(tableName, insertQueryBuilder, postgrestConfig, response));
            } else {
                helper.generateError(response, onPostgrestCallback);
            }
        });
    }

    /**
     * Inserts multiple rows into the specified table.
     *
     * @param tableName Name of the table.
     * @param values List of maps, each representing a row to be inserted.
     * @param insertQueryBuilder Optional builder for configuring the insert.
     * @param postgrestConfig Optional configuration for the request.
     * @param onPostgrestCallback Callback to handle success or failure.
     */
    public void insert(String tableName, ArrayList<HashMap<String, Object>> values, InsertQueryBuilder insertQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        HashMap<String, String> queryParams = insertQueryBuilder != null ? insertQueryBuilder.buildQueryParams() : new HashMap<>();
        HashMap<String, String> headers = insertQueryBuilder != null ? insertQueryBuilder.buildHeaders() : new HashMap<>();
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

            Response response = new RequestHandler().post(urlBuilder.build(), jsonArrayStringBuilder.build(), headers);
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                onPostgrestCallback.onSuccess(new PostgrestResult(tableName, insertQueryBuilder, postgrestConfig, response));
            } else {
                helper.generateError(response, onPostgrestCallback);
            }
        });
    }
}
