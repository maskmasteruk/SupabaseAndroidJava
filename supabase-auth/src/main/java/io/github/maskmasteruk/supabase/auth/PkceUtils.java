package io.github.maskmasteruk.supabase.auth;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility class for PKCE (Proof Key for Code Exchange) operations.
 * <p>
 * PKCE is used during the OAuth authorization flow to prevent code injection attacks.
 * This class provides methods to generate a secure random code verifier and its
 * corresponding SHA-256 code challenge.
 * <p>
 * <b>Architectural Responsibility:</b> Provides cryptographic utilities for secure authentication flows.
 *
 * @see <a href="https://tools.ietf.org/html/rfc7636">RFC 7636</a>
 * @since 1.0.0
 */
class PkceUtils {
    /**
     * Generates a cryptographically secure random code verifier.
     * The verifier is a high-entropy cryptographic random string using the characters [A-Z, a-z, 0-9, -, ., _, ~].
     *
     * @return A URL-safe Base64 encoded string representing the code verifier.
     */
    public static String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.encodeToString(codeVerifier, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }

    /**
     * Generates a code challenge from a given code verifier using the SHA-256 algorithm.
     *
     * @param codeVerifier The code verifier string.
     * @return A URL-safe Base64 encoded string representing the code challenge.
     * @throws RuntimeException if the SHA-256 algorithm is not available.
     */
    public static String generateCodeChallenge(String codeVerifier) {
        byte[] bytes = codeVerifier.getBytes(StandardCharsets.US_ASCII);
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] digest = messageDigest.digest(bytes);
        return Base64.encodeToString(digest, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
    }
}

