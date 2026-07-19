package io.github.maskmasteruk.supabase.storage;

/**
 * Internal constants for Supabase Storage API endpoints.
 * <p>
 * Purpose: This class centralizes all the string literals used for constructing
 * API URLs to ensure consistency and ease of updates.
 * </p>
 */
class STORAGE_END_POINTS {

    /** The root storage endpoint segment. */
    public static final String STORAGE = "storage";

    /** The API version segment. */
    public static final String VERSION = "v1";

    /** Segment for bucket-related operations. */
    public static final String BUCKET = "bucket";

    /** Segment for accessing public objects. */
    public static final String PUBLIC = "public";

    /** Segment for moving objects. */
    public static final String MOVE = "move";

    /** Segment for copying objects. */
    public static final String COPY = "copy";

    /** Segment for listing objects. */
    public static final String LIST = "list";

    /** Segment for object-related operations. */
    public static final String OBJECT = "object";

    /** Segment for resumable upload operations. */
    public static final String RESUMABLE = "resumable";

    /** Segment for signed URL operations. */
    public static final String SIGN = "sign";

    /** Segment for upload operations. */
    public static final String UPLOAD = "upload";

    /** Segment for emptying a bucket. */
    public static final String EMPTY = "empty";
}
