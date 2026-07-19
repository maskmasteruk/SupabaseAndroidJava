package io.github.maskmasteruk.supabase.core.Objects;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.maskmasteruk.supabase.core.Network.HttpMethod;
import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;

/**
 * Represents the response received from a Supabase API request.
 *
 * Responsibilities:
 * - Parsing the HTTP response code and body.
 * - Handling download streaming if a callback is provided in the request.
 * - Providing access to response headers and body (as string or JSON).
 * - Proper closing of network connections.
 *
 * Usage:
 * Response response = requestHandler.get(url);
 * if (response.getCode() == 200) {
 *     JSONObject data = response.getResponseJSON();
 * }
 *
 * Thread Safety:
 * Not thread-safe. Typically used once after a request is completed.
 */
public class Response {

    /**
     * The original request that triggered this response.
     */
    private Request request;

    /**
     * The raw response body as a string.
     */
    private String response;

    /**
     * The HTTP response code.
     */
    private int code;

    /**
     * The response headers.
     */
    private Map<String, List<String>> headerFields;

    /**
     * Constructs a Response object from an active HttpURLConnection.
     * This constructor reads the response body and disconnects the connection.
     *
     * @param request    The original Request object.
     * @param connection The active HttpURLConnection.
     * @throws SupabaseError If an I/O error occurs while reading the response.
     */
    public Response(Request request, HttpURLConnection connection) {
        this.request = request;
        try {
            code = connection.getResponseCode();

            if (request.hasAnyDownloadSource()) {
                if (code == HttpURLConnection.HTTP_OK) {
                    request.getDownloadRunnable().accept(connection);
                } else {
                    if (request.getHttpMethod() != HttpMethod.HEAD) {
                        readOutput(connection);
                    }
                }
            } else {
                if (request.getHttpMethod() != HttpMethod.HEAD) {
                    readOutput(connection);
                }
            }

            headerFields = connection.getHeaderFields();
            connection.disconnect();
        } catch (IOException e) {
            throw new SupabaseError(e);
        }
    }

    /**
     * Reads the response body from the connection's input or error stream.
     *
     * @param connection The active HttpURLConnection.
     * @throws IOException If an I/O error occurs.
     */
    private void readOutput(HttpURLConnection connection) throws IOException {
        InputStreamReader reader;
        if (code >= 200 && code < 300) {
            reader = new InputStreamReader(connection.getInputStream());
        } else {
            reader = new InputStreamReader(connection.getErrorStream());
        }

        BufferedReader bufferedReader = new BufferedReader(reader);
        response = bufferedReader.lines().collect(Collectors.joining("\n"));
        bufferedReader.close();
    }

    /**
     * Gets the original request.
     *
     * @return The Request object.
     */
    public Request getRequest() {
        return request;
    }

    /**
     * Gets the raw response body string.
     *
     * @return The response string.
     */
    public String getResponse() {
        return response;
    }

    /**
     * Parses and returns the response body as a JSONObject.
     *
     * @return The response body as a JSONObject.
     * @throws SupabaseError if the response is not a valid JSON object.
     */
    public JSONObject getResponseJSON() {
        return JsonUtils.getJsonObject(response);
    }

    /**
     * Parses and returns the response body as a JSONArray.
     *
     * @return The response body as a JSONArray.
     * @throws SupabaseError if the response is not a valid JSON array.
     */
    public JSONArray getResponseJSONArray() {
        return JsonUtils.getJsonArray(response);
    }

    /**
     * Gets the HTTP response code.
     *
     * @return The status code (e.g., 200, 404).
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the response headers.
     *
     * @return A map of header names to lists of values.
     */
    public Map<String, List<String>> getHeaderFields() {
        return headerFields;
    }

    /**
     * Sets the response headers manually.
     *
     * @param headerFields The headers map.
     * @return This Response instance for chaining.
     */
    public Response setHeaderFields(Map<String, List<String>> headerFields) {
        this.headerFields = headerFields;
        return this;
    }

    /**
     * Returns a string representation of the response, including body and headers.
     *
     * @return A formatted string with response details.
     */
    @Override
    public String toString() {
        return "Response{" +
                "\nresponse='" + response + '\'' +
                ", \ncode=" + code +
                ", \nheaderFields=" + headerFields +
                "\n}";
    }
}
