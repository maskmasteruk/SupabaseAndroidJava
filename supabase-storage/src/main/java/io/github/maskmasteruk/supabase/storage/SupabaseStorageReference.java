package io.github.maskmasteruk.supabase.storage;

import java.util.ArrayList;

import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

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

    /**
     * Builds the public URL for the object in a public storage bucket.
     * <p>
     * The generated URL points to the Storage API's public object endpoint:
     * <pre>
     * {projectUrl}/storage/v1/object/public/{bucketId}/{path}
     * </pre>
     * If a {@link SupabaseObjectUrlBuilder} is provided, its query parameters
     * (such as image transformations, download options, or cache-busting values)
     * are appended to the URL.
     *
     * @param supabaseObjectUrlBuilder optional builder used to generate query
     *                                 parameters for the public URL; may be
     *                                 {@code null}.
     * @return the complete public URL for the storage object.
     * @throws SupabaseError if the bucket ID has not been set.
     */
    public String publicUrl(SupabaseObjectUrlBuilder supabaseObjectUrlBuilder) {
        if (bucketId == null) {
            throw new SupabaseError("Bucket ID is required but was null.");
        }
        UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
        if (supabaseObjectUrlBuilder != null && supabaseObjectUrlBuilder.getTransformation() != null) {
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.RENDER);
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.IMAGE);

        }else {
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.OBJECT);
        }
        baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.PUBLIC);
        baseStorageUrlBuilder.appendPath(bucketId);
        paths.forEach(baseStorageUrlBuilder::appendPath);

        if (supabaseObjectUrlBuilder != null) {
            supabaseObjectUrlBuilder.buildQueryParams().forEach(baseStorageUrlBuilder::appendQueryParam);
        }

        return baseStorageUrlBuilder.build();
    }

    /**
     * Builds the public URL for the object without any additional query
     * parameters.
     * <p>
     * Equivalent to calling:
     * <pre>
     * publicUrl(null)
     * </pre>
     *
     * @return the complete public URL for the storage object.
     * @throws SupabaseError if the bucket ID has not been set.
     */
    public String publicUrl() {
        return publicUrl(null);
    }

    /**
     * Builds the authenticated URL for the object in a private/public storage bucket.
     * <p>
     * The generated URL points to the Storage API's authenticated object endpoint:
     * <pre>
     * {projectUrl}/storage/v1/object/authenticated/{bucketId}/{path}
     * </pre>
     * If a {@link SupabaseObjectUrlBuilder} is provided, its query parameters
     * (such as image transformations, download options, or cache-busting values)
     * are appended to the URL.
     *
     * @param supabaseObjectUrlBuilder optional builder used to generate query
     *                                 parameters for the public URL; may be
     *                                 {@code null}.
     * @return the complete public URL for the storage object.
     * @throws SupabaseError if the bucket ID has not been set.
     */
    public String authenticatedUrl(SupabaseObjectUrlBuilder supabaseObjectUrlBuilder) {
        if (bucketId == null) {
            throw new SupabaseError("Bucket ID is required but was null.");
        }
        UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
        if (supabaseObjectUrlBuilder != null && supabaseObjectUrlBuilder.getTransformation() != null) {
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.RENDER);
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.IMAGE);

        }else {
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.OBJECT);
        }
        baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.AUTHENTICATED);
        baseStorageUrlBuilder.appendPath(bucketId);
        paths.forEach(baseStorageUrlBuilder::appendPath);

        if (supabaseObjectUrlBuilder != null) {
            supabaseObjectUrlBuilder.buildQueryParams().forEach(baseStorageUrlBuilder::appendQueryParam);
        }

        return baseStorageUrlBuilder.build();
    }

    /**
     * Builds the authenticated URL for the object without any additional query
     * parameters.
     * <p>
     * Equivalent to calling:
     * <pre>
     * authenticatedUrl(null)
     * </pre>
     *
     * @return the complete authenticated URL for the storage object.
     * @throws SupabaseError if the bucket ID has not been set.
     */
    public String authenticatedUrl() {
        return authenticatedUrl(null);
    }
}
