package io.github.maskmasteruk.supabase.storage.Tasks;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Objects.Request;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.storage.Helper;
import io.github.maskmasteruk.supabase.storage.Listeners.OnProgressListener;
import io.github.maskmasteruk.supabase.storage.Object.StorageMetadata;
import io.github.maskmasteruk.supabase.storage.Object.SupabaseObject;

/**
 * Handles the execution of an update task for an existing object.
 * <p>
 * Purpose: This class implements the logic for updating an existing object's content
 * in Supabase Storage using a PUT request.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Managing data sources for update (File, InputStream, byte array).</li>
 *     <li>Streaming updated content to the network.</li>
 *     <li>Reporting update progress to listeners.</li>
 *     <li>Mapping API responses to {@link SupabaseObject} or {@link SupabaseError}.</li>
 * </ul>
 * </p>
 */
public class UpdateTask {

    /** Internal enumeration of supported data sources. */
    private enum DataType {
        /** Update from a {@link File}. */
        FILE,
        /** Update from an {@link InputStream}. */
        INPUT_STREAM,
        /** Update from a byte array. */
        BYTES
    }

    private final String uploadUrl;
    private File file;
    private byte[] byteArray;
    private InputStream inputStream;
    private final StorageMetadata storageMetadata;

    private final DataType dataType;
    private final long fileSize;
    Task task;

    /**
     * Constructs an UpdateTask for a {@link File}.
     *
     * @param uploadUrl       The destination URL.
     * @param file            The new file content.
     * @param storageMetadata Metadata for the object.
     * @param task            The task instance to notify.
     */
    public UpdateTask(String uploadUrl, File file, StorageMetadata storageMetadata, Task task) {
        this.uploadUrl = uploadUrl;
        this.file = file;
        fileSize = file.length();
        this.task = task;
        this.storageMetadata = storageMetadata;
        dataType = DataType.FILE;
    }

    /**
     * Constructs an UpdateTask for a byte array.
     *
     * @param uploadUrl       The destination URL.
     * @param byteArray       The new data.
     * @param storageMetadata Metadata for the object.
     * @param task            The task instance.
     */
    public UpdateTask(String uploadUrl, byte[] byteArray, StorageMetadata storageMetadata, Task task) {
        this.uploadUrl = uploadUrl;
        this.byteArray = byteArray;
        fileSize = byteArray.length;
        this.task = task;
        this.storageMetadata = storageMetadata;
        dataType = DataType.BYTES;
    }

    /**
     * Constructs an UpdateTask for an {@link InputStream}.
     *
     * @param uploadUrl       The destination URL.
     * @param inputStream     The stream to read from.
     * @param fileSize        The total size of the content.
     * @param storageMetadata Metadata for the object.
     * @param task            The task instance.
     */
    public UpdateTask(String uploadUrl, InputStream inputStream, long fileSize, StorageMetadata storageMetadata, Task task) {
        this.uploadUrl = uploadUrl;
        this.inputStream = inputStream;
        if (fileSize == 0 && storageMetadata.getFileSize() != null) {
            fileSize = storageMetadata.getFileSize();
        }
        this.fileSize = fileSize;
        this.task = task;
        this.storageMetadata = storageMetadata;
        dataType = DataType.INPUT_STREAM;
    }

    /**
     * Executes the update operation.
     */
    public void update() {
        int BUFFER_SIZE = Helper.getOptimalBufferSize(fileSize);

        Consumer<HttpURLConnection> uploadConsumer = connection -> {
            try {
                switch (dataType) {
                    case FILE:
                        connection.setFixedLengthStreamingMode(file.length());
                        try (OutputStream outputStream = connection.getOutputStream(); FileInputStream fis = new FileInputStream(file)) {
                            writeInputStreamToOutputStream(BUFFER_SIZE, fis, outputStream);
                        }
                        break;
                    case INPUT_STREAM:
                        try (InputStream inputStream = this.inputStream; OutputStream outputStream = connection.getOutputStream()) {
                            writeInputStreamToOutputStream(BUFFER_SIZE, inputStream, outputStream);
                        }
                        break;
                    case BYTES:
                        connection.setFixedLengthStreamingMode(byteArray.length);
                        long totalBytes = byteArray.length;
                        long bytesWritten = 0;
                        int offset = 0;

                        if (fileSize > 0 && task.hasOnProgressListeners()) {
                            try (OutputStream outputStream = connection.getOutputStream()) {
                                while (offset < totalBytes) {
                                    int bytesToWrite = Math.toIntExact(Math.min(BUFFER_SIZE, totalBytes - offset));

                                    outputStream.write(byteArray, offset, bytesToWrite);
                                    outputStream.flush();

                                    offset += bytesToWrite;
                                    bytesWritten += bytesToWrite;

                                    task.onProgress((int) (bytesWritten * 100.0 / fileSize));
                                }
                            }
                        } else {
                            try (OutputStream outputStream = connection.getOutputStream()) {
                                while (offset < totalBytes) {
                                    int bytesToWrite = Math.toIntExact(Math.min(BUFFER_SIZE, totalBytes - offset));

                                    outputStream.write(byteArray, offset, bytesToWrite);
                                    outputStream.flush();

                                    offset += bytesToWrite;
                                    bytesWritten += bytesToWrite;
                                }
                            }
                        }
                        break;
                }
            } catch (Exception e) {
                task.onError(new SupabaseError(e));
            }
        };


        try {
            Request request = new Request(uploadUrl)
                    .setUploadRunnable(uploadConsumer)
                    .setHeaders(storageMetadata.buildHeaders());
            Response response = new RequestHandler().put(request);

            if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
                task.onSuccess(new SupabaseObject(response.getResponseJSON()));
            } else {
                Helper.generateError(response, task);
            }
        } catch (Exception e) {
            task.onError(new SupabaseError(e));
        }
    }

    /**
     * Copies bytes from a {@link FileInputStream} to an {@link OutputStream} and reports progress.
     *
     * @param BUFFER_SIZE  The buffer size.
     * @param fis          Source stream.
     * @param outputStream Destination stream.
     * @throws IOException If an I/O error occurs.
     */
    private void writeInputStreamToOutputStream(int BUFFER_SIZE, FileInputStream fis, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        if (fileSize > 0 && task.hasOnProgressListeners()) {
            long sent = 0;
            while ((bytesRead = fis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                sent += bytesRead;
                task.onProgress((int) (sent * 100.0 / fileSize));
            }
        } else {
            while ((bytesRead = fis.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        outputStream.flush();
    }

    /**
     * Copies bytes from an {@link InputStream} to an {@link OutputStream} and reports progress.
     *
     * @param BUFFER_SIZE  The buffer size.
     * @param inputStream  Source stream.
     * @param outputStream Destination stream.
     * @throws IOException If an I/O error occurs.
     */
    private void writeInputStreamToOutputStream(int BUFFER_SIZE, InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;

        if (fileSize > 0 && task.hasOnProgressListeners()) {
            long sent = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                sent += bytesRead;
                task.onProgress((int) (sent * 100.0 / fileSize));
            }
        } else {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
        outputStream.flush();
    }

    /** @return The task associated with this update. */
    public Task getTask() {
        return task;
    }

    /**
     * Specialized {@link Task} for update operations, supporting progress listeners.
     */
    public static class Task extends io.github.maskmasteruk.supabase.storage.Tasks.Task<SupabaseObject> {
        private final ArrayList<OnProgressListener> onProgressListeners;

        /**
         * Constructs a new Update Task.
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
        public ArrayList<OnProgressListener> getOnProgressListeners() {
            return onProgressListeners;
        }

        /** @return {@code true} if there are any progress listeners attached. */
        public boolean hasOnProgressListeners() {
            return !onProgressListeners.isEmpty();
        }

        private final AtomicInteger lastProgressPercentage = new AtomicInteger(-1);

        /**
         * Reports progress to all listeners.
         *
         * @param progress The progress percentage.
         */
        public void onProgress(int progress) {
            if (lastProgressPercentage.get() != progress) {
                getOnProgressListeners().forEach(onProgressListener -> onProgressListener.onProgress(progress));
                lastProgressPercentage.set(progress);
            }
        }

    }
}
