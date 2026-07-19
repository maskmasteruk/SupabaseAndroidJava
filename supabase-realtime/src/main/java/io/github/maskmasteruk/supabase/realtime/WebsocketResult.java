package io.github.maskmasteruk.supabase.realtime;

import java.io.InputStream;
import java.io.OutputStream;

import javax.net.ssl.SSLSocket;

/**
 * Data class representing the result of a successful Websocket connection attempt.
 *
 * <p>This class bundles the established SSL socket along with its input and output
 * streams and the initial HTTP upgrade response from the server.</p>
 */
public class WebsocketResult {
    /** The established SSL socket. */
    public final SSLSocket socket;

    /** The output stream for sending data to the server. */
    public final OutputStream outputStream;

    /** The input stream for receiving data from the server. */
    public final InputStream inputStream;

    /** The initial HTTP response received during the websocket upgrade handshake. */
    public final String response;

    /**
     * Constructs a new WebsocketResult.
     *
     * @param socket The connected SSLSocket.
     * @param outputStream The socket's output stream.
     * @param inputStream The socket's input stream.
     * @param response The initial handshake response.
     */
    public WebsocketResult(SSLSocket socket, OutputStream outputStream, InputStream inputStream, String response) {
        this.socket = socket;
        this.outputStream = outputStream;
        this.inputStream = inputStream;
        this.response = response;
    }
}

