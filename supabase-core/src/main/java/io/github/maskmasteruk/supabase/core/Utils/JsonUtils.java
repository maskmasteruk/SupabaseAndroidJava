package io.github.maskmasteruk.supabase.core.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Utility class for working with JSON data.
 *
 * Responsibilities:
 * - Parsing JSON strings into JSONObject/JSONArray.
 * - Converting Maps to JSONObject.
 * - Providing builder classes for easier JSON construction.
 *
 * Usage:
 * JSONObject json = JsonUtils.getJsonObject("{\"key\": \"value\"}");
 *
 * Thread Safety:
 * Methods are thread-safe as they operate on local data.
 */
public class JsonUtils {

    /**
     * Parses a JSON string into a JSONObject.
     *
     * @param jsonString The JSON string to parse.
     * @return A JSONObject representing the data.
     * @throws SupabaseError if the string is not valid JSON.
     */
    public static JSONObject getJsonObject(String jsonString) {
        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            throw new SupabaseError(e);
        }
    }

    /**
     * Converts a Map to a JSONObject.
     *
     * @param inputData The map of data.
     * @return A JSONObject containing the map's data.
     */
    public static JSONObject toJsonObject(Map<String, Object> inputData) {
        if (inputData == null) {
            return new JSONObject();
        }

        return new JSONObject(inputData);
    }

    /**
     * Parses a JSON string into a JSONArray.
     *
     * @param jsonString The JSON string to parse.
     * @return A JSONArray representing the data.
     * @throws SupabaseError if the string is not valid JSON.
     */
    public static JSONArray getJsonArray(String jsonString) {
        try {
            return new JSONArray(jsonString);
        } catch (JSONException e) {
            throw new SupabaseError(e);
        }
    }

    /**
     * A builder class for creating JSON objects as strings.
     */
    public static class JsonObjectStringBuilder {
        /**
         * Internal storage for the JSON object data.
         */
        private final HashMap<String, Object> jsonObject;

        /**
         * Creates a new JsonObjectStringBuilder.
         */
        public JsonObjectStringBuilder() {
            jsonObject = new LinkedHashMap<>();
        }

        /**
         * Appends a key-value pair to the builder.
         *
         * @param key   The key.
         * @param value The value.
         * @return The builder instance for chaining.
         */
        public JsonObjectStringBuilder append(String key, Object value) {
            jsonObject.put(key, value);
            return this;
        }

        /**
         * Builds the final JSON string.
         *
         * @return The JSON string representation.
         */
        public String build() {
            return new JSONObject(jsonObject).toString();
        }
    }

    /**
     * A builder class for creating JSON arrays as strings.
     */
    public static class JsonArrayStringBuilder {
        /**
         * Internal storage for the JSON array data.
         */
        private final List<Object> jsonArray;

        /**
         * Creates a new JsonArrayStringBuilder.
         */
        public JsonArrayStringBuilder() {
            jsonArray = new ArrayList<>();
        }

        /**
         * Appends an object to the builder.
         *
         * @param object The object to add.
         * @return The builder instance for chaining.
         */
        public JsonArrayStringBuilder append(Object object) {
            jsonArray.add(object);
            return this;
        }

        /**
         * Builds the final JSON string.
         *
         * @return The JSON string representation.
         */
        public String build() {
            return new JSONArray(jsonArray).toString();
        }
    }

    /**
     * A builder class for creating JSONObject instances.
     */
    public static class JsonObjectBuilder {
        /**
         * Internal storage for the JSON object data.
         */
        private final HashMap<String, Object> jsonObject;

        /**
         * Creates a new JsonObjectBuilder.
         */
        public JsonObjectBuilder() {
            jsonObject = new LinkedHashMap<>();
        }

        /**
         * Appends a key-value pair to the builder.
         *
         * @param key   The key.
         * @param value The value.
         * @return The builder instance for chaining.
         */
        public JsonObjectBuilder append(String key, Object value) {
            jsonObject.put(key, value);
            return this;
        }

        /**
         * Builds the final JSONObject.
         *
         * @return The JSONObject instance.
         */
        public JSONObject build() {
            return new JSONObject(jsonObject);
        }
    }

    /**
     * A builder class for creating JSONArray instances.
     */
    public static class JsonArrayBuilder {
        /**
         * Internal storage for the JSON array data.
         */
        private final List<Object> jsonArray;

        /**
         * Creates a new JsonArrayBuilder.
         */
        public JsonArrayBuilder() {
            jsonArray = new ArrayList<>();
        }

        /**
         * Appends an object to the builder.
         *
         * @param object The object to add.
         * @return The builder instance for chaining.
         */
        public JsonArrayBuilder append(Object object) {
            jsonArray.add(object);
            return this;
        }


        /**
         * Builds the final JSONArray.
         *
         * @return The JSONArray instance.
         */
        public JSONArray build() {
            return new JSONArray(jsonArray);
        }
    }
}
