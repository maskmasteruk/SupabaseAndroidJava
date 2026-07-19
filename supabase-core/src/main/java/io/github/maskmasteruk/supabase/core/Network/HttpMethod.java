package io.github.maskmasteruk.supabase.core.Network;

import androidx.annotation.NonNull;

/**
 * Enumeration of HTTP methods supported by the Supabase SDK.
 *
 * Responsibilities:
 * - Representing standard HTTP verbs.
 * - Providing a string representation for network requests.
 */
public enum HttpMethod {
    /**
     * The GET method requests a representation of the specified resource.
     */
    GET,

    /**
     * The HEAD method asks for a response identical to that of a GET request, but without the response body.
     */
    HEAD,

    /**
     * The POST method is used to submit an entity to the specified resource.
     */
    POST,

    /**
     * The PUT method replaces all current representations of the target resource with the request payload.
     */
    PUT,

    /**
     * The PATCH method is used to apply partial modifications to a resource.
     */
    PATCH,

    /**
     * The DELETE method deletes the specified resource.
     */
    DELETE;

    /**
     * Returns the string representation of the HTTP method.
     *
     * @return The method name (e.g., "GET").
     */
    @NonNull
    @Override
    public String toString() {
        return name();
    }
}