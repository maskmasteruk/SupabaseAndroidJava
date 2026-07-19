package io.github.maskmasteruk.supabase.storage.Object;

import java.util.ArrayList;

/**
 * Represents a reference to a path within a Supabase Storage bucket.
 * <p>
 * Purpose: This class is used to build and store paths to objects or folders
 * inside a specific bucket, similar to how file paths work on a file system.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Storing the bucket ID.</li>
 *     <li>Maintaining a list of path segments.</li>
 *     <li>Providing a fluent API for building nested paths using {@link #child(String)}.</li>
 *     <li>Generating a string representation of the path prefix.</li>
 * </ul>
 * </p>
 * <p>
 * Example Usage:
 * <pre>
 * SupabaseStorageReference ref = new SupabaseStorageReference("avatars")
 *     .child("users")
 *     .child("profile.png");
 * // Resulting path: users/profile.png in bucket "avatars"
 * </pre>
 * </p>
 */
public class SupabaseStorageReference {
    private ArrayList<String> paths;
    private String bucketId;

    /**
     * Constructs a reference for a specific bucket.
     *
     * @param bucketId The ID of the bucket.
     */
    public SupabaseStorageReference(String bucketId) {
        this.bucketId = bucketId;
        paths = new ArrayList<>();
    }

    /**
     * Default constructor for an empty reference.
     */
    public SupabaseStorageReference() {
        paths = new ArrayList<>();
    }

    /** @return The ID of the bucket this reference points to. */
    public String getBucketId() {
        return bucketId;
    }

    /** @param bucketId The bucket ID. @return This instance. */
    public SupabaseStorageReference setBucketId(String bucketId) {
        this.bucketId = bucketId;
        return this;
    }

    /**
     * Appends a child segment to the path.
     *
     * @param child The name of the child file or folder.
     * @return This instance for method chaining.
     */
    public SupabaseStorageReference child(String child) {
        paths.add(child);
        return this;
    }

    /** @return The list of path segments. */
    public ArrayList<String> getPaths() {
        return paths;
    }

    /**
     * Joins all path segments with a forward slash.
     *
     * @return The complete path string (without the bucket ID).
     */
    public String buildPrefix() {
        return String.join("/", paths);
    }
}
