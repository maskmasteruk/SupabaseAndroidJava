package io.github.maskmasteruk.supabase.storage.Object;

import androidx.annotation.NonNull;

import org.json.JSONObject;

/**
 * Represents an object (file or folder) within Supabase Storage.
 * <p>
 * Purpose: This class is a data model that describes an individual item stored in a bucket.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Holding properties of a stored object (ID, name, key, owner, timestamps).</li>
 *     <li>Holding metadata about the file (size, MIME type, etc.).</li>
 *     <li>Distinguishing between files and folders.</li>
 * </ul>
 * </p>
 * <p>
 * Example Usage:
 * <pre>
 * storage.list(ref).onSuccess(objects -> {
 *     for (SupabaseObject obj : objects) {
 *         if (obj.isFile()) {
 *             System.out.println("File name: " + obj.getName());
 *         }
 *     }
 * });
 * </pre>
 * </p>
 */
public class SupabaseObject {

    private String key;


    private String id;
    private String name;
    private String bucket_id;
    private String owner;
    private String owner_id;
    private String version;

    private String created_at;
    private String updated_at;
    private String last_accessed_at;

    private Metadata metadata;

    private Object user_metadata;

    /**
     * Default constructor for creating an empty SupabaseObject.
     */
    public SupabaseObject() {
    }

    /**
     * Constructs a SupabaseObject by parsing a {@link JSONObject}.
     *
     * @param json The JSON object containing object details.
     */
    public SupabaseObject(JSONObject json) {
        this.key = json.optString("Key", null);
        this.id = json.optString("id", json.optString("Id", null));
        this.name = json.optString("name", null);
        this.bucket_id = json.optString("bucket_id", null);
        this.owner = json.optString("owner", null);
        this.owner_id = json.optString("owner_id", null);
        this.version = json.optString("version", null);

        this.created_at = json.optString("created_at", null);
        this.updated_at = json.optString("updated_at", null);
        this.last_accessed_at = json.optString("last_accessed_at", null);

        JSONObject metadataObj = json.optJSONObject("metadata");
        if (metadataObj != null) {
            this.metadata = new Metadata(metadataObj);
        }

        this.user_metadata = json.opt("user_metadata");
    }

    /** @return The unique key (path) of the object. */
    public String getKey() {
        return key;
    }

    /** @return The unique ID of the object. Returns null if it is a folder. */
    public String getId() {
        return id;
    }

    /** @return The name of the file or folder. */
    public String getName() {
        return name;
    }

    /** @return The ID of the bucket containing this object. */
    public String getBucket_id() {
        return bucket_id;
    }

    /** @return The owner identifier. */
    public String getOwner() {
        return owner;
    }

    /** @return The owner's unique ID. */
    public String getOwner_id() {
        return owner_id;
    }

    /** @return The version of the object. */
    public String getVersion() {
        return version;
    }

    /** @return ISO timestamp of creation. */
    public String getCreated_at() {
        return created_at;
    }

    /** @return ISO timestamp of last update. */
    public String getUpdated_at() {
        return updated_at;
    }

    /** @return ISO timestamp of last access. */
    public String getLast_accessed_at() {
        return last_accessed_at;
    }

    /** @return The system metadata for this object. */
    public Metadata getMetadata() {
        return metadata;
    }

    /** @return The user-defined metadata associated with the object. */
    public Object getUser_metadata() {
        return user_metadata;
    }

    /**
     * Determines if this object represents a folder.
     * In some list responses, folders have no ID.
     *
     * @return {@code true} if it's a folder, {@code false} otherwise.
     */
    public boolean isFolder() {
        return id == null;
    }

    /**
     * Determines if this object represents a file.
     *
     * @return {@code true} if it's a file, {@code false} otherwise.
     */
    public boolean isFile() {
        return id != null;
    }

    /**
     * Nested class containing technical metadata about a {@link SupabaseObject}.
     */
    public static class Metadata {

        private String eTag;
        private Long size;
        private String mimetype;
        private String cacheControl;
        private String lastModified;
        private Long contentLength;
        private Integer httpStatusCode;

        /**
         * Default constructor.
         */
        public Metadata() {
        }

        /**
         * Constructs Metadata from a {@link JSONObject}.
         *
         * @param json JSON object containing metadata fields.
         */
        public Metadata(JSONObject json) {
            this.eTag = json.optString("eTag", null);
            this.size = json.has("size") ? json.optLong("size") : null;
            this.mimetype = json.optString("mimetype", null);
            this.cacheControl = json.optString("cacheControl", null);
            this.lastModified = json.optString("lastModified", null);
            this.contentLength = json.has("contentLength") ? json.optLong("contentLength") : null;
            this.httpStatusCode = json.has("httpStatusCode") ? json.optInt("httpStatusCode") : null;
        }

        /** @return The ETag of the object. */
        public String geteTag() {
            return eTag;
        }

        /** @return The size of the object in bytes. */
        public Long getSize() {
            return size;
        }

        /** @return The MIME type. */
        public String getMimetype() {
            return mimetype;
        }

        /** @return The cache control string. */
        public String getCacheControl() {
            return cacheControl;
        }

        /** @return The last modified timestamp string. */
        public String getLastModified() {
            return lastModified;
        }

        /** @return The content length in bytes. */
        public Long getContentLength() {
            return contentLength;
        }

        /** @return The HTTP status code from the server. */
        public Integer getHttpStatusCode() {
            return httpStatusCode;
        }

        @NonNull
        @Override
        public String toString() {
            return "Metadata{" +
                    "eTag='" + eTag + '\'' +
                    ", size=" + size +
                    ", mimetype='" + mimetype + '\'' +
                    ", cacheControl='" + cacheControl + '\'' +
                    ", lastModified='" + lastModified + '\'' +
                    ", contentLength=" + contentLength +
                    ", httpStatusCode=" + httpStatusCode +
                    '}';
        }

    }

    @NonNull
    @Override
    public String toString() {
        return "SupabaseObject{" +
                "key='" + key + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", bucket_id='" + bucket_id + '\'' +
                ", owner='" + owner + '\'' +
                ", owner_id='" + owner_id + '\'' +
                ", version='" + version + '\'' +
                ", created_at='" + created_at + '\'' +
                ", updated_at='" + updated_at + '\'' +
                ", last_accessed_at='" + last_accessed_at + '\'' +
                ", metadata=" + metadata +
                ", user_metadata=" + user_metadata +
                '}';
    }
}
