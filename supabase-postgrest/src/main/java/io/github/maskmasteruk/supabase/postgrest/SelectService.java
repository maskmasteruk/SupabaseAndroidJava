package io.github.maskmasteruk.supabase.postgrest;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;

import java.util.HashMap;

import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Runnables;
import io.github.maskmasteruk.supabase.postgrest.Callback.OnPostgrestCallback;
import io.github.maskmasteruk.supabase.postgrest.Object.PostgrestResult;
import io.github.maskmasteruk.supabase.postgrest.Query.PostgrestConfig;
import io.github.maskmasteruk.supabase.postgrest.Query.SelectQueryBuilder;

/**
 * Service class for handling row retrieval (SELECT) operations in PostgREST.
 *
 * <p>This class provides methods to query data from tables using filters and configuration options.</p>
 */
class SelectService {
    private static volatile SelectService instance;
    private final Helper helper;

    private SelectService() {
        helper = Helper.getInstance();
    }

    /**
     * Gets the singleton instance of {@link SelectService}.
     *
     * @return The {@link SelectService} instance.
     */
    public static SelectService getInstance() {
        if (instance == null) {
            synchronized (SelectService.class) {
                if (instance == null) {
                    instance = new SelectService();
                }
            }
        }
        return instance;
    }

    /**
     * Executes a SELECT query against the specified table.
     *
     * @param tableName Name of the table.
     * @param selectQueryBuilder Builder containing column selection, filters, ordering, and pagination.
     * @param postgrestConfig Optional configuration for the request (e.g., count).
     * @param onPostgrestCallback Callback to handle success or failure.
     */
    public void select(String tableName, SelectQueryBuilder selectQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        HashMap<String, String> queryParams = selectQueryBuilder != null ? selectQueryBuilder.buildQueryParams() : new HashMap<>();
        HashMap<String, String> headers = selectQueryBuilder != null ? selectQueryBuilder.buildHeaders() : new HashMap<>();
        if (postgrestConfig != null) {
            headers.putAll(postgrestConfig.buildHeaders(headers.getOrDefault("Accept", null)));
        }
        headers.putAll(RequestHandler.baseHeaders);

        Runnables.getExecutorService().execute(() -> {
            UrlBuilder urlBuilder = helper.getBaseRestUrlBuilder();
            urlBuilder.appendPath(tableName);
            queryParams.forEach(urlBuilder::appendQueryParam);

            Response response = new RequestHandler().get(urlBuilder.build(), headers);
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                onPostgrestCallback.onSuccess(new PostgrestResult(tableName, selectQueryBuilder, postgrestConfig, response));
            } else {
                helper.generateError(response, onPostgrestCallback);
            }
        });
    }
}
