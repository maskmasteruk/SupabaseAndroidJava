package io.github.maskmasteruk.supabase.auth.Object;

import android.net.Uri;
import android.net.UrlQuerySanitizer;

/**
 * Represents an authentication error parsed from a redirection URI fragment.
 * <p>
 * When an authentication operation fails on the server and redirects back to the app,
 * error details are typically appended to the URI fragment. This class parses those
 * details.
 * <p>
 * <b>Architectural Responsibility:</b> Utility model for parsing error state from URIs.
 *
 * @since 1.0.0
 */
public class UriError {

    private String error;
    private String errorCode;
    private String errorDescription;
    private String sb;

    private boolean isError = true;

    /**
     * Default constructor for UriError.
     */
    public UriError() {
    }

    /**
     * Constructs a UriError by parsing the fragment of the given URI.
     *
     * @param uri The URI containing potential error parameters in its fragment.
     */
    public UriError(Uri uri) {
        String fragment = uri.getFragment();

        UrlQuerySanitizer sanitizer = new UrlQuerySanitizer();
        sanitizer.setAllowUnregisteredParamaters(true);
        sanitizer.parseQuery(fragment);

        if (!sanitizer.hasParameter("error")) {
            isError = false;
            return;
        }

        error = sanitizer.getValue("error");
        errorCode = sanitizer.getValue("error_code");
        errorDescription = sanitizer.getValue("error_description");
        sb = sanitizer.getValue("sb");
    }

    /**
     * Gets the error name.
     *
     * @return The error string.
     */
    public String getError() {
        return error;
    }

    /**
     * Gets the error code returned by Supabase.
     *
     * @return The error code string.
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets the error code.
     *
     * @param errorCode The error code string.
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Gets a human-readable description of the error.
     *
     * @return The error description string.
     */
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * Sets the error description.
     *
     * @param errorDescription The error description string.
     */
    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    /**
     * Gets the internal state value (if applicable).
     *
     * @return The sb value string.
     */
    public String getSb() {
        return sb;
    }

    /**
     * Sets the sb value.
     *
     * @param sb The sb value string.
     */
    public void setSb(String sb) {
        this.sb = sb;
    }

    /**
     * Checks if the URI actually contained an error.
     *
     * @return {@code true} if an error was found, {@code false} otherwise.
     */
    public boolean isError() {
        return isError;
    }

    /**
     * Sets the error name.
     *
     * @param error The error name.
     */
    public void setError(String error) {
        this.error = error;
    }

    /**
     * Sets whether an error exists.
     *
     * @param error The error status.
     */
    public void setError(boolean error) {
        isError = error;
    }

    public String toString() {
        return "UriError{" +
                "error='" + error + '\'' +
                ", error_code='" + errorCode + '\'' +
                ", error_description=" + errorDescription +
                ", sb=" + sb +
                '}';
    }
}

