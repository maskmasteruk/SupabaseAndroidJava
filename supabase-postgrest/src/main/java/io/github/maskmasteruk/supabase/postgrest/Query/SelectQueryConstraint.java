package io.github.maskmasteruk.supabase.postgrest.Query;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Builder for PostgREST query constraints (filters).
 *
 * <p>This class provides a fluent API to construct various filters like equality,
 * pattern matching, range checks, and full-text searches. It also supports
 * logical operations like AND/OR and query modifiers like ordering and pagination.</p>
 */
public class SelectQueryConstraint {
    /** The underlying constraint being built. */
    Constraint constraint;

    /** Whether the constraint should be negated. */
    boolean negate = false;

    /**
     * Default constructor for SelectQueryConstraint.
     */
    public SelectQueryConstraint() {

    }

    /**
     * Checks if the constraint is negated.
     * @return {@code true} if negated, {@code false} otherwise.
     */
    public boolean isNegate() {
        return negate;
    }

    /**
     * Sets whether the constraint should be negated.
     * @param negate {@code true} to negate, {@code false} otherwise.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint setNegate(boolean negate) {
        this.negate = negate;
        return this;
    }

    private SelectQueryConstraint setConstraint(String columnName, Object value, ConstraintOperator... constraintOperators) {
        constraint = new Constraint(columnName, value.toString(), constraintOperators);

        return this;
    }

    private SelectQueryConstraint setConstraint(String columnName, ConstraintOperator... constraintOperators) {
        constraint = new Constraint(columnName, constraintOperators);

        return this;
    }

    private SelectQueryConstraint setConstraint(ConstraintOperator constraintOperator, SelectQueryConstraint[] selectQueryConstraints) {
        constraint = new Constraint(constraintOperator, selectQueryConstraints);
        return this;
    }

    private SelectQueryConstraint setConstraint(Order[] orders) {
        constraint = new Constraint(orders);
        return this;
    }

    private SelectQueryConstraint setConstraint(String columnName, Object value) {
        constraint = new Constraint(columnName, value.toString());
        return this;
    }

    /**
     * Matches records where {@code columnName} is equal to {@code value}.
     *
     * @param columnName Column name.
     * @param value Value to match.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint equal(String columnName, Object value) {
        return setConstraint(columnName, value, ConstraintOperator.EQUALS);
    }

    /**
     * Matches records where {@code columnName} is not equal to {@code value}.
     *
     * @param columnName Column name.
     * @param value Value to match.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint notEqual(String columnName, Object value) {
        return setConstraint(columnName, value, ConstraintOperator.NOT_EQUALS);
    }

    /**
     * Matches records where {@code columnName} is greater than {@code value}.
     *
     * @param columnName Column name.
     * @param value Value to compare.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint greaterThan(String columnName, Object value) {
        return setConstraint(columnName, value, ConstraintOperator.GREATER_THAN);
    }

    /**
     * Matches records where {@code columnName} is greater than or equal to {@code value}.
     *
     * @param columnName Column name.
     * @param value Value to compare.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint greaterThanOrEqual(String columnName, Object value) {
        return setConstraint(columnName, value, ConstraintOperator.GREATER_THAN_OR_EQUALS);
    }

    /**
     * Matches records where {@code columnName} is less than {@code value}.
     *
     * @param columnName Column name.
     * @param value Value to compare.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint lessThan(String columnName, Object value) {
        return setConstraint(columnName, value, ConstraintOperator.LESS_THAN);
    }

    /**
     * Matches records where {@code columnName} is less than or equal to {@code value}.
     *
     * @param columnName Column name.
     * @param value Value to compare.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint lessThanOrEqual(String columnName, Object value) {
        return setConstraint(columnName, value, ConstraintOperator.LESS_THAN_OR_EQUALS);
    }

    /**
     * Matches records using pattern matching (case-sensitive).
     *
     * @param columnName Column name.
     * @param value Pattern to match (e.g., "val%").
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint like(String columnName, Object value) {
        return setConstraint(columnName, value, ConstraintOperator.LIKE);
    }

    /**
     * Matches records using pattern matching (case-insensitive).
     *
     * @param columnName Column name.
     * @param value Pattern to match.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint iLike(String columnName, Object value) {
        return setConstraint(columnName, value, ConstraintOperator.ILIKE);
    }

    /**
     * Matches records where {@code columnName} is present in the provided collection.
     *
     * @param columnName Column name.
     * @param value A collection or comma-separated string of values.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint in(String columnName, Object value) {
        value = convertIterableObjectIntoString(value, ",", "(", ")");
        return setConstraint(columnName, value, ConstraintOperator.IN);
    }

    /**
     * Matches records where {@code columnName} is present in the provided values.
     *
     * @param columnName Column name.
     * @param values Values to match.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint in(String columnName, Object... values) {
        return in(columnName, Arrays.asList(values));
    }

    /**
     * Matches records where {@code columnName} is null.
     *
     * @param columnName Column name.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint isNull(String columnName) {
        return setConstraint(columnName, ConstraintOperator.IS, ConstraintOperator.NULL);
    }

    /**
     * Matches records where {@code columnName} is not null.
     *
     * @param columnName Column name.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint isNotNull(String columnName) {
        return setConstraint(columnName, ConstraintOperator.NOT, ConstraintOperator.IS, ConstraintOperator.NULL);
    }

    /**
     * Matches records where an array column contains all of the specified values.
     *
     * @param columnName Column name (array type).
     * @param value A collection of values.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint containsAll(String columnName, Object value) {
        value = convertIterableObjectIntoString(value, ",", "{", "}");
        return setConstraint(columnName, value, ConstraintOperator.CONTAINS);
    }

    /**
     * Matches records where an array column contains all of the specified values.
     *
     * @param columnName Column name (array type).
     * @param values Values to check.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint containsAll(String columnName, Object... values) {
        return containsAll(columnName, Arrays.asList(values));
    }

    /**
     * Matches records where an array column contains the specified values.
     *
     * @param columnName Column name.
     * @param value Values to check.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint contains(String columnName, Object value) {
        return containsAll(columnName, value);
    }

    /**
     * Matches records where an array column contains the specified values.
     *
     * @param columnName Column name.
     * @param values Values to check.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint contains(String columnName, Object... values) {
        return containsAll(columnName, Arrays.asList(values));
    }

    /**
     * Matches records where an array column is a subset of the specified values.
     *
     * @param columnName Column name.
     * @param value Values to check.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint isSubsetOf(String columnName, Object value) {
        value = convertIterableObjectIntoString(value, ",", "{", "}");
        return setConstraint(columnName, value, ConstraintOperator.CONTAINED_BY);
    }

    /**
     * Matches records where an array column is a subset of the specified values.
     *
     * @param columnName Column name.
     * @param values Values to check.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint isSubsetOf(String columnName, Object... values) {
        return isSubsetOf(columnName, Arrays.asList(values));
    }

    /**
     * Matches records where an array column is contained by the specified values.
     *
     * @param columnName Column name.
     * @param value Values to check.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint containedBy(String columnName, Object value) {
        return isSubsetOf(columnName, value);
    }

    /**
     * Matches records where an array column is contained by the specified values.
     *
     * @param columnName Column name.
     * @param values Values to check.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint containedBy(String columnName, Object... values) {
        return isSubsetOf(columnName, Arrays.asList(values));
    }

    /**
     * Matches records where an array column overlaps with the specified values.
     *
     * @param columnName Column name.
     * @param value Values to check.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint containsAny(String columnName, Object value) {
        value = convertIterableObjectIntoString(value, ",", "{", "}");
        return setConstraint(columnName, value, ConstraintOperator.OVERLAPS);
    }

    /**
     * Matches records where an array column overlaps with the specified values.
     *
     * @param columnName Column name.
     * @param values Values to check.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint containsAny(String columnName, Object... values) {
        return containsAny(columnName, Arrays.asList(values));
    }

    /**
     * Matches records where an array column overlaps with the specified values.
     *
     * @param columnName Column name.
     * @param value Values to check.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint overlaps(String columnName, Object value) {
        return containsAny(columnName, value);
    }

    /**
     * Matches records where an array column overlaps with the specified values.
     *
     * @param columnName Column name.
     * @param values Values to check.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint overlaps(String columnName, Object... values) {
        return containsAny(columnName, Arrays.asList(values));
    }

    /**
     * Matches records where a range column is strictly to the left of {@code range}.
     *
     * @param columnName Column name.
     * @param range Range to compare.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint strictlyLeft(String columnName, String range) {
        return setConstraint(columnName, range, ConstraintOperator.STRICTLY_LEFT);
    }

    /**
     * Matches records where a range column is strictly to the right of {@code range}.
     *
     * @param columnName Column name.
     * @param range Range to compare.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint strictlyRight(String columnName, String range) {
        return setConstraint(columnName, range, ConstraintOperator.STRICTLY_RIGHT);
    }

    /**
     * Matches records where a range column does not extend to the left of {@code range}.
     *
     * @param columnName Column name.
     * @param range Range to compare.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint notExtendLeft(String columnName, String range) {
        return setConstraint(columnName, range, ConstraintOperator.NO_EXTEND_LEFT);
    }

    /**
     * Matches records where a range column does not extend to the right of {@code range}.
     *
     * @param columnName Column name.
     * @param range Range to compare.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint notExtendRight(String columnName, String range) {
        return setConstraint(columnName, range, ConstraintOperator.NO_EXTEND_RIGHT);
    }

    /**
     * Matches records where a range column is adjacent to {@code range}.
     *
     * @param columnName Column name.
     * @param range Range to compare.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint adjacent(String columnName, String range) {
        return setConstraint(columnName, range, ConstraintOperator.ADJACENT);
    }

    /**
     * Matches records using a POSIX regular expression.
     *
     * @param columnName Column name.
     * @param regex Regular expression.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint matchRegex(String columnName, String regex) {
        return setConstraint(columnName, regex, ConstraintOperator.MATCH_REGEX);
    }

    /**
     * Matches records using a POSIX regular expression (case-insensitive).
     *
     * @param columnName Column name.
     * @param regex Regular expression.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint iMatchRegex(String columnName, String regex) {
        return setConstraint(columnName, regex, ConstraintOperator.IMATCH_REGEX);
    }

    /**
     * Matches records that do not match the POSIX regular expression.
     *
     * @param columnName Column name.
     * @param regex Regular expression.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint notMatchRegex(String columnName, String regex) {
        negate = true;
        return setConstraint(columnName, regex, ConstraintOperator.MATCH_REGEX);
    }

    /**
     * Matches records using full-text search.
     *
     * @param columnName Column name.
     * @param query Search query.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint fullTextSearch(String columnName, String query) {
        return setConstraint(columnName, query, ConstraintOperator.FULL_TEXT_SEARCH);
    }

    /**
     * Matches records using plain full-text search.
     *
     * @param columnName Column name.
     * @param query Search query.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint plainFullTextSearch(String columnName, String query) {
        return setConstraint(columnName, query, ConstraintOperator.PLAIN_FULL_TEXT_SEARCH);
    }

    /**
     * Matches records using phrase full-text search.
     *
     * @param columnName Column name.
     * @param query Search query.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint phraseFullTextSearch(String columnName, String query) {
        return setConstraint(columnName, query, ConstraintOperator.PHRASE_FULL_TEXT_SEARCH);
    }

    /**
     * Matches records using web-style search.
     *
     * @param columnName Column name.
     * @param query Search query.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint webSearch(String columnName, String query) {
        return setConstraint(columnName, query, ConstraintOperator.WEB_SEARCH);
    }

    /**
     * Combines multiple constraints using a logical OR.
     *
     * @param selectQueryConstraints The constraints to combine.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint or(SelectQueryConstraint... selectQueryConstraints) {
        return setConstraint(ConstraintOperator.OR, selectQueryConstraints);
    }

    /**
     * Combines multiple constraints using a logical AND.
     *
     * @param selectQueryConstraints The constraints to combine.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint and(SelectQueryConstraint... selectQueryConstraints) {
        return setConstraint(ConstraintOperator.AND, selectQueryConstraints);
    }

    /**
     * Sets the ordering of the results.
     *
     * @param orders The order definitions.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint order(Order... orders) {
        return setConstraint(orders);
    }

    /**
     * Limits the number of records returned.
     *
     * @param limit Maximum number of records.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint limit(long limit) {
        return setConstraint(ConstraintOperator.LIMIT.getValue(), limit);
    }

    /**
     * Sets the offset for the results (used for pagination).
     *
     * @param offset Number of records to skip.
     * @return This {@link SelectQueryConstraint} instance for chaining.
     */
    public SelectQueryConstraint offset(long offset) {
        return setConstraint(ConstraintOperator.OFFSET.getValue(), offset);
    }

    private static String convertIterableObjectIntoString(Object value, String delimiter, String prefix, String suffix) {
        if (value instanceof Iterable) {
            value = StreamSupport.stream(((Iterable<?>) value).spliterator(), false)
                    .map(String::valueOf)
                    .collect(Collectors.joining(delimiter, prefix, suffix));
        } else if (value instanceof String) {
            if (!((String) value).startsWith(prefix)) {
                value = prefix+value;
            }
            if (!((String) value).endsWith(suffix)) {
                value = value + suffix;
            }
        }
        return value.toString();
    }

    /**
     * Builds a string representation of the constraint for use within nested groupings.
     *
     * @param tableName Optional table name prefix.
     * @return The string representation.
     */
    public String buildSubConstraint(String tableName) {
        return constraint.buildSubConstraint(negate, tableName);
    }

    /**
     * Builds the query parameter representation of this constraint.
     *
     * @return A {@link Map.Entry} with the parameter name and value.
     */
    public Map.Entry<String, String> build() {
        return constraint.build(negate, null);
    }

    /**
     * Builds the query parameter representation of this constraint with a table name prefix.
     *
     * @param tableName Table name prefix.
     * @return A {@link Map.Entry} with the parameter name and value.
     */
    public Map.Entry<String, String> build(String tableName) {
        return constraint.build(negate, tableName);
    }

    /**
     * Enum representing operators used in PostgREST filters.
     */
    public enum ConstraintOperator {

        // Comparison
        /** Equality operator. */
        EQUALS("eq"),
        /** Inequality operator. */
        NOT_EQUALS("neq"),
        /** Greater than operator. */
        GREATER_THAN("gt"),
        /** Greater than or equal operator. */
        GREATER_THAN_OR_EQUALS("gte"),
        /** Less than operator. */
        LESS_THAN("lt"),
        /** Less than or equal operator. */
        LESS_THAN_OR_EQUALS("lte"),

        // Pattern Matching
        /** Like operator (case-sensitive). */
        LIKE("like"),
        /** Ilike operator (case-insensitive). */
        ILIKE("ilike"),
        /** POSIX case-sensitive regex match. */
        MATCH_REGEX("match"),
        /** POSIX case-insensitive regex match. */
        IMATCH_REGEX("imatch"),

        // Collection
        /** In operator. */
        IN("in"),
        /** Contains operator (for arrays). */
        CONTAINS("cs"),
        /** Contained by operator (for arrays). */
        CONTAINED_BY("cd"),
        /** Overlaps operator (for arrays). */
        OVERLAPS("ov"),

        // Null Checks
        /** Is operator. */
        IS("is"),
        /** Null constant. */
        NULL("null"),
        /** Not operator. */
        NOT("not"),

        // Range
        /** Strictly left of. */
        STRICTLY_LEFT("sl"),
        /** Strictly right of. */
        STRICTLY_RIGHT("sr"),
        /** Does not extend to the right of. */
        NO_EXTEND_RIGHT("nxr"),
        /** Does not extend to the left of. */
        NO_EXTEND_LEFT("nxl"),
        /** Adjacent to. */
        ADJACENT("adj"),

        // Full Text Search
        /** Full text search. */
        FULL_TEXT_SEARCH("fts"),
        /** Plain full text search. */
        PLAIN_FULL_TEXT_SEARCH("plfts"),
        /** Phrase full text search. */
        PHRASE_FULL_TEXT_SEARCH("phfts"),
        /** Web-style full text search. */
        WEB_SEARCH("wfts"),

        // Boolean Operations
        /** Logical OR. */
        OR("or"),
        /** Logical AND. */
        AND("and"),
        // Order Operations
        /** Order modifier. */
        ORDER("order"),
        /** Nulls first modifier. */
        NULLSFIRST("nullsfirst"),
        /** Nulls last modifier. */
        NULLSLAST("nullslast"),
        // LIMITS
        /** Offset modifier. */
        OFFSET("offset"),
        /** Limit modifier. */
        LIMIT("limit");

        private final String value;

        ConstraintOperator(String value) {
            this.value = value;
        }

        /**
         * Gets the string value of the operator.
         * @return The operator value.
         */
        public String getValue() {
            return value;
        }
    }
}

