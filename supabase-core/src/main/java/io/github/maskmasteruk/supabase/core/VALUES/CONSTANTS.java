package io.github.maskmasteruk.supabase.core.VALUES;

import java.util.Map;

/**
 * Global constants used throughout the Supabase Android SDK.
 *
 * Responsibilities:
 * - Storing predefined strings and configurations.
 * - Providing a central location for error message mappings.
 */
public class CONSTANTS {

    /**
     * A mapping of Supabase Auth error codes to user-friendly error messages.
     * This map is immutable and provides localized-like descriptions for common auth issues.
     */
    public static final Map<String, String> AUTH_ERROR_MESSAGES = Map.ofEntries(
            Map.entry("user_already_exists", "An account with this email already exists."),
            Map.entry("email_address_invalid", "Please enter a valid email address."),
            Map.entry("weak_password", "Password is too weak."),
            Map.entry("email_not_confirmed", "Please verify your email."),
            Map.entry("invalid_credentials", "Incorrect email or password."),
            Map.entry("user_not_found", "Account not found."),
            Map.entry("otp_expired", "Verification code expired."),
            Map.entry("over_request_rate_limit", "Too many attempts. Please try again later."),
            Map.entry("signup_disabled", "Registration is disabled."),
            Map.entry("session_expired", "Session expired. Please sign in again.")
    );
}
