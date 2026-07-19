package io.github.maskmasteruk.supabase.postgrest.Query;

import java.util.Map;

/**
 * Internal class representing a query constraint in a PostgREST request.
 *
 * <p>This class handles the construction of individual filter parts, such as equalities,
 * ranges, full-text searches, and logical groupings (AND/OR).</p>
 */
class Constraint {

    /**
     * Enum defining the type of constraint being applied.
     */
    public enum ConstraintType {
        /** A simple constraint with operators and a value (e.g., eq.value). */
        WITH_CONSTRAINTS(1),
        /** A logical grouping constraint with nested child constraints (e.g., and(...)). */
        WITH_CHILD_CONSTRAINTS(2),
        /** An ordering constraint. */
        ORDER(3),
        /** A simple equality constraint without explicit operators. */
        WITHOUT_CONSTRAINTS(4),
        /** A constraint with only operators and no value. */
        ONLY_CONSTRAINT(5);

        private final int value;

        ConstraintType(int value) {
            this.value = value;
        }
    }

    /** The type of this constraint. */
    final ConstraintType constraintType;

    /** The name of the column this constraint applies to. */
    String columnName;
    /** The operators to apply to the column. */
    SelectQueryConstraint.ConstraintOperator[] constraintOperators;
    /** The value to compare against. */
    String value;

    /**
     * Constructs a simple constraint without operators (defaults to equality).
     *
     * @param columnName Column name.
     * @param value Value to match.
     */
    public Constraint(String columnName, String value) {
        constraintType = ConstraintType.WITHOUT_CONSTRAINTS;
        this.columnName = columnName;
        this.value = value;
    }

    /**
     * Constructs a constraint with operators and a value.
     *
     * @param columnName Column name.
     * @param value Value to compare.
     * @param constraintOperators Operators to apply.
     */
    public Constraint(String columnName, String value, SelectQueryConstraint.ConstraintOperator... constraintOperators) {
        constraintType = ConstraintType.WITH_CONSTRAINTS;
        this.columnName = columnName;
        this.constraintOperators = constraintOperators;
        this.value = value;
    }

    /**
     * Constructs a constraint with only operators (e.g., is.null).
     *
     * @param columnName Column name.
     * @param constraintOperators Operators to apply.
     */
    public Constraint(String columnName, SelectQueryConstraint.ConstraintOperator... constraintOperators) {
        constraintType = ConstraintType.ONLY_CONSTRAINT;
        this.columnName = columnName;
        this.constraintOperators = constraintOperators;
    }

    /** The logical operator for grouping (AND/OR). */
    SelectQueryConstraint.ConstraintOperator constraintOperator;
    /** Child constraints for logical grouping. */
    SelectQueryConstraint[] selectQueryConstraints;

    /**
     * Constructs a logical grouping constraint (AND/OR).
     *
     * @param constraintOperator The logical operator.
     * @param selectQueryConstraints The nested constraints.
     */
    public Constraint(SelectQueryConstraint.ConstraintOperator constraintOperator, SelectQueryConstraint[] selectQueryConstraints) {
        constraintType = ConstraintType.WITH_CHILD_CONSTRAINTS;
        this.constraintOperator = constraintOperator;
        this.selectQueryConstraints = selectQueryConstraints;
    }

    /** Array of orders for an ORDER constraint. */
    Order[] orders;

    /**
     * Constructs an ORDER constraint.
     *
     * @param orders The order definitions.
     */
    public Constraint(Order... orders) {
        constraintType = ConstraintType.ORDER;
        this.orders = orders;
    }

    /**
     * Builds the query parameter representation of this constraint.
     *
     * @param negate Whether to negate the constraint.
     * @param tableName Optional table name prefix for the column.
     * @return A {@link Map.Entry} where the key is the query parameter name and the value is the parameter value.
     */
    public Map.Entry<String, String> build(boolean negate, String tableName) {
        switch (constraintType) {
            case WITH_CONSTRAINTS:
                StringBuilder withConstraint = new StringBuilder();
                for (SelectQueryConstraint.ConstraintOperator constraintOperator : constraintOperators) {
                    withConstraint.append(constraintOperator.getValue()).append(".");
                }
                withConstraint.append(value);

                return Map.entry((tableName != null ? tableName + "." : "") + (negate ? SelectQueryConstraint.ConstraintOperator.NOT.getValue() + "." : "") + columnName, withConstraint.toString());
            case ONLY_CONSTRAINT:
                StringBuilder onlyConstraint = new StringBuilder();
                for (SelectQueryConstraint.ConstraintOperator constraintOperator : constraintOperators) {
                    onlyConstraint.append(constraintOperator.getValue()).append(".");
                }

                return Map.entry((tableName != null ? tableName + "." : "") + (negate ? SelectQueryConstraint.ConstraintOperator.NOT.getValue() + "." : "") + columnName, onlyConstraint.toString());
            case WITHOUT_CONSTRAINTS:
                return Map.entry((tableName != null ? tableName + "." : "") + columnName, value);
            case WITH_CHILD_CONSTRAINTS:
                StringBuilder withChildConstraint = new StringBuilder("(");
                for (SelectQueryConstraint selectQueryConstraint : selectQueryConstraints) {
                    withChildConstraint.append(selectQueryConstraint.buildSubConstraint(null)).append(",");
                }
                if (withChildConstraint.toString().endsWith(",")) {
                    withChildConstraint.deleteCharAt(withChildConstraint.length() - 1);
                }
                withChildConstraint.append(")");
                return Map.entry((tableName != null ? tableName + "." : "") + (negate ? SelectQueryConstraint.ConstraintOperator.NOT.getValue() + "." : "") + constraintOperator.getValue(), withChildConstraint.toString());
            case ORDER:
                StringBuilder stringBuilder = new StringBuilder();
                for (Order order : orders) {
                    stringBuilder.append(order.getColumnName()).append(".").append(order.getOrderType().getValue());
                    if (order.nullsFirst) {
                        stringBuilder.append(SelectQueryConstraint.ConstraintOperator.NULLSFIRST.getValue());
                    } else if (order.nullsLast) {
                        stringBuilder.append(SelectQueryConstraint.ConstraintOperator.NULLSLAST.getValue());
                    }

                    stringBuilder.append(",");
                }
                if (stringBuilder.toString().endsWith(",")) {
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                }
                return Map.entry((tableName != null ? tableName + "." : "") + SelectQueryConstraint.ConstraintOperator.ORDER.getValue(), stringBuilder.toString());
        }
        return null;
    }

    /**
     * Builds a string representation of the constraint for use within nested logical groupings.
     *
     * @param negate Whether to negate the constraint.
     * @param tableName Optional table name prefix.
     * @return The string representation of the constraint.
     */
    public String buildSubConstraint(boolean negate, String tableName) {
        switch (constraintType) {
            case WITH_CONSTRAINTS:
                StringBuilder withConstraint = new StringBuilder();
                for (SelectQueryConstraint.ConstraintOperator constraintOperator : constraintOperators) {
                    withConstraint.append(constraintOperator.getValue()).append(".");
                }
                withConstraint.append(value);

                return (tableName != null ? tableName + "." : "") + (negate ? SelectQueryConstraint.ConstraintOperator.NOT.getValue() + "." : "") + columnName + "." + withConstraint.toString();
            case ONLY_CONSTRAINT:
                StringBuilder onlyConstraint = new StringBuilder();
                for (SelectQueryConstraint.ConstraintOperator constraintOperator : constraintOperators) {
                    onlyConstraint.append(constraintOperator.getValue()).append(".");
                }

                return (tableName != null ? tableName + "." : "") + (negate ? SelectQueryConstraint.ConstraintOperator.NOT.getValue() + "." : "") + columnName + "." + onlyConstraint.toString();
            case WITHOUT_CONSTRAINTS:
                return (tableName != null ? tableName + "." : "") + columnName + "=" + value;
            case WITH_CHILD_CONSTRAINTS:
                StringBuilder withChildConstraint = new StringBuilder("(");
                for (SelectQueryConstraint selectQueryConstraint : selectQueryConstraints) {
                    withChildConstraint.append(selectQueryConstraint.buildSubConstraint(null)).append(",");
                }
                if (withChildConstraint.toString().endsWith(",")) {
                    withChildConstraint.deleteCharAt(withChildConstraint.length() - 1);
                }
                withChildConstraint.append(")");
                if (SelectQueryConstraint.ConstraintOperator.AND.equals(constraintOperator) || SelectQueryConstraint.ConstraintOperator.OR.equals(constraintOperator)) {
                    return (tableName != null ? tableName + "." : "") + (negate ? SelectQueryConstraint.ConstraintOperator.NOT.getValue() + "." : "") + constraintOperator.getValue() + (negate ? "." + SelectQueryConstraint.ConstraintOperator.NOT.getValue() : "") + withChildConstraint.toString();
                } else {
                    return (tableName != null ? tableName + "." : "") + (negate ? SelectQueryConstraint.ConstraintOperator.NOT.getValue() + "." : "") + constraintOperator.getValue() + (negate ? "." + SelectQueryConstraint.ConstraintOperator.NOT.getValue() : "") + "." + withChildConstraint.toString();
                }
        }
        Map.Entry<String, String> build = build(negate, tableName);
        return build.getKey() + "=" + build.getValue();
    }
}
