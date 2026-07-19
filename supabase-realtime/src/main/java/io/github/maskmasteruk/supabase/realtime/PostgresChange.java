package io.github.maskmasteruk.supabase.realtime;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;
import io.github.maskmasteruk.supabase.realtime.Enum.PostgresChangeEvent;

/**
 * Represents a configuration for subscribing to PostgreSQL database changes.
 *
 * <p>This class defines which table, schema, and events (INSERT, UPDATE, DELETE)
 * should be monitored. It also supports complex filtering using {@link PostgresChangeFilter}.</p>
 *
 * <p>Before using this, ensure that Realtime is enabled for the target table in your
 * Supabase dashboard.</p>
 *
 * <pre>{@code
 * PostgresChange change = new PostgresChange(
 *     PostgresChangeEvent.INSERT,
 *     "public",
 *     "messages",
 *     new PostgresChange.PostgresChangeFilter.Equals("room_id", 123).build()
 * );
 * }</pre>
 */
public class PostgresChange {
    private final PostgresChangeEvent event;
    private final String schema;
    private final String table;
    private final PostgresChangeFilter postgresChangeFilter;

    /**
     * Constructs a new PostgresChange subscription configuration.
     *
     * @param event The type of database event to listen for.
     * @param schema The database schema (usually "public").
     * @param table The database table name.
     * @param postgresChangeFilter Optional filter to narrow down the events.
     */
    public PostgresChange(PostgresChangeEvent event, String schema, String table, PostgresChangeFilter postgresChangeFilter) {
        this.event = event;
        this.schema = schema;
        this.table = table;
        this.postgresChangeFilter = postgresChangeFilter;
    }

    /**
     * Converts this configuration into a JSONObject for the Phoenix protocol.
     *
     * @return A JSONObject representing the change configuration.
     */
    public JSONObject toBuilderObject() {
        JsonUtils.JsonObjectBuilder jsonObjectBuilder = new JsonUtils.JsonObjectBuilder()
                .append("event", this.event != null ? this.event.getEventValue() : null)
                .append("schema", this.schema)
                .append("table", this.table);
        if (postgresChangeFilter != null) {
            jsonObjectBuilder.append("filter", this.postgresChangeFilter.build());
        }
        return jsonObjectBuilder
                .build();
    }

    /**
     * Defines filters for Postgres CDC (Change Data Capture).
     *
     * <p>Filters allow you to subscribe to only a subset of changes, such as rows
     * where a specific column matches a value.</p>
     */
    public static class PostgresChangeFilter {
        private final String columnName;
        private final String operator;
        private final Object value;

        /**
         * Internal constructor for PostgresChangeFilter.
         */
        private PostgresChangeFilter(String columnName, String operator, Object value) {
            this.columnName = columnName;
            this.operator = operator;
            this.value = value;
        }

        /** Filter for equality (eq). */
        public static class Equals {
            private final String columnName;
            private final Object value;

            public Equals(String columnName, Object value) {
                this.columnName = columnName;
                this.value = value;
            }

            public PostgresChangeFilter build() {
                return new PostgresChangeFilter(columnName, "eq", value);
            }
        }

        /** Filter for inequality (neq). */
        public static class NotEquals {
            private final String columnName;
            private final Object value;

            public NotEquals(String columnName, Object value) {
                this.columnName = columnName;
                this.value = value;
            }

            public PostgresChangeFilter build() {
                return new PostgresChangeFilter(columnName, "neq", value);
            }
        }

        /** Filter for less than (lt). */
        public static class LessThan {
            private final String columnName;
            private final Object value;

            public LessThan(String columnName, Object value) {
                this.columnName = columnName;
                this.value = value;
            }

            public PostgresChangeFilter build() {
                return new PostgresChangeFilter(columnName, "lt", value);
            }
        }

        /** Filter for less than or equal to (lte). */
        public static class LessThanOrEquals {
            private final String columnName;
            private final Object value;

            public LessThanOrEquals(String columnName, Object value) {
                this.columnName = columnName;
                this.value = value;
            }

            public PostgresChangeFilter build() {
                return new PostgresChangeFilter(columnName, "lte", value);
            }
        }

        /** Filter for greater than (gt). */
        public static class GreaterThan {
            private final String columnName;
            private final Object value;

            public GreaterThan(String columnName, Object value) {
                this.columnName = columnName;
                this.value = value;
            }

            public PostgresChangeFilter build() {
                return new PostgresChangeFilter(columnName, "gt", value);
            }
        }

        /** Filter for greater than or equal to (gte). */
        public static class GreaterThanOrEquals {
            private final String columnName;
            private final Object value;

            public GreaterThanOrEquals(String columnName, Object value) {
                this.columnName = columnName;
                this.value = value;
            }

            public PostgresChangeFilter build() {
                return new PostgresChangeFilter(columnName, "gte", value);
            }
        }

        /** Filter for checking if a value is in a list (in). */
        public static class In {
            private final String columnName;
            private final String value;

            public In(String columnName, Object... value) {
                this.columnName = columnName;
                this.value = convertIterableObjectIntoString(Arrays.asList(value));
            }

            public PostgresChangeFilter build() {
                return new PostgresChangeFilter(columnName, "in", value);
            }
        }

        /** Filter for checking if a value is null (is). */
        public static class IsNull {
            private final String columnName;
            public IsNull(String columnName) {
                this.columnName = columnName;
            }

            public PostgresChangeFilter build() {
                return new PostgresChangeFilter(columnName, "is", "null");
            }
        }

        /** Filter for checking if a boolean column is true (is). */
        public static class IsTrue {
            private final String columnName;
            public IsTrue(String columnName) {
                this.columnName = columnName;
            }

            public PostgresChangeFilter build() {
                return new PostgresChangeFilter(columnName, "is", "true");
            }
        }

        /** Filter for checking if a boolean column is false (is). */
        public static class IsFalse {
            private final String columnName;
            public IsFalse(String columnName) {
                this.columnName = columnName;
            }

            public PostgresChangeFilter build() {
                return new PostgresChangeFilter(columnName, "is", "false");
            }
        }

        /** Filter for checking if a value is unknown (is). */
        public static class IsUnknown {
            private final String columnName;
            public IsUnknown(String columnName) {
                this.columnName = columnName;
            }

            public PostgresChangeFilter build() {
                return new PostgresChangeFilter(columnName, "is", "unknown");
            }
        }

        /** Filter for checking distinctness (isdistinct). */
        public static class IsDistinct {
            private final String columnName;
            private final Object value;

            public IsDistinct(String columnName, Object value) {
                this.columnName = columnName;
                this.value = value;
            }

            public PostgresChangeFilter build() {
                return new PostgresChangeFilter(columnName, "isdistinct", value);
            }
        }

        /** Filter for pattern matching (like). */
        public static class Like {
            private final String columnName;
            private final Object value;

            public Like(String columnName, Object value) {
                this.columnName = columnName;
                this.value = value;
            }

            public PostgresChangeFilter build() {
                return new PostgresChangeFilter(columnName, "like", value);
            }
        }

        /** Case-insensitive pattern matching (ilike). */
        public static class ILike {
            private final String columnName;
            private final Object value;

            public ILike(String columnName, Object value) {
                this.columnName = columnName;
                this.value = value;
            }

            public PostgresChangeFilter build() {
                return new PostgresChangeFilter(columnName, "ilike", value);
            }
        }

        /** Full-text search match (match). */
        public static class Match {
            private final String columnName;
            private final Object value;

            public Match(String columnName, Object value) {
                this.columnName = columnName;
                this.value = value;
            }

            public PostgresChangeFilter build() {
                return new PostgresChangeFilter(columnName, "match", value);
            }
        }

        /** Case-insensitive full-text search match (imatch). */
        public static class IMatch {
            private final String columnName;
            private final Object value;

            public IMatch(String columnName, Object value) {
                this.columnName = columnName;
                this.value = value;
            }

            public PostgresChangeFilter build() {
                return new PostgresChangeFilter(columnName, "imatch", value);
            }
        }


        /**
         * Builds the filter string used in the Phoenix protocol.
         *
         * @return The filter string (e.g., "id=eq.1").
         */
        private String build() {
            return columnName + "=" + operator + "." + value;
        }
    }

    /**
     * Utility method to convert an iterable or string into the format expected by the "in" filter.
     *
     * @param value The value to convert.
     * @return The formatted string.
     */
    private static String convertIterableObjectIntoString(Object value) {
        if (value instanceof Iterable) {
            value = StreamSupport.stream(((Iterable<?>) value).spliterator(), false)
                    .map(String::valueOf)
                    .collect(Collectors.joining(",", "(", ")"));
        } else if (value instanceof String) {
            if (!((String) value).startsWith("(")) {
                value = "(" +value;
            }
            if (!((String) value).endsWith(")")) {
                value = value + ")";
            }
        }
        return value.toString();
    }

}

