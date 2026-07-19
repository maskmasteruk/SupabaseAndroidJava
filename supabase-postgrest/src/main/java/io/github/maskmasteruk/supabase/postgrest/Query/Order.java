package io.github.maskmasteruk.supabase.postgrest.Query;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;

/**
 * Represents an ordering criteria for a PostgREST query.
 */
public class Order {

    /** The name of the column to order by. */
    String columnName;
    /** The type of ordering (Ascending or Descending). */
    OrderType ascending = OrderType.ASCENDING;
    /** Whether nulls should appear first. */
    boolean nullsFirst;
    /** Whether nulls should appear last. */
    boolean nullsLast;

    /**
     * Default constructor for Order.
     */
    public Order() {
    }

    /**
     * Constructs an Order for the specified column (defaults to Ascending).
     * @param columnName Column name.
     */
    public Order(String columnName) {
        this.columnName = columnName;
    }

    /**
     * Constructs an Order for the specified column with a given direction.
     * @param columnName Column name.
     * @param ascending {@code true} for Ascending, {@code false} for Descending.
     */
    public Order(String columnName, boolean ascending) {
        this.columnName = columnName;
        if (!ascending) {
            this.ascending = OrderType.DESCENDING;
        }
    }

    /**
     * Constructs an Order with all options.
     *
     * @param columnName Column name.
     * @param ascending {@code true} for Ascending, {@code false} for Descending.
     * @param nullsFirst {@code true} to put nulls at the beginning.
     * @param nullsLast {@code true} to put nulls at the end.
     * @throws SupabaseError if both nullsFirst and nullsLast are true.
     */
    public Order(String columnName, boolean ascending, boolean nullsFirst, boolean nullsLast) {
        if (nullsFirst && nullsLast) {
            throw new SupabaseError("Both nullsFirst and nullsLast can't set to be true;");
        }
        this.columnName = columnName;
        if (!ascending) {
            this.ascending = OrderType.DESCENDING;
        }
        this.nullsFirst = nullsFirst;
        this.nullsLast = nullsLast;
    }

    /**
     * Gets the column name.
     * @return Column name.
     */
    public String getColumnName() {
        return columnName;
    }

    /**
     * Gets the order type.
     * @return Order type.
     */
    public OrderType getOrderType() {
        return ascending;
    }

    /**
     * Checks if nulls first is requested.
     * @return {@code true} if nulls first, {@code false} otherwise.
     */
    public boolean isNullsFirst() {
        return nullsFirst;
    }

    /**
     * Checks if nulls last is requested.
     * @return {@code true} if nulls last, {@code false} otherwise.
     */
    public boolean isNullsLast() {
        return nullsLast;
    }

    /**
     * Enum representing the direction of ordering.
     */
    public enum OrderType {
        /** Ascending order. */
        ASCENDING("asc"),
        /** Descending order. */
        DESCENDING("desc");

        private final String value;

        OrderType(String value) {
            this.value = value;
        }

        /**
         * Gets the string value of the order type.
         * @return The order value.
         */
        public String getValue() {
            return value;
        }
    }
}

