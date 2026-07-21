package io.github.maskmasteruk.supabase.postgrest;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;

import java.util.HashMap;

import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Runnables;
import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;
import io.github.maskmasteruk.supabase.postgrest.Callback.OnPostgrestCallback;
import io.github.maskmasteruk.supabase.postgrest.Object.PostgrestResult;
import io.github.maskmasteruk.supabase.postgrest.Query.PostgrestConfig;
import io.github.maskmasteruk.supabase.postgrest.Query.SelectQueryBuilder;

/**
 * Service class for executing Remote Procedure Calls (RPC) in PostgREST.
 *
 * <p>This class provides methods to call database functions via the PostgREST /rpc endpoint.</p>
 */
class RPCService {
    private static volatile RPCService instance;
    private final Helper helper;

    private RPCService() {
        helper = Helper.getInstance();
    }

    /**
     * Gets the singleton instance of {@link RPCService}.
     *
     * @return The {@link RPCService} instance.
     */
    public static RPCService getInstance() {
        if (instance == null) {
            synchronized (RPCService.class) {
                if (instance == null) {
                    instance = new RPCService();
                }
            }
        }
        return instance;
    }

    /**
     * Executes a database function (RPC).
     *
     * @param rpcName Name of the database function.
     * @param values Arguments for the function.
     * @param selectQueryBuilder Optional builder for selecting columns from the function's return value.
     * @param postgrestConfig Optional configuration for the request.
     * @param onPostgrestCallback Callback to handle success or failure.
     */
    public void rpc(String rpcName, HashMap<String, Object> values, SelectQueryBuilder selectQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        HashMap<String, String> queryParams = selectQueryBuilder != null ? selectQueryBuilder.buildQueryParams() : new HashMap<>();
        HashMap<String, String> headers = selectQueryBuilder != null ? selectQueryBuilder.buildHeaders() : new HashMap<>();
        if (postgrestConfig != null) {
            headers.putAll(postgrestConfig.buildHeaders(headers.getOrDefault("Accept", null)));
        }
        headers.putAll(RequestHandler.baseHeaders);

        Runnables.getExecutorService().execute(() -> {
            UrlBuilder urlBuilder = helper.getBaseRestUrlBuilder();
            urlBuilder.appendPath(POSTGREST_END_POINTS.RPC);
            urlBuilder.appendPath(rpcName);
            queryParams.forEach(urlBuilder::appendQueryParam);

            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            if (values != null) {
                values.forEach(jsonObjectStringBuilder::append);
            }

            Response response = new RequestHandler().post(urlBuilder.build(), jsonObjectStringBuilder.build(), headers);
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                onPostgrestCallback.onSuccess(new PostgrestResult(rpcName, selectQueryBuilder, postgrestConfig, response));
            } else {
                helper.generateError(response, onPostgrestCallback);
            }
        });
    }
}
