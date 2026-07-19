package io.github.maskmasteruk.supabase.storage;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.github.maskmasteruk.supabase.core.Network.RequestHandler;
import io.github.maskmasteruk.supabase.core.Network.UrlBuilder;
import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.storage.Callback.OnCompleteCallback;
import io.github.maskmasteruk.supabase.storage.Callback.OnGetBucket;
import io.github.maskmasteruk.supabase.storage.Callback.OnGetBuckets;
import io.github.maskmasteruk.supabase.storage.Callback.OnProgressCallback;
import io.github.maskmasteruk.supabase.storage.Tasks.Task;

/**
 * Utility class for Supabase Storage.
 * <p>
 * Purpose: Provides shared utility methods for URL construction, MIME type detection,
 * file size calculation, error generation, and hashing.
 * </p>
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Building base URLs for storage endpoints.</li>
 *     <li>Detecting MIME types from files, URIs, and byte arrays.</li>
 *     <li>Calculating optimal buffer sizes for network operations.</li>
 *     <li>Mapping error responses to callbacks.</li>
 *     <li>Providing hashing utilities (SHA-256).</li>
 * </ul>
 * </p>
 */
public class Helper {

    /**
     * Creates a {@link UrlBuilder} pre-configured with the base storage path and version.
     *
     * @return A new {@link UrlBuilder} instance.
     */
    public static UrlBuilder getBaseStorageUrlBuilder() {
        return new UrlBuilder().appendPath(STORAGE_END_POINTS.STORAGE).appendPath(STORAGE_END_POINTS.VERSION);
    }

    /**
     * Retrieves the file size from a given {@link Uri}.
     *
     * @param context The application context.
     * @param uri     The URI of the file.
     * @return The size of the file in bytes, or 0 if it cannot be determined.
     */
    public static long getFileSizeFromUri(Context context, Uri uri) {
        long fileSize = 0;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        if (cursor != null) {
            int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
            if (cursor.moveToFirst() && sizeIndex != -1) {
                fileSize = cursor.getLong(sizeIndex);
            }
            cursor.close();
        }
        return fileSize;
    }

    /**
     * Determines the MIME type of a {@link File}.
     *
     * @param file The file to check.
     * @return The detected MIME type string, or "application/octet-stream" if unknown.
     */
    protected static String getMimeType(File file) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(file.getName());

        if (extension != null) {
            String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            if (mimeTypeFromExtension != null) {
                return mimeTypeFromExtension;
            }
        }

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String s = URLConnection.guessContentTypeFromStream(fileInputStream);
            if (s != null) {
                return s;
            }
        } catch (IOException ignored) {
        }

        return "application/octet-stream";
    }

    /**
     * Determines the MIME type of a {@link Uri}.
     *
     * @param context The application context.
     * @param uri     The URI to check.
     * @return The detected MIME type string, or "application/octet-stream" if unknown.
     */
    public static String getMimeType(Context context, Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());

            if (extension != null) {
                String mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                if (mimeTypeFromExtension != null) {
                    return mimeTypeFromExtension;
                }
            }
            return "application/octet-stream";
        }
        return mimeType;
    }

    /**
     * Determines the MIME type of a byte array.
     *
     * @param byteArray The data to check.
     * @return The detected MIME type string, or "application/octet-stream" if unknown.
     */
    public static String getMimeType(byte[] byteArray) {
        if (byteArray == null || byteArray.length == 0) {
            return "application/octet-stream";
        }

        try (InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(byteArray))) {
            String s = URLConnection.guessContentTypeFromStream(inputStream);
            if (s != null) {
                return s;
            }
        } catch (IOException ignored) {
        }
        return "application/octet-stream";
    }

    /**
     * Calculates an optimal buffer size based on the file size.
     *
     * @param fileSize The size of the file.
     * @return Recommended buffer size in bytes.
     */
    public static int getOptimalBufferSize(long fileSize) {
        if (fileSize <= 0) {
            return RequestHandler.MIN_BUFFER_SIZE;
        }else if (fileSize <= 500 * 1024) {
            return 4096;
        }else if (fileSize <= 2 * 1024 * 1024) {
            return 16384;
        }


        int calculatedSize = Math.toIntExact((fileSize / 100));
        return Math.max(RequestHandler.MIN_BUFFER_SIZE, Math.min(calculatedSize, RequestHandler.MAX_BUFFER_SIZE));
    }

    /**
     * Parses a {@link Response} for errors and notifies the appropriate callback.
     *
     * @param response The network response.
     * @param object   The callback object (e.g., OnGetBuckets, OnCompleteCallback).
     */
    public static void generateError(Response response, Object object) {
        SupabaseError supabaseError;

        try {
            supabaseError = new SupabaseError(response.getResponseJSON().toString(4));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        if (object == null) {
            Log.e("SupabasePostgrestError", supabaseError.getErrorMessage());
        } else if (object instanceof OnGetBuckets) {
            ((OnGetBuckets) object).onFailure(supabaseError);
        } else if (object instanceof OnGetBucket) {
            ((OnGetBucket) object).onFailure(supabaseError);
        } else if (object instanceof OnCompleteCallback) {
            ((OnCompleteCallback) object).onError(supabaseError);
        } else if (object instanceof OnProgressCallback) {
            ((OnProgressCallback) object).onError(supabaseError);
        } else {
            Log.e("SupabasePostgrestError", supabaseError.getErrorMessage());
        }

    }

    /**
     * Parses a {@link Response} for errors and notifies a {@link Task}.
     *
     * @param response The network response.
     * @param task     The task to notify of the error.
     */
    public static void generateError(Response response, Task task) {
        SupabaseError supabaseError;

        try {
            supabaseError = new SupabaseError(response.getResponseJSON().toString(4));
        } catch (Exception e) {
            supabaseError = new SupabaseError(response.getResponse());
        }

        if (task == null) {
            Log.e("SupabasePostgrestError", supabaseError.getErrorMessage());
        } else {
            task.onError(supabaseError);
        }

    }

    /**
     * Encodes a string to Base64 (NO_WRAP).
     *
     * @param input The string to encode.
     * @return The Base64 encoded string.
     */
    public static String encodeToBase64(String input) {
        return Base64.encodeToString(input.getBytes(), Base64.NO_WRAP);
    }

    /**
     * Generates a SHA-256 hash for a {@link File}.
     *
     * @param file The file to hash.
     * @return The hex string of the hash, or {@code null} on failure.
     */
    public static String generateHash(File file) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    messageDigest.update(buffer, 0, bytesRead);
                }

            } catch (IOException e) {
                return null;
            }

            byte[] hash = messageDigest.digest();
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * Generates a SHA-256 hash for a {@link String}.
     *
     * @param string The string to hash.
     * @return The hex string of the hash, or {@code null} on failure.
     */
    public static String generateHash(String string) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(string.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
