package io.github.maskmasteruk.supabase.realtime;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Internal utility class for handling low-level Websocket protocol operations.
 *
 * <p>This class implements the RFC 6455 WebSocket protocol, including frame parsing,
 * masking, and SSL socket creation. It is used internally by the realtime library
 * to manage the raw socket connection to Supabase.</p>
 */
class Websocket {

    /**
     * Represents a single WebSocket frame.
     */
    private static class Frame {
        /** Final fragment flag. */
        boolean fin;
        /** Operation code (e.g., 0x1 for text, 0x8 for close). */
        int opcode;
        /** The data payload of the frame. */
        byte[] payload;
    }

    /**
     * Reads a single WebSocket frame from the input stream.
     *
     * @param in The input stream from the socket.
     * @return The parsed {@link Frame}, or null if the stream ended.
     * @throws IOException if a network error occurs or the frame is invalid.
     */
    private static Frame readFrame(InputStream in) throws IOException {
        int b1 = in.read();
        if (b1 == -1)
            return null;

        int b2 = in.read();
        if (b2 == -1)
            return null;

        Frame frame = new Frame();

        frame.fin = (b1 & 0x80) != 0;
        frame.opcode = b1 & 0x0F;

        boolean masked = (b2 & 0x80) != 0;
        long len = b2 & 0x7F;

        if (len == 126) {
            int b3 = in.read();
            int b4 = in.read();

            if (b3 == -1 || b4 == -1)
                throw new EOFException();

            len = ((b3 & 0xFF) << 8) | (b4 & 0xFF);
        } else if (len == 127) {
            len = 0;

            for (int i = 0; i < 8; i++) {
                int b = in.read();

                if (b == -1)
                    throw new EOFException();

                len = (len << 8) | (b & 0xFF);
            }
        }

        if (len > Integer.MAX_VALUE)
            throw new IOException("Frame too large.");

        byte[] mask = null;

        if (masked) {
            mask = new byte[4];
            new DataInputStream(in).readFully(mask);
        }

        byte[] payload = new byte[(int) len];
        new DataInputStream(in).readFully(payload);

        if (masked) {
            for (int i = 0; i < payload.length; i++) {
                payload[i] ^= mask[i % 4];
            }
        }

        frame.payload = payload;

        return frame;
    }

    /**
     * Reads a full WebSocket message, which may consist of multiple frames.
     *
     * @param in The input stream.
     * @return The message as a String, or null if the connection closed.
     * @throws IOException if a network or protocol error occurs.
     */
    public static String readMessage(InputStream in) throws IOException {

        ByteArrayOutputStream message = new ByteArrayOutputStream();

        while (true) {

            Frame frame = readFrame(in);

            if (frame == null)
                return null;

            switch (frame.opcode) {

                case 0x1:
                case 0x0:
                    message.write(frame.payload);

                    if (frame.fin) {
                        return message.toString(StandardCharsets.UTF_8.toString());
                    }
                    break;

                case 0x8: // Close
                    return null;

                case 0x9: // Ping
                    // You should send a Pong with the same payload.
                    // Ignored here.
                    break;

                case 0xA: // Pong
                    // Ignore
                    break;

                default:
                    throw new IOException("Unsupported opcode: " + frame.opcode);
            }
        }
    }

    /**
     * Writes text to the output stream as a masked WebSocket frame.
     *
     * <p>Masking is mandatory for all frames sent from client to server.</p>
     *
     * @param outputStream The output stream.
     * @param text The text to send.
     * @throws IOException if a network error occurs.
     */
    public static void writeAsMaskedFrame(OutputStream outputStream, String text) throws IOException {
        byte[] payload = text.getBytes(StandardCharsets.UTF_8);
        outputStream.write(0x81);

        int len = payload.length;

        if (len <= 125) {
            outputStream.write(0x80 | len);
        } else if (len <= 65535) {
            outputStream.write(0x80 | 126);
            outputStream.write((len >> 8) & 0xFF);
            outputStream.write(len & 0xFF);
        } else  {
            outputStream.write(0x80 | 127);

            for (int i = 7; i >= 0; i--) {
                outputStream.write((len >> (8*i)) & 0xFF);
            }
        }

        byte[] mask = new byte[4];
        new SecureRandom().nextBytes(mask);
        outputStream.write(mask);

        for (int i = 0; i < payload.length; i++) {
            payload[i] ^= mask[i%4];
        }

        outputStream.write(payload);
        outputStream.flush();
    }

    /**
     * Writes raw text to the output stream. Typically used for initial HTTP upgrade request.
     *
     * @param outputStream The output stream.
     * @param text The text to write.
     * @throws IOException if a network error occurs.
     */
    public static void writeText(OutputStream outputStream, String text) throws IOException {
        outputStream.write(text.getBytes(StandardCharsets.UTF_8));
        outputStream.flush();
    }

    /**
     * Reads raw text from the input stream until an empty line is encountered.
     * Typically used for reading HTTP upgrade responses.
     *
     * @param inputStream The input stream.
     * @return The text read.
     * @throws IOException if a network error occurs.
     */
    public static String readText(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        StringBuilder lines = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null && !line.isEmpty()) {
            lines.append(line).append("\n");
        }

        return lines.toString();
    }

    /**
     * Creates and handshakes an SSL socket.
     *
     * @param host The host to connect to.
     * @param port The port to connect to.
     * @return An established {@link SSLSocket}.
     * @throws IOException if the connection or handshake fails.
     */
    public static SSLSocket createSocket(String host, int port) throws IOException {
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        socket.startHandshake();

        return socket;
    }
}
