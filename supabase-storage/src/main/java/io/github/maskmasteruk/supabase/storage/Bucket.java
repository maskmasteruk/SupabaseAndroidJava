package io.github.maskmasteruk.supabase.storage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import io.github.maskmasteruk.supabase.storage.Enum.BucketType;

/**
 * Represents a Supabase Storage bucket.
 * <p>
 * Purpose: This class is a data model that holds information about a storage bucket,
 * such as its ID, name, public status, and restrictions.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Storing bucket properties.</li>
 *     <li>Parsing bucket data from a {@link JSONObject}.</li>
 *     <li>Serializing bucket data to a {@link JSONObject} for API requests.</li>
 * </ul>
 * </p>
 * <p>
 * Usage Overview:
 * Instances are typically returned by {@code BucketService} when listing or getting buckets.
 * They can also be created and populated manually to update a bucket's settings.
 * </p>
 * <p>
 * Example Usage:
 * <pre>
 * Bucket bucket = new Bucket();
 * bucket.setPublic(true);
 * bucket.setFileSizeLimit(1048576L); // 1MB
 * </pre>
 * </p>
 */
public class Bucket {

    private String id;
    private String name;
    private String owner;

    private Boolean isPublic;

    private BucketType type;
    private Long fileSizeLimit;

    private ArrayList<String> allowedMimeTypes;

    private Date createdAt;

    private Date updatedAt;

    private boolean removeFileSizeLimit = false;
    private boolean removeAllowedMimeTypes = false;

    /**
     * Default constructor for creating an empty Bucket object.
     */
    public Bucket() {
    }

    /**
     * Constructs a Bucket with all fields.
     *
     * @param id               Unique identifier of the bucket.
     * @param name             Name of the bucket.
     * @param owner            Owner ID.
     * @param isPublic         Whether the bucket is public.
     * @param type             Type of the bucket (e.g., generic).
     * @param fileSizeLimit    Maximum file size allowed in bytes.
     * @param allowedMimeTypes List of allowed MIME types.
     * @param createdAt        Timestamp of creation.
     * @param updatedAt        Timestamp of last update.
     */
    public Bucket(String id, String name, String owner, Boolean isPublic, BucketType type, Long fileSizeLimit, ArrayList<String> allowedMimeTypes, Date createdAt, Date updatedAt) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.isPublic = isPublic;
        this.type = type;
        this.fileSizeLimit = fileSizeLimit;
        this.allowedMimeTypes = allowedMimeTypes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Constructs a Bucket by parsing a {@link JSONObject}.
     *
     * @param jsonObject The JSON object containing bucket data.
     */
    public Bucket(JSONObject jsonObject) {
        if (jsonObject == null) {
            return;
        }

        id = jsonObject.optString("id");
        name = jsonObject.optString("name");
        owner = jsonObject.optString("owner");
        isPublic = jsonObject.optBoolean("public");
        if (jsonObject.has("type")) {
            type = BucketType.fromValue(jsonObject.optString("type"));
        } else {
            type = BucketType.STANDARD;
        }


        if (!jsonObject.isNull("file_size_limit")) {
            fileSizeLimit = jsonObject.optLong("file_size_limit");
        }

        if (!jsonObject.isNull("allowed_mime_types")) {
            JSONArray array = jsonObject.optJSONArray("allowed_mime_types");
            if (array != null) {
                allowedMimeTypes = new ArrayList<>();
                for (int i = 0; i < array.length(); i++) {
                    allowedMimeTypes.add(array.optString(i));
                }
            }
        }

        SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.US);

        try {
            createdAt = jsonObject.isNull("created_at")
                    ? null
                    : sdf.parse(jsonObject.getString("created_at"));

            updatedAt = jsonObject.isNull("updated_at")
                    ? null
                    : sdf.parse(jsonObject.getString("updated_at"));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    /** @return The unique identifier of the bucket. */
    public String getId() {
        return id;
    }

    /** @param id The ID to set. @return This instance for chaining. */
    public Bucket setId(String id) {
        this.id = id;
        return this;
    }

    /** @return The name of the bucket. */
    public String getName() {
        return name;
    }

    /** @param name The name to set. @return This instance for chaining. */
    public Bucket setName(String name) {
        this.name = name;
        return this;
    }

    /** @return The owner identifier of the bucket. */
    public String getOwner() {
        return owner;
    }

    /** @param owner The owner ID to set. @return This instance for chaining. */
    public Bucket setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    /** @return Whether the bucket is public. */
    public Boolean isPublic() {
        return isPublic;
    }

    /** @param aPublic Public status to set. @return This instance for chaining. */
    public Bucket setPublic(Boolean aPublic) {
        isPublic = aPublic;
        return this;
    }

    /** @return The type of the bucket. */
    public BucketType getType() {
        return type;
    }

    /** @param type The type to set. @return This instance for chaining. */
    public Bucket setType(BucketType type) {
        this.type = type;
        return this;
    }

    /** @return The file size limit in bytes. */
    public Long getFileSizeLimit() {
        return fileSizeLimit;
    }

    /** @param fileSizeLimit Limit in bytes. @return This instance for chaining. */
    public Bucket setFileSizeLimit(Long fileSizeLimit) {
        this.fileSizeLimit = fileSizeLimit;
        return this;
    }

    /** @return List of allowed MIME types. */
    public List<String> getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    /** @param allowedMimeTypes List of MIME types. @return This instance for chaining. */
    public Bucket setAllowedMimeTypes(ArrayList<String> allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
        return this;
    }

    /** @return Timestamp when the bucket was created. */
    public Date getCreatedAt() {
        return createdAt;
    }

    /** @param createdAt Creation date. @return This instance for chaining. */
    public Bucket setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /** @return Timestamp when the bucket was last updated. */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /** @param updatedAt Update date. @return This instance for chaining. */
    public Bucket setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    /**
     * Marks the file size limit for removal in the next update.
     *
     * @return This instance for chaining.
     */
    public Bucket removeFileSizeLimit() {
        this.removeFileSizeLimit = true;
        return this;
    }

    /**
     * Marks the allowed MIME types for removal in the next update.
     *
     * @return This instance for chaining.
     */
    public Bucket removeAllowedMimeTypes() {
        this.removeAllowedMimeTypes = true;
        return this;
    }

    /**
     * Serializes the bucket data into a {@link JSONObject} for API requests.
     *
     * @return A JSON representation of the bucket.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            if (name != null) {
                json.put("name", name);
            }

            if (owner != null) {
                json.put("owner", owner);
            }

            if (isPublic != null) {
                json.put("public", isPublic);
            }
            if (type != null) {
                json.put("type", type.toString());
            }

            if (fileSizeLimit != null) {
                json.put("file_size_limit", fileSizeLimit + "B");
            }

            if (allowedMimeTypes != null) {
                json.put("allowed_mime_types", new JSONArray(allowedMimeTypes));
            }

            SimpleDateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            ISO_8601.setTimeZone(TimeZone.getTimeZone("UTC"));

            if (createdAt != null) {
                json.put("created_at", ISO_8601.format(createdAt));
            }

            if (updatedAt != null) {
                json.put("updated_at", ISO_8601.format(updatedAt));
            }

            if (removeFileSizeLimit) {
                json.put("file_size_limit", JSONObject.NULL);
            }

            if (removeAllowedMimeTypes) {
                json.put("allowed_mime_types", JSONObject.NULL);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return json;
    }

    @Override
    public String toString() {
        return "Bucket{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", owner='" + owner + '\'' +
                ", isPublic=" + isPublic +
                ", type='" + type + '\'' +
                ", fileSizeLimit=" + fileSizeLimit +
                ", removeFileSizeLimit=" + removeFileSizeLimit +
                ", allowedMimeTypes=" + allowedMimeTypes +
                ", removeAllowedMimeTypes=" + removeAllowedMimeTypes +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
