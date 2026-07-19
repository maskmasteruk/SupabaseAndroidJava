package io.github.maskmasteruk.supabase.storage.Tasks;

import java.util.ArrayList;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.storage.Listeners.OnCompleteListener;
import io.github.maskmasteruk.supabase.storage.Listeners.OnFailureListener;
import io.github.maskmasteruk.supabase.storage.Listeners.OnSuccessListener;

/**
 * Represents an asynchronous operation in Supabase Storage.
 * <p>
 * Purpose: This class provides a way to track the progress and result of asynchronous tasks,
 * allowing users to attach listeners for success, failure, and completion.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Maintaining lists of listeners.</li>
 *     <li>Storing the result of the operation.</li>
 *     <li>Notifying listeners when the task succeeds or fails.</li>
 * </ul>
 * </p>
 * <p>
 * Example Usage:
 * <pre>
 * Task&lt;Bucket&gt; task = ...;
 * task.addOnSuccessLister(bucket -> {
 *     // Handle success
 * }).addOnFailureListeners(error -> {
 *     // Handle failure
 * });
 * </pre>
 * </p>
 *
 * @param <T> The type of the result object.
 */
public class Task<T> {
    private final ArrayList<OnSuccessListener<T>> onSuccessListeners;
    private final ArrayList<OnFailureListener> onFailureListeners;
    private final ArrayList<OnCompleteListener<T>> onCompleteListeners;

    private T result;
    private boolean isSuccessful = false;

    /**
     * Constructs a new Task.
     */
    public Task() {
        onSuccessListeners = new ArrayList<>();
        onFailureListeners = new ArrayList<>();
        onCompleteListeners = new ArrayList<>();
    }

    /**
     * Adds a listener that will be called if the task succeeds.
     *
     * @param onSuccessListener The listener to add.
     * @return This task instance for chaining.
     */
    public Task<T> addOnSuccessLister(OnSuccessListener<T> onSuccessListener) {
        onSuccessListeners.add(onSuccessListener);
        return this;
    }

    /**
     * Adds a listener that will be called if the task fails.
     *
     * @param onFailureListener The listener to add.
     * @return This task instance for chaining.
     */
    public Task<T> addOnFailureListeners(OnFailureListener onFailureListener) {
        onFailureListeners.add(onFailureListener);
        return this;
    }

    /**
     * Adds a listener that will be called when the task completes (either success or failure).
     *
     * @param onCompleteListener The listener to add.
     * @return This task instance for chaining.
     */
    public Task<T> addOnCompleteListener(OnCompleteListener<T> onCompleteListener) {
        onCompleteListeners.add(onCompleteListener);
        return this;
    }

    /** @return The list of success listeners. */
    public ArrayList<OnSuccessListener<T>> getOnSuccessListeners() {
        return onSuccessListeners;
    }

    /** @return The list of failure listeners. */
    public ArrayList<OnFailureListener> getOnFailureListeners() {
        return onFailureListeners;
    }

    /** @return The list of completion listeners. */
    public ArrayList<OnCompleteListener<T>> getOnCompleteListeners() {
        return onCompleteListeners;
    }

    /**
     * Notifies all listeners that the task succeeded.
     *
     * @param result The result of the task.
     */
    public void onSuccess(T result) {
        isSuccessful = true;
        this.result = result;
        getOnSuccessListeners().forEach(onSuccessListeners -> onSuccessListeners.onSuccess(result));
        getOnCompleteListeners().forEach(onCompleteListener -> onCompleteListener.onSuccess(result));
    }

    /**
     * Notifies all listeners that the task failed.
     *
     * @param supabaseError The error that occurred.
     */
    public void onError(SupabaseError supabaseError) {
        isSuccessful = false;
        getOnFailureListeners().forEach(onFailureListener -> onFailureListener.onFailure(supabaseError));
        getOnCompleteListeners().forEach(onFailureListener -> onFailureListener.onError(supabaseError));
    }

    /** @return {@code true} if the task succeeded, {@code false} otherwise. */
    public boolean isSuccessful() {
        return isSuccessful;
    }

    /** @return The result of the task, or {@code null} if not yet finished or if it failed. */
    public T getResult() {
        return result;
    }

    /** @param object The result to set. */
    public void setResult(T object) {
        result = object;
    }
}
