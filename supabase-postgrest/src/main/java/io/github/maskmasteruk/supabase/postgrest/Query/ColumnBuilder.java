package io.github.maskmasteruk.supabase.postgrest.Query;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Helper class for building column selection strings for PostgREST queries.
 *
 * <p>This class manages a set of columns to be selected, supporting aliasing
 * and nested selection for joined tables.</p>
 */
public class ColumnBuilder {
    /** The set of columns to be selected. */
    private Set<String> columns = new LinkedHashSet<>();
    /** The list of reference (joined) table column selections. */
    private final ArrayList<String> referenceTableColumns = new ArrayList<>();


    /**
     * Adds a column to the selection.
     *
     * @param s Column name.
     * @return This {@link ColumnBuilder} instance for chaining.
     */
    public ColumnBuilder addColumn(String s) {
        if (columns.contains(Column.ALL.getValue())) {
            Log.w("SupabasePostgrest", "Already Column.ALL present");
        }
        columns.add(s);
        return this;
    }

    /**
     * Adds multiple columns to the selection.
     *
     * @param s Column names.
     * @return This {@link ColumnBuilder} instance for chaining.
     */
    public ColumnBuilder addColumn(String... s) {
        if (columns.contains(Column.ALL.getValue())) {
            Log.w("SupabasePostgrest", "Already Column.ALL present");
        }
        columns.addAll(Arrays.asList(s));
        return this;
    }

    /**
     * Sets the selection to a specific {@link Column} (e.g., Column.ALL).
     * Clears any previous selections.
     *
     * @param column The column to select.
     * @return This {@link ColumnBuilder} instance for chaining.
     */
    public ColumnBuilder addColumn(Column column) {
        columns = new LinkedHashSet<>();
        columns.add(column.getValue());
        return this;
    }

    /**
     * Adds a list of columns to the selection.
     *
     * @param s List of column names.
     * @return This {@link ColumnBuilder} instance for chaining.
     */
    public ColumnBuilder addColumn(ArrayList<String> s) {
        if (columns.contains(Column.ALL.getValue())) {
            Log.w("SupabasePostgrest", "Already Column.ALL present");
        }
        columns.addAll(s);
        return this;
    }

    /**
     * Adds a column with an alias.
     *
     * @param columnName Original column name.
     * @param newName Alias for the column.
     * @return This {@link ColumnBuilder} instance for chaining.
     */
    public ColumnBuilder addColumnAs(String columnName, String newName) {
        columns.add(newName+":"+columnName);
        return this;
    }

    /**
     * Adds multiple columns with aliases from a map.
     *
     * @param asNames Map of original names to aliases.
     * @return This {@link ColumnBuilder} instance for chaining.
     */
    public ColumnBuilder addColumnAs(HashMap<String, String> asNames) {
        ColumnBuilder columnBuilder = this;
        asNames.forEach(this::addColumnAs);
        return columnBuilder;
    }

    /**
     * Adds a reference to another table's columns (join).
     *
     * @param tableName Name of the referenced table.
     * @param preferredColumnName Optional alias for the joined table.
     * @param join Join type.
     * @param columnBuilder Builder for the referenced table's columns.
     * @return This {@link ColumnBuilder} instance for chaining.
     */
    public ColumnBuilder addReference(String tableName, String preferredColumnName, Join join, ColumnBuilder columnBuilder) {
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
     * Checks if no columns have been added to the selection.
     * @return {@code true} if empty, {@code false} otherwise.
     */
    public boolean isColumnsEmpty() {
        return columns.isEmpty();
    }

    /**
     * Builds the final column selection string.
     * @return The comma-separated column selection string.
     */
    public String buildColumn() {
        ArrayList<String> finalColumns = new ArrayList<>(columns);
        finalColumns.addAll(referenceTableColumns);
        return String.join(",", finalColumns);
    }

    /**
     * Gets all selected columns as a list.
     * @return List of column strings.
     */
    public ArrayList<String> getColumns() {
        ArrayList<String> finalColumns = new ArrayList<>(columns);
        finalColumns.addAll(referenceTableColumns);
        return finalColumns;
    }

}
