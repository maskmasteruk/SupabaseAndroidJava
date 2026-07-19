package io.github.maskmasteruk.supabase.postgrest.Object;

import static java.net.HttpURLConnection.HTTP_PARTIAL;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.IntStream;

import io.github.maskmasteruk.supabase.core.Objects.Response;
import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;
import io.github.maskmasteruk.supabase.postgrest.Query.AcceptHeader;
import io.github.maskmasteruk.supabase.postgrest.Query.DeleteQueryBuilder;
import io.github.maskmasteruk.supabase.postgrest.Query.InsertQueryBuilder;
import io.github.maskmasteruk.supabase.postgrest.Query.PostgrestConfig;
import io.github.maskmasteruk.supabase.postgrest.Query.SelectQueryBuilder;
import io.github.maskmasteruk.supabase.postgrest.Query.UpdateQueryBuilder;
import io.github.maskmasteruk.supabase.postgrest.Query.UpsertQueryBuilder;

/**
 * Represents the result of a PostgREST query execution.
 *
 * <p>This class encapsulates the response from the PostgREST server, including the raw response body,
 * HTTP status codes, and headers. It provides utility methods to parse the response into Java objects,
 * JSON objects, or arrays.</p>
 *
 * <p>It supports various query types such as SELECT, INSERT, UPDATE, UPSERT, and DELETE.</p>
 */
public class PostgrestResult {
    /**
     * Internal enum representing the type of PostgREST operation performed.
     */
    enum Type {
        /** Select operation. */
        SELECT("select"),
        /** Insert operation. */
        INSERT("insert"),
        /** Update operation. */
        UPDATE("update"),
        /** Upsert operation. */
        UPSERT("upsert"),
        /** Delete operation. */
        DELETE("delete");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        /**
         * Gets the string value of the operation type.
         * @return The operation type as a string.
         */
        public String getValue() {
            return value;
        }
    }

    /** The name of the table the query was executed against. */
    String tableName;
    /** The builder used for SELECT queries, if applicable. */
    SelectQueryBuilder selectQueryBuilder;
    /** The builder used for INSERT queries, if applicable. */
    InsertQueryBuilder insertQueryBuilder;
    /** The builder used for UPDATE queries, if applicable. */
    UpdateQueryBuilder updateQueryBuilder;
    /** The builder used for UPSERT queries, if applicable. */
    UpsertQueryBuilder upsertQueryBuilder;
    /** The builder used for DELETE queries, if applicable. */
    DeleteQueryBuilder deleteQueryBuilder;
    /** The configuration used for the request. */
    PostgrestConfig postgrestConfig;
    /** The underlying network response. */
    Response response;
    /** The type of operation performed. */
    Type type;

    /** Gson instance for JSON parsing. */
    Gson gson = new Gson();

    /**
     * Constructs a PostgrestResult for a SELECT operation.
     *
     * @param tableName Name of the table.
     * @param selectQueryBuilder Builder for the select query.
     * @param postgrestConfig Configuration for the request.
     * @param response The network response.
     */
    public PostgrestResult(String tableName, SelectQueryBuilder selectQueryBuilder, PostgrestConfig postgrestConfig, Response response) {
        this.tableName = tableName;
        this.selectQueryBuilder = selectQueryBuilder;
        this.postgrestConfig = postgrestConfig;
        this.response = response;
        type = Type.SELECT;
    }

    /**
     * Constructs a PostgrestResult for an INSERT operation.
     *
     * @param tableName Name of the table.
     * @param insertQueryBuilder Builder for the insert query.
     * @param postgrestConfig Configuration for the request.
     * @param response The network response.
     */
    public PostgrestResult(String tableName, InsertQueryBuilder insertQueryBuilder, PostgrestConfig postgrestConfig, Response response) {
        this.tableName = tableName;
        this.insertQueryBuilder = insertQueryBuilder;
        this.postgrestConfig = postgrestConfig;
        this.response = response;
        type = Type.INSERT;
    }

    /**
     * Constructs a PostgrestResult for an UPDATE operation.
     *
     * @param tableName Name of the table.
     * @param updateQueryBuilder Builder for the update query.
     * @param postgrestConfig Configuration for the request.
     * @param response The network response.
     */
    public PostgrestResult(String tableName, UpdateQueryBuilder updateQueryBuilder, PostgrestConfig postgrestConfig, Response response) {
        this.tableName = tableName;
        this.updateQueryBuilder = updateQueryBuilder;
        this.postgrestConfig = postgrestConfig;
        this.response = response;
        type = Type.UPDATE;
    }

    /**
     * Constructs a PostgrestResult for an UPSERT operation.
     *
     * @param tableName Name of the table.
     * @param upsertQueryBuilder Builder for the upsert query.
     * @param postgrestConfig Configuration for the request.
     * @param response The network response.
     */
    public PostgrestResult(String tableName, UpsertQueryBuilder upsertQueryBuilder, PostgrestConfig postgrestConfig, Response response) {
        this.tableName = tableName;
        this.upsertQueryBuilder = upsertQueryBuilder;
        this.postgrestConfig = postgrestConfig;
        this.response = response;
        type = Type.UPSERT;
    }

    /**
     * Constructs a PostgrestResult for a DELETE operation.
     *
     * @param tableName Name of the table.
     * @param deleteQueryBuilder Builder for the delete query.
     * @param postgrestConfig Configuration for the request.
     * @param response The network response.
     */
    public PostgrestResult(String tableName, DeleteQueryBuilder deleteQueryBuilder, PostgrestConfig postgrestConfig, Response response) {
        this.tableName = tableName;
        this.deleteQueryBuilder = deleteQueryBuilder;
        this.postgrestConfig = postgrestConfig;
        this.response = response;
        type = Type.UPSERT;
    }

    /**
     * Returns the raw response body as a string.
     *
     * @return The raw response content.
     */
    public String getRawData() {
        return response.getResponse();
    }

    /**
     * Checks if the response contains a single object instead of an array.
     *
     * <p>This is determined by checking the 'Content-Type' header for the 'vnd.pgrst.object' media type.</p>
     *
     * @return {@code true} if it's a single object response, {@code false} otherwise.
     */
    public boolean isSingleResponse() {
        if (response.getHeaderFields().containsKey("Content-Type")) {
            return Objects.requireNonNull(response.getHeaderFields().get("Content-Type")).get(0).contains(new AcceptHeader().Single(true).get()) || Objects.requireNonNull(response.getHeaderFields().get("Content-Type")).get(0).contains(new AcceptHeader().Single(false).get());
        }
        return false;
    }

    /**
     * Parses the response into a single object of the specified class.
     *
     * <p>If the response is an array, it returns the first element.</p>
     *
     * @param clazz The class to parse the response into.
     * @param <T> The type of the object.
     * @return The parsed object, or {@code null} if the response is empty.
     */
    public <T> T getSingle(Class<T> clazz) {
        if (!isSingleResponse()) {
            ArrayList<T> ts = get(clazz);
            return ts == null || ts.isEmpty() ? null : ts.get(0);
        }
        if (response.getResponse().isEmpty()) {
            return null;
        }
        return gson.fromJson(response.getResponse(), clazz);
    }

    /**
     * Parses the response into a single {@link JSONObject}.
     *
     * <p>If the response is an array, it returns the first element as a {@link JSONObject}.</p>
     *
     * @return The parsed {@link JSONObject}, or {@code null} if the response is empty.
     * @throws RuntimeException if there's an error parsing the JSON.
     */
    public JSONObject getSingle() {
        if (!isSingleResponse()) {
            try {
                JSONArray ts = get();
                return ts == null || ts.length() == 0 ? null : ts.getJSONObject(0);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        if (response.getResponse().isEmpty()) {
            return null;
        }
        return response.getResponseJSON();
    }

    /**
     * Parses the response into a {@link JSONArray}.
     *
     * <p>If the response is a single object, it wraps it in a {@link JSONArray}.</p>
     *
     * @return The parsed {@link JSONArray}, or {@code null} if the response is empty.
     */
    public JSONArray get() {
        if (response.getResponse().isEmpty()) {
            return null;
        }
        if (isSingleResponse()) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(getSingle());
            return jsonArray;
        }
        return JsonUtils.getJsonArray(response.getResponse());
    }

    /**
     * Parses the response into an {@link ArrayList} of objects of the specified class.
     *
     * <p>If the response is a single object, it returns a list containing that object.</p>
     *
     * @param clazz The class to parse the items into.
     * @param <T> The type of the objects.
     * @return An {@link ArrayList} of parsed objects, or {@code null} if the response is empty.
     */
    public <T> ArrayList<T> get(Class<T> clazz) {
        if (response.getResponse().isEmpty()) {
            return null;
        }
        if (isSingleResponse()) {
            ArrayList<T> arrayList = new ArrayList<>();
            arrayList.add(getSingle(clazz));
            return arrayList;
        }
        return gson.fromJson(response.getResponse(), TypeToken.getParameterized(ArrayList.class, clazz).getType());
    }

    /**
     * Gets the identifier (Location header) of the created/updated resource.
     *
     * @return The URI of the resource, or {@code null} if the 'Location' header is missing.
     */
    public String getIdentifier() {
        if (response.getHeaderFields().containsKey("Location") && !response.getHeaderFields().get("Location").isEmpty()) {
            return response.getHeaderFields().get("Location").get(0);
        }
        return null;
    }

    /**
     * Gets the count of records matching the query.
     *
     * <p>Requires that a count preference was set in the {@link PostgrestConfig}.
     * Parses the 'Content-Range' header to extract the total count.</p>
     *
     * @return The total count of records.
     * @throws NullPointerException if the 'Content-Range' header is missing.
     */
    public Long getCount() {
        if (postgrestConfig != null && !postgrestConfig.hasRequestedAnyCount() && !response.getHeaderFields().containsKey("Content-Range")) {
            Log.e("SupabasePostgrest", "Count not requested. Use SelectConfig with getExactCount(), getEstimatedCount(), or getPlannedCount().");
        }

        String count = Objects.requireNonNull(response.getHeaderFields().get("Content-Range")).get(0).split("/")[1];
        return Long.parseLong(count);
    }

    /**
     * Gets the range of records returned in the current response.
     *
     * <p>Requires that a count preference was set in the {@link PostgrestConfig}.
     * Parses the 'Content-Range' header to extract the range.</p>
     *
     * @return An {@link IntStream} representing the range (e.g., 0-9), or {@code null} if not available.
     */
    public IntStream getRange() {
        if (postgrestConfig != null && !postgrestConfig.hasRequestedAnyCount() && !response.getHeaderFields().containsKey("Content-Range")) {
            Log.e("SupabasePostgrest", "Count not requested. Use SelectConfig with getExactCount(), getEstimatedCount(), or getPlannedCount().");
            return null;
        }

        String[] range = Objects.requireNonNull(response.getHeaderFields().get("Content-Range")).get(0).split("/")[0].split("-");
        return IntStream.rangeClosed(Integer.parseInt(range[0]), Integer.parseInt(range[1]));
    }

    /**
     * Checks if the response is partial (HTTP 206).
     *
     * @return {@code true} if the response is partial, {@code false} otherwise.
     */
    public boolean isPartial() {
        return response.getCode() == HTTP_PARTIAL;
    }

}
