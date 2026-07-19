package io.github.maskmasteruk.supabase.core.Network;

import static io.github.maskmasteruk.supabase.core.Network.HttpMethod.DELETE;
import static io.github.maskmasteruk.supabase.core.Network.HttpMethod.GET;
import static io.github.maskmasteruk.supabase.core.Network.HttpMethod.HEAD;
import static io.github.maskmasteruk.supabase.core.Network.HttpMethod.PATCH;
import static io.github.maskmasteruk.supabase.core.Network.HttpMethod.POST;
import static io.github.maskmasteruk.supabase.core.Network.HttpMethod.PUT;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import io.github.maskmasteruk.supabase.core.Objects.Request;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.core.Supabase;

/**
 * Utility class for handling HTTP requests to the Supabase API.
 *
 * Responsibilities:
 * - Executing various HTTP methods (GET, POST, etc.).
 * - Managing request headers including authentication (apikey, Authorization).
 * - Handling request body (JSON payloads).
 * - Supporting file uploads through custom runnables.
 * - Providing convenience methods for service role operations.
 *
 * Usage:
 * RequestHandler handler = new RequestHandler();
 * Response response = handler.get("https://...");
 *
 * Thread Safety:
 * This class is stateless with respect to the requests it handles, but relies on the singleton Supabase instance.
 * It is safe for concurrent use.
 */
public class RequestHandler {
    /**
     * Reference to the Supabase singleton instance.
     */
    private final Supabase supabase;

    /**
     * Default headers used for most requests.
     */
    public static final HashMap<String, String> baseHeaders = new HashMap<>(Map.ofEntries(Map.entry("Content-Type", "application/json")));

    /**
     * Minimum buffer size for data transfer operations.
     */
    public static final int MIN_BUFFER_SIZE = 2048;

    /**
     * Average/default buffer size for data transfer operations.
     */
    public static final int AVG_BUFFER_SIZE = 8192;

    /**
     * Maximum buffer size for data transfer operations.
     */
    public static final int MAX_BUFFER_SIZE = 65536;


    /**
     * Constructs a new RequestHandler.
     * Initializes the Supabase reference.
     */
    public RequestHandler() {
        this.supabase = Supabase.getInstance();
    }

    /**
     * Low-level method to send an HTTP request.
     *
     * @param request Request object containing the HTTP method, requestUrl, headers, and optional JSON payload.
     * @param apiKey  The API Key to use (passed in 'apikey' header).
     * @param bearer  The Authorization token (passed in 'Authorization: Bearer' header).
     * @return A Response object wrapping the HttpURLConnection.
     * @throws SupabaseError If an I/O error occurs during the request.
     */
    public Response sendRequest(Request request, String apiKey, String bearer) {
        try {
            URL url = new URL(request.getRequestUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(request.getHttpMethod().toString());
            for (Map.Entry<String, String> keyValue : request.getHeaders().entrySet()) {
                connection.setRequestProperty(keyValue.getKey(), keyValue.getValue());
            }
            connection.setRequestProperty("apikey", apiKey);
            connection.setRequestProperty("Authorization", "Bearer " + bearer);
            if (request.getJson() != null) {
                connection.setDoOutput(true);
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(request.getJson().getBytes(StandardCharsets.UTF_8));
                outputStream.close();
            }
            if (request.hasAnyUploadSource()) {
                connection.setDoOutput(true);
                request.getUploadRunnable().accept(connection);
            }
            return new Response(request, connection);

        } catch (IOException e) {
            throw new SupabaseError(e);
        }
    }

    /**
     * Performs a GET request using a Request object.
     *
     * @param request The Request object.
     * @return The Response from the server.
     */
    public Response get(Request request) {
        return sendRequest(request.setHttpMethod(GET), supabase.getSupabaseConfig().getProjectPublishableKey(), supabase.getSupabaseConfig().getBearer());
    }

    /**
     * Performs a GET request.
     *
     * @param requestUrl The target URL.
     * @param headers    The Headers to include.
     * @return The Response from the server.
     */
    public Response get(String requestUrl, HashMap<String, String> headers) {
        return sendRequest(new Request(GET, headers, requestUrl, null), supabase.getSupabaseConfig().getProjectPublishableKey(), supabase.getSupabaseConfig().getBearer());
    }

    /**
     * Performs a GET request with default headers.
     *
     * @param requestUrl The target URL.
     * @return The Response from the server.
     */
    public Response get(String requestUrl) {
        return get(requestUrl, baseHeaders);
    }

    /**
     * Performs a HEAD request using a Request object.
     *
     * @param request The Request object.
     * @return The Response from the server.
     */
    public Response head(Request request) {
        return sendRequest(request.setHttpMethod(HEAD), supabase.getSupabaseConfig().getProjectPublishableKey(), supabase.getSupabaseConfig().getBearer());
    }

    /**
     * Performs a HEAD request.
     *
     * @param requestUrl The target URL.
     * @param headers    The Headers to include.
     * @return The Response from the server.
     */
    public Response head(String requestUrl, HashMap<String, String> headers) {
        return sendRequest(new Request(HEAD, headers, requestUrl, null), supabase.getSupabaseConfig().getProjectPublishableKey(), supabase.getSupabaseConfig().getBearer());
    }

    /**
     * Performs a HEAD request with default headers.
     *
     * @param requestUrl The target URL.
     * @return The Response from the server.
     */
    public Response head(String requestUrl) {
        return head(requestUrl, baseHeaders);
    }

    /**
     * Performs a POST request using a Request object.
     *
     * @param request The Request object.
     * @return The Response from the server.
     */
    public Response post(Request request) {
        return sendRequest(request.setHttpMethod(POST), supabase.getSupabaseConfig().getProjectPublishableKey(), supabase.getSupabaseConfig().getBearer());
    }

    /**
     * Performs a POST request.
     *
     * @param requestUrl The target URL.
     * @param json       The JSON payload.
     * @param headers    The Headers to include.
     * @return The Response from the server.
     */
    public Response post(String requestUrl, String json, HashMap<String, String> headers) {
        return sendRequest(new Request(POST, headers, requestUrl, json), supabase.getSupabaseConfig().getProjectPublishableKey(), supabase.getSupabaseConfig().getBearer());
    }

    /**
     * Performs a POST request with default headers.
     *
     * @param requestUrl The target URL.
     * @param json       The JSON payload.
     * @return The Response from the server.
     */
    public Response post(String requestUrl, String json) {
        return post(requestUrl, json, baseHeaders);
    }

    /**
     * Performs a PUT request using a Request object.
     *
     * @param request The Request object.
     * @return The Response from the server.
     */
    public Response put(Request request) {
        return sendRequest(request.setHttpMethod(PUT), supabase.getSupabaseConfig().getProjectPublishableKey(), supabase.getSupabaseConfig().getBearer());
    }

    /**
     * Performs a PUT request.
     *
     * @param requestUrl The target URL.
     * @param json       The JSON payload.
     * @param headers    The Headers to include.
     * @return The Response from the server.
     */
    public Response put(String requestUrl, String json, HashMap<String, String> headers) {
        return sendRequest(new Request(PUT, headers, requestUrl, json), supabase.getSupabaseConfig().getProjectPublishableKey(), supabase.getSupabaseConfig().getBearer());
    }

    /**
     * Performs a PUT request with default headers.
     *
     * @param requestUrl The target URL.
     * @param json       The JSON payload.
     * @return The Response from the server.
     */
    public Response put(String requestUrl, String json) {
        return put(requestUrl, json, baseHeaders);
    }

    /**
     * Performs a PATCH request using a Request object.
     *
     * @param request The Request object.
     * @return The Response from the server.
     */
    public Response patch(Request request) {
        return sendRequest(request.setHttpMethod(PATCH), supabase.getSupabaseConfig().getProjectPublishableKey(), supabase.getSupabaseConfig().getBearer());
    }

    /**
     * Performs a PATCH request.
     *
     * @param requestUrl The target URL.
     * @param json       The JSON payload.
     * @param headers    The Headers to include.
     * @return The Response from the server.
     */
    public Response patch(String requestUrl, String json, HashMap<String, String> headers) {
        return sendRequest(new Request(PATCH, headers, requestUrl, json), supabase.getSupabaseConfig().getProjectPublishableKey(), supabase.getSupabaseConfig().getBearer());
    }

    /**
     * Performs a PATCH request with default headers.
     *
     * @param requestUrl The target URL.
     * @param json       The JSON payload.
     * @return The Response from the server.
     */
    public Response patch(String requestUrl, String json) {
        return patch(requestUrl, json, baseHeaders);
    }

    /**
     * Performs a DELETE request.
     *
     * @param requestUrl The target URL.
     * @param json       Optional JSON payload.
     * @param headers    The Headers to include.
     * @return The Response from the server.
     */
    public Response delete(String requestUrl, String json, HashMap<String, String> headers) {
        return sendRequest(new Request(DELETE, headers, requestUrl, json), supabase.getSupabaseConfig().getProjectPublishableKey(), supabase.getSupabaseConfig().getBearer());
    }

    /**
     * Performs a DELETE request with default headers.
     *
     * @param requestUrl The target URL.
     * @param json       Optional JSON payload.
     * @return The Response from the server.
     */
    public Response delete(String requestUrl, String json) {
        return delete(requestUrl, json, baseHeaders);
    }


    /**
     * Performs a GET request using the service role key for authentication.
     *
     * @param requestUrl The target URL.
     * @return The Response from the server.
     */
    public Response getServiceRole(String requestUrl) {
        return sendRequest(new Request(GET, baseHeaders, requestUrl, null), supabase.getSupabaseConfig().getProjectServiceRoleKey(), supabase.getSupabaseConfig().getProjectServiceRoleKey());
    }

    /**
     * Performs a POST request using the service role key for authentication.
     *
     * @param requestUrl The target URL.
     * @param json       The JSON payload.
     * @return The Response from the server.
     */
    public Response postServiceRole(String requestUrl, String json) {
        return sendRequest(new Request(POST, baseHeaders, requestUrl, json), supabase.getSupabaseConfig().getProjectServiceRoleKey(), supabase.getSupabaseConfig().getProjectServiceRoleKey());
    }

    /**
     * Performs a PUT request using the service role key for authentication.
     *
     * @param requestUrl The target URL.
     * @param json       The JSON payload.
     * @return The Response from the server.
     */
    public Response putServiceRole(String requestUrl, String json) {
        return sendRequest(new Request(PUT, baseHeaders, requestUrl, json), supabase.getSupabaseConfig().getProjectServiceRoleKey(), supabase.getSupabaseConfig().getProjectServiceRoleKey());
    }

    /**
     * Performs a DELETE request using the service role key for authentication.
     *
     * @param requestUrl The target URL.
     * @param json       Optional JSON payload.
     * @return The Response from the server.
     */
    public Response deleteServiceRole(String requestUrl, String json) {
        return sendRequest(new Request(DELETE, baseHeaders, requestUrl, json), supabase.getSupabaseConfig().getProjectServiceRoleKey(), supabase.getSupabaseConfig().getProjectServiceRoleKey());
    }

}
