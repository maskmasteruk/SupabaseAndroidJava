package io.github.maskmasteruk.supabase.postgrest.Query;

import android.icu.util.TimeZone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Builder for PostgREST INSERT queries.
 *
 * <p>This class provides a fluent API to construct INSERT requests, supporting
 * single and bulk inserts, column selection for the response, upsert options,
 * and conflict resolution preferences.</p>
 */
public class InsertQueryBuilder {

    /** Internal builder for selecting columns to return in the response. */
    private final ColumnBuilder selectColumnBuilder = new ColumnBuilder();
    /** Internal builder for specifying which columns to include in the insert. */
    private final ColumnBuilder insertColumnBuilder = new ColumnBuilder();
    /** Internal builder for specifying columns to check for conflicts during upsert. */
    private final ColumnBuilder conflictColumnBuilder = new ColumnBuilder();
    /** List of columns from referenced tables to return. */
    private final ArrayList<String> referenceTableColumns = new ArrayList<>();
    /** Additional headers for the request. */
    private final HashMap<String, String> headers = new HashMap<>();
    /** Accept header configuration. */
    private AcceptHeader acceptHeader;
    /** Whether to return a single object. */
    private boolean isSingle = false;

    /** The database schema. */
    private String schema;

    /** Set of preference headers. */
    private final Set<PreferHeader> preferHeaders = new LinkedHashSet<>();
    /** Set of preference headers as strings. */
    private final Set<String> preferHeadersString = new LinkedHashSet<>();

    /**
     * Default constructor for InsertQueryBuilder.
     */
    public InsertQueryBuilder() {
    }

    /**
     * Sets the database schema for this query.
     * @param schema Schema name.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    /**
     * Selects a column to return in the response.
     * @param s Column name.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder select(String s) {
        selectColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Selects multiple columns to return.
     * @param s Column names.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder select(String... s) {
        selectColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Selects a specific {@link Column} to return.
     * @param column Column enum.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder select(Column column) {
        selectColumnBuilder.addColumn(column);
        return this;
    }

    /**
     * Selects a list of columns to return.
     * @param s Column names.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder select(ArrayList<String> s) {
        selectColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Selects a column with an alias in the response.
     * @param columnName Original name.
     * @param newName Alias.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder selectAs(String columnName, String newName) {
        selectColumnBuilder.addColumnAs(columnName, newName);
        return this;
    }

    /**
     * Selects multiple columns with aliases from a map.
     * @param asNames Map of names to aliases.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder selectAs(HashMap<String, String> asNames) {
        InsertQueryBuilder InsertQueryBuilder = this;
        for (Map.Entry<String, String> asName : asNames.entrySet()) {
            InsertQueryBuilder = selectAs(asName.getKey(), asName.getValue());
        }
        return InsertQueryBuilder;
    }

    /**
     * Specifies a column to be included in the insert operation.
     * @param s Column name.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder insertOnly(String s) {
        insertColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Specifies multiple columns to be included in the insert operation.
     * @param s Column names.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder insertOnly(String... s) {
        insertColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Specifies a list of columns to be included in the insert operation.
     * @param s Column names.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder insertOnly(ArrayList<String> s) {
        insertColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Adds a column to check for conflicts during an upsert operation.
     * @param s Column name.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder addConflictColumn(String s) {
        conflictColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Adds multiple columns to check for conflicts during an upsert operation.
     * @param s Column names.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder addConflictColumn(String... s) {
        conflictColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Adds a list of columns to check for conflicts during an upsert operation.
     * @param s Column names.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder addConflictColumn(ArrayList<String> s) {
        conflictColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Adds a reference for returning related table data.
     * @param tableName Related table name.
     * @param preferredColumnName Alias.
     * @param join Join type.
     * @param columnBuilder Builder for related columns.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder addReference(String tableName, String preferredColumnName, Join join, ColumnBuilder columnBuilder) {
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
     * Adds a custom header to the request.
     * @param key Header name.
     * @param value Header value.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    private InsertQueryBuilder addPreferHeader(PreferHeader preferHeader) {
        preferHeaders.add(preferHeader);
        return this;
    }

    private InsertQueryBuilder addPreferHeader(String key, String value) {
        preferHeadersString.add(key + "=" + value);
        return this;
    }

    /**
     * Preference to return the inserted records.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder returnData() {
        return addPreferHeader(PreferHeader.RETURN_REPRESENTATION);
    }

    /**
     * Preference to return only headers (e.g., Location for the new resource).
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder returnIdentifier() {
        return addPreferHeader(PreferHeader.RETURN_HEADERS_ONLY);
    }

    /**
     * Preference to return no data in the response body.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder returnNothing() {
        return addPreferHeader(PreferHeader.RETURN_MINIMAL);
    }

    /**
     * Configures the insert as an upsert (update on conflict).
     * @param conflictColumns Columns to check for conflicts.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder upsert(String... conflictColumns) {
        onConflictMergeDuplicates();
        return addConflictColumn(conflictColumns);
    }

    /**
     * Preference to merge duplicates on conflict (UPSERT).
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder onConflictMergeDuplicates() {
        return addPreferHeader(PreferHeader.RESOLUTION_MERGE_DUPLICATES);
    }

    /**
     * Preference to ignore duplicates on conflict.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder onConflictIgnoreDuplicates() {
        return addPreferHeader(PreferHeader.RESOLUTION_IGNORE_DUPLICATES);
    }

    /**
     * Preference to set missing columns to their default values.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder setMissingToDefault() {
        return addPreferHeader(PreferHeader.MISSING_DEFAULT);
    }

    /**
     * Sets the time zone.
     * @param timeZone Time zone.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder setTimeZone(TimeZone timeZone) {
        addPreferHeader(PreferHeader.TIMEZONE.getValue(), timeZone.getID());
        return this;
    }

    /**
     * Sets current time zone.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder setCurrentTimeZone() {
        return setTimeZone(TimeZone.getDefault());
    }

    /**
     * Configures the query to return a single object.
     * @param stripNulls Whether to strip nulls.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder single(boolean stripNulls) {
        isSingle = true;
        acceptHeader = new AcceptHeader().Single(stripNulls);
        return addHeader("Accept", acceptHeader.get());
    }

    /**
     * Configures the query to return a single object.
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder single() {
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
     * @return This {@link InsertQueryBuilder} instance for chaining.
     */
    public InsertQueryBuilder setDataFormat(AcceptHeader acceptHeader) {
        this.acceptHeader = acceptHeader;
        return addHeader("Accept", acceptHeader.get());
    }

    /**
     * Builds all query parameters for the request.
     * @return Map of query parameters.
     */
    public HashMap<String, String> buildQueryParams() {
        HashMap<String, String> queryParams = new HashMap<>();
        if (!selectColumnBuilder.isColumnsEmpty()) {
            ArrayList<String> columns = selectColumnBuilder.getColumns();
            columns.addAll(referenceTableColumns);
            queryParams.put("select", String.join(",", columns));
        }
        if (!conflictColumnBuilder.isColumnsEmpty()) {
            queryParams.put("on_conflict", conflictColumnBuilder.buildColumn());
        }
        if (!insertColumnBuilder.isColumnsEmpty()) {
            queryParams.put("columns", insertColumnBuilder.buildColumn());
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
