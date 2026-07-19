# Supabase Core

## Overview

Supabase Core is the foundational library for the Supabase Android SDK. It provides the essential infrastructure required for all other Supabase modules (Auth, Postgrest, Storage, Realtime) to function. 

This library exists to centralize common logic such as network communication, configuration management, threading, and JSON utilities. By providing a unified core, it ensures consistency across the entire SDK and simplifies the implementation of feature-specific modules.

### Problems Solved:
- **Centralized Configuration:** Managing project URLs and API keys in one place.
- **Unified Networking:** A robust wrapper around `HttpURLConnection` tailored for Supabase's REST API.
- **Authentication Flow:** Automatic handling of Bearer tokens across all outgoing requests.
- **Thread Management:** Dedicated thread pools for different types of background tasks (e.g., standard requests vs. file uploads).
- **Error Handling:** A consistent error wrapping mechanism that distinguishes between technical and user-friendly messages.

## Features

- **Global Initialization:** Simple singleton-based setup for the entire SDK.
- **Fluent URL Building:** Easily construct complex Supabase URLs with paths and query parameters.
- **HTTP Request Handler:** Support for GET, POST, PUT, PATCH, DELETE, and HEAD with automatic header management.
- **JSON Utilities:** High-level builders and parsers for `JSONObject` and `JSONArray`.
- **Config Listeners:** React to configuration changes (like user login/logout) throughout the SDK.
- **Asynchronous Execution:** Pre-configured `ExecutorService` instances for various workloads.
- **Progress Tracking:** Standardized interfaces for monitoring long-running data transfers.

## Architecture

The Supabase Core module follows a modular and singleton-based architecture. The `Supabase` class acts as the central registry and configuration holder.

### Main Classes
- **`Supabase`**: The main entry point and singleton instance.
- **`SupabaseConfig`**: Data class holding project credentials and state.
- **`RequestHandler`**: The engine that executes network requests.
- **`Runnables`**: Manages background thread pools.

### Data Flow
1. **Initialization:** The app calls `Supabase.initialize()`.
2. **Configuration:** Credentials are stored in `SupabaseConfig`.
3. **Request Creation:** Other modules (like Auth) use `UrlBuilder` and `Request` to define an operation.
4. **Execution:** `RequestHandler` takes the `Request`, adds auth headers from `SupabaseConfig`, and executes it.
5. **Response:** A `Response` object is returned, parsing the result or capturing errors in `SupabaseError`.

### ASCII Diagram

```
       +-----------------------+
       |      Application      |
       +-----------+-----------+
                   |
                   v
       +-----------------------+
       |       Supabase        |<------+
       | (Singleton / Registry)|       |
       +-----------+-----------+       |
                   |                   |
    +--------------+---------------+   | Notify
    |              |               |   | Changes
    v              v               v   |
+--------+   +-----------+   +---------+--+
| Config |   | Runnables |   | Callbacks  |
+--------+   +-----------+   +------------+
    |              |
    |              |
    v              v
+-----------------------+
|    RequestHandler     |
| (Network Operations)  |
+-----------+-----------+
            |
            v
+-----------------------+
|   Supabase API (IO)   |
+-----------------------+
```

## Package Structure

### `io.github.maskmasteruk.supabase.core`
- **`Supabase`**: Entry point for the SDK.
- **`Runnables`**: Thread pool management.

### `io.github.maskmasteruk.supabase.core.Config`
- **`SupabaseConfig`**: Project-level configuration (URL, Keys).
- **`SessionConfig`**: Structure for authentication tokens.

### `io.github.maskmasteruk.supabase.core.Network`
- **`RequestHandler`**: Executes HTTP requests.
- **`UrlBuilder`**: Builds encoded URLs.
- **`HttpMethod`**: Enum of supported HTTP verbs.

### `io.github.maskmasteruk.supabase.core.Objects`
- **`Request`**: Model for an outgoing HTTP request.
- **`Response`**: Model for an incoming HTTP response.
- **`SupabaseError`**: Custom exception wrapper.

### `io.github.maskmasteruk.supabase.core.Utils`
- **`JsonUtils`**: JSON parsing and building helpers.

### `io.github.maskmasteruk.supabase.core.Callback`
- **`OnProgressCallback`**: Listener for transfer progress.
- **`OnSupabaseConfigChangeCallback`**: Listener for configuration updates.

### `io.github.maskmasteruk.supabase.core.VALUES`
- **`CONSTANTS`**: Static values and error mappings.

## Installation

```kotlin
plugins {
    id("io.github.maskmasteruk.supabase") version "0.0.1"
}

dependencies {
    implementation("io.github.maskmasteruk:supabase-android-core:0.0.1")
}
```

## Quick Start

```java
// 1. Initialize
SupabaseConfig config = new SupabaseConfig("https://your-project.supabase.co", "your-anon-key");
Supabase.initialize(config);

// 2. Perform a simple GET request
Runnables.getExecutorService().execute(() -> {
    try {
        RequestHandler handler = new RequestHandler();
        Response response = handler.get("https://your-project.supabase.co/rest/v1/health");
        
        if (response.getCode() == 200) {
            System.out.println("Status: " + response.getResponse());
        }
    } catch (SupabaseError e) {
        e.printStackTrace();
    }
});
```

## Initialization

Initialization must happen before any other SDK feature is used. It is recommended to do this in your `Application.onCreate()`.

### Step 1: Create Configuration
```java
SupabaseConfig config = new SupabaseConfig()
    .setProjectUrl("https://xyz.supabase.co")
    .setProjectPublishableKey("sb_publishable_...");
```

### Step 2: Initialize Singleton
```java
Supabase.initialize(config);
```

## Authentication

Authentication is handled by managing the Bearer token in the `Supabase` singleton.

### Setting a User Token
When a user logs in (via `supabase-auth`), the access token should be set:
```java
Supabase.getInstance().setBearer("user-jwt-token");
```
This automatically updates all future requests to include `Authorization: Bearer user-jwt-token`.

### Listening for Changes
Internal modules listen for these changes to refresh their internal state:
```java
Supabase.getInstance().addOnSupabaseConfigChangeCallbacks(() -> {
    // React to new token or config change
});
```

## Database APIs (Low Level)

While `supabase-postgrest` provides high-level APIs, the core `RequestHandler` can be used for direct REST calls.

### Example: Custom POST request
```java
RequestHandler handler = new RequestHandler();
String json = new JsonUtils.JsonObjectStringBuilder()
    .append("name", "New Item")
    .append("description", "Description")
    .build();

Response response = handler.post("https://.../rest/v1/items", json);
```

## Storage APIs (Low Level)

The Core module provides the networking foundation for file transfers.

### Upload with Progress
```java
Request request = new Request(url)
    .setHttpMethod(HttpMethod.POST)
    .setUploadRunnable(connection -> {
        // Stream file content to connection.getOutputStream()
        // Call callback.onProgress()
    });

RequestHandler handler = new RequestHandler();
Response response = handler.sendRequest(request, apiKey, bearer);
```

## Session Management

`SessionConfig` is a simple container for tokens.

```java
SessionConfig session = new SessionConfig()
    .setAccessToken("jwt...")
    .setRefreshToken("refresh...");

// Store this locally using SharedPreferences or EncryptedSharedPreferences
```

## Network Layer

The network layer uses `HttpURLConnection` for broad compatibility and zero external networking dependencies (like OkHttp).

### Request Flow:
1. `Request` object is prepared.
2. `RequestHandler.sendRequest()` is called.
3. Headers are injected: `apikey`, `Authorization`, `Content-Type`.
4. Body is written if present.
5. Connection is established.
6. `Response` object captures the stream and disconnects.

## Error Handling

All network and logic errors are wrapped in `SupabaseError`.

### Handling errors:
```java
try {
    // Supabase operation
} catch (SupabaseError error) {
    if (error.isUserFriendly()) {
        showToast(error.getToastMessage());
    } else {
        logError(error.getException());
    }
}
```

## Configuration

`SupabaseConfig` allows for specific behavior adjustments:

- `setAllowOtherKeys(boolean)`: By default, keys must start with `sb_publishable_`. Set to `true` to use custom or legacy keys.
- `setProjectServiceRoleKey(String)`: Use this to perform administrative tasks (bypass RLS). **Never use in production apps.**

## Threading

The SDK does **not** run network operations on the main thread. You must use `Runnables` or your own threading solution.

- **`Runnables.getExecutorService()`**: 5 threads for general API calls.
- **`Runnables.getStorageUploadExecutorService()`**: Dedicated pool for uploads.
- **`Runnables.getStorageDownloadExecutorService()`**: Dedicated pool for downloads.

**Warning:** Calling `RequestHandler` methods on the UI thread will cause a `NetworkOnMainThreadException`.

## Lifecycle

The `Supabase` instance is a process-level singleton. It persists as long as the app process is alive.

To clean up resources (e.g., when the app is closing or in tests):
```java
Runnables.shutdown();
```

## API Reference

### `Supabase`

The central manager of the SDK.

#### `initialize(SupabaseConfig)`
- **Purpose:** Initializes the singleton.
- **Parameters:** `SupabaseConfig config`
- **Returns:** `void`

#### `getInstance()`
- **Purpose:** Retrieves the singleton instance.
- **Throws:** `SupabaseError` if not initialized.

#### `setBearer(String)`
- **Purpose:** Sets the current JWT and notifies listeners.

---

### `RequestHandler`

Handles HTTP communication.

#### `get(String url)`
- **Purpose:** Performs a GET request.
- **Returns:** `Response`

#### `post(String url, String json)`
- **Purpose:** Performs a POST request.
- **Returns:** `Response`

#### `sendRequest(Request, String, String)`
- **Purpose:** Advanced method for custom requests.

---

### `JsonUtils`

JSON construction helpers.

#### `JsonObjectBuilder`
- **Purpose:** Builds `JSONObject` instances fluently.

#### `JsonArrayBuilder`
- **Purpose:** Builds `JSONArray` instances fluently.

---

### `SupabaseError`

Exception wrapper.

#### `getToastMessage()`
- **Purpose:** Returns a message safe for UI display.

---

## Examples

### Full Initialization and Usage
```java
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SupabaseConfig config = new SupabaseConfig("https://url.supabase.co", "sb_publishable_key");
        Supabase.initialize(config);
    }
}
```

### Building a Complex JSON
```java
String json = new JsonUtils.JsonObjectStringBuilder()
    .append("id", 1)
    .append("metadata", new JsonUtils.JsonObjectBuilder()
        .append("color", "red")
        .append("tags", new JsonUtils.JsonArrayBuilder()
            .append("new")
            .append("featured")
            .build())
        .build())
    .build();
```

### Custom URL Construction
```java
String url = new UrlBuilder()
    .appendPath("rest")
    .appendPath("v1")
    .appendPath("users")
    .appendQueryParam("id", "eq.123")
    .build();
```

## Best Practices

1. **Initialize once:** Always initialize in the `Application` class.
2. **Use the General Executor:** For most tasks, use `Runnables.getExecutorService()`.
3. **Handle User Friendly Errors:** Use `error.getToastMessage()` to show errors to users.
4. **Keep Secrets Secret:** Never embed the `service_role` key in a client-side Android app.

## Common Mistakes

- **Main Thread Requests:** Attempting to use `RequestHandler` on the UI thread.
- **Double Initialization:** Calling `Supabase.initialize()` multiple times (it will ignore subsequent calls).
- **Hardcoding URLs:** Always use `UrlBuilder` to ensure proper encoding and base URL usage.

## Performance Tips

- **Reuse RequestHandler:** While stateless, it's efficient to reuse a single instance of `RequestHandler` within a module.
- **Buffer Sizes:** Use the constants in `RequestHandler` (`AVG_BUFFER_SIZE`) when implementing stream-based uploads/downloads.

## FAQ

**Q: Can I use this without the other Supabase modules?**
A: Yes, you can use the Core module as a standalone light-weight networking library for Supabase.

**Q: Does it support HTTPS?**
A: Yes, it uses `HttpURLConnection` which handles HTTPS and SSL/TLS automatically.

**Q: How do I handle token expiration?**
A: The `Supabase` class provides the `setBearer` method. When your auth module refreshes a token, call this method to update the core.

## Troubleshooting

- **"Supabase is not Initialized":** Ensure you called `Supabase.initialize()` before calling `Supabase.getInstance()`.
- **401 Unauthorized:** Check if your API key is correct and if you've set the bearer token correctly after login.
- **403 Forbidden:** Likely a Row Level Security (RLS) issue on your Supabase project.

## Internal Architecture

The Core module is designed to be "passive". It doesn't initiate actions on its own but provides the tools for other modules. For example, `supabase-auth` uses `RequestHandler` to talk to GoTrue, and `supabase-postgrest` uses `UrlBuilder` to construct query strings.

## Class Relationships

- `Supabase` <-> `SupabaseConfig`: `Supabase` holds the active `SupabaseConfig`.
- `RequestHandler` -> `Supabase`: `RequestHandler` pulls the current URL and keys from the `Supabase` singleton.
- `Request` -> `Response`: `RequestHandler` transforms a `Request` into a `Response`.
- `Response` -> `JsonUtils`: `Response` uses `JsonUtils` to parse its body into JSON objects.

## Glossary

- **JWT:** JSON Web Token, used for authentication.
- **RLS:** Row Level Security, Supabase's mechanism for authorization.
- **Anon Key:** The public API key used for anonymous or user-authenticated requests.
- **Service Role Key:** An administrative key that bypasses all security policies.
- **Bearer Token:** An authentication scheme using a JWT.
