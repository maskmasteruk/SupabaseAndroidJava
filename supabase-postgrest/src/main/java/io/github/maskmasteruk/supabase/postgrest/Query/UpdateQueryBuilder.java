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
 * Builder for PostgREST UPDATE queries.
 *
 * <p>This class provides a fluent API to construct UPDATE requests, allowing
 * specified column updates, filters (constraints), and response preferences.</p>
 */
public class UpdateQueryBuilder {

    /** Internal builder for selecting columns to return in the response. */
    private final ColumnBuilder selectColumnBuilder = new ColumnBuilder();
    /** Internal builder for specifying which columns to include in the update. */
    private final ColumnBuilder updateColumnBuilder = new ColumnBuilder();
    /** Internal builder for specifying conflict columns (if applicable for some backends). */
    private final ColumnBuilder conflictColumnBuilder = new ColumnBuilder();

    /** Set of preference headers. */
    private final Set<PreferHeader> preferHeaders = new LinkedHashSet<>();
    /** Set of preference headers as strings. */
    private final Set<String> preferHeadersString = new LinkedHashSet<>();

    /** List of constraints (filters) to apply to the update. */
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
     * Default constructor for UpdateQueryBuilder.
     */
    public UpdateQueryBuilder() {
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
     * @param schema Schema name.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    /**
     * Selects a column to return in the response.
     * @param s Column name.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder select(String s) {
        selectColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Selects multiple columns to return in the response.
     * @param s Column names.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder select(String... s) {
        selectColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Selects a specific {@link Column} to return.
     * @param column Column enum.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder select(Column column) {
        selectColumnBuilder.addColumn(column);
        return this;
    }

    /**
     * Selects a list of columns to return.
     * @param s Column names.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder select(ArrayList<String> s) {
        selectColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Selects a column and aliases it in the response.
     * @param columnName Original name.
     * @param newName Alias.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder selectAs(String columnName, String newName) {
        selectColumnBuilder.addColumnAs(columnName, newName);
        return this;
    }

    /**
     * Selects multiple columns with aliases from a map.
     * @param asNames Map of names to aliases.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder selectAs(HashMap<String, String> asNames) {
        UpdateQueryBuilder UpdateQueryBuilder = this;
        for (Map.Entry<String, String> asName : asNames.entrySet()) {
            UpdateQueryBuilder = selectAs(asName.getKey(), asName.getValue());
        }
        return UpdateQueryBuilder;
    }

    /**
     * Specifies a column to be included in the update.
     * @param s Column name.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder updateOnly(String s) {
        updateColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Specifies multiple columns to be included in the update.
     * @param s Column names.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder updateOnly(String... s) {
        updateColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Specifies a list of columns to be included in the update.
     * @param s Column names.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder updateOnly(ArrayList<String> s) {
        updateColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Adds a conflict column.
     * @param s Column name.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder addConflictColumn(String s) {
        conflictColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Adds multiple conflict columns.
     * @param s Column names.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder addConflictColumn(String... s) {
        conflictColumnBuilder.addColumn(s);
        return this;
    }

    /**
     * Adds a list of conflict columns.
     * @param s Column names.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder addConflictColumn(ArrayList<String> s) {
        conflictColumnBuilder.addColumn(s);
        return this;
    }

    private UpdateQueryBuilder addPreferHeader(PreferHeader preferHeader) {
        preferHeaders.add(preferHeader);
        return this;
    }

    private UpdateQueryBuilder addPreferHeader(String key, String value) {
        preferHeadersString.add(key + "=" + value);
        return this;
    }

    /**
     * Preference to return the updated records in the response.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder returnData() {
        return addPreferHeader(PreferHeader.RETURN_REPRESENTATION);
    }

    /**
     * Preference to return no data in the response body.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder returnNothing() {
        return addPreferHeader(PreferHeader.RETURN_MINIMAL);
    }

    /**
     * Preference to merge duplicates on conflict.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder onConflictMergeDuplicates() {
        return addPreferHeader(PreferHeader.RESOLUTION_MERGE_DUPLICATES);
    }

    /**
     * Preference to ignore duplicates on conflict.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder onConflictIgnoreDuplicates() {
        return addPreferHeader(PreferHeader.RESOLUTION_IGNORE_DUPLICATES);
    }

    /**
     * Preference to set missing columns to their default values.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder setMissingToDefault() {
        return addPreferHeader(PreferHeader.MISSING_DEFAULT);
    }

    /**
     * Sets the time zone.
     * @param timeZone Time zone.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder setTimeZone(TimeZone timeZone) {
        return addPreferHeader(PreferHeader.TIMEZONE.getValue(), timeZone.getID());
    }

    /**
     * Sets current time zone.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder setCurrentTimeZone() {
        return setTimeZone(TimeZone.getDefault());
    }

    /**
     * Adds a reference for returning related table data.
     * @param tableName Related table name.
     * @param preferredColumnName Alias.
     * @param join Join type.
     * @param columnBuilder Builder for related columns.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder addSelectReference(String tableName, String preferredColumnName, Join join, ColumnBuilder columnBuilder) {
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
     * Adds a constraint (filter) to the update query.
     * @param selectQueryConstraint The constraint.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder addConstraint(SelectQueryConstraint selectQueryConstraint) {
        constraints.add(selectQueryConstraint);
        return this;
    }

    /**
     * Adds a custom header to the request.
     * @param key Header name.
     * @param value Header value.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }


    /**
     * Equality filter.
     * @param columnName Column name.
     * @param value Value.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder equal(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().equal(columnName, value));
    }

    /**
     * Inequality filter.
     * @param columnName Column name.
     * @param value Value.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder notEqual(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().notEqual(columnName, value));
    }

    /**
     * Greater than filter.
     * @param columnName Column name.
     * @param value Value.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder greaterThan(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().greaterThan(columnName, value));
    }

    /**
     * Greater than or equal filter.
     * @param columnName Column name.
     * @param value Value.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder greaterThanOrEqual(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().greaterThanOrEqual(columnName, value));
    }

    /**
     * Less than filter.
     * @param columnName Column name.
     * @param value Value.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder lessThan(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().lessThan(columnName, value));
    }

    /**
     * Less than or equal filter.
     * @param columnName Column name.
     * @param value Value.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder lessThanOrEqual(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().lessThanOrEqual(columnName, value));
    }

    /**
     * Like pattern matching.
     * @param columnName Column name.
     * @param value Pattern.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder like(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().like(columnName, value));
    }

    /**
     * Case-insensitive like pattern matching.
     * @param columnName Column name.
     * @param value Pattern.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder iLike(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().iLike(columnName, value));
    }

    /**
     * In filter.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder in(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().in(columnName, value));
    }

    /**
     * In filter with multiple values.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder in(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().in(columnName, values));
    }

    /**
     * Is null filter.
     * @param columnName Column name.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder isNull(String columnName) {
        return addConstraint(new SelectQueryConstraint().isNull(columnName));
    }

    /**
     * Is not null filter.
     * @param columnName Column name.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder isNotNull(String columnName) {
        return addConstraint(new SelectQueryConstraint().isNotNull(columnName));
    }

    /**
     * Array contains all filter.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder containsAll(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().containsAll(columnName, value));
    }

    /**
     * Array contains all filter.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder containsAll(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().containsAll(columnName, values));
    }

    /**
     * Array contains filter.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder contains(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().contains(columnName, value));
    }

    /**
     * Array contains filter.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder contains(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().contains(columnName, values));
    }

    /**
     * Array is subset filter.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder isSubsetOf(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().isSubsetOf(columnName, value));
    }

    /**
     * Array is subset filter.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder isSubsetOf(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().isSubsetOf(columnName, values));
    }

    /**
     * Array is contained by filter.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder containedBy(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().containedBy(columnName, value));
    }

    /**
     * Array is contained by filter.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder containedBy(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().containedBy(columnName, values));
    }

    /**
     * Array contains any filter.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder containsAny(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().containsAny(columnName, value));
    }

    /**
     * Array contains any filter.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder containsAny(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().containsAny(columnName, values));
    }

    /**
     * Range overlaps filter.
     * @param columnName Column name.
     * @param value Values.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder overlaps(String columnName, Object value) {
        return addConstraint(new SelectQueryConstraint().overlaps(columnName, value));
    }

    /**
     * Range overlaps filter.
     * @param columnName Column name.
     * @param values Values.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder overlaps(String columnName, Object... values) {
        return addConstraint(new SelectQueryConstraint().overlaps(columnName, values));
    }

    /**
     * Strictly left range filter.
     * @param columnName Column name.
     * @param range Range.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder strictlyLeft(String columnName, String range) {
        return addConstraint(new SelectQueryConstraint().strictlyLeft(columnName, range));
    }

    /**
     * Strictly right range filter.
     * @param columnName Column name.
     * @param range Range.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder strictlyRight(String columnName, String range) {
        return addConstraint(new SelectQueryConstraint().strictlyRight(columnName, range));
    }

    /**
     * No extend left range filter.
     * @param columnName Column name.
     * @param range Range.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder notExtendLeft(String columnName, String range) {
        return addConstraint(new SelectQueryConstraint().notExtendLeft(columnName, range));
    }

    /**
     * No extend right range filter.
     * @param columnName Column name.
     * @param range Range.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder notExtendRight(String columnName, String range) {
        return addConstraint(new SelectQueryConstraint().notExtendRight(columnName, range));
    }

    /**
     * Adjacent range filter.
     * @param columnName Column name.
     * @param range Range.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder adjacent(String columnName, String range) {
        return addConstraint(new SelectQueryConstraint().adjacent(columnName, range));
    }

    /**
     * Regex matching filter.
     * @param columnName Column name.
     * @param regex Regex.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder matchRegex(String columnName, String regex) {
        return addConstraint(new SelectQueryConstraint().matchRegex(columnName, regex));
    }

    /**
     * Case-insensitive regex matching filter.
     * @param columnName Column name.
     * @param regex Regex.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder iMatchRegex(String columnName, String regex) {
        return addConstraint(new SelectQueryConstraint().iMatchRegex(columnName, regex));
    }

    /**
     * Negated regex matching filter.
     * @param columnName Column name.
     * @param regex Regex.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder notMatchRegex(String columnName, String regex) {
        return addConstraint(new SelectQueryConstraint().notMatchRegex(columnName, regex));
    }

    /**
     * Full-text search filter.
     * @param columnName Column name.
     * @param query Search query.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder fullTextSearch(String columnName, String query) {
        return addConstraint(new SelectQueryConstraint().fullTextSearch(columnName, query));
    }

    /**
     * Plain full-text search filter.
     * @param columnName Column name.
     * @param query Search query.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder plainFullTextSearch(String columnName, String query) {
        return addConstraint(new SelectQueryConstraint().plainFullTextSearch(columnName, query));
    }

    /**
     * Phrase full-text search filter.
     * @param columnName Column name.
     * @param query Search query.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder phraseFullTextSearch(String columnName, String query) {
        return addConstraint(new SelectQueryConstraint().phraseFullTextSearch(columnName, query));
    }

    /**
     * Web-style full-text search filter.
     * @param columnName Column name.
     * @param query Search query.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder webSearch(String columnName, String query) {
        return addConstraint(new SelectQueryConstraint().webSearch(columnName, query));
    }

    /**
     * Logical OR constraint.
     * @param selectQueryConstraints Child constraints.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder or(SelectQueryConstraint... selectQueryConstraints) {
        return addConstraint(new SelectQueryConstraint().or(selectQueryConstraints));
    }

    /**
     * Logical AND constraint.
     * @param selectQueryConstraints Child constraints.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder and(SelectQueryConstraint... selectQueryConstraints) {
        return addConstraint(new SelectQueryConstraint().and(selectQueryConstraints));
    }

    /**
     * Sets range for pagination.
     * @param start Start index.
     * @param end End index.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder range(int start, int end) {
        return addHeader("Range", start + "-" + end);
    }

    /**
     * Sets the maximum number of rows that can be affected.
     * @param count Max rows.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder setMaxRowsAffected(int count) {
        addPreferHeader(PreferHeader.HANDLING_STRICT);
        return addPreferHeader("max-affected", String.valueOf(count));
    }

    /**
     * Configures the query to return a single object.
     * @param stripNulls Whether to strip nulls.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder single(boolean stripNulls) {
        isSingle = true;
        acceptHeader = new AcceptHeader().Single(stripNulls);
        return addHeader("Accept", acceptHeader.get());
    }

    /**
     * Configures the query to return a single object.
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder single() {
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
     * @return This {@link UpdateQueryBuilder} instance for chaining.
     */
    public UpdateQueryBuilder setDataFormat(AcceptHeader acceptHeader) {
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
        if (!conflictColumnBuilder.isColumnsEmpty()) {
            queryParams.put("on_conflict", conflictColumnBuilder.buildColumn());
        }
        if (!updateColumnBuilder.isColumnsEmpty()) {
            queryParams.put("columns", updateColumnBuilder.buildColumn());
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
