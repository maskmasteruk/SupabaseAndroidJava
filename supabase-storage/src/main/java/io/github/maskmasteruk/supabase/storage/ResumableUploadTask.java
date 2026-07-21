package io.github.maskmasteruk.supabase.storage;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;

import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Request;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;
import io.github.maskmasteruk.supabase.storage.Listeners.OnProgressListener;

/**
 * Handles the execution of resumable upload tasks using the TUS protocol.
 * <p>
 * Purpose: This class enables large file uploads that can survive network interruptions.
 * It tracks progress and persists upload URLs to allow resuming from where it left off.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Implementing the TUS resumable upload protocol.</li>
 *     <li>Managing upload offsets and chunks.</li>
 *     <li>Persisting and retrieving resumable URLs via {@link ResumableTasksInfo}.</li>
 *     <li>Supporting File, InputStream, and byte array data sources.</li>
 *     <li>Reporting upload progress.</li>
 * </ul>
 * </p>
 */
public class ResumableUploadTask {

    /** Internal enumeration of supported data sources. */
    private enum DataType {
        FILE, INPUT_STREAM, BYTES
    }

    private File file;
    private byte[] byteArray;
    private InputStream inputStream;
    private final StorageMetadata storageMetadata;

    private final DataType dataType;
    private final long fileSize;
    private final Task task;

    ResumableTasksInfo resumableTasksInfo;

    private final SupabaseStorageReference supabaseStorageReference;

    /**
     * Constructs a ResumableUploadTask for a {@link File}.
     *
     * @param supabaseStorageReference Destination reference.
     * @param context                  Android context.
     * @param file                     The file to upload.
     * @param storageMetadata          Object metadata.
     * @param task                     The task instance.
     */
    public ResumableUploadTask(SupabaseStorageReference supabaseStorageReference, Context context, File file, StorageMetadata storageMetadata, Task task) {
        this.supabaseStorageReference = supabaseStorageReference;
        this.resumableTasksInfo = ResumableTasksInfo.getInstance(context);
        this.file = file;
        fileSize = file.length();
        this.task = task;
        this.storageMetadata = storageMetadata;
        dataType = DataType.FILE;
    }

    /**
     * Constructs a ResumableUploadTask for a byte array.
     *
     * @param supabaseStorageReference Destination reference.
     * @param context                  Android context.
     * @param byteArray                The data to upload.
     * @param storageMetadata          Object metadata.
     * @param task                     The task instance.
     */
    public ResumableUploadTask(SupabaseStorageReference supabaseStorageReference, Context context, byte[] byteArray, StorageMetadata storageMetadata, Task task) {
        this.supabaseStorageReference = supabaseStorageReference;
        this.resumableTasksInfo = ResumableTasksInfo.getInstance(context);
        this.byteArray = byteArray;
        fileSize = byteArray.length;
        this.task = task;
        this.storageMetadata = storageMetadata;
        dataType = DataType.BYTES;
    }

    /**
     * Constructs a ResumableUploadTask for an {@link InputStream}.
     *
     * @param supabaseStorageReference Destination reference.
     * @param context                  Android context.
     * @param inputStream              The stream to read from.
     * @param fileSize                 Total content size.
     * @param storageMetadata          Object metadata.
     * @param task                     The task instance.
     */
    public ResumableUploadTask(SupabaseStorageReference supabaseStorageReference, Context context, InputStream inputStream, long fileSize, StorageMetadata storageMetadata, Task task) {
        this.supabaseStorageReference = supabaseStorageReference;
        this.resumableTasksInfo = ResumableTasksInfo.getInstance(context);
        this.inputStream = inputStream;
        if (fileSize == 0 && storageMetadata.getFileSize() != null) {
            fileSize = storageMetadata.getFileSize();
        }
        if (fileSize <= 0) {
            throw new SupabaseError("File size must be greater than 0 or could not be determined.");
        }
        this.fileSize = fileSize;
        this.task = task;
        this.storageMetadata = storageMetadata;
        dataType = DataType.INPUT_STREAM;
    }

    /**
     * Starts or resumes the upload.
     * It first attempts to find an existing resumable URL and its offset,
     * otherwise it creates a new one.
     */
    void upload() {
        String bucketId = supabaseStorageReference.getBucketId();
        Uri.Builder uri = Uri.parse("").buildUpon();
        supabaseStorageReference.getPaths().forEach(uri::appendPath);
        String objectPath = String.valueOf(uri.build()).substring(1);
        String metadata = storageMetadata.getMetadataB64();

        JsonUtils.JsonObjectStringBuilder jsonObjectStringBuilder = new JsonUtils.JsonObjectStringBuilder();
        jsonObjectStringBuilder.append("bucketId", bucketId);
        jsonObjectStringBuilder.append("objectPath", objectPath);
        jsonObjectStringBuilder.append("metadata", metadata);
        if (dataType == DataType.FILE) {
            jsonObjectStringBuilder.append("file", Helper.generateHash(file));
        } else if (dataType == DataType.INPUT_STREAM) {
            jsonObjectStringBuilder.append("uri", inputStream.hashCode());
        } else if (dataType == DataType.BYTES) {
            jsonObjectStringBuilder.append("bytes", Arrays.hashCode(byteArray));
        }
        String hashKey = Helper.generateHash(jsonObjectStringBuilder.build());
        String resumableUrl = resumableTasksInfo.getResumableTasksInfo(hashKey);
        int offset = 0;

        if (resumableUrl == null) {
            resumableUrl = createResumableUrl(bucketId, objectPath, metadata, fileSize);
            resumableTasksInfo.setResumableTasksInfo(hashKey, resumableUrl);
        } else {
            Integer resumeOffset = getOffsetOfResumable(resumableUrl);
            if (resumeOffset == null) {
                resumableUrl = createResumableUrl(bucketId, objectPath, metadata, fileSize);
            } else {
                offset = resumeOffset;
            }
        }

        if (offset == fileSize) {
            task.onSuccess(null);
            return;
        }

        if (resumableUrl == null) {
            return;
        }

        uploadData(hashKey, resumableUrl, offset);
    }

    /**
     * Routes the upload to the correct method based on data type.
     */
    private void uploadData(String hashKey, String uploadUrl, int offset) {
        switch (dataType) {
            case FILE:
                uploadFileData(hashKey, uploadUrl, offset);
                break;
            case INPUT_STREAM:
                uploadInputStreamData(hashKey, uploadUrl, offset);
                break;
            case BYTES:
                uploadByteArrayData(hashKey, uploadUrl, offset);
                break;
        }
    }

    /**
     * Uploads chunks from an {@link InputStream}.
     */
    private void uploadInputStreamData(String hashKey, String uploadUrl, int offset) {
        final int BUFFER_SIZE = Helper.getOptimalBufferSize(fileSize);
        try {
            long skipped = 0;
            while (skipped < offset) {
                skipped += inputStream.skip(offset - skipped);
            }
            while (offset < fileSize) {
                final int chunkSize = Math.toIntExact(Math.min(BUFFER_SIZE, fileSize - offset));
                byte[] buffer = new byte[chunkSize];
                int read = inputStream.read(buffer, 0, chunkSize);

                if (read == -1) {
                    task.onError(new SupabaseError("Unable to read"));
                    return;
                }

                Consumer<HttpURLConnection> uploadConsumer = connection -> {
                    try {
                        connection.setFixedLengthStreamingMode(read);

                        try (OutputStream outputStream = connection.getOutputStream()) {
                            if (read > 0) {
                                outputStream.write(buffer, 0, read);
                            }
                            outputStream.flush();
                        }

                    } catch (Exception e) {
                        task.onError(new SupabaseError(e));
                    }
                };

                HashMap<String, String> headers = new HashMap<>();
                headers.put("Tus-Resumable", "1.0.0");
                headers.put("Upload-Offset", String.valueOf(offset));
                headers.put("Content-Type", "application/offset+octet-stream");
                headers.put("Content-Length", String.valueOf(read));

                Request request = new Request(uploadUrl).setUploadRunnable(uploadConsumer).setHeaders(headers);

                Response response = new RequestHandler().patch(request);


                if (task.hasOnProgressListeners()) {
                    task.onProgress((int) ((offset + chunkSize) * 100.0 / fileSize));
                }

                if (response.getCode() < HTTP_OK || response.getCode() >= 300) {
                    Helper.generateError(response, task);
                    return;
                }

                offset = Integer.parseInt(Objects.requireNonNull(response.getHeaderFields().get("Upload-Offset")).get(0));

                if (offset >= fileSize) {
                    resumableTasksInfo.remove(hashKey);
                    task.onSuccess(null);
                    return;
                }
            }
        } catch (IOException e) {
            task.onError(new SupabaseError(e));
        }

    }

    /**
     * Uploads chunks from a byte array.
     */
    private void uploadByteArrayData(String hashKey, String uploadUrl, int offset) {
        final int BUFFER_SIZE = Helper.getOptimalBufferSize(fileSize);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
        long skipped = 0;
        while (skipped < offset) {
            skipped += byteArrayInputStream.skip(offset - skipped);
        }
        while (offset < fileSize) {
            final int chunkSize = Math.toIntExact(Math.min(BUFFER_SIZE, fileSize - offset));
            byte[] buffer = new byte[chunkSize];
            int read = byteArrayInputStream.read(buffer, 0, chunkSize);

            if (read == -1) {
                task.onError(new SupabaseError("Unable to read"));
                return;
            }

            Consumer<HttpURLConnection> uploadConsumer = connection -> {
                try {
                    connection.setFixedLengthStreamingMode(read);

                    try (OutputStream outputStream = connection.getOutputStream()) {
                        if (read > 0) {
                            outputStream.write(buffer, 0, read);
                        }
                        outputStream.flush();
                    }

                } catch (Exception e) {
                    task.onError(new SupabaseError(e));
                }
            };

            HashMap<String, String> headers = new HashMap<>();
            headers.put("Tus-Resumable", "1.0.0");
            headers.put("Upload-Offset", String.valueOf(offset));
            headers.put("Content-Type", "application/offset+octet-stream");
            headers.put("Content-Length", String.valueOf(read));

            Request request = new Request(uploadUrl).setUploadRunnable(uploadConsumer).setHeaders(headers);

            Response response = new RequestHandler().patch(request);


            if (task.hasOnProgressListeners()) {
                task.onProgress((int) ((offset + chunkSize) * 100.0 / fileSize));
            }

            if (response.getCode() < HTTP_OK || response.getCode() >= 300) {
                Helper.generateError(response, task);
                return;
            }

            offset = Integer.parseInt(Objects.requireNonNull(response.getHeaderFields().get("Upload-Offset")).get(0));

            if (offset >= fileSize) {
                resumableTasksInfo.remove(hashKey);
                task.onSuccess(null);
                return;
            }
        }
    }

    /**
     * Uploads chunks from a {@link File}.
     */
    private void uploadFileData(String hashKey, String uploadUrl, int offset) {
        final int BUFFER_SIZE = Helper.getOptimalBufferSize(fileSize);
        try (FileInputStream fis = new FileInputStream(file)) {
            long skipped = 0;
            while (skipped < offset) {
                skipped += fis.skip(offset - skipped);
            }
            while (offset < fileSize) {
                final int chunkSize = Math.toIntExact(Math.min(BUFFER_SIZE, fileSize - offset));
                byte[] buffer = new byte[chunkSize];
                int read = fis.read(buffer, 0, chunkSize);

                if (read == -1) {
                    task.onError(new SupabaseError("Unable to read"));
                    return;
                }

                Consumer<HttpURLConnection> uploadConsumer = connection -> {
                    try {
                        connection.setFixedLengthStreamingMode(read);

                        try (OutputStream outputStream = connection.getOutputStream()) {
                            if (read > 0) {
                                outputStream.write(buffer, 0, read);
                            }
                            outputStream.flush();
                        }

                    } catch (Exception e) {
                        task.onError(new SupabaseError(e));
                    }
                };

                HashMap<String, String> headers = new HashMap<>();
                headers.put("Tus-Resumable", "1.0.0");
                headers.put("Upload-Offset", String.valueOf(offset));
                headers.put("Content-Type", "application/offset+octet-stream");
                headers.put("Content-Length", String.valueOf(read));

                Request request = new Request(uploadUrl).setUploadRunnable(uploadConsumer).setHeaders(headers);

                Response response = new RequestHandler().patch(request);


                if (task.hasOnProgressListeners()) {
                    task.onProgress((int) ((offset + chunkSize) * 100.0 / fileSize));
                }

                if (response.getCode() < HTTP_OK || response.getCode() >= 300) {
                    Helper.generateError(response, task);
                    return;
                }

                offset = Integer.parseInt(Objects.requireNonNull(response.getHeaderFields().get("Upload-Offset")).get(0));

                if (offset >= fileSize) {
                    resumableTasksInfo.remove(hashKey);
                    task.onSuccess(null);
                    return;
                }
            }
        } catch (IOException e) {
            task.onError(new SupabaseError(e));
        }

    }

    /**
     * Creates a new resumable upload URL via the Supabase Storage API.
     */
    private String createResumableUrl(String bucketId, String objectPath, String metadata, long fileSize) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Tus-Resumable", "1.0.0");
        headers.put("Upload-Length", String.valueOf(fileSize));
        headers.put("Upload-Metadata", "bucketName " + Helper.encodeToBase64(bucketId) + ",objectName " + Helper.encodeToBase64(objectPath) + (metadata != null ? ",metadata " + Helper.encodeToBase64(metadata) : "") + (storageMetadata.getContentType() != null ? ",contentType " + Helper.encodeToBase64(storageMetadata.getContentType()) : ""));

        UrlBuilder baseStorageUrlBuilder = Helper.getBaseStorageUrlBuilder();
        baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.UPLOAD);
        baseStorageUrlBuilder.appendPath(STORAGE_END_POINTS.RESUMABLE);

        Response response = new RequestHandler().post(baseStorageUrlBuilder.build(), null, headers);

        if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
            return Objects.requireNonNull(response.getHeaderFields().get("Location")).get(0);
        } else {
            task.onError(new SupabaseError(response.getResponse()));
        }

        return null;
    }

    /**
     * Queries the server for the current upload offset of a resumable upload.
     */
    private Integer getOffsetOfResumable(String url) {
        HashMap<String, String> headers = new HashMap<>();

        headers.put("Tus-Resumable", "1.0.0");
        Response response = new RequestHandler().head(url, headers);

        if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
            return Integer.valueOf(Objects.requireNonNull(response.getHeaderFields().get("Upload-Offset")).get(0));
        }

        return null;
    }

    /** @return The task associated with this resumable upload. */
    public Task getTask() {
        return task;
    }

    /**
     * Specialized {@link Task} for resumable upload operations, supporting progress listeners.
     */
    public static class Task extends io.github.maskmasteruk.supabase.storage.Task<Void> {
        private final ArrayList<OnProgressListener> onProgressListeners;

        /**
         * Constructs a new Resumable Upload Task.
         */
        public Task() {
            super();
            onProgressListeners = new ArrayList<>();
        }

        /**
         * Adds a progress listener to the task.
         *
         * @param onProgressListener The listener to add.
         * @return This task instance for chaining.
         */
        public Task addOnProgressListener(OnProgressListener onProgressListener) {
            onProgressListeners.add(onProgressListener);
            return this;
        }

        /** @return The list of progress listeners. */
        ArrayList<OnProgressListener> getOnProgressListeners() {
            return onProgressListeners;
        }

        /** @return {@code true} if there are any progress listeners attached. */
        boolean hasOnProgressListeners() {
            return !onProgressListeners.isEmpty();
        }

        private final AtomicInteger lastProgressPercentage = new AtomicInteger(-1);

        /**
         * Reports progress to all listeners.
         *
         * @param progress The progress percentage.
         */
        void onProgress(int progress) {
            if (lastProgressPercentage.get() != progress) {
                getOnProgressListeners().forEach(onProgressListener -> onProgressListener.onProgress(progress));
                lastProgressPercentage.set(progress);
            }
        }

    }
}
