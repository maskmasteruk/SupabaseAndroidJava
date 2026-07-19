package io.github.maskmasteruk.supabase.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages thread pools for background operations in the Supabase SDK.
 *
 * Responsibilities:
 * - Providing dedicated executors for different types of operations (general, storage upload, storage download).
 * - Lifecycle management of thread pools (shutdown).
 *
 * Usage:
 * Runnables.getExecutorService().execute(() -> {
 *     // Background task
 * });
 *
 * Thread Safety:
 * Thread-safe singleton-like access to executors.
 */
public class Runnables {
    /**
     * General purpose executor service for most Supabase requests.
     */
    private static final ExecutorService executorService = Executors.newFixedThreadPool(5, r -> {
                Thread t = new Thread(r);
                t.setName("supabase-worker");
                return t;
            });

    /**
     * Dedicated executor service for standard storage upload operations.
     */
    private static final ExecutorService storageUploadExecutorService = Executors.newFixedThreadPool(5, r -> {
        Thread t = new Thread(r);
        t.setName("supabase-storage-upload-worker");
        return t;
    });

    /**
     * Dedicated executor service for resumable storage upload operations.
     */
    private static final ExecutorService storageResumableUploadExecutorService = Executors.newFixedThreadPool(5, r -> {
        Thread t = new Thread(r);
        t.setName("supabase-storage-upload-worker");
        return t;
    });

    /**
     * Dedicated executor service for storage download operations.
     */
    private static final ExecutorService storageDownloadExecutorService = Executors.newFixedThreadPool(5, r -> {
        Thread t = new Thread(r);
        t.setName("supabase-storage-download-worker");
        return t;
    });

    /**
     * Gets the general purpose executor service.
     *
     * @return The general ExecutorService.
     */
    public static ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Gets the executor service for storage upload operations.
     *
     * @return The storage upload ExecutorService.
     */
    public static ExecutorService getStorageUploadExecutorService() {
        return storageUploadExecutorService;
    }

    /**
     * Gets the executor service for storage download operations.
     *
     * @return The storage download ExecutorService.
     */
    public static ExecutorService getStorageDownloadExecutorService() {
        return storageDownloadExecutorService;
    }

    /**
     * Gets the executor service for resumable storage upload operations.
     *
     * @return The resumable storage upload ExecutorService.
     */
    public static ExecutorService getStorageResumableUploadExecutorService() {
        return storageResumableUploadExecutorService;
    }

    /**
     * Initiates an orderly shutdown of all managed thread pools.
     * Previously submitted tasks are executed, but no new tasks will be accepted.
     */
    public static void shutdown() {
        executorService.shutdown();
        storageUploadExecutorService.shutdown();
        storageDownloadExecutorService.shutdown();
        storageResumableUploadExecutorService.shutdown();
    }

    /**
     * Attempts to stop all actively executing tasks and halts processing of waiting tasks for all managed thread pools.
     */
    public static void shutdownNow() {
        executorService.shutdownNow();
        storageUploadExecutorService.shutdownNow();
        storageDownloadExecutorService.shutdownNow();
        storageResumableUploadExecutorService.shutdown();
    }
}
