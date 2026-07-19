package io.github.maskmasteruk.supabase.storage;

import io.github.maskmasteruk.supabase.core.Runnables;
import io.github.maskmasteruk.supabase.storage.Tasks.DownloadTask;
import io.github.maskmasteruk.supabase.storage.Tasks.UpdateTask;
import io.github.maskmasteruk.supabase.storage.Tasks.UploadTask;

/**
 * Manager class for enqueuing and executing storage-related tasks.
 * <p>
 * Purpose: This class acts as a central dispatcher for various storage tasks
 * (upload, download, update, resumable upload), ensuring they are executed
 * on the appropriate background executor services.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Providing a singleton access point for task enqueuing.</li>
 *     <li>Routing tasks to specialized executors (upload, download, resumable).</li>
 * </ul>
 * </p>
 */
public class StorageTaskManager {
    private static volatile StorageTaskManager instance;


    /**
     * Private constructor for singleton pattern.
     */
    private StorageTaskManager() {
    }

    /**
     * Returns the singleton instance of {@code StorageTaskManager}.
     *
     * @return The singleton instance.
     */
    public static StorageTaskManager getInstance() {
        if (instance == null) {
            synchronized (StorageTaskManager.class) {
                if (instance == null) {
                    instance = new StorageTaskManager();
                }
            }
        }
        return instance;
    }

    /**
     * Enqueues a standard upload task.
     *
     * @param uploadTask The task to execute.
     */
    public void enqueue(UploadTask uploadTask) {
        Runnables.getStorageUploadExecutorService().execute(uploadTask::upload);
    }

    /**
     * Enqueues a resumable upload task.
     *
     * @param resumableUploadTask The task to execute.
     */
    public void enqueue(ResumableUploadTask resumableUploadTask) {
        Runnables.getStorageResumableUploadExecutorService().execute(resumableUploadTask::upload);
    }

    /**
     * Enqueues an update task.
     *
     * @param updateTask The task to execute.
     */
    public void enqueue(UpdateTask updateTask) {
        Runnables.getStorageUploadExecutorService().execute(updateTask::update);
    }

    /**
     * Enqueues a download task.
     *
     * @param downloadTask The task to execute.
     */
    public void enqueue(DownloadTask downloadTask) {
        Runnables.getStorageDownloadExecutorService().execute(downloadTask::download);
    }
}
