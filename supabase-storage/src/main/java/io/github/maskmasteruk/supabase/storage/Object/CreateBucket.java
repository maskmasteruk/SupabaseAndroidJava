package io.github.maskmasteruk.supabase.storage.Object;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.github.maskmasteruk.supabase.storage.Enum.BucketType;

/**
 * Request object for creating a new Supabase Storage bucket.
 * <p>
 * Purpose: This class encapsulates the configuration required to create a new bucket in Supabase Storage.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Holding parameters for bucket creation (id, name, public status, limits).</li>
 *     <li>Providing a JSON representation for the API request.</li>
 * </ul>
 * </p>
 * <p>
 * Example Usage:
 * <pre>
 * CreateBucket createRequest = new CreateBucket("my-new-bucket")
 *     .setPublic(true)
 *     .setFileSizeLimit(5242880L); // 5MB
 * </pre>
 * </p>
 */
public class CreateBucket {

    private String id;
    private String name;

    private Boolean isPublic;

    private Long fileSizeLimit;

    private ArrayList<String> allowedMimeTypes;
    private BucketType type;

    /**
     * Default constructor for creating an empty request object.
     */
    public CreateBucket() {
    }

    /**
     * Constructs a request with a bucket name.
     *
     * @param name The name of the bucket to create.
     */
    public CreateBucket(String name) {
        this.name = name;
    }

    /**
     * Constructs a request with name, public status, file size limit, and allowed MIME types.
     *
     * @param name             The name of the bucket.
     * @param isPublic         Whether the bucket should be public.
     * @param fileSizeLimit    Maximum file size in bytes.
     * @param allowedMimeTypes List of allowed MIME types.
     */
    public CreateBucket(String name, Boolean isPublic, Long fileSizeLimit, ArrayList<String> allowedMimeTypes) {
        this.name = name;
        this.isPublic = isPublic;
        this.fileSizeLimit = fileSizeLimit;
        this.allowedMimeTypes = allowedMimeTypes;
    }

    /**
     * Constructs a request with id, name, public status, file size limit, and allowed MIME types.
     *
     * @param id               The unique ID for the bucket.
     * @param name             The name of the bucket.
     * @param isPublic         Whether the bucket should be public.
     * @param fileSizeLimit    Maximum file size in bytes.
     * @param allowedMimeTypes List of allowed MIME types.
     */
    public CreateBucket(String id, String name, Boolean isPublic, Long fileSizeLimit, ArrayList<String> allowedMimeTypes) {
        this.id = id;
        this.name = name;
        this.isPublic = isPublic;
        this.fileSizeLimit = fileSizeLimit;
        this.allowedMimeTypes = allowedMimeTypes;
    }

    /**
     * Constructs a request with name, public status, limits, and bucket type.
     *
     * @param name             The name.
     * @param isPublic         Public status.
     * @param fileSizeLimit    Limit in bytes.
     * @param allowedMimeTypes Allowed types.
     * @param type             Bucket type.
     */
    public CreateBucket(String name, Boolean isPublic, Long fileSizeLimit, ArrayList<String> allowedMimeTypes, BucketType type) {
        this.name = name;
        this.isPublic = isPublic;
        this.fileSizeLimit = fileSizeLimit;
        this.allowedMimeTypes = allowedMimeTypes;
        this.type = type;
    }

    /**
     * Constructs a request with ID, name, public status, limits, and bucket type.
     *
     * @param id               Unique ID.
     * @param name             The name.
     * @param isPublic         Public status.
     * @param fileSizeLimit    Limit in bytes.
     * @param allowedMimeTypes Allowed types.
     * @param type             Bucket type.
     */
    public CreateBucket(String id, String name, Boolean isPublic, Long fileSizeLimit, ArrayList<String> allowedMimeTypes, BucketType type) {
        this.id = id;
        this.name = name;
        this.isPublic = isPublic;
        this.fileSizeLimit = fileSizeLimit;
        this.allowedMimeTypes = allowedMimeTypes;
        this.type = type;
    }


    /** @return The bucket ID. */
    public String getId() {
        return id;
    }

    /** @param id The ID to set. @return This instance. */
    public CreateBucket setId(String id) {
        this.id = id;
        return this;
    }

    /** @return The bucket name. */
    public String getName() {
        return name;
    }

    /** @param name The name to set. @return This instance. */
    public CreateBucket setName(String name) {
        this.name = name;
        return this;
    }

    /** @return Whether the bucket will be public. */
    public Boolean getPublic() {
        return isPublic;
    }

    /** @param aPublic Public status to set. @return This instance. */
    public CreateBucket setPublic(Boolean aPublic) {
        isPublic = aPublic;
        return this;
    }

    /** @return The file size limit in bytes. */
    public Long getFileSizeLimit() {
        return fileSizeLimit;
    }

    /** @param fileSizeLimit Limit in bytes. @return This instance. */
    public CreateBucket setFileSizeLimit(Long fileSizeLimit) {
        this.fileSizeLimit = fileSizeLimit;
        return this;
    }

    /** @return List of allowed MIME types. */
    public List<String> getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    /** @param allowedMimeTypes List of MIME types. @return This instance. */
    public CreateBucket setAllowedMimeTypes(ArrayList<String> allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
        return this;
    }

    /** @return The bucket type. */
    public BucketType getType() {
        return type;
    }

    /** @param type The type to set. @return This instance. */
    public CreateBucket setType(BucketType type) {
        this.type = type;
        return this;
    }

    /**
     * Serializes the creation request data into a {@link JSONObject}.
     *
     * @return A JSON representation for the create bucket request.
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();

        try {
            if (id == null && name != null) {
                id = name;
            } else if (id != null && name == null) {
                name = id;
            }

            if (id != null) {
                json.put("id", id);
            }

            if (name != null) {
                json.put("name", name);
            }

            if (isPublic != null) {
                json.put("public", isPublic);
            }

            if (type != null) {
                json.put("type", type.getValue());
            }

            if (fileSizeLimit != null) {
                json.put("file_size_limit", fileSizeLimit);
            }

            if (allowedMimeTypes != null) {
                json.put("allowed_mime_types", new JSONArray(allowedMimeTypes));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        return json;
    }

    @Override
    public String toString() {
        return "CreateBucket{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", isPublic=" + isPublic +
                ", fileSizeLimit=" + fileSizeLimit +
                ", allowedMimeTypes=" + allowedMimeTypes +
                '}';
    }
}
