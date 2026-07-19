package io.github.maskmasteruk.supabase.storage.Object;

import java.util.HashMap;

import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;
import io.github.maskmasteruk.supabase.storage.Helper;

/**
 * Represents metadata for a storage object.
 * <p>
 * Purpose: This class allows users to specify properties like content type, cache control,
 * and custom metadata when uploading or updating objects in Supabase Storage.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Holding object metadata fields.</li>
 *     <li>Building HTTP headers from metadata for API requests.</li>
 *     <li>Encoding custom metadata into Base64 format as required by Supabase.</li>
 * </ul>
 * </p>
 * <p>
 * Example Usage:
 * <pre>
 * StorageMetadata metadata = new StorageMetadata()
 *     .setContentType("image/jpeg")
 *     .setCacheControl(7200L) // 2 hours
 *     .upsert();
 * </pre>
 * </p>
 */
public class StorageMetadata {
    String contentType;

    /** Cache control duration in seconds. Default is 3600 (1 hour). */
    Long cacheControl = 3600L;

    Long fileSize;
    Boolean upsert;
    HashMap<String, String> metadata;


    /**
     * Default constructor.
     */
    public StorageMetadata() {
    }

    /** @return The MIME type of the content. */
    public String getContentType() {
        return contentType;
    }

    /** @param contentType MIME type (e.g., "application/pdf"). @return This instance. */
    public StorageMetadata setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /** @return The cache control duration in seconds. */
    public Long getCacheControl() {
        return cacheControl;
    }

    /** @param cacheControl Duration in seconds. @return This instance. */
    public StorageMetadata setCacheControl(Long cacheControl) {
        this.cacheControl = cacheControl;
        return this;
    }

    /** @return The file size in bytes. */
    public Long getFileSize() {
        return fileSize;
    }

    /** @param fileSize Size in bytes. @return This instance. */
    public StorageMetadata setFileSize(Long fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    /**
     * Enables upsert mode, allowing overwriting of existing objects with the same name.
     *
     * @return This instance.
     */
    public StorageMetadata upsert() {
        this.upsert = true;
        return this;
    }

    /** @return Map of custom metadata. */
    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    /** @param metadata Map of key-value pairs for custom metadata. @return This instance. */
    public StorageMetadata setMetadata(HashMap<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Constructs a map of HTTP headers representing this metadata.
     *
     * @return Map containing standard and custom Supabase headers.
     */
    public HashMap<String, String> buildHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType);
        if (upsert != null) {
            headers.put("x-upsert", upsert.toString());
        }
        if (metadata != null) {
            String metadata_b64 = getMetadataB64();
            headers.put("x-metadata", metadata_b64);
        }
        headers.put("Cache-Control", String.valueOf(cacheControl));
        return headers;
    }

    /**
     * Encodes the custom metadata map into a Base64-encoded JSON string.
     *
     * @return Base64-encoded metadata string, or {@code null} if no metadata is set.
     */
    public String getMetadataB64() {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
        metadata.forEach(jsonObjectStringBuilder::append);
        return Helper.encodeToBase64(jsonObjectStringBuilder.build());
    }
}
