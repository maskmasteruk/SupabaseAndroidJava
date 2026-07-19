package io.github.maskmasteruk.supabase.storage;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.core.Runnables;
import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;
import io.github.maskmasteruk.supabase.storage.Enum.ObjectSortBy;
import io.github.maskmasteruk.supabase.storage.Object.StorageMetadata;
import io.github.maskmasteruk.supabase.storage.Object.SupabaseObject;
import io.github.maskmasteruk.supabase.storage.Object.SupabaseStorageReference;
import io.github.maskmasteruk.supabase.storage.Tasks.DownloadTask;
import io.github.maskmasteruk.supabase.storage.Tasks.Task;
import io.github.maskmasteruk.supabase.storage.Tasks.UpdateTask;
import io.github.maskmasteruk.supabase.storage.Tasks.UploadTask;

/**
 * Service class for handling object-related operations in Supabase Storage.
 * <p>
 * Purpose: This class manages the interaction with the Supabase Storage API for object (file) management,
 * including uploading, downloading, moving, copying, and listing objects.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Managing {@link UploadTask}, {@link DownloadTask}, and {@link ResumableUploadTask} instances.</li>
 *     <li>Enqueuing tasks into {@link StorageTaskManager}.</li>
 *     <li>Handling various input types for uploads (File, Uri, byte[]).</li>
 *     <li>Implementing object manipulation logic (move, copy, delete, list).</li>
 *     <li>Generating appropriate URLs for storage endpoints.</li>
 * </ul>
 * </p>
 * <p>
 * Thread Safety: This class is thread-safe. All major operations are executed on background threads
 * using {@link Runnables#getExecutorService()}.
 * </p>
 */
public class ObjectService {
    private static volatile ObjectService instance;
    private final StorageTaskManager storageTaskManager;
    private final Context context;

    /**
     * Private constructor for singleton pattern.
     *
     * @param context The application context.
     */
    private ObjectService(Context context) {
        storageTaskManager = StorageTaskManager.getInstance();
        this.context = context.getApplicationContext();
    }

    /**
     * Returns the singleton instance of {@code ObjectService}.
     *
     * @param context The Android context.
     * @return The singleton instance.
     */
    public static ObjectService getInstance(Context context) {
        if (instance == null) {
            synchronized (ObjectService.class) {
                if (instance == null) {
                    instance = new ObjectService(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    /**
     * Initiates or resumes a resumable upload for a {@link File}.
     *
     * @param supabaseStorageReference Path reference in storage.
     * @param storageMetadata          Metadata for the object.
     * @param file                     The file to upload.
     * @return A {@link ResumableUploadTask.Task} to track the process.
     */
    public ResumableUploadTask.Task uploadOrResume(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata, File file) {
        ResumableUploadTask.Task task = new ResumableUploadTask.Task();
        Runnables.getExecutorService().execute(() -> {
            if (supabaseStorageReference.getBucketId() == null) {
                task.onError(new SupabaseError("Bucket ID is required but was null."));
                return;
            }
            if (file == null) {
                task.onError(new SupabaseError("A valid file must be provided."));
                return;
            }
            if (!file.exists()) {
                task.onError(new SupabaseError("The specified file does not exist: " + file.getAbsolutePath()));
                return;
            }
            StorageMetadata storageMetadata1 = getStorageMetadata(storageMetadata, file);

            ResumableUploadTask resumableUploadTask = new ResumableUploadTask(supabaseStorageReference, context, file, storageMetadata1, task);
            storageTaskManager.enqueue(resumableUploadTask);
        });
        return task;
    }

    /**
     * Initiates or resumes a resumable upload for a {@link Uri}.
     *
     * @param supabaseStorageReference Path reference in storage.
     * @param storageMetadata          Metadata for the object.
     * @param uri                      The URI of the content to upload.
     * @return A {@link ResumableUploadTask.Task} to track the process.
     */
    public ResumableUploadTask.Task uploadOrResume(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata, Uri uri) {
        ResumableUploadTask.Task task = new ResumableUploadTask.Task();
        Runnables.getExecutorService().execute(() -> {
            if (supabaseStorageReference.getBucketId() == null) {
                task.onError(new SupabaseError("Bucket ID is required but was null."));
                return;
            }
            if (uri == null) {
                task.onError(new SupabaseError("A valid URI must be provided."));
                return;
            }

            StorageMetadata storageMetadata1 = getStorageMetadata(storageMetadata, uri);
            try {
                long fileSizeFromUri = Helper.getFileSizeFromUri(context, uri);
                ResumableUploadTask resumableUploadTask = new ResumableUploadTask(supabaseStorageReference, context, context.getContentResolver().openInputStream(uri), fileSizeFromUri, storageMetadata1, task);
                storageTaskManager.enqueue(resumableUploadTask);
            } catch (FileNotFoundException e) {
                task.onError(new SupabaseError(e));
                return;
            }
        });
        return task;
    }

    /**
     * Initiates or resumes a resumable upload for a byte array.
     *
     * @param supabaseStorageReference Path reference in storage.
     * @param storageMetadata          Metadata for the object.
     * @param byteArray                The data to upload.
     * @return A {@link ResumableUploadTask.Task} to track the process.
     */
    public ResumableUploadTask.Task uploadOrResume(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata, byte[] byteArray) {
        ResumableUploadTask.Task task = new ResumableUploadTask.Task();
        Runnables.getExecutorService().execute(() -> {
            if (supabaseStorageReference.getBucketId() == null) {
                task.onError(new SupabaseError("Bucket ID is required but was null."));
                return;
            }
            if (byteArray == null || byteArray.length == 0) {
                task.onError(new SupabaseError("byteArray must not be null or empty."));
                return;
            }

            StorageMetadata storageMetadata1 = getStorageMetadata(storageMetadata, byteArray);

            ResumableUploadTask resumableUploadTask = new ResumableUploadTask(supabaseStorageReference, context, byteArray, storageMetadata1, task);
            storageTaskManager.enqueue(resumableUploadTask);
        });
        return task;
    }

    /**
     * Uploads a {@link File} using a standard (non-resumable) upload task.
     *
     * @param supabaseStorageReference Path reference in storage.
     * @param storageMetadata          Metadata for the object.
     * @param file                     The file to upload.
     * @return An {@link UploadTask.Task} to track the upload.
     */
    public UploadTask.Task upload(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata, File file) {
        UploadTask.Task task = new UploadTask.Task();
        Runnables.getExecutorService().execute(() -> {
            if (supabaseStorageReference.getBucketId() == null) {
                task.onError(new SupabaseError("Bucket ID is required but was null."));
                return;
            }
            if (file == null) {
                task.onError(new SupabaseError("A valid file must be provided."));
                return;
            }
            if (!file.exists()) {
                task.onError(new SupabaseError("The specified file does not exist: " + file.getAbsolutePath()));
                return;
            }
            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.OBJECT);
            baseStorageUrlBuilder.appendPath(supabaseStorageReference.getBucketId());
            supabaseStorageReference.getPaths().forEach(baseStorageUrlBuilder::appendPath);

            StorageMetadata storageMetadata1 = getStorageMetadata(storageMetadata, file);

            UploadTask uploadTask = new UploadTask(baseStorageUrlBuilder.build(), file, storageMetadata1, task);
            storageTaskManager.enqueue(uploadTask);
        });
        return task;
    }

    /**
     * Uploads content from a {@link Uri} using a standard upload task.
     *
     * @param supabaseStorageReference Path reference in storage.
     * @param storageMetadata          Metadata for the object.
     * @param uri                      The URI to upload from.
     * @return An {@link UploadTask.Task} to track the upload.
     */
    public UploadTask.Task upload(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata, Uri uri) {
        UploadTask.Task task = new UploadTask.Task();
        Runnables.getExecutorService().execute(() -> {
            if (supabaseStorageReference.getBucketId() == null) {
                task.onError(new SupabaseError("Bucket ID is required but was null."));
                return;
            }
            if (uri == null) {
                task.onError(new SupabaseError("A valid URI must be provided."));
                return;
            }
            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.OBJECT);
            baseStorageUrlBuilder.appendPath(supabaseStorageReference.getBucketId());
            supabaseStorageReference.getPaths().forEach(baseStorageUrlBuilder::appendPath);

            StorageMetadata storageMetadata1 = getStorageMetadata(storageMetadata, uri);


            try {
                long fileSizeFromUri = Helper.getFileSizeFromUri(context, uri);
                UploadTask uploadTask = new UploadTask(baseStorageUrlBuilder.build(), context.getContentResolver().openInputStream(uri), fileSizeFromUri, storageMetadata1, task);
                storageTaskManager.enqueue(uploadTask);
            } catch (FileNotFoundException e) {
                task.onError(new SupabaseError(e));
                return;
            }
        });
        return task;
    }

    /**
     * Uploads a byte array using a standard upload task.
     *
     * @param supabaseStorageReference Path reference in storage.
     * @param storageMetadata          Metadata for the object.
     * @param byteArray                The data to upload.
     * @return An {@link UploadTask.Task} to track the upload.
     */
    public UploadTask.Task upload(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata, byte[] byteArray) {
        UploadTask.Task task = new UploadTask.Task();
        Runnables.getExecutorService().execute(() -> {
            if (supabaseStorageReference.getBucketId() == null) {
                task.onError(new SupabaseError("Bucket ID is required but was null."));
                return;
            }
            if (byteArray == null || byteArray.length == 0) {
                task.onError(new SupabaseError("byteArray must not be null or empty."));
                return;
            }

            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.OBJECT);
            baseStorageUrlBuilder.appendPath(supabaseStorageReference.getBucketId());
            supabaseStorageReference.getPaths().forEach(baseStorageUrlBuilder::appendPath);

            StorageMetadata storageMetadata1 = getStorageMetadata(storageMetadata, byteArray);

            UploadTask uploadTask = new UploadTask(baseStorageUrlBuilder.build(), byteArray, storageMetadata1, task);
            storageTaskManager.enqueue(uploadTask);
        });
        return task;
    }

    /**
     * Updates an existing object with a new {@link File}.
     *
     * @param supabaseStorageReference Path reference in storage.
     * @param storageMetadata          Metadata for the object.
     * @param file                     The new file content.
     * @return An {@link UpdateTask.Task} to track the update.
     */
    public UpdateTask.Task update(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata, File file) {
        UpdateTask.Task task = new UpdateTask.Task();
        Runnables.getExecutorService().execute(() -> {
            if (supabaseStorageReference.getBucketId() == null) {
                task.onError(new SupabaseError("Bucket ID is required but was null."));
                return;
            }
            if (file == null) {
                task.onError(new SupabaseError("A valid file must be provided."));
                return;
            }
            if (!file.exists()) {
                task.onError(new SupabaseError("The specified file does not exist: " + file.getAbsolutePath()));
                return;
            }
            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.OBJECT);
            baseStorageUrlBuilder.appendPath(supabaseStorageReference.getBucketId());
            supabaseStorageReference.getPaths().forEach(baseStorageUrlBuilder::appendPath);

            StorageMetadata storageMetadata1 = getStorageMetadata(storageMetadata, file);

            UpdateTask updateTask = new UpdateTask(baseStorageUrlBuilder.build(), file, storageMetadata1, task);
            storageTaskManager.enqueue(updateTask);
        });
        return task;
    }

    /**
     * Updates an existing object with content from a {@link Uri}.
     *
     * @param supabaseStorageReference Path reference in storage.
     * @param storageMetadata          Metadata for the object.
     * @param uri                      The new content URI.
     * @return An {@link UpdateTask.Task} to track the update.
     */
    public UpdateTask.Task update(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata, Uri uri) {
        UpdateTask.Task task = new UpdateTask.Task();
        Runnables.getExecutorService().execute(() -> {
            if (supabaseStorageReference.getBucketId() == null) {
                task.onError(new SupabaseError("Bucket ID is required but was null."));
                return;
            }
            if (uri == null) {
                task.onError(new SupabaseError("A valid URI must be provided."));
                return;
            }
            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.OBJECT);
            baseStorageUrlBuilder.appendPath(supabaseStorageReference.getBucketId());
            supabaseStorageReference.getPaths().forEach(baseStorageUrlBuilder::appendPath);

            StorageMetadata storageMetadata1 = getStorageMetadata(storageMetadata, uri);


            try {
                long fileSizeFromUri = Helper.getFileSizeFromUri(context, uri);
                UpdateTask updateTask = new UpdateTask(baseStorageUrlBuilder.build(), context.getContentResolver().openInputStream(uri), fileSizeFromUri, storageMetadata1, task);
                storageTaskManager.enqueue(updateTask);
            } catch (FileNotFoundException e) {
                task.onError(new SupabaseError(e));
                return;
            }
        });
        return task;
    }

    /**
     * Updates an existing object with a byte array.
     *
     * @param supabaseStorageReference Path reference in storage.
     * @param storageMetadata          Metadata for the object.
     * @param byteArray                The new data.
     * @return An {@link UpdateTask.Task} to track the update.
     */
    public UpdateTask.Task update(SupabaseStorageReference supabaseStorageReference, StorageMetadata storageMetadata, byte[] byteArray) {
        UpdateTask.Task task = new UpdateTask.Task();
        Runnables.getExecutorService().execute(() -> {
            if (supabaseStorageReference.getBucketId() == null) {
                task.onError(new SupabaseError("Bucket ID is required but was null."));
                return;
            }
            if (byteArray == null || byteArray.length == 0) {
                task.onError(new SupabaseError("byteArray must not be null or empty."));
                return;
            }

            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.OBJECT);
            baseStorageUrlBuilder.appendPath(supabaseStorageReference.getBucketId());
            supabaseStorageReference.getPaths().forEach(baseStorageUrlBuilder::appendPath);

            StorageMetadata storageMetadata1 = getStorageMetadata(storageMetadata, byteArray);

            UpdateTask updateTask = new UpdateTask(baseStorageUrlBuilder.build(), byteArray, storageMetadata1, task);
            storageTaskManager.enqueue(updateTask);
        });
        return task;
    }

    /**
     * Helper to resolve metadata, specifically the content type if missing.
     */
    private static StorageMetadata getStorageMetadata(StorageMetadata storageMetadata, File file) {
        StorageMetadata storageMetadata1 = storageMetadata;

        if (storageMetadata1 == null) {
            storageMetadata1 = new StorageMetadata().setContentType(Helper.getMimeType(file));
        } else if (storageMetadata1.getContentType() == null) {
            storageMetadata1.setContentType(Helper.getMimeType(file));
        }
        return storageMetadata1;
    }

    /**
     * Helper to resolve metadata from a URI.
     */
    private StorageMetadata getStorageMetadata(StorageMetadata storageMetadata, Uri uri) {
        StorageMetadata storageMetadata1 = storageMetadata;

        if (storageMetadata1 == null) {
            storageMetadata1 = new StorageMetadata().setContentType(Helper.getMimeType(context, uri));
        } else if (storageMetadata1.getContentType() == null) {
            storageMetadata1.setContentType(Helper.getMimeType(context, uri));
        }
        return storageMetadata1;
    }

    /**
     * Helper to resolve metadata from bytes.
     */
    private static StorageMetadata getStorageMetadata(StorageMetadata storageMetadata, byte[] byteArray) {
        StorageMetadata storageMetadata1 = storageMetadata;

        if (storageMetadata1 == null) {
            storageMetadata1 = new StorageMetadata().setContentType(Helper.getMimeType(byteArray));
        } else if (storageMetadata1.getContentType() == null) {
            storageMetadata1.setContentType(Helper.getMimeType(byteArray));
        }
        return storageMetadata1;
    }

    /**
     * Downloads an object to a {@link File}.
     *
     * @param supabaseStorageReference Path reference in storage.
     * @param file                     The destination file.
     * @return A {@link DownloadTask.Task} to track the download.
     */
    public DownloadTask.Task download(SupabaseStorageReference supabaseStorageReference, File file) {
        DownloadTask.Task task = new DownloadTask.Task();
        Runnables.getExecutorService().execute(() -> {
            if (supabaseStorageReference.getBucketId() == null) {
                task.onError(new SupabaseError("Bucket ID is required but was null."));
                return;
            }
            if (file == null) {
                task.onError(new SupabaseError("A valid file must be provided."));
                return;
            }
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                task.onError(new SupabaseError("The parent directory does not exist: " + file.getParentFile().getAbsolutePath()));
                return;
            }
            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.OBJECT);
            baseStorageUrlBuilder.appendPath(supabaseStorageReference.getBucketId());
            supabaseStorageReference.getPaths().forEach(baseStorageUrlBuilder::appendPath);

            DownloadTask downloadTask = new DownloadTask(baseStorageUrlBuilder.build(), file, task);
            storageTaskManager.enqueue(downloadTask);
        });
        return task;
    }

    /**
     * Downloads an object into a {@link ByteArrayOutputStream}.
     *
     * @param supabaseStorageReference Path reference in storage.
     * @param byteArrayOutputStream    The destination stream.
     * @return A {@link DownloadTask.Task} to track the download.
     */
    public DownloadTask.Task download(SupabaseStorageReference supabaseStorageReference, ByteArrayOutputStream byteArrayOutputStream) {
        DownloadTask.Task task = new DownloadTask.Task();
        Runnables.getExecutorService().execute(() -> {
            if (supabaseStorageReference.getBucketId() == null) {
                task.onError(new SupabaseError("Bucket ID is required but was null."));
                return;
            }
            if (byteArrayOutputStream == null) {
                task.onError(new SupabaseError("The ByteArrayOutputStream must not be null."));
                return;
            }
            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.OBJECT);
            baseStorageUrlBuilder.appendPath(supabaseStorageReference.getBucketId());
            supabaseStorageReference.getPaths().forEach(baseStorageUrlBuilder::appendPath);

            DownloadTask downloadTask = new DownloadTask(baseStorageUrlBuilder.build(), byteArrayOutputStream, task);
            storageTaskManager.enqueue(downloadTask);
        });
        return task;
    }

    /**
     * Downloads a public object to a {@link File}.
     *
     * @param supabaseStorageReference Path reference in storage.
     * @param file                     The destination file.
     * @return A {@link DownloadTask.Task} to track the download.
     */
    public DownloadTask.Task downloadPublic(SupabaseStorageReference supabaseStorageReference, File file) {
        DownloadTask.Task task = new DownloadTask.Task();
        Runnables.getExecutorService().execute(() -> {
            if (supabaseStorageReference.getBucketId() == null) {
                task.onError(new SupabaseError("Bucket ID is required but was null."));
                return;
            }
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                task.onError(new SupabaseError("The parent directory does not exist: " + file.getParentFile().getAbsolutePath()));
                return;
            }
            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.OBJECT);
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.PUBLIC);
            baseStorageUrlBuilder.appendPath(supabaseStorageReference.getBucketId());
            supabaseStorageReference.getPaths().forEach(baseStorageUrlBuilder::appendPath);

            DownloadTask downloadTask = new DownloadTask(baseStorageUrlBuilder.build(), file, task);
            storageTaskManager.enqueue(downloadTask);
        });
        return task;
    }

    /**
     * Downloads a public object into a {@link ByteArrayOutputStream}.
     *
     * @param supabaseStorageReference Path reference in storage.
     * @param byteArrayOutputStream    The destination stream.
     * @return A {@link DownloadTask.Task} to track the download.
     */
    public DownloadTask.Task downloadPublic(SupabaseStorageReference supabaseStorageReference, ByteArrayOutputStream byteArrayOutputStream) {
        DownloadTask.Task task = new DownloadTask.Task();
        Runnables.getExecutorService().execute(() -> {
            if (supabaseStorageReference.getBucketId() == null) {
                task.onError(new SupabaseError("Bucket ID is required but was null."));
                return;
            }
            if (byteArrayOutputStream == null) {
                task.onError(new SupabaseError("The ByteArrayOutputStream must not be null."));
                return;
            }

            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.OBJECT);
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.PUBLIC);
            baseStorageUrlBuilder.appendPath(supabaseStorageReference.getBucketId());
            supabaseStorageReference.getPaths().forEach(baseStorageUrlBuilder::appendPath);

            DownloadTask downloadTask = new DownloadTask(baseStorageUrlBuilder.build(), byteArrayOutputStream, task);
            storageTaskManager.enqueue(downloadTask);
        });
        return task;
    }

    /**
     * Deletes multiple objects from a bucket.
     *
     * @param bucketId                  The bucket ID.
     * @param supabaseStorageReferences List of object references to delete.
     * @return A {@link Task} containing the list of deleted {@link SupabaseObject}s.
     */
    public Task<ArrayList<SupabaseObject>> deleteObjects(String bucketId, ArrayList<SupabaseStorageReference> supabaseStorageReferences) {
        Task<ArrayList<SupabaseObject>> task = new Task<>();

        Runnables.getExecutorService().execute(() -> {
            if (bucketId == null) {
                task.onError(new SupabaseError("Bucket ID is required but was null."));
                return;
            }

            if (supabaseStorageReferences.isEmpty()) {
                task.onError(new SupabaseError("At least one Supabase storage reference is required."));
                return;
            }
            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.OBJECT);
            baseStorageUrlBuilder.appendPath(bucketId);

            JsonUtils.JsonArrayBuilder jsonArrayBuilder = new JsonUtils.JsonArrayBuilder();
            supabaseStorageReferences.forEach(supabaseStorageReference -> jsonArrayBuilder.append(supabaseStorageReference.buildPrefix()));

            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("prefixes", jsonArrayBuilder.build());

            Response response = new RequestHandler().delete(baseStorageUrlBuilder.build(), jsonObjectStringBuilder.build());
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                JSONArray responseJSONArray = response.getResponseJSONArray();
                ArrayList<SupabaseObject> supabaseObjects = new ArrayList<>();
                for (int i = 0; i < responseJSONArray.length(); i++) {
                    try {
                        supabaseObjects.add(new SupabaseObject(responseJSONArray.getJSONObject(i)));
                    } catch (JSONException e) {
                        task.onError(new SupabaseError(e));
                        return;
                    }
                }
                task.onSuccess(supabaseObjects);
            } else {
                Helper.generateError(response, task);
            }
        });

        return task;
    }

    /**
     * Moves an object within a bucket.
     *
     * @param bucketId     The bucket ID.
     * @param oldReference The source reference.
     * @param newReference The destination reference.
     * @return A {@link Task} for completion notification.
     */
    public Task<Void> moveObject(String bucketId, SupabaseStorageReference oldReference, SupabaseStorageReference newReference) {
        Task<Void> task = new Task<>();

        Runnables.getExecutorService().execute(() -> {
            if (bucketId == null) {
                task.onError(new SupabaseError("Bucket ID is required but was null."));
                return;
            }

            if (oldReference == null) {
                task.onError(new SupabaseError("Existing Supabase storage reference is required."));
                return;
            }

            if (newReference == null) {
                task.onError(new SupabaseError("A new Supabase storage reference must be provided."));
                return;
            }

            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.OBJECT);
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.MOVE);

            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("bucketId", bucketId);
            jsonObjectStringBuilder.append("sourceKey", oldReference.buildPrefix());
            jsonObjectStringBuilder.append("destinationKey", newReference.buildPrefix());

            Response response = new RequestHandler().post(baseStorageUrlBuilder.build(), jsonObjectStringBuilder.build());
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                task.onSuccess(null);
            } else {
                Helper.generateError(response, task);
            }
        });

        return task;
    }

    /**
     * Copies an object within a bucket.
     *
     * @param bucketId      The bucket ID.
     * @param fromReference The source reference.
     * @param toReference   The destination reference.
     * @return A {@link Task} containing the copied {@link SupabaseObject}.
     */
    public Task<SupabaseObject> copyObject(String bucketId, SupabaseStorageReference fromReference, SupabaseStorageReference toReference) {
        Task<SupabaseObject> task = new Task<>();

        Runnables.getExecutorService().execute(() -> {
            if (bucketId == null) {
                task.onError(new SupabaseError("Bucket ID is required but was null."));
                return;
            }

            if (fromReference == null) {
                task.onError(new SupabaseError("Existing Supabase storage reference is required."));
                return;
            }

            if (toReference == null) {
                task.onError(new SupabaseError("A new Supabase storage reference must be provided."));
                return;
            }
            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.OBJECT);
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.COPY);

            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("bucketId", bucketId);
            jsonObjectStringBuilder.append("sourceKey", fromReference.buildPrefix());
            jsonObjectStringBuilder.append("destinationKey", toReference.buildPrefix());

            Response response = new RequestHandler().post(baseStorageUrlBuilder.build(), jsonObjectStringBuilder.build());
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                task.onSuccess(new SupabaseObject(response.getResponseJSON()));
            } else {
                Helper.generateError(response, task);
            }
        });

        return task;
    }

    /**
     * Lists objects in a bucket path.
     *
     * @param supabaseStorageReference The path reference.
     * @param limit                    Pagination limit.
     * @param offset                   Pagination offset.
     * @param objectSortBy             Sort column.
     * @param ascending                Sort order.
     * @return A {@link Task} containing the list of {@link SupabaseObject}s.
     */
    public Task<ArrayList<SupabaseObject>> list(SupabaseStorageReference supabaseStorageReference, Integer limit, Integer offset, ObjectSortBy objectSortBy, Boolean ascending) {
        Task<ArrayList<SupabaseObject>> task = new Task<>();

        Runnables.getExecutorService().execute(() -> {
            if (supabaseStorageReference.getBucketId() == null) {
                task.onError(new SupabaseError("Bucket ID is required but was null."));
                return;
            }
            UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.OBJECT);
            baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.LIST);
            baseStorageUrlBuilder.appendPath(supabaseStorageReference.getBucketId());

            JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
            jsonObjectStringBuilder.append("prefix", supabaseStorageReference.buildPrefix());
            if (limit != null) {
                jsonObjectStringBuilder.append("limit", limit);
            }
            if (offset != null) {
                jsonObjectStringBuilder.append("offset", offset);
            }

            if (objectSortBy != null) {
                jsonObjectStringBuilder.append("sortBy", new JsonUtils.JsonObjectBuilder().append("column", objectSortBy.getValue()).append("order", ascending ? "asc" : "desc").build());
            }

            Response response = new RequestHandler().post(baseStorageUrlBuilder.build(), jsonObjectStringBuilder.build());
            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                JSONArray responseJSONArray = response.getResponseJSONArray();
                ArrayList<SupabaseObject> supabaseObjects = new ArrayList<>();
                for (int i = 0; i < responseJSONArray.length(); i++) {
                    try {
                        supabaseObjects.add(new SupabaseObject(responseJSONArray.getJSONObject(i)));
                    } catch (JSONException e) {
                        task.onError(new SupabaseError(e));
                        return;
                    }
                }
                task.onSuccess(supabaseObjects);
            } else {
                Helper.generateError(response, task);
            }
        });

        return task;
    }
}
