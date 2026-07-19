package io.github.maskmasteruk.supabase.auth;

import java.util.concurrent.atomic.AtomicReference;

/**
 * An in-memory cache for storing PKCE code verifiers during the OAuth flow.
 * <p>
 * This class uses an {@link AtomicReference} to safely store and retrieve the code verifier
 * across different stages of the authentication process.
 * <p>
 * <b>Architectural Responsibility:</b> Provides temporary storage for PKCE data.
 * <p>
 * <b>Thread Safety:</b> Thread-safe using {@link AtomicReference}.
 *
 * @since 1.0.0
 */
class InMemoryCodeVerifyCache {
    private final AtomicReference<String> codeVerifier = new AtomicReference<>(null);

    /**
     * Saves the code verifier to the cache.
     *
     * @param verifier The code verifier string.
     */
    public void save(String verifier) {
        codeVerifier.set(verifier);
    }

    /**
     * Retrieves the stored code verifier.
     *
     * @return The cached code verifier, or {@code null} if not found.
     */
    public String load() {
        return codeVerifier.get();
    }

    /**
     * Clears the cached code verifier.
     */
    public void delete() {
        codeVerifier.set(null);
    }
}

