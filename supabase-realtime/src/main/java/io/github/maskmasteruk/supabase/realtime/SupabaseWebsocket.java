package io.github.maskmasteruk.supabase.realtime;

import io.github.maskmasteruk.supabase.core.Network.HttpMethod;

/**
 * Utility class for constructing the initial HTTP upgrade request for Websockets.
 *
 * <p>This class handles the formatting of the HTTP request headers required to
 * transition a standard TCP/SSL connection into a WebSocket connection.</p>
 */
public class SupabaseWebsocket {

    /**
     * Builds a raw HTTP GET request string for the WebSocket handshake.
     *
     * @param httpMethod The HTTP method (should always be GET for WebSocket upgrade).
     * @param url The full URL including query parameters.
     * @param key The randomly generated Sec-WebSocket-Key.
     * @param host The host name of the server.
     * @return The formatted HTTP request string.
     *
     * <pre>{@code
     * String request = SupabaseWebsocket.buildWebsocketRequest(
     *     HttpMethod.GET,
     *     "/realtime/v1/websocket?apikey=...",
     *     "dGhlIHNhbXBsZSBub25jZQ==",
     *     "your-project.supabase.co"
     * );
     * }</pre>
     */
    public static String buildWebsocketRequest(HttpMethod httpMethod, String url, String key, String host) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append(httpMethod.toString())
                .append(" ")
                .append(url)
                .append(" HTTP/1.1")
                .append("\r\n")
                .append("Host: ")
                .append(host)
                .append("\r\n")
                .append("Upgrade: ")
                .append("websocket")
                .append("\r\n")
                .append("Connection: ")
                .append("Upgrade")
                .append("\r\n")
                .append("Sec-WebSocket-Key: ")
                .append(key)
                .append("\r\n")
                .append("Sec-WebSocket-Version: ")
                .append("13")
                .append("\r\n")
                .append("\r\n");

        return stringBuilder.toString();
    }
}
