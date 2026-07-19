package io.github.maskmasteruk.supabase.storage;

import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import io.github.maskmasteruk.supabase.storage.Callback.OnCompleteCallback;
import io.github.maskmasteruk.supabase.storage.Callback.OnGetBucket;
import io.github.maskmasteruk.supabase.storage.Callback.OnGetBuckets;
import io.github.maskmasteruk.supabase.storage.Enum.ObjectSortBy;
import io.github.maskmasteruk.supabase.storage.Object.Bucket;
import io.github.maskmasteruk.supabase.storage.Object.CreateBucket;
import io.github.maskmasteruk.supabase.storage.Object.StorageMetadata;
import io.github.maskmasteruk.supabase.storage.Object.SupabaseObject;
import io.github.maskmasteruk.supabase.storage.Object.SupabaseStorageReference;
import io.github.maskmasteruk.supabase.storage.Tasks.DownloadTask;
import io.github.maskmasteruk.supabase.storage.Tasks.Task;
import io.github.maskmasteruk.supabase.storage.Tasks.UpdateTask;
import io.github.maskmasteruk.supabase.storage.Tasks.UploadTask;

/**
 * The main entry point for the Supabase Storage library.
 * <p>
 * Purpose: This class provides a high-level API for interacting with Supabase Storage,
 * allowing users to manage buckets and objects (files).
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Managing Buckets (create, list, get, update, delete, empty).</li>
 *     <li>Managing Objects (upload, download, update, move, copy, delete, list).</li>
 *     <li>Providing access to resumable upload tasks.</li>
 * </ul>
 * </p>
 * <p>
 * Lifecycle: This is a singleton class. It should be initialized via {@link #getInstance(Context)}.
 * </p>
 * <p>
 * Thread Safety: This class is thread-safe. It uses a volatile instance and synchronized block for initialization.
 * Methods typically delegate work to internal services which execute on background threads.
 * </p>
 * <p>
 * Relationships:
 * <ul>
 *     <li>Uses {@link BucketService} for bucket-related operations.</li>
 *     <li>Uses {@link ObjectService} for object-related operations.</li>
 *     <li>Interacts with {@link SupabaseStorageReference} to identify objects within buckets.</li>
 * </ul>
 * </p>
 * <p>
 * Example Usage:
 * <pre>
 * SupabaseStorage storage = SupabaseStorage.getInstance(context);
 * storage.getBuckets(new OnGetBuckets() {
 *     &#64;Override
 *     public void onSuccess(ArrayList&lt;Bucket&gt; buckets) {
 *         // Handle success
 *     }
 *     &#64;Override
 *     public void onFailure(SupabaseError error) {
 *         // Handle failure
 *     }
 * });
 * </pre>
 * </p>
 */
public class SupabaseStorage {
    private static volatile SupabaseStorage instance;
    private final BucketService bucketService;
    private final ObjectService objectService;

    /**
     * Private constructor to enforce singleton pattern.
     *
     * @param context The Android application context.
     */
    private SupabaseStorage(Context context) {
        bucketService = BucketService.getInstance();
        objectService = ObjectService.getInstance(context);
    }

    /**
     * Returns the singleton instance of {@code SupabaseStorage}.
     *
     * @param context The Android context.
     * @return The singleton instance.
     */
    public static SupabaseStorage getInstance(Context context) {
        if (instance == null) {
            synchronized (SupabaseStorage.class) {
                if (instance == null) {
                    instance = new SupabaseStorage(context);
                }
            }
        }
        return instance;
    }

    /**
     * Retrieves all buckets.
     *
     * @param onGetBuckets Callback to receive the list of buckets or an error.
     *
     * <p>Example:
     * <pre>
     * storage.getBuckets(new OnGetBuckets() { ... });
     * </pre>
     * </p>
     */
    public void getBuckets(OnGetBuckets onGetBuckets) {
        bucketService.getBuckets(onGetBuckets);
    }

    /**
     * Retrieves a specific bucket by its ID.
     *
     * @param bucketId    The unique identifier of the bucket.
     * @param onGetBucket Callback to receive the bucket details or an error.
     *
     * <p>Example:
     * <pre>
     * storage.getBucket("my-bucket", new OnGetBucket() { ... });
     * </pre>
     * </p>
     */
    public void getBucket(String bucketId, OnGetBucket onGetBucket) {
        bucketService.getBucket(bucketId, onGetBucket);
    }

    /**
     * Creates a new bucket.
     *
     * @param createBucket       Object containing details for the new bucket.
     * @param onCompleteCallback Callback to be notified of completion or failure.
     *
     * <p>Example:
     * <pre>
     * CreateBucket createBucket = new CreateBucket("new-bucket").setPublic(true);
     * storage.createBucket(createBucket, new OnCompleteCallback() { ... });
     * </pre>
     * </p>
     */
    public void createBucket(CreateBucket createBucket, OnCompleteCallback onCompleteCallback) {
        bucketService.createBucket(createBucket, onCompleteCallback);
    }

    /**
     * Updates an existing bucket's configuration.
     * Note: Bucket ID and name cannot be updated.
     *
     * @param bucketId           The ID of the bucket to update.
     * @param bucket             The bucket object containing updated settings (e.g., public status).
     * @param onCompleteCallback Callback to be notified of completion or failure.
     *
     * <p>Example:
     * <pre>
     * Bucket updatedBucket = new Bucket();
     * updatedBucket.setPublic(false);
     * storage.updateBucket("my-bucket", updatedBucket, new OnCompleteCallback() { ... });
     * </pre>
     * </p>
     */
    public void updateBucket(String bucketId, Bucket bucket, OnCompleteCallback onCompleteCallback) {
        bucketService.updateBucket(bucketId, bucket, onCompleteCallback);
    }

    /**
     * Deletes a bucket by its ID.
     *
     * @param bucketId           The unique identifier of the bucket.
     * @param onCompleteCallback Callback to be notified of completion or failure.
     *
     * <p>Example:
     * <pre>
     * storage.deleteBucket("my-bucket", new OnCompleteCallback() { ... });
     * </pre>
     * </p>
     */
    public void deleteBucket(String bucketId, OnCompleteCallback onCompleteCallback) {
        bucketService.deleteBucket(bucketId, onCompleteCallback);
    }

    /**
     * Empties a bucket, deleting all objects within it.
     *
     * @param bucketId           The unique identifier of the bucket.
     * @param onCompleteCallback Callback to be notified of completion or failure.
     *
     * <p>Example:
     * <pre>
     * storage.emptyBucket("my-bucket", new OnCompleteCallback() { ... });
     * </pre>
     * </p>
     */
    public void emptyBucket(String bucketId, OnCompleteCallback onCompleteCallback) {
        bucketService.emptyBucket(bucketId, onCompleteCallback);
    }

    /**
     * Uploads a file to the specified storage reference.
     *
     * @param supabaseStorageReference The reference defining the destination path.
     * @param file                     The file to upload.
     * @return An {@link UploadTask.Task} to track the upload progress and result.
     *
     * <p>Example:
     * <pre>
     * File file = new File(context.getFilesDir(), "test.png");
     * SupabaseStorageReference ref = new SupabaseStorageReference("avatars").child("user1.png");
     * storage.uploadFile(ref, file).addOnProgressListener(snapshot -> { ... });
     * </pre>
     * </p>
     */
    public UploadTask.Task uploadFile(SupabaseStorageReference supabaseStorageReference, File file) {
        return objectService.upload(supabaseStorageReference, null, file);
    }

    /**
     * Uploads a file with metadata to the specified storage reference.
     *
     * @param supabaseStorageReference The reference defining the destination path.
     * @param storageMetadata          Metadata for the file (e.g., content-type).
     * @param file                     The file to upload.
     * @return An {@link UploadTask.Task} to track the upload.
     *
     * <p>Example:
     * <pre>
     * StorageMetadata metadata = new StorageMetadata().setContentType("image/png");
     * storage.uploadFile(ref, metadata, file).addOnSuccessListener(snapshot -> { ... });
     * </pre>
     * </p>
     */
    public UploadTask.Task uploadFile(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata, File file) {
        return objectService.upload(supabaseStorageReference, storageMetadata, file);
    }

    /**
     * Starts or resumes a resumable upload of a file.
     *
     * @param supabaseStorageReference The reference defining the destination path.
     * @param file                     The file to upload.
     * @return A {@link ResumableUploadTask.Task} to track the resumable upload.
     *
     * <p>Example:
     * <pre>
     * storage.uploadOrResumeFile(ref, file).addOnProgressListener(snapshot -> { ... });
     * </pre>
     * </p>
     */
    public ResumableUploadTask.Task uploadOrResumeFile(SupabaseStorageReference supabaseStorageReference, File file) {
        return objectService.uploadOrResume(supabaseStorageReference, null, file);
    }

    /**
     * Starts or resumes a resumable upload of a file with metadata.
     *
     * @param supabaseStorageReference The reference defining the destination path.
     * @param storageMetadata          Metadata for the file.
     * @param file                     The file to upload.
     * @return A {@link ResumableUploadTask.Task} to track the resumable upload.
     */
    public ResumableUploadTask.Task uploadOrResumeFile(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata, File file) {
        return objectService.uploadOrResume(supabaseStorageReference, storageMetadata, file);
    }

    /**
     * Uploads content from a URI to the specified storage reference.
     *
     * @param supabaseStorageReference The reference defining the destination path.
     * @param uri                      The URI of the content to upload.
     * @return An {@link UploadTask.Task} to track the upload.
     *
     * <p>Example:
     * <pre>
     * storage.uploadFromUri(ref, imageUri).addOnCompleteListener(snapshot -> { ... });
     * </pre>
     * </p>
     */
    public UploadTask.Task uploadFromUri(SupabaseStorageReference supabaseStorageReference, Uri uri) {
        return objectService.upload(supabaseStorageReference, null, uri);
    }

    /**
     * Uploads content from a URI with metadata to the specified storage reference.
     *
     * @param supabaseStorageReference The reference defining the destination path.
     * @param storageMetadata          Metadata for the content.
     * @param uri                      The URI of the content to upload.
     * @return An {@link UploadTask.Task} to track the upload.
     */
    public UploadTask.Task uploadFromUri(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata, Uri uri) {
        return objectService.upload(supabaseStorageReference, storageMetadata, uri);
    }

    /**
     * Starts or resumes a resumable upload from a URI.
     *
     * @param supabaseStorageReference The reference defining the destination path.
     * @param uri                      The URI of the content to upload.
     * @return A {@link ResumableUploadTask.Task} to track the resumable upload.
     */
    public ResumableUploadTask.Task uploadOrResumeFromUri(SupabaseStorageReference supabaseStorageReference, Uri uri) {
        return objectService.uploadOrResume(supabaseStorageReference, null, uri);
    }

    /**
     * Starts or resumes a resumable upload from a URI with metadata.
     *
     * @param supabaseStorageReference The reference defining the destination path.
     * @param storageMetadata          Metadata for the content.
     * @param uri                      The URI of the content to upload.
     * @return A {@link ResumableUploadTask.Task} to track the resumable upload.
     */
    public ResumableUploadTask.Task uploadOrResumeFromUri(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata, Uri uri) {
        return objectService.uploadOrResume(supabaseStorageReference, storageMetadata, uri);
    }

    /**
     * Uploads a byte array to the specified storage reference.
     *
     * @param supabaseStorageReference The reference defining the destination path.
     * @param byteArray                The byte array to upload.
     * @return An {@link UploadTask.Task} to track the upload.
     *
     * <p>Example:
     * <pre>
     * byte[] data = "Hello World".getBytes();
     * storage.uploadBytes(ref, data).addOnSuccessListener(snapshot -> { ... });
     * </pre>
     * </p>
     */
    public UploadTask.Task uploadBytes(SupabaseStorageReference supabaseStorageReference, byte[] byteArray) {
        return objectService.upload(supabaseStorageReference, null, byteArray);
    }

    /**
     * Uploads a byte array with metadata to the specified storage reference.
     *
     * @param supabaseStorageReference The reference defining the destination path.
     * @param storageMetadata          Metadata for the content.
     * @param byteArray                The byte array to upload.
     * @return An {@link UploadTask.Task} to track the upload.
     */
    public UploadTask.Task uploadBytes(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata, byte[] byteArray) {
        return objectService.upload(supabaseStorageReference, storageMetadata, byteArray);
    }

    /**
     * Starts or resumes a resumable upload from a byte array.
     *
     * @param supabaseStorageReference The reference defining the destination path.
     * @param byteArray                The byte array to upload.
     * @return A {@link ResumableUploadTask.Task} to track the resumable upload.
     */
    public ResumableUploadTask.Task uploadOrResumeBytes(SupabaseStorageReference supabaseStorageReference, byte[] byteArray) {
        return objectService.uploadOrResume(supabaseStorageReference, null, byteArray);
    }

    /**
     * Starts or resumes a resumable upload from a byte array with metadata.
     *
     * @param supabaseStorageReference The reference defining the destination path.
     * @param storageMetadata          Metadata for the content.
     * @param byteArray                The byte array to upload.
     * @return A {@link ResumableUploadTask.Task} to track the resumable upload.
     */
    public ResumableUploadTask.Task uploadOrResumeBytes(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata,  byte[] byteArray) {
        return objectService.uploadOrResume(supabaseStorageReference, storageMetadata, byteArray);
    }

    /**
     * Updates an existing file in storage with a new file.
     *
     * @param supabaseStorageReference The reference defining the path of the file to update.
     * @param file                     The new file content.
     * @return An {@link UpdateTask.Task} to track the update.
     *
     * <p>Example:
     * <pre>
     * storage.updateFile(ref, newFile).addOnCompleteListener(snapshot -> { ... });
     * </pre>
     * </p>
     */
    public UpdateTask.Task updateFile(SupabaseStorageReference supabaseStorageReference, File file) {
        return objectService.update(supabaseStorageReference, null, file);
    }

    /**
     * Updates an existing file with metadata.
     *
     * @param supabaseStorageReference The reference defining the path.
     * @param storageMetadata          Updated metadata.
     * @param file                     The new file content.
     * @return An {@link UpdateTask.Task} to track the update.
     */
    public UpdateTask.Task updateFile(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata, File file) {
        return objectService.update(supabaseStorageReference, storageMetadata, file);
    }

    /**
     * Updates an existing file from a URI.
     *
     * @param supabaseStorageReference The reference defining the path.
     * @param uri                      The new content URI.
     * @return An {@link UpdateTask.Task} to track the update.
     */
    public UpdateTask.Task updateFromUri(SupabaseStorageReference supabaseStorageReference, Uri uri) {
        return objectService.update(supabaseStorageReference, null, uri);
    }

    /**
     * Updates an existing file from a URI with metadata.
     *
     * @param supabaseStorageReference The reference defining the path.
     * @param storageMetadata          Updated metadata.
     * @param uri                      The new content URI.
     * @return An {@link UpdateTask.Task} to track the update.
     */
    public UpdateTask.Task updateFromUri(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata, Uri uri) {
        return objectService.update(supabaseStorageReference, storageMetadata, uri);
    }

    /**
     * Updates an existing file from a byte array.
     *
     * @param supabaseStorageReference The reference defining the path.
     * @param byteArray                The new content as byte array.
     * @return An {@link UpdateTask.Task} to track the update.
     */
    public UpdateTask.Task updateBytes(SupabaseStorageReference supabaseStorageReference, byte[] byteArray) {
        return objectService.update(supabaseStorageReference, null, byteArray);
    }

    /**
     * Updates an existing file from a byte array with metadata.
     *
     * @param supabaseStorageReference The reference defining the path.
     * @param storageMetadata          Updated metadata.
     * @param byteArray                The new content as byte array.
     * @return An {@link UpdateTask.Task} to track the update.
     */
    public UpdateTask.Task updateBytes(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata, byte[] byteArray) {
        return objectService.update(supabaseStorageReference, storageMetadata, byteArray);
    }

    /**
     * Downloads an object from storage to a file.
     *
     * @param supabaseStorageReference The reference defining the source path.
     * @param file                     The destination file.
     * @return A {@link DownloadTask.Task} to track the download.
     *
     * <p>Example:
     * <pre>
     * File destFile = new File(context.getCacheDir(), "downloaded.png");
     * storage.download(ref, destFile).addOnProgressListener(snapshot -> { ... });
     * </pre>
     * </p>
     */
    public DownloadTask.Task download(SupabaseStorageReference supabaseStorageReference, File file) {
        return objectService.download(supabaseStorageReference, file);
    }

    /**
     * Downloads an object from storage into a {@link ByteArrayOutputStream}.
     *
     * @param supabaseStorageReference The reference defining the source path.
     * @param byteArrayOutputStream    The stream to write the downloaded content into.
     * @return A {@link DownloadTask.Task} to track the download.
     */
    public DownloadTask.Task download(SupabaseStorageReference supabaseStorageReference, ByteArrayOutputStream byteArrayOutputStream) {
        return objectService.download(supabaseStorageReference, byteArrayOutputStream);
    }

    /**
     * Downloads a public object from storage to a file.
     *
     * @param supabaseStorageReference The reference defining the source path.
     * @param file                     The destination file.
     * @return A {@link DownloadTask.Task} to track the download.
     */
    public DownloadTask.Task downloadPublic(SupabaseStorageReference supabaseStorageReference, File file) {
        return objectService.downloadPublic(supabaseStorageReference, file);
    }

    /**
     * Downloads a public object from storage into a {@link ByteArrayOutputStream}.
     *
     * @param supabaseStorageReference The reference defining the source path.
     * @param byteArrayOutputStream    The stream to write the downloaded content into.
     * @return A {@link DownloadTask.Task} to track the download.
     */
    public DownloadTask.Task downloadPublic(SupabaseStorageReference supabaseStorageReference, ByteArrayOutputStream byteArrayOutputStream) {
        return objectService.downloadPublic(supabaseStorageReference, byteArrayOutputStream);
    }

    /**
     * Deletes multiple objects from a bucket.
     *
     * @param bucketId                  The ID of the bucket.
     * @param supabaseStorageReferences List of references to the objects to delete.
     * @return A {@link Task} containing the list of deleted {@link SupabaseObject}s.
     *
     * <p>Example:
     * <pre>
     * ArrayList&lt;SupabaseStorageReference&gt; refs = new ArrayList&lt;&gt;();
     * refs.add(new SupabaseStorageReference("bucket").child("img1.png"));
     * storage.deleteObjects("bucket", refs).onSuccess(objects -> { ... });
     * </pre>
     * </p>
     */
    public Task<ArrayList<SupabaseObject>> deleteObjects(String bucketId, ArrayList<SupabaseStorageReference> supabaseStorageReferences) {
        return objectService.deleteObjects(bucketId, supabaseStorageReferences);
    }

    /**
     * Moves an object from one path to another within the same bucket.
     *
     * @param bucketId     The ID of the bucket.
     * @param oldReference The current reference of the object.
     * @param newReference The new reference for the object.
     * @return A {@link Task} indicating completion or failure.
     *
     * <p>Example:
     * <pre>
     * storage.moveObject("bucket", oldRef, newRef).onSuccess(v -> { ... });
     * </pre>
     * </p>
     */
    public Task<Void> moveObject(String bucketId, SupabaseStorageReference oldReference, SupabaseStorageReference newReference) {
        return objectService.moveObject(bucketId, oldReference, newReference);
    }

    /**
     * Copies an object from one path to another within the same bucket.
     *
     * @param bucketId      The ID of the bucket.
     * @param fromReference The source reference.
     * @param toReference   The destination reference.
     * @return A {@link Task} containing the metadata of the copied {@link SupabaseObject}.
     *
     * <p>Example:
     * <pre>
     * storage.copyObject("bucket", fromRef, toRef).onSuccess(obj -> { ... });
     * </pre>
     * </p>
     */
    public Task<SupabaseObject> copyObject(String bucketId, SupabaseStorageReference fromReference, SupabaseStorageReference toReference) {
        return objectService.copyObject(bucketId, fromReference, toReference);
    }

    /**
     * Lists objects within a specified reference path with sorting and pagination.
     *
     * @param supabaseStorageReference The reference path to list objects from.
     * @param limit                    Maximum number of objects to return.
     * @param offset                   Number of objects to skip.
     * @param objectSortBy             Column to sort by.
     * @param ascending                Whether to sort in ascending order.
     * @return A {@link Task} containing the list of {@link SupabaseObject}s.
     *
     * <p>Example:
     * <pre>
     * storage.list(ref, 10, 0, ObjectSortBy.NAME, true).onSuccess(list -> { ... });
     * </pre>
     * </p>
     */
    public Task<ArrayList<SupabaseObject>> list(SupabaseStorageReference supabaseStorageReference, int limit, int offset, ObjectSortBy objectSortBy, boolean ascending) {
        return objectService.list(supabaseStorageReference, limit, offset, objectSortBy, ascending);
    }

    /**
     * Lists objects within a specified reference path with pagination.
     *
     * @param supabaseStorageReference The reference path.
     * @param limit                    Maximum number of objects.
     * @param offset                   Number of objects to skip.
     * @return A {@link Task} containing the list of {@link SupabaseObject}s.
     */
    public Task<ArrayList<SupabaseObject>> list(SupabaseStorageReference supabaseStorageReference, int limit, int offset) {
        return objectService.list(supabaseStorageReference, limit, offset, null, null);
    }

    /**
     * Lists all objects within a specified reference path.
     *
     * @param supabaseStorageReference The reference path.
     * @return A {@link Task} containing the list of {@link SupabaseObject}s.
     */
    public Task<ArrayList<SupabaseObject>> list(SupabaseStorageReference supabaseStorageReference) {
        return objectService.list(supabaseStorageReference, null, null, null, null);
    }


}
