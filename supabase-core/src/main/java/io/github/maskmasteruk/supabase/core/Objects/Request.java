package io.github.maskmasteruk.supabase.core.Objects;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.function.Consumer;

import io.github.maskmasteruk.supabase.core.Network.HttpMethod;

/**
 * Represents an HTTP request to be sent to the Supabase API.
 *
 * Responsibilities:
 * - Holding request metadata (URL, method, headers).
 * - Holding request body (JSON).
 * - Storing callbacks for data streaming (upload/download).
 *
 * Usage:
 * Request request = new Request(HttpMethod.POST, headers, url, json);
 *
 * Thread Safety:
 * Not thread-safe. Should be built and used within the same execution flow.
 */
public class Request {

    /**
     * The HTTP method to be used for the request.
     */
    HttpMethod httpMethod;

    /**
     * Headers to be included in the request.
     */
    HashMap<String, String> headers;

    /**
     * The full URL for the request.
     */
    String requestUrl;

    /**
     * The JSON payload for the request body.
     */
    String json;

    /**
     * Callback for handling upload streaming.
     */
    private Consumer<HttpURLConnection> uploadRunnable;

    /**
     * Callback for handling download streaming.
     */
    private Consumer<HttpURLConnection> downloadRunnable;

    /**
     * Creates a Request with all parameters.
     *
     * @param httpMethod The HTTP method.
     * @param headers    The request headers.
     * @param requestUrl The target URL.
     * @param json       The JSON body.
     */
    public Request(HttpMethod httpMethod, HashMap<String, String> headers, String requestUrl, String json) {
        this.httpMethod = httpMethod;
        this.headers = headers;
        this.requestUrl = requestUrl;
        this.json = json;
    }

    /**
     * Creates a Request with only the URL.
     *
     * @param requestUrl The target URL.
     */
    public Request(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    /**
     * Sets the HTTP method.
     *
     * @param httpMethod The HTTP method to set.
     * @return This Request instance for chaining.
     */
    public Request setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
        return this;
    }

    /**
     * Checks if there is an upload callback defined.
     *
     * @return true if an uploadRunnable is present.
     */
    public boolean hasAnyUploadSource() {
        return uploadRunnable != null;
    }

    /**
     * Checks if there is a download callback defined.
     *
     * @return true if a downloadRunnable is present.
     */
    public boolean hasAnyDownloadSource() {
        return downloadRunnable != null;
    }

    /**
     * Gets the upload callback.
     *
     * @return The uploadRunnable Consumer.
     */
    public Consumer<HttpURLConnection> getUploadRunnable() {
        return uploadRunnable;
    }

    /**
     * Sets the upload callback.
     *
     * @param uploadRunnable The Consumer to handle upload streaming.
     * @return This Request instance for chaining.
     */
    public Request setUploadRunnable(Consumer<HttpURLConnection> uploadRunnable) {
        this.uploadRunnable = uploadRunnable;
        return this;
    }

    /**
     * Gets the download callback.
     *
     * @return The downloadRunnable Consumer.
     */
    public Consumer<HttpURLConnection> getDownloadRunnable() {
        return downloadRunnable;
    }

    /**
     * Sets the download callback.
     *
     * @param downloadRunnable The Consumer to handle download streaming.
     * @return This Request instance for chaining.
     */
    public Request setDownloadRunnable(Consumer<HttpURLConnection> downloadRunnable) {
        this.downloadRunnable = downloadRunnable;
        return this;
    }

    /**
     * Gets the HTTP method.
     *
     * @return The HttpMethod.
     */
    public HttpMethod getHttpMethod() {
        return httpMethod;
    }


    /**
     * Sets the request headers.
     *
     * @param headers The headers to set.
     * @return This Request instance for chaining.
     */
    public Request setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Gets the request headers.
     *
     * @return The headers map.
     */
    public HashMap<String, String> getHeaders() {
        return headers;
    }

    /**
     * Gets the request URL.
     *
     * @return The target URL.
     */
    public String getRequestUrl() {
        return requestUrl;
    }

    /**
     * Gets the JSON payload.
     *
     * @return The JSON string.
     */
    public String getJson() {
        return json;
    }
}
