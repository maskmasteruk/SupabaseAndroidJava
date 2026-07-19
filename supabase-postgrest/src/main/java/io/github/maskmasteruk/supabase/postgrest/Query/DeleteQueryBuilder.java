package io.github.maskmasteruk.supabase.postgrest.Query;

import android.icu.util.TimeZone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builder for PostgREST DELETE queries.
 *
 * <p>This class provides a fluent API to construct DELETE requests, including
 * filters (constraints), returning data, and managing preferences like
 * row count limitations.</p>
 */
public class DeleteQueryBuilder {

    /** Internal builder for selecting columns to return in the response. */
    private final ColumnBuilder selectColumnBuilder = new ColumnBuilder();

    /** Set of preference headers. */
    private final Set<PreferHeader> preferHeaders = new LinkedHashSet<>();
    /** Set of preference headers as strings. */
    private final Set<String> preferHeadersString = new LinkedHashSet<>();

    /** List of constraints (filters) for the delete operation. */
    private final ArrayList<SelectQueryConstraint> constraints = new ArrayList<>();
    /** Additional headers for the request. */
    private final HashMap<String, String> headers = new HashMap<>();

    /** List of columns from referenced tables to return. */
    private final ArrayList<String> referenceTableColumns = new ArrayList<>();
    /** Accept header configuration. */
    private AcceptHeader acceptHeader;

    /** Whether to return a single object. */
    private boolean isSingle = false;
    /** The database schema. */
    private String schema = null;

    /**
     * Default constructor for DeleteQueryBuilder.
     */
    public DeleteQueryBuilder() {
    }

    /**
     * Gets the configured schema.
     * @return Schema name.
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Sets the database schema for this query.
     *
     * @param schema Schema name.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    /**
     * Selects a column to return in the response.
     * @param s Column name.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder select(String s) {
        selectColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Selects multiple columns to return in the response.
     * @param s Column names.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder select(String... s) {
        selectColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Selects a specific {@link Column} to return.
     * @param column Column enum.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder select(Column column) {
        selectColumnBuilder.addColumn(column);
        return this;
    }

    /**
     * Selects a list of columns to return.
     * @param s Column names.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder select(ArrayList<String> s) {
        selectColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Selects a column and aliases it in the response.
     * @param columnName Original column name.
     * @param newName Alias.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder selectAs(String columnName, String newName) {
        selectColumnBuilder.addColumnAs(columnName, newName);
        return this;
    }

    /**
     * Selects multiple columns with aliases from a map.
     * @param asNames Map of original names to aliases.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder selectAs(HashMap<String, String> asNames) {
        DeleteQueryBuilder UpdateQueryBuilder = this;
        for (Map.Entry<String, String> asName : asNames.entrySet()) {
            UpdateQueryBuilder = selectAs(asName.getKey(), asName.getValue());
        }
        return UpdateQueryBuilder;
    }

    private DeleteQueryBuilder addPreferHeader(PreferHeader preferHeader) {
        preferHeaders.add(preferHeader);
        return this;
    }

    private DeleteQueryBuilder addPreferHeader(String key, String value) {
        preferHeadersString.add(key + "=" + value);
        return this;
    }

    /**
     * Sets the preference to return the deleted records in the response.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder returnData() {
        return addPreferHeader(PreferHeader.RETURN_REPRESENTATION);
    }

    /**
     * Sets the preference to return no data in the response body.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder returnNothing() {
        return addPreferHeader(PreferHeader.RETURN_MINIMAL);
    }

    /**
     * Sets the resolution preference to merge duplicates on conflict.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder onConflictMergeDuplicates() {
        return addPreferHeader(PreferHeader.RESOLUTION_MERGE_DUPLICATES);
    }

    /**
     * Sets the resolution preference to ignore duplicates on conflict.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder onConflictIgnoreDuplicates() {
        return addPreferHeader(PreferHeader.RESOLUTION_IGNORE_DUPLICATES);
    }

    /**
     * Sets the preference to set missing columns to their default values.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder setMissingToDefault() {
        return addPreferHeader(PreferHeader.MISSING_DEFAULT);
    }

    /**
     * Sets the time zone for the request.
     * @param timeZone The time zone.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder setTimeZone(TimeZone timeZone) {
        return addPreferHeader(PreferHeader.TIMEZONE.getValue(), timeZone.getID());
    }

    /**
     * Sets the time zone to the system's default.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder setCurrentTimeZone() {
        return setTimeZone(TimeZone.getDefault());
    }

    /**
     * Sets the maximum number of rows that can be affected by the delete.
     * @param count Maximum row count.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder setMaxRowsAffected(int count) {
        addPreferHeader(PreferHeader.HANDLING_STRICT);
        return addPreferHeader("max-affected", String.valueOf(count));
    }

    /**
     * Adds a reference for returning related table data.
     * @param tableName Related table name.
     * @param preferredColumnName Alias.
     * @param join Join type.
     * @param columnBuilder Builder for related columns.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder addSelectReference(String tableName, String preferredColumnName, Join join, ColumnBuilder columnBuilder) {
        if (columnBuilder == null) {
            columnBuilder = new ColumnBuilder();
        }
        String refColumns = columnBuilder.buildColumn();
        if (!refColumns.isEmpty()) {
            referenceTableColumns.add((preferredColumnName != null ? preferredColumnName + ":" : "") + tableName + (join.getValue() == null ? "" : "!" + join.getValue()) + "(" + refColumns + ")");
        }

        return this;
    }

    /**
     * Adds a constraint (filter) to the delete query.
     * @param selectQueryConstraint The constraint.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder addConstraint(SelectQueryConstraint selectQueryConstraint) {
        constraints.add(selectQueryConstraint);
        return this;
    }

    /**
     * Adds a custom header to the request.
     * @param key Header name.
     * @param value Header value.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }


    /**
     * Equality filter.
     * @param columnName Column name.
     * @param value Value.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder equal(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().equal(columnName, value));
    }

    /**
     * Inequality filter.
     * @param columnName Column name.
     * @param value Value.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder notEqual(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().notEqual(columnName, value));
    }

    /**
     * Greater than filter.
     * @param columnName Column name.
     * @param value Value.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder greaterThan(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().greaterThan(columnName, value));
    }

    /**
     * Greater than or equal filter.
     * @param columnName Column name.
     * @param value Value.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder greaterThanOrEqual(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().greaterThanOrEqual(columnName, value));
    }

    /**
     * Less than filter.
     * @param columnName Column name.
     * @param value Value.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder lessThan(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().lessThan(columnName, value));
    }

    /**
     * Less than or equal filter.
     * @param columnName Column name.
     * @param value Value.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder lessThanOrEqual(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().lessThanOrEqual(columnName, value));
    }

    /**
     * Like pattern matching.
     * @param columnName Column name.
     * @param value Pattern.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder like(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().like(columnName, value));
    }

    /**
     * Ilike pattern matching (case-insensitive).
     * @param columnName Column name.
     * @param value Pattern.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder iLike(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().iLike(columnName, value));
    }

    /**
     * In filter.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder in(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().in(columnName, value));
    }

    /**
     * In filter with multiple values.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder in(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().in(columnName, values));
    }

    /**
     * Is null filter.
     * @param columnName Column name.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder isNull(String columnName) {
        return addConstraint(new SelectQueryConstraint().isNull(columnName));
    }

    /**
     * Is not null filter.
     * @param columnName Column name.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder isNotNull(String columnName) {
        return addConstraint(new SelectQueryConstraint().isNotNull(columnName));
    }

    /**
     * Array contains all filter.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder containsAll(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().containsAll(columnName, value));
    }

    /**
     * Array contains all filter.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder containsAll(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().containsAll(columnName, values));
    }

    /**
     * Array contains filter.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder contains(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().contains(columnName, value));
    }

    /**
     * Array contains filter.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder contains(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().contains(columnName, values));
    }

    /**
     * Array is subset filter.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder isSubsetOf(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().isSubsetOf(columnName, value));
    }

    /**
     * Array is subset filter.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder isSubsetOf(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().isSubsetOf(columnName, values));
    }

    /**
     * Array is contained by filter.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder containedBy(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().containedBy(columnName, value));
    }

    /**
     * Array is contained by filter.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder containedBy(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().containedBy(columnName, values));
    }

    /**
     * Array contains any filter.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder containsAny(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().containsAny(columnName, value));
    }

    /**
     * Array contains any filter.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder containsAny(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().containsAny(columnName, values));
    }

    /**
     * Range overlaps filter.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder overlaps(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().overlaps(columnName, value));
    }

    /**
     * Range overlaps filter.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder overlaps(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().overlaps(columnName, values));
    }

    /**
     * Strictly left range filter.
     * @param columnName Column name.
     * @param range Range.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder strictlyLeft(String columnName, String range) {
        return addConstraint(new SelectQueryConstraint().strictlyLeft(columnName, range));
    }

    /**
     * Strictly right range filter.
     * @param columnName Column name.
     * @param range Range.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder strictlyRight(String columnName, String range) {
        return addConstraint(new SelectQueryConstraint().strictlyRight(columnName, range));
    }

    /**
     * No extend left range filter.
     * @param columnName Column name.
     * @param range Range.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder notExtendLeft(String columnName, String range) {
        return addConstraint(new SelectQueryConstraint().notExtendLeft(columnName, range));
    }

    /**
     * No extend right range filter.
     * @param columnName Column name.
     * @param range Range.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder notExtendRight(String columnName, String range) {
        return addConstraint(new SelectQueryConstraint().notExtendRight(columnName, range));
    }

    /**
     * Adjacent range filter.
     * @param columnName Column name.
     * @param range Range.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder adjacent(String columnName, String range) {
        return addConstraint(new SelectQueryConstraint().adjacent(columnName, range));
    }

    /**
     * Regex matching filter.
     * @param columnName Column name.
     * @param regex Regex.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder matchRegex(String columnName, String regex) {
        return addConstraint(new SelectQueryConstraint().matchRegex(columnName, regex));
    }

    /**
     * Case-insensitive regex matching filter.
     * @param columnName Column name.
     * @param regex Regex.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder iMatchRegex(String columnName, String regex) {
        return addConstraint(new SelectQueryConstraint().iMatchRegex(columnName, regex));
    }

    /**
     * Negated regex matching filter.
     * @param columnName Column name.
     * @param regex Regex.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder notMatchRegex(String columnName, String regex) {
        return addConstraint(new SelectQueryConstraint().notMatchRegex(columnName, regex));
    }

    /**
     * Full-text search filter.
     * @param columnName Column name.
     * @param query Search query.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder fullTextSearch(String columnName, String query) {
        return addConstraint(new SelectQueryConstraint().fullTextSearch(columnName, query));
    }

    /**
     * Plain full-text search filter.
     * @param columnName Column name.
     * @param query Search query.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder plainFullTextSearch(String columnName, String query) {
        return addConstraint(new SelectQueryConstraint().plainFullTextSearch(columnName, query));
    }

    /**
     * Phrase full-text search filter.
     * @param columnName Column name.
     * @param query Search query.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder phraseFullTextSearch(String columnName, String query) {
        return addConstraint(new SelectQueryConstraint().phraseFullTextSearch(columnName, query));
    }

    /**
     * Web-style full-text search filter.
     * @param columnName Column name.
     * @param query Search query.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder webSearch(String columnName, String query) {
        return addConstraint(new SelectQueryConstraint().webSearch(columnName, query));
    }

    /**
     * Logical OR constraint.
     * @param selectQueryConstraints Child constraints.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder or(SelectQueryConstraint... selectQueryConstraints) {
        return addConstraint(new SelectQueryConstraint().or(selectQueryConstraints));
    }

    /**
     * Logical AND constraint.
     * @param selectQueryConstraints Child constraints.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder and(SelectQueryConstraint... selectQueryConstraints) {
        return addConstraint(new SelectQueryConstraint().and(selectQueryConstraints));
    }

    /**
     * Configures the query to return a single object.
     * @param stripNulls Whether to strip nulls.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder single(boolean stripNulls) {
        isSingle = true;
        acceptHeader = new AcceptHeader().Single(stripNulls);
        return addHeader("Accept", acceptHeader.get());
    }

    /**
     * Configures the query to return a single object.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder single() {
        return single(false);
    }

    /**
     * Checks if single object response is requested.
     * @return {@code true} if single, {@code false} otherwise.
     */
    public boolean isSingle() {
        return isSingle;
    }

    /**
     * Sets the data format.
     * @param acceptHeader Accept header.
     * @return This {@link DeleteQueryBuilder} instance for chaining.
     */
    public DeleteQueryBuilder setDataFormat(AcceptHeader acceptHeader) {
        this.acceptHeader = acceptHeader;
        return addHeader("Accept", acceptHeader.get());
    }

    /**
     * Gets the Accept header.
     * @return The {@link AcceptHeader}.
     */
    public AcceptHeader getAcceptHeader() {
        return acceptHeader;
    }

    /**
     * Builds the query parameters for constraints.
     * @return Map of query parameters.
     */
    public HashMap<String, String> buildConstraints() {
        HashMap<String, String> finalConstraints = new LinkedHashMap<>();
        for (SelectQueryConstraint constraint : constraints) {
            Map.Entry<String, String> build = constraint.build();
            finalConstraints.put(build.getKey(), build.getValue());
        }
        return finalConstraints;
    }

    /**
     * Builds all query parameters for the request.
     * @return Map of query parameters.
     */
    public HashMap<String, String> buildQueryParams() {
        HashMap<String, String> queryParams = buildConstraints();
        if (!selectColumnBuilder.isColumnsEmpty() || !referenceTableColumns.isEmpty()) {
            ArrayList<String> columns = selectColumnBuilder.isColumnsEmpty() ? new ArrayList<>() : selectColumnBuilder.getColumns();
            columns.addAll(referenceTableColumns);
            queryParams.put("select", String.join(",", columns));
        }
        return queryParams;
    }

    private String buildPreferHeader() {
        List<String> preferHeader = preferHeaders.stream().map(PreferHeader::getValue).collect(Collectors.toList());
        preferHeader.addAll(preferHeadersString);
        return String.join(",", preferHeader);
    }

    /**
     * Builds the headers for the request.
     * @return Map of headers.
     */
    public HashMap<String, String> buildHeaders() {
        if (schema != null) {
            headers.put("Accept-Profile", schema);
            headers.put("Content-Profile", schema);
        }
        String value = buildPreferHeader();
        if (!value.isEmpty()) {
            headers.put("Prefer", value);
        }
        return headers;
    }

}
