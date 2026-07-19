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
import io.github.maskmasteruk.supabase.postgrest.Query.DeleteQueryBuilder;
import io.github.maskmasteruk.supabase.postgrest.Query.PostgrestConfig;

/**
 * Service class for handling row deletion (DELETE) operations in PostgREST.
 *
 * <p>This class provides methods to delete rows from a table based on filters.
 * It handles URL construction and asynchronous request execution.</p>
 */
class DeleteService {
    private static volatile DeleteService instance;
    private final Helper helper;

    private DeleteService() {
        helper = Helper.getInstance();
    }

    /**
     * Gets the singleton instance of {@link DeleteService}.
     *
     * @return The {@link DeleteService} instance.
     */
    public static DeleteService getInstance() {
        if (instance == null) {
            synchronized (DeleteService.class) {
                if (instance == null) {
                    instance = new DeleteService();
                }
            }
        }
        return instance;
    }

    /**
     * Deletes records from the specified table matching the given filters.
     *
     * @param tableName Name of the table.
     * @param deleteQueryBuilder Builder containing filters for the deletion.
     * @param postgrestConfig Optional configuration for the request.
     * @param onPostgrestCallback Callback to handle success or failure.
     */
    public void delete(String tableName, DeleteQueryBuilder deleteQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        HashMap<String, String> queryParams = deleteQueryBuilder != null ? deleteQueryBuilder.buildQueryParams() : new HashMap<>();
        HashMap<String, String> headers = deleteQueryBuilder != null ? deleteQueryBuilder.buildHeaders() : new HashMap<>();
        if (postgrestConfig != null) {
            headers.putAll(postgrestConfig.buildHeaders(headers.getOrDefault("Accept", null)));
        }
        headers.putAll(RequestHandler.baseHeaders);

        Runnables.getExecutorService().execute(() -> {
            UrlBuilder urlBuilder = helper.getBaseRestUrlBuilder();
            urlBuilder.appendPath(tableName);
            queryParams.forEach(urlBuilder::appendQueryParam);


            Response response = new RequestHandler().delete(urlBuilder.build(), null, headers);
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                onPostgrestCallback.onSuccess(new PostgrestResult(tableName, deleteQueryBuilder, postgrestConfig, response));
            } else {
                helper.generateError(response, onPostgrestCallback);
            }
        });
    }
}
