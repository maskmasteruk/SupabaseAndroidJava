package io.github.maskmasteruk.supabase.auth;

/**
 * Internal constants for Supabase authentication endpoints.
 */
class AUTH_END_POINTS {
    /**
     * The base path for authentication services.
     */
    public static final String AUTH = "auth";

    /**
     * The version of the authentication service.
     */
    public static final String VERSION = "v1";

    /**
     * Endpoint for user signup.
     */
    public static final String SIGNUP = "signup";

    /**
     * Endpoint for sign-in and token operations.
     */
    public static final String SIGN_IN = "token";

    /**
     * Endpoint for OAuth authorization.
     */
    public static final String OAUTH_AUTHORIZATION = "authorize";

    /**
     * Endpoint for refreshing tokens.
     */
    public static final String TOKEN = "token";

    /**
     * Endpoint for user logout.
     */
    public static final String LOGOUT = "logout";

    /**
     * Endpoint for retrieving and updating user details.
     */
    public static final String USER_DETAILS = "user";

    /**
     * Endpoint for generating OTPs.
     */
    public static final String OTP = "otp";

    /**
     * Endpoint for verifying OTPs.
     */
    public static final String VERIFY = "verify";

    /**
     * Endpoint for password recovery.
     */
    public static final String RECOVER = "recover";

    /**
     * Endpoint for resending verification triggers.
     */
    public static final String RESEND = "resend";

    /**
     * Endpoint for forcing user re-authentication.
     */
    public static final String REAUTHENTICATE = "reauthenticate";

    /**
     * Endpoint for managing Multi-Factor Authentication (MFA) factors.
     */
    public static final String FACTORS = "factors";

    /**
     * Endpoint for initiating an MFA challenge.
     */
    public static final String CHALLENGE = "challenge";
}


