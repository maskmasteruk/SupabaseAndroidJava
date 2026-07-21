package io.github.maskmasteruk.supabase.postgrest;

import java.util.ArrayList;
import java.util.HashMap;

import io.github.maskmasteruk.supabase.postgrest.Callback.OnPostgrestCallback;
import io.github.maskmasteruk.supabase.postgrest.Query.DeleteQueryBuilder;
import io.github.maskmasteruk.supabase.postgrest.Query.InsertQueryBuilder;
import io.github.maskmasteruk.supabase.postgrest.Query.PostgrestConfig;
import io.github.maskmasteruk.supabase.postgrest.Query.SelectQueryBuilder;
import io.github.maskmasteruk.supabase.postgrest.Query.UpdateQueryBuilder;
import io.github.maskmasteruk.supabase.postgrest.Query.UpsertQueryBuilder;

/**
 * The main entry point for interacting with PostgREST in the Supabase Android library.
 *
 * <p>This class provides a high-level API for performing CRUD operations (SELECT, INSERT, UPDATE, UPSERT, DELETE)
 * and executing Remote Procedure Calls (RPC) against a Supabase database.</p>
 *
 * <p>Use {@link #getInstance()} to obtain an instance of this class.</p>
 */
public class SupabasePostgres {

    private static volatile SupabasePostgres instance;
    private final SelectService selectService;
    private final CreateService createService;
    private final UpdateService updateService;
    private final DeleteService deleteService;
    private final RPCService rpcService;
    private SupabasePostgres() {
        Helper.getInstance();
        selectService = SelectService.getInstance();
        createService = CreateService.getInstance();
        updateService = UpdateService.getInstance();
        deleteService = DeleteService.getInstance();
        rpcService = RPCService.getInstance();
    }

    /**
     * Gets the singleton instance of {@link SupabasePostgres}.
     *
     * @return The {@link SupabasePostgres} instance.
     */
    public static SupabasePostgres getInstance() {
        if (instance == null) {
            synchronized (SupabasePostgres.class) {
                if (instance == null) {
                    instance = new SupabasePostgres();
                }
            }
        }
        return instance;
    }

    /**
     * Performs a SELECT query.
     *
     * @param tableName Name of the table.
     * @param selectQueryBuilder Builder for configuring the query.
     * @param postgrestConfig Optional configuration for the request.
     * @param onPostgrestCallback Callback for results.
     */
    public void select(String tableName, SelectQueryBuilder selectQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        selectService.select(tableName, selectQueryBuilder, postgrestConfig, onPostgrestCallback);
    }

    /**
     * Inserts a single row.
     *
     * @param tableName Name of the table.
     * @param values Map of column names to values.
     * @param insertQueryBuilder Optional builder for configurations.
     * @param postgrestConfig Optional configuration.
     * @param onPostgrestCallback Callback for results.
     */
    public void insert(String tableName, HashMap<String, Object> values, InsertQueryBuilder insertQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        createService.insert(tableName, values, insertQueryBuilder, postgrestConfig, onPostgrestCallback);
    }

    /**
     * Inserts multiple rows.
     *
     * @param tableName Name of the table.
     * @param values List of maps representing rows.
     * @param insertQueryBuilder Optional builder.
     * @param postgrestConfig Optional configuration.
     * @param onPostgrestCallback Callback for results.
     */
    public void insert(String tableName, ArrayList<HashMap<String, Object>> values, InsertQueryBuilder insertQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        createService.insert(tableName, values, insertQueryBuilder, postgrestConfig, onPostgrestCallback);
    }

    /**
     * Updates rows matching the filters in the builder.
     *
     * @param tableName Name of the table.
     * @param values Map of columns and values to update.
     * @param updateQueryBuilder Builder containing filters for the update.
     * @param postgrestConfig Optional configuration.
     * @param onPostgrestCallback Callback for results.
     */
    public void update(String tableName, HashMap<String, Object> values, UpdateQueryBuilder updateQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        updateService.update(tableName, values, updateQueryBuilder, postgrestConfig, onPostgrestCallback);
    }

    /**
     * Updates multiple rows.
     *
     * @param tableName Name of the table.
     * @param values List of maps to update.
     * @param updateQueryBuilder Builder containing filters.
     * @param postgrestConfig Optional configuration.
     * @param onPostgrestCallback Callback for results.
     */
    public void update(String tableName, ArrayList<HashMap<String, Object>> values, UpdateQueryBuilder updateQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        updateService.update(tableName, values, updateQueryBuilder, postgrestConfig, onPostgrestCallback);
    }

    /**
     * Performs an UPSERT (update or insert) operation.
     *
     * <p>Note: This replaces the entire row with default values if not provided.
     * For selective updates on conflict, use {@link #insert(String, HashMap, InsertQueryBuilder, PostgrestConfig, OnPostgrestCallback)}
     * with an {@link InsertQueryBuilder} configured via {@link InsertQueryBuilder#upsert(String...)}.</p>
     *
     * @param tableName Name of the table.
     * @param values Map of values.
     * @param upsertQueryBuilder Builder for configuration.
     * @param postgrestConfig Optional configuration.
     * @param onPostgrestCallback Callback for results.
     */
    public void upsert(String tableName, HashMap<String, Object> values, UpsertQueryBuilder upsertQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        updateService.upsert(tableName, values, upsertQueryBuilder, postgrestConfig, onPostgrestCallback);
    }

    /**
     * Replaces an entire row (synonym for {@link #upsert}).
     *
     * @param tableName Name of the table.
     * @param values Map of values.
     * @param upsertQueryBuilder Builder for configuration.
     * @param postgrestConfig Optional configuration.
     * @param onPostgrestCallback Callback for results.
     */
    public void replace(String tableName, HashMap<String, Object> values, UpsertQueryBuilder upsertQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        updateService.replace(tableName, values, upsertQueryBuilder, postgrestConfig, onPostgrestCallback);
    }

    /**
     * Deletes rows matching the filters.
     *
     * @param tableName Name of the table.
     * @param deleteQueryBuilder Builder containing filters.
     * @param postgrestConfig Optional configuration.
     * @param onPostgrestCallback Callback for results.
     */
    public void delete(String tableName, DeleteQueryBuilder deleteQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        deleteService.delete(tableName, deleteQueryBuilder, postgrestConfig, onPostgrestCallback);
    }

    /**
     * Executes a database function (RPC).
     *
     * @param rpcName Name of the function.
     * @param values Map of arguments for the function.
     * @param selectQueryBuilder Optional builder for selecting response columns.
     * @param postgrestConfig Optional configuration.
     * @param onPostgrestCallback Callback for results.
     */
    public void rpc(String rpcName, HashMap<String, Object> values, SelectQueryBuilder selectQueryBuilder, PostgrestConfig postgrestConfig, OnPostgrestCallback onPostgrestCallback) {
        rpcService.rpc(rpcName, values, selectQueryBuilder, postgrestConfig, onPostgrestCallback);
    }

    /**
     * Executes a database function (RPC).
     *
     * @param rpcName Name of the function.
     * @param values Map of arguments for the function.
     * @param selectQueryBuilder Optional builder for selecting response columns.
     * @param onPostgrestCallback Callback for results.
     */
    public void rpc(String rpcName, HashMap<String, Object> values, SelectQueryBuilder selectQueryBuilder, OnPostgrestCallback onPostgrestCallback) {
        rpc(rpcName, values, selectQueryBuilder, null, onPostgrestCallback);
    }

    /**
     * Executes a database function (RPC).
     *
     * @param rpcName Name of the function.
     * @param values Map of arguments for the function.
     * @param onPostgrestCallback Callback for results.
     */
    public void rpc(String rpcName, HashMap<String, Object> values, OnPostgrestCallback onPostgrestCallback) {
        rpc(rpcName, values, null, null, onPostgrestCallback);
    }

    /**
     * Executes a database function (RPC).
     *
     * @param rpcName Name of the function.
     * @param onPostgrestCallback Callback for results.
     */
    public void rpc(String rpcName, OnPostgrestCallback onPostgrestCallback) {
        rpc(rpcName, null, null, null, onPostgrestCallback);
    }
}
