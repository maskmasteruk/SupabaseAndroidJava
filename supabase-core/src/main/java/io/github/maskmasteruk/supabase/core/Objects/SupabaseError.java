package io.github.maskmasteruk.supabase.core.Objects;

import androidx.annotation.Nullable;

import io.github.maskmasteruk.supabase.core.VALUES.CONSTANTS;


/**
 * A wrapper class for exceptions and error messages throughout the Supabase SDK.
 *
 * Responsibilities:
 * - Wrapping standard Java exceptions.
 * - Categorizing errors as user-friendly or technical.
 * - Providing simplified error messages for UI display.
 * - Facilitating consistent error handling across different modules.
 *
 * Usage:
 * try { ... } catch (Exception e) { throw new SupabaseError(e); }
 *
 * Thread Safety:
 * Immutable once created (mostly), safe to pass between threads.
 */
public class SupabaseError extends RuntimeException {
    /**
     * The underlying exception that occurred.
     */
    private Exception exception;

    /**
     * Indicates whether the error message is safe/appropriate to show directly to the user.
     */
    private boolean isUserFriendly = false;

    /**
     * Constructs an SupabaseError from an Exception.
     * Automatically determines if the message is user-friendly based on known auth error messages.
     *
     * @param exception The exception to wrap.
     */
    public SupabaseError(Exception exception) {
        if (CONSTANTS.AUTH_ERROR_MESSAGES.containsValue(exception.getMessage())) {
            setUserFriendly(true);
        }
        this.exception = exception;
    }

    /**
     * Constructs an SupabaseError from a specific error message string.
     * This message is considered user-friendly by default.
     *
     * @param s The user-facing error message.
     */
    public SupabaseError(String s) {
        this.exception = new Exception(s);
        setUserFriendly(true);
    }

    /**
     * Gets the wrapped underlying exception.
     *
     * @return The Exception object.
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Sets a new underlying exception for this error.
     *
     * @param exception The exception to wrap.
     */
    public void setException(Exception exception) {
        this.exception = exception;
    }

    /**
     * Returns a message suitable for displaying in UI components like Toasts or Snackbars.
     * If the error was explicitly marked as user-friendly, returns the exception's localized message.
     * Otherwise, returns a generic fallback message.
     *
     * @return A string message for the user.
     */
    public String getToastMessage() {
        return isUserFriendly ? exception.getLocalizedMessage() : "An unforeseen issue prevented the process from completing successfully.";
    }

    /**
     * Gets the raw message from the wrapped exception.
     *
     * @return The exception's message string.
     */
    public String getErrorMessage() {
        return exception.getMessage();
    }

    /**
     * Checks if the error message is intended for the end-user.
     *
     * @return true if the message is user-friendly.
     */
    public boolean isUserFriendly() {
        return isUserFriendly;
    }

    /**
     * Sets whether the error message should be considered user-friendly.
     *
     * @param userFriendly true if the message is suitable for display to the user.
     * @return This SupabaseError instance for chaining.
     */
    public SupabaseError setUserFriendly(boolean userFriendly) {
        isUserFriendly = userFriendly;
        return this;
    }

    /**
     * Returns the message of the wrapped exception.
     *
     * @return The exception message.
     */
    @Nullable
    @Override
    public String toString() {
        return exception.getMessage();
    }
}
