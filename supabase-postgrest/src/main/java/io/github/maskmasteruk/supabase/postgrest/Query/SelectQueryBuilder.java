package io.github.maskmasteruk.supabase.postgrest.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builder for PostgREST SELECT queries.
 *
 * <p>This class provides a comprehensive API to construct SELECT queries, including
 * column selection, joining related tables, applying filters (constraints),
 * pagination, and ordering.</p>
 */
public class SelectQueryBuilder {

    /** Internal builder for column selection. */
    private final ColumnBuilder columnBuilder = new ColumnBuilder();

    /** List of constraints (filters) to apply to the query. */
    private final ArrayList<SelectQueryConstraint> constraints = new ArrayList<>();
    /** Additional headers for the request. */
    private final HashMap<String, String> headers = new HashMap<>();

    /** List of columns from referenced (joined) tables. */
    private final ArrayList<String> referenceTableColumns = new ArrayList<>();
    /** Builders for referenced (joined) tables. */
    private final HashMap<String, SelectQueryBuilder> referenceSelectQueryBuilders = new HashMap<>();
    /** The Accept header configuration for the request. */
    private AcceptHeader acceptHeader;

    /** Whether the query should return a single object. */
    private boolean isSingle = false;
    /** The database schema to use. */
    private String schema = null;

    /**
     * Default constructor for SelectQueryBuilder.
     */
    public SelectQueryBuilder() {
    }

    /**
     * Gets the configured database schema.
     * @return The schema name.
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Sets the database schema for this query.
     *
     * @param schema The schema name (e.g., "public").
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    /**
     * Selects a single column.
     *
     * @param s Column name.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder select(String s) {
        columnBuilder.addColumn(s);
        return this;
    }

    /**
     * Selects multiple columns.
     *
     * @param s Column names.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder select(String... s) {
        columnBuilder.addColumn(s);
        return this;
    }

    /**
     * Selects a column using the {@link Column} enum.
     *
     * @param column Column enum value (e.g., Column.ALL).
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder select(Column column) {
        columnBuilder.addColumn(column);
        return this;
    }

    /**
     * Selects a list of columns.
     *
     * @param s List of column names.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder select(ArrayList<String> s) {
        columnBuilder.addColumn(s);
        return this;
    }

    /**
     * Selects a column and aliases it.
     *
     * @param columnName Original column name.
     * @param newName Alias for the column.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder selectAs(String columnName, String newName) {
        columnBuilder.addColumnAs(columnName, newName);
        return this;
    }

    /**
     * Selects multiple columns with aliases.
     *
     * @param asNames Map of original names to aliases.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder selectAs(HashMap<String, String> asNames) {
        columnBuilder.addColumnAs(asNames);
        return this;
    }

    /**
     * Adds a reference (join) to another table.
     *
     * @param tableName Name of the referenced table.
     * @param preferredColumnName Optional alias for the joined table in the response.
     * @param join The type of join (Inner or Left).
     * @param selectQueryBuilder Builder for the columns and filters of the referenced table.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder addReference(String tableName, String preferredColumnName, Join join, SelectQueryBuilder selectQueryBuilder) {
        if (selectQueryBuilder == null) {
            selectQueryBuilder = new SelectQueryBuilder();
        }

        String refColumns = selectQueryBuilder.buildColumn();
        if (!refColumns.isEmpty()) {
            referenceTableColumns.add((preferredColumnName != null ? preferredColumnName + ":" : "") + tableName + (join.getValue() == null ? "" : "!" + join.getValue()) + "(" + refColumns + ")");
        }
        referenceSelectQueryBuilders.put((preferredColumnName != null ? preferredColumnName : tableName), selectQueryBuilder);

        return this;
    }

    /**
     * Adds a custom constraint to the query.
     *
     * @param selectQueryConstraint The constraint to add.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder addConstraint(SelectQueryConstraint selectQueryConstraint) {
        constraints.add(selectQueryConstraint);
        return this;
    }

    /**
     * Adds a custom header to the request.
     *
     * @param key Header name.
     * @param value Header value.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }


    /**
     * Adds an equality filter.
     * @param columnName Column name.
     * @param value Value to match.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder equal(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().equal(columnName, value));
    }

    /**
     * Adds a "not equal" filter.
     * @param columnName Column name.
     * @param value Value to match.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder notEqual(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().notEqual(columnName, value));
    }

    /**
     * Adds a "greater than" filter.
     * @param columnName Column name.
     * @param value Value to compare.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder greaterThan(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().greaterThan(columnName, value));
    }

    /**
     * Adds a "greater than or equal" filter.
     * @param columnName Column name.
     * @param value Value to compare.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder greaterThanOrEqual(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().greaterThanOrEqual(columnName, value));
    }

    /**
     * Adds a "less than" filter.
     * @param columnName Column name.
     * @param value Value to compare.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder lessThan(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().lessThan(columnName, value));
    }

    /**
     * Adds a "less than or equal" filter.
     * @param columnName Column name.
     * @param value Value to compare.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder lessThanOrEqual(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().lessThanOrEqual(columnName, value));
    }

    /**
     * Adds a "like" pattern matching filter (case-sensitive).
     * @param columnName Column name.
     * @param value Pattern.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder like(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().like(columnName, value));
    }

    /**
     * Adds an "ilike" pattern matching filter (case-insensitive).
     * @param columnName Column name.
     * @param value Pattern.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder iLike(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().iLike(columnName, value));
    }

    /**
     * Adds an "in" filter.
     * @param columnName Column name.
     * @param value Collection or string of values.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder in(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().in(columnName, value));
    }

    /**
     * Adds an "in" filter with multiple values.
     * @param columnName Column name.
     * @param values Values to match.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder in(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().in(columnName, values));
    }

    /**
     * Adds a filter for null values.
     * @param columnName Column name.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder isNull(String columnName) {
        return addConstraint(new SelectQueryConstraint().isNull(columnName));
    }

    /**
     * Adds a filter for non-null values.
     * @param columnName Column name.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder isNotNull(String columnName) {
        return addConstraint(new SelectQueryConstraint().isNotNull(columnName));
    }

    /**
     * Adds a filter to check if an array contains all specified values.
     * @param columnName Column name.
     * @param value Collection of values.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder containsAll(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().containsAll(columnName, value));
    }

    /**
     * Adds a filter to check if an array contains all specified values.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder containsAll(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().containsAll(columnName, values));
    }

    /**
     * Adds a filter to check if an array contains the specified values.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder contains(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().contains(columnName, value));
    }

    /**
     * Adds a filter to check if an array contains the specified values.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder contains(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().contains(columnName, values));
    }

    /**
     * Adds a filter to check if an array is a subset of the specified values.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder isSubsetOf(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().isSubsetOf(columnName, value));
    }

    /**
     * Adds a filter to check if an array is a subset of the specified values.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder isSubsetOf(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().isSubsetOf(columnName, values));
    }

    /**
     * Adds a filter to check if an array is contained by the specified values.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder containedBy(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().containedBy(columnName, value));
    }

    /**
     * Adds a filter to check if an array is contained by the specified values.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder containedBy(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().containedBy(columnName, values));
    }

    /**
     * Adds a filter to check if an array contains any of the specified values.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder containsAny(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().containsAny(columnName, value));
    }

    /**
     * Adds a filter to check if an array contains any of the specified values.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder containsAny(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().containsAny(columnName, values));
    }

    /**
     * Adds an "overlaps" filter for array columns.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder overlaps(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().overlaps(columnName, value));
    }

    /**
     * Adds an "overlaps" filter for array columns.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder overlaps(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().overlaps(columnName, values));
    }

    /**
     * Adds a "strictly left" range filter.
     * @param columnName Column name.
     * @param range Range string.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder strictlyLeft(String columnName, String range) {
        return addConstraint(new SelectQueryConstraint().strictlyLeft(columnName, range));
    }

    /**
     * Adds a "strictly right" range filter.
     * @param columnName Column name.
     * @param range Range string.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder strictlyRight(String columnName, String range) {
        return addConstraint(new SelectQueryConstraint().strictlyRight(columnName, range));
    }

    /**
     * Adds a "no extend left" range filter.
     * @param columnName Column name.
     * @param range Range string.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder notExtendLeft(String columnName, String range) {
        return addConstraint(new SelectQueryConstraint().notExtendLeft(columnName, range));
    }

    /**
     * Adds a "no extend right" range filter.
     * @param columnName Column name.
     * @param range Range string.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder notExtendRight(String columnName, String range) {
        return addConstraint(new SelectQueryConstraint().notExtendRight(columnName, range));
    }

    /**
     * Adds an "adjacent" range filter.
     * @param columnName Column name.
     * @param range Range string.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder adjacent(String columnName, String range) {
        return addConstraint(new SelectQueryConstraint().adjacent(columnName, range));
    }

    /**
     * Adds a regex matching filter.
     * @param columnName Column name.
     * @param regex Regex pattern.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder matchRegex(String columnName, String regex) {
        return addConstraint(new SelectQueryConstraint().matchRegex(columnName, regex));
    }

    /**
     * Adds an i-regex matching filter (case-insensitive).
     * @param columnName Column name.
     * @param regex Regex pattern.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder iMatchRegex(String columnName, String regex) {
        return addConstraint(new SelectQueryConstraint().iMatchRegex(columnName, regex));
    }

    /**
     * Adds a "not regex matching" filter.
     * @param columnName Column name.
     * @param regex Regex pattern.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder notMatchRegex(String columnName, String regex) {
        return addConstraint(new SelectQueryConstraint().notMatchRegex(columnName, regex));
    }

    /**
     * Adds a full-text search filter.
     * @param columnName Column name.
     * @param query Search query.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder fullTextSearch(String columnName, String query) {
        return addConstraint(new SelectQueryConstraint().fullTextSearch(columnName, query));
    }

    /**
     * Adds a plain full-text search filter.
     * @param columnName Column name.
     * @param query Search query.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder plainFullTextSearch(String columnName, String query) {
        return addConstraint(new SelectQueryConstraint().plainFullTextSearch(columnName, query));
    }

    /**
     * Adds a phrase full-text search filter.
     * @param columnName Column name.
     * @param query Search query.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder phraseFullTextSearch(String columnName, String query) {
        return addConstraint(new SelectQueryConstraint().phraseFullTextSearch(columnName, query));
    }

    /**
     * Adds a web-style full-text search filter.
     * @param columnName Column name.
     * @param query Search query.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder webSearch(String columnName, String query) {
        return addConstraint(new SelectQueryConstraint().webSearch(columnName, query));
    }

    /**
     * Adds a logical OR constraint.
     * @param selectQueryConstraints Child constraints.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder or(SelectQueryConstraint... selectQueryConstraints) {
        return addConstraint(new SelectQueryConstraint().or(selectQueryConstraints));
    }

    /**
     * Adds a logical AND constraint.
     * @param selectQueryConstraints Child constraints.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder and(SelectQueryConstraint... selectQueryConstraints) {
        return addConstraint(new SelectQueryConstraint().and(selectQueryConstraints));
    }

    /**
     * Sets the ordering for the query results.
     * @param orders Order definitions.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder order(Order... orders) {
        return addConstraint(new SelectQueryConstraint().order(orders));
    }

    /**
     * Limits the number of rows returned.
     * @param limit Maximum number of rows.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder limit(long limit) {
        return addConstraint(new SelectQueryConstraint().limit(limit));
    }

    /**
     * Sets the offset for pagination.
     * @param offset Number of rows to skip.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder offset(long offset) {
        return addConstraint(new SelectQueryConstraint().offset(offset));
    }

    /**
     * Sets the range for pagination using the Range header.
     * @param start Start index.
     * @param end End index.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder range(int start, int end) {
        return addHeader("Range", start + "-" + end);
    }

    /**
     * Configures the query to return a single object instead of an array.
     *
     * @param stripNulls Whether to strip null fields from the response.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder single(boolean stripNulls) {
        isSingle = true;
        acceptHeader = new AcceptHeader().Single(stripNulls);
        return addHeader("Accept", acceptHeader.get());
    }

    /**
     * Configures the query to return a single object.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder single() {
        return single(false);
    }

    /**
     * Checks if the query is configured for a single object response.
     * @return {@code true} if single, {@code false} otherwise.
     */
    public boolean isSingle() {
        return isSingle;
    }

    /**
     * Sets the data format using the Accept header.
     *
     * @param acceptHeader Accept header configuration.
     * @return This {@link SelectQueryBuilder} instance for chaining.
     */
    public SelectQueryBuilder setDataFormat(AcceptHeader acceptHeader) {
        this.acceptHeader = acceptHeader;
        return addHeader("Accept", acceptHeader.get());
    }

    /**
     * Gets the configured Accept header.
     * @return The {@link AcceptHeader} instance.
     */
    public AcceptHeader getAcceptHeader() {
        return acceptHeader;
    }

    /**
     * Builds the headers for the request.
     *
     * @return A map of headers.
     */
    public HashMap<String, String> buildHeaders() {
        if (schema != null) {
            headers.put("Accept-Profile", schema);
        }
        return headers;
    }

    /**
     * Builds the column selection string for the 'select' query parameter.
     *
     * @return The column selection string.
     */
    public String buildColumn() {
        ArrayList<String> finalColumns = new ArrayList<>(columnBuilder.getColumns());
        finalColumns.addAll(referenceTableColumns);
        return String.join(",", finalColumns);
    }

    /**
     * Builds the query parameters for the constraints.
     *
     * @param tableName Optional table name prefix.
     * @return A map of query parameters.
     */
    public HashMap<String, String> buildConstraints(String tableName) {
        HashMap<String, String> finalConstraints = new LinkedHashMap<>();
        for (SelectQueryConstraint constraint : constraints) {
            Map.Entry<String, String> build = constraint.build(tableName);
            finalConstraints.put(build.getKey(), build.getValue());
        }
        for (Map.Entry<String, SelectQueryBuilder> keyValue : referenceSelectQueryBuilders.entrySet()) {
            String paramTableName = (tableName != null ? tableName + "." : "") + keyValue.getKey();
            finalConstraints.putAll(keyValue.getValue().buildConstraints(paramTableName));
        }
        return finalConstraints;
    }

    /**
     * Builds the full map of query parameters for the request.
     *
     * @return A map of query parameters including 'select' and filters.
     */
    public HashMap<String, String> buildQueryParams() {
        HashMap<String, String> queryParams = buildConstraints(null);
        queryParams.put("select", buildColumn());
        return queryParams;
    }

}
