package io.github.maskmasteruk.supabase.storage;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PARTIAL;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Objects.Request;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.storage.Listeners.OnProgressListener;

/**
 * Handles the execution of a download task.
 * <p>
 * Purpose: This class implements the logic for downloading an object from Supabase Storage
 * into a local {@link File} or a {@link ByteArrayOutputStream}.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Managing the connection and streaming the downloaded data.</li>
 *     <li>Reporting download progress to listeners.</li>
 *     <li>Handling errors during the download process.</li>
 * </ul>
 * </p>
 */
public class DownloadTask {

    /** Internal enumeration of supported download destinations. */
    private enum DataType {
        /** Download into a {@link File}. */
        FILE,
        /** Download into a {@link ByteArrayOutputStream}. */
        BYTES
    }

    private final String uploadUrl;
    private File file;
    private ByteArrayOutputStream byteArrayOutputStream;

    private final DataType dataType;
    Task task;

    /**
     * Constructs a DownloadTask to save content to a {@link File}.
     *
     * @param uploadUrl The URL of the object to download.
     * @param file      The destination file.
     * @param task      The task instance to notify.
     */
    public DownloadTask(String uploadUrl, File file, Task task) {
        this.uploadUrl = uploadUrl;
        this.file = file;
        this.task = task;
        dataType = DataType.FILE;
    }

    /**
     * Constructs a DownloadTask to save content to a {@link ByteArrayOutputStream}.
     *
     * @param uploadUrl             The URL of the object to download.
     * @param byteArrayOutputStream The destination stream.
     * @param task                  The task instance to notify.
     */
    public DownloadTask(String uploadUrl, ByteArrayOutputStream byteArrayOutputStream, Task task) {
        this.uploadUrl = uploadUrl;
        this.byteArrayOutputStream = byteArrayOutputStream;
        this.task = task;
        dataType = DataType.BYTES;
    }

    /**
     * Executes the download operation.
     */
    void download() {

        Consumer<HttpURLConnection> downloadRunnable = (connection) -> {
            try {
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);

                long fileSize = connection.getHeaderFieldLong("Content-Length", -1);
                int BUFFER_SIZE = Helper.getOptimalBufferSize(fileSize);

                switch (dataType) {
                    case FILE:
                        try (InputStream inputStream = connection.getInputStream(); FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                            byte[] buffer = new byte[BUFFER_SIZE];
                            int bytesRead;
                            if (fileSize > 0 && task.hasOnProgressListeners()) {
                                int downloaded = 0;
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    fileOutputStream.write(buffer, 0, bytesRead);
                                    downloaded += bytesRead;
                                    task.onProgress(Math.toIntExact(Math.round(downloaded * 100.0 / fileSize)));
                                }
                            } else {
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    fileOutputStream.write(buffer, 0, bytesRead);
                                }
                            }
                        }
                        break;
                    case BYTES:
                        try (InputStream inputStream = connection.getInputStream();) {
                            byte[] buffer = new byte[BUFFER_SIZE];
                            int bytesRead;
                            if (fileSize > 0 && task.hasOnProgressListeners()) {
                                int downloaded = 0;
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                                    downloaded += bytesRead;
                                    task.onProgress(Math.toIntExact(Math.round(downloaded * 100.0 / fileSize)));
                                }
                            } else {
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                                }
                            }
                        }
                        break;
                }
            } catch (Exception e) {
                task.onError(new SupabaseError(e));
            }
        };

        Request request = new Request(uploadUrl)
                .setHeaders(RequestHandler.baseHeaders)
                .setDownloadRunnable(downloadRunnable);

        Response response = new RequestHandler().get(request);

        if (response.getCode() >= HTTP_OK && response.getCode() <= HTTP_PARTIAL) {
            switch (dataType) {
                case FILE:
                    task.onSuccess(file);
                    break;
                case BYTES:
                    task.onSuccess(byteArrayOutputStream);
                    break;
                default:
                    task.onSuccess(null);
                    break;
            }
        } else {
            Helper.generateError(response, task);
        }
    }

    /** @return The task associated with this download. */
    public Task getTask() {
        return task;
    }

    /**
     * Specialized {@link Task} for download operations, supporting progress listeners.
     */
    public static class Task extends io.github.maskmasteruk.supabase.storage.Task<Object> {
        private final ArrayList<OnProgressListener> onProgressListeners;

        /**
         * Constructs a new Download Task.
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
