# Supabase PostgREST Java Library

A powerful, type-safe PostgREST client for Android, allowing you to interact with your Supabase database directly from your application.

## Overview

The Supabase PostgREST module provides a fluent and intuitive interface for performing database operations. It maps your Java calls to PostgREST RESTful endpoints, handling query parameter construction, header management, and response parsing.

### Architecture

The library is designed with a service-oriented architecture:
- **`SupabasePostgres`**: The main entry point and facade for all operations.
- **Service Classes (`SelectService`, `CreateService`, etc.)**: Handle the specifics of different HTTP methods.
- **Builders (`SelectQueryBuilder`, `InsertQueryBuilder`, etc.)**: Provide a fluent API for constructing complex queries.
- **`PostgrestResult`**: Encapsulates the response and provides utility methods for data extraction.

## Features

- [x] **Full CRUD Support**: Create, Read, Update, and Delete operations.
- [x] **Advanced Filtering**: Support for all PostgREST filter operators (eq, neq, gt, lt, in, like, etc.).
- [x] **Resource Embedding**: Easily join related tables.
- [x] **Remote Procedure Calls (RPC)**: Call database functions with ease.
- [x] **Pagination & Ordering**: Built-in support for limit, offset, and complex sort orders.
- [x] **Type-Safe Results**: Automatically parse JSON responses into your POJO/Data classes using Gson.
- [x] **Asynchronous Execution**: All network calls are performed on background threads.

## Module Structure

- `io.github.maskmasteruk.supabase.postgrest`: Core services and the main client.
- `io.github.maskmasteruk.supabase.postgrest.Query`: Builders for constructing queries and filters.
- `io.github.maskmasteruk.supabase.postgrest.Object`: Result models and response encapsulation.
- `io.github.maskmasteruk.supabase.postgrest.Callback`: Interface for asynchronous operation results.

## Installation

```kotlin
plugins {
    id("io.github.maskmasteruk.supabase") version "0.0.1"
}

dependencies {
    implementation("io.github.maskmasteruk:supabase-android-core:0.0.1")
    implementation("io.github.maskmasteruk:supabase-android-postgrest:0.0.1")
}
```

## Initialization

Initialize the `SupabasePostgres` client using your application context:

```java
SupabasePostgres postgrest = SupabasePostgres.getInstance(context);
```

## Basic CRUD Operations

### Read (SELECT)

```java
postgrest.select("users", 
    new SelectQueryBuilder()
        .select("id", "username", "email")
        .equal("status", "active"),
    null,
    new OnPostgrestCallback() {
        @Override
        public void onSuccess(PostgrestResult websocketResult) {
            List<User> users = websocketResult.get(User.class);
            // Handle users
        }

        @Override
        public void onFailure(SupabaseError error) {
            // Handle error
        }
    });
```

### Create (INSERT)

```java
HashMap<String, Object> userData = new HashMap<>();
userData.put("username", "johndoe");
userData.put("email", "john@example.com");

postgrest.insert("users", userData, null, null, callback);
```

### Update

```java
HashMap<String, Object> updates = new HashMap<>();
updates.put("status", "inactive");

postgrest.update("users", updates, 
    new UpdateQueryBuilder().equal("id", 123),
    null, callback);
```

### Delete

```java
postgrest.delete("users", 
    new DeleteQueryBuilder().equal("id", 123),
    null, callback);
```

## Query Builder & Filters

The `SelectQueryBuilder` (and others) supports a wide range of filters:

| Method | Description | SQL Equivalent |
|--------|-------------|----------------|
| `equal(col, val)` | Equals | `col = val` |
| `notEqual(col, val)` | Not equals | `col <> val` |
| `greaterThan(col, val)` | Greater than | `col > val` |
| `greaterThanOrEqual(col, val)` | Greater than or equal | `col >= val` |
| `lessThan(col, val)` | Less than | `col < val` |
| `lessThanOrEqual(col, val)` | Less than or equal | `col <= val` |
| `like(col, pattern)` | Pattern matching | `col LIKE pattern` |
| `iLike(col, pattern)` | Case-insensitive matching | `col ILIKE pattern` |
| `in(col, values)` | In list | `col IN (values)` |
| `isNull(col)` | Is null | `col IS NULL` |
| `contains(col, val)` | Contains (arrays) | `col @> val` |
| `overlaps(col, val)` | Overlaps (arrays) | `col && val` |
| `matchRegex(col, regex)` | Regex match | `col ~ regex` |

### Complex Logic (OR/AND)

```java
builder.or(
    new SelectQueryConstraint().equal("id", 1),
    new SelectQueryConstraint().greaterThan("age", 18)
);
```

## Ordering and Pagination

```java
builder.order(new Order("created_at", false)) // Descending
       .limit(10)
       .offset(20);
```

## Resource Embedding (Joins)

```java
builder.select("title")
       .addReference("authors", null, Join.INNER, 
           new SelectQueryBuilder().select("name"));
```

## Remote Procedure Calls (RPC)

```java
HashMap<String, Object> params = new HashMap<>();
params.add("arg_name", "value");

postgrest.rpc("my_function_name", params, null, null, callback);
```

## Error Handling

Errors are returned via the `OnPostgrestCallback#onFailure` method as a `SupabaseError` object, containing the status code and error message from the server.

---

## API Reference

### `SupabasePostgres`
The primary interface for the PostgREST library.

- `getInstance(Context)`: Gets the singleton client.
- `select(...)`: Executes a GET request.
- `insert(...)`: Executes a POST request.
- `update(...)`: Executes a PATCH request.
- `upsert(...)`: Executes a PUT request.
- `delete(...)`: Executes a DELETE request.
- `rpc(...)`: Executes a database function.

### `PostgrestResult`
Encapsulates the response data.

- `get(Class<T>)`: Returns an ArrayList of the specified type.
- `getSingle(Class<T>)`: Returns a single object.
- `get()`: Returns a JSONArray.
- `getSingle()`: Returns a JSONObject.
- `getCount()`: Returns the total count (if requested).
- `getRawData()`: Returns the raw JSON string.

---

## Best Practices

1. **Singleton Usage**: Always use `SupabasePostgres.getInstance()` to reuse the underlying service instances.
2. **Type Safety**: Define POJO classes for your tables and use `websocketResult.get(MyClass.class)` for automatic deserialization.
3. **Count Requests**: If you need the total count for pagination, configure `PostgrestConfig` with `getExactCount()`.
4. **Error Checking**: Always check the `SupabaseError` in your callback to handle network or database issues gracefully.

## Troubleshooting

- **401 Unauthorized**: Ensure your `SupabaseClient` is initialized with a valid API key and the user is authenticated if RLS is enabled.
- **404 Not Found**: Check the table name spelling and ensure it's exposed in the `public` schema (or the schema you've configured).
- **Empty Results**: Verify your filters and ensure data exists in the database.

---
*Generated by Documentation Specialist Agent*
