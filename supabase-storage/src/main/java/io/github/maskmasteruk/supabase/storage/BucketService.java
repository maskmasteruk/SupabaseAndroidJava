package io.github.maskmasteruk.supabase.storage;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.core.Runnables;
import io.github.maskmasteruk.supabase.storage.Callback.OnCompleteCallback;
import io.github.maskmasteruk.supabase.storage.Callback.OnGetBucket;
import io.github.maskmasteruk.supabase.storage.Callback.OnGetBuckets;

/**
 * Service class for handling bucket-related operations in Supabase Storage.
 * <p>
 * Purpose: This class manages the interaction with the Supabase Storage API for bucket management,
 * including listing, creating, updating, and deleting buckets.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Formulating HTTP requests for bucket operations.</li>
 *     <li>Parsing JSON responses into {@link Bucket} objects.</li>
 *     <li>Handling network execution on background threads.</li>
 *     <li>Notifying callbacks of success or failure.</li>
 * </ul>
 * </p>
 * <p>
 * Thread Safety: This class is thread-safe as it follows the singleton pattern and all network
 * operations are offloaded to background threads.
 * </p>
 */
public class BucketService {
    private static volatile BucketService instance;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private BucketService() {
    }

    /**
     * Returns the singleton instance of {@code BucketService}.
     *
     * @return The singleton instance.
     */
    public static BucketService getInstance() {
        if (instance == null) {
            synchronized (BucketService.class) {
                if (instance == null) {
                    instance = new BucketService();
                }
            }
        }
        return instance;
    }

    /**
     * Retrieves a list of all buckets from the storage.
     *
     * @param onGetBuckets Callback to receive the list of {@link Bucket}s.
     */
    public void getBuckets(OnGetBuckets onGetBuckets) {
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.BUCKET);

            Response response = new RequestHandler().get(baseStorageUrlBuilder.build());
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                ArrayList<Bucket> buckets = new ArrayList<>();
                JSONArray responseJSONArray = response.getResponseJSONArray();
                for (int i = 0; i < responseJSONArray.length(); i++) {
                    try {
                        buckets.add(new Bucket(responseJSONArray.getJSONObject(i)));
                    } catch (JSONException e) {
                        onGetBuckets.onFailure(new SupabaseError(e));
                    }
                }
                onGetBuckets.onSuccess(buckets);
            } else {
                Helper.generateError(response, onGetBuckets);
            }
        });
    }

    /**
     * Retrieves details for a specific bucket.
     *
     * @param bucketId    The ID of the bucket to retrieve.
     * @param onGetBucket Callback to receive the {@link Bucket} object.
     */
    public void getBucket(String bucketId, OnGetBucket onGetBucket) {
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.BUCKET);
            baseStorageUrlBuilder.appendPath(bucketId);

            Response response = new RequestHandler().get(baseStorageUrlBuilder.build());
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                onGetBucket.onSuccess(new Bucket(response.getResponseJSON()));
            } else {
                Helper.generateError(response, onGetBucket);
            }
        });
    }

    /**
     * Creates a new bucket.
     *
     * @param createBucket       The configuration for the new bucket.
     * @param onCompleteCallback Callback to be notified of the result.
     */
    public void createBucket(CreateBucket createBucket, OnCompleteCallback onCompleteCallback) {
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.BUCKET);

            Response response = new RequestHandler().post(baseStorageUrlBuilder.build(), createBucket.toJson().toString());
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                onCompleteCallback.onSuccess();
            } else {
                Helper.generateError(response, onCompleteCallback);
            }
        });
    }

    /**
     * Updates an existing bucket.
     *
     * @param bucketId           The ID of the bucket to update.
     * @param bucket             The bucket object with updated values.
     * @param onCompleteCallback Callback to be notified of the result.
     */
    public void updateBucket(String bucketId, Bucket bucket, OnCompleteCallback onCompleteCallback) {
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.BUCKET);
            baseStorageUrlBuilder.appendPath(bucketId);

            Response response = new RequestHandler().put(baseStorageUrlBuilder.build(), bucket.toJson().toString());
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                onCompleteCallback.onSuccess();
            } else {
                Helper.generateError(response, onCompleteCallback);
            }
        });
    }

    /**
     * Deletes a bucket.
     *
     * @param bucketId           The ID of the bucket to delete.
     * @param onCompleteCallback Callback to be notified of the result.
     */
    public void deleteBucket(String bucketId, OnCompleteCallback onCompleteCallback) {
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.BUCKET);
            baseStorageUrlBuilder.appendPath(bucketId);

            Response response = new RequestHandler().delete(baseStorageUrlBuilder.build(), null);
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                onCompleteCallback.onSuccess();
            } else {
                Helper.generateError(response, onCompleteCallback);
            }
        });
    }

    /**
     * Deletes all objects within a bucket, effectively emptying it.
     *
     * @param bucketId           The ID of the bucket to empty.
     * @param onCompleteCallback Callback to be notified of the result.
     */
    public void emptyBucket(String bucketId, OnCompleteCallback onCompleteCallback) {
        Runnables.getExecutorService().execute(() -> {
            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.BUCKET);
            baseStorageUrlBuilder.appendPath(bucketId);
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.EMPTY);

            Response response = new RequestHandler().post(baseStorageUrlBuilder.build(), null);
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                onCompleteCallback.onSuccess();
            } else {
                Helper.generateError(response, onCompleteCallback);
            }
        });
    }

}
