package io.github.maskmasteruk.supabase.realtime;

/**
 * Defines the URL path segments and constants for Supabase Realtime endpoints.
 *
 * <p>This class is package-private and intended for internal use within the
 * realtime module to construct correct API URLs.</p>
 */
class REALTIME_END_POINTS {
    /** The base path segment for realtime services. */
    public static final String REALTIME = "realtime";

    /** The version of the realtime API. */
    public static final String VERSION = "v1";

    /** The endpoint segment for websocket connections. */
    public static final String WEBSOCKET = "websocket";
}
