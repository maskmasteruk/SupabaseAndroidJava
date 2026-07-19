# Supabase Realtime Android

## Overview

The **Supabase Realtime Android** library is a robust Java implementation of the Phoenix Realtime protocol, specifically tailored for Android applications using Supabase. It allows developers to build interactive, multi-user applications with features like live chat, presence tracking, and real-time database updates.

### Architecture

The library is built on a custom WebSocket implementation (RFC 6455) and follows the Phoenix protocol's multiplexing pattern. A single WebSocket connection can manage multiple logical `SupabaseChannel` instances, each representing a different topic.

### Design Philosophy

- **Asynchronous First**: All network operations are offloaded to background threads using `ExecutorService`.
- **Callback Driven**: Simple interface-based callbacks for handling events and errors.
- **Type Safety**: Use of Java Generics and Enums to ensure robust API interactions.
- **Minimal Dependencies**: Lightweight implementation with minimal external library requirements.

---

## Features

- **Channels**: Create, join, and leave multiple logical channels over a single connection.
- **Broadcast**: Send and receive low-latency messages between clients.
- **Presence**: Track user online status and share custom state (e.g., status, cursor position).
- **Database Changes (CDC)**: Listen to PostgreSQL database events (INSERT, UPDATE, DELETE) with complex server-side filtering.
- **Automatic Authentication**: seamless integration with Supabase Auth for private channels, including automatic token refresh.
- **Heartbeat System**: Automatic heartbeat mechanism to maintain persistent connections.
- **Flexible Listening**: Filter incoming messages by specific events or listen to all activity on a channel.

---

## Installation

```kotlin
plugins {
    id("io.github.maskmasteruk.supabase") version "0.0.1"
}

dependencies {
    implementation("io.github.maskmasteruk:supabase-android-core:0.0.1")
    implementation("io.github.maskmasteruk:supabase-android-realtime:0.0.1")
}
```

---

## Requirements

- **Android SDK**: API level 21 or higher.
- **Java**: Java 8 or higher.
- **Permissions**:
  ```xml
  <uses-permission android:name="android.permission.INTERNET" />
  ```

---

## Project Structure

- `io.github.maskmasteruk.supabase.realtime`: Core classes like `SupabaseRealtime` and `SupabaseChannel`.
- `io.github.maskmasteruk.supabase.realtime.Enum`: Protocol enums for events and actions.
- `io.github.maskmasteruk.supabase.realtime.Callback`: Callback interfaces for async operations.
- `io.github.maskmasteruk.supabase.realtime.Event`: Data models for incoming and outgoing messages.

---

## Architecture Details

### Connection Flow
1. **Handshake**: The library performs an HTTP upgrade request to establish a WebSocket connection.
2. **Authentication**: If the channel is private, the Supabase JWT is sent during the join request.
3. **Multiplexing**: Channels are differentiated by their `topic` name.

### Socket Lifecycle
The WebSocket connection is managed by `SupabaseRealtime`. Each channel manages its own heartbeat and reconnection logic if the connection is dropped.

### Threading
- **Sender Thread**: A dedicated thread for outgoing WebSocket frames to prevent blocking the UI.
- **Receiver Thread**: A dedicated thread for reading and parsing incoming WebSocket frames.
- **Callback Thread**: Callbacks are typically invoked on the background receiver thread; developers should switch to the UI thread if performing view updates.

---

## Quick Start

### 1. Initialize Supabase
Ensure `Supabase.init()` is called before using Realtime.

### 2. Join a Channel
```java
SupabaseRealtime realtime = SupabaseRealtime.getInstance();

SupabaseChannel<JSONObject> channel = realtime.joinChannel(
    "lobby",    // channel name
    true,       // broadcastAck
    true,       // broadcastSelf
    true,       // presence
    false,      // isPrivate
    null        // postgresChanges
);

channel.addOnRealtimeCallback(new OnRealtimeCallback<JSONObject>() {
    @Override
    public void onConnected() {
        Log.d("Realtime", "Successfully joined the channel!");
    }

    @Override
    public void onClose() {
        Log.d("Realtime", "Channel closed.");
    }

    @Override
    public void onError(SupabaseError error) {
        Log.e("Realtime", "Error: " + error.getMessage());
    }
});
```

### 3. Broadcast Messages
```java
JSONObject payload = new JSONObject();
payload.put("message", "Hello everyone!");

// Broadcast is triggered immediately when calling broadcast()
channel.broadcast("chat_message", payload);
```

---

## Channels

### Creating and Joining
Channels are created via `SupabaseRealtime.getInstance().joinChannel()`. The topic is automatically prefixed with `realtime:`.

### Leaving a Channel
```java
// Leaving is triggered immediately when calling leave()
channel.leave().addOnCompleteCallback(new OnCompleteCallback<Void>() {
    @Override
    public void OnSuccess(Void result) {
        Log.d("Realtime", "Left channel successfully");
    }
    // ...
});
```

---

## Presence

Presence allows you to track users in a channel.

### Tracking State
```java
JSONObject meta = new JSONObject();
meta.put("status", "typing");
// Tracking is triggered immediately
channel.presenceTrack(meta);
```

### Listening to Presence Events
```java
channel.listen(PresenceEvent.SYNC, PresenceEvent.JOIN, PresenceEvent.LEAVE)
       .addOnReceiveCallback(new OnReceiveCallback<Event.PhoenixMessage>() {
           @Override
           public void onReceive(Event.PhoenixMessage message) {
               if (message instanceof Event.PresenceDiffMessage) {
                   Event.PresenceDiffMessage diff = (Event.PresenceDiffMessage) message;
                   Log.d("Presence", "Joins: " + diff.getJoins());
                   Log.d("Presence", "Leaves: " + diff.getLeaves());
               }
           }
           // ...
       });
```

---

## Database Changes (CDC)

Listen to database events for a specific table.

### Configuration
```java
ArrayList<PostgresChange> changes = new ArrayList<>();
changes.add(new PostgresChange(
    PostgresChangeEvent.INSERT,
    "public",
    "messages",
    new PostgresChange.PostgresChangeFilter.Equals("room_id", 1).build()
));

SupabaseChannel<JSONObject> channel = realtime.joinChannel("db-changes", true, true, false, true, changes);
```

### Receiving Changes
```java
channel.listen(PostgresChangeEvent.INSERT)
       .addOnReceiveCallback(new OnReceiveCallback<Event.PhoenixMessage>() {
           @Override
           public void onReceive(Event.PhoenixMessage message) {
               if (message instanceof Event.PostgresChangesMessage) {
                   JSONObject newRecord = ((Event.PostgresChangesMessage) message).getData().getRecord();
                   Log.d("DB", "New row: " + newRecord.toString());
               }
           }
           // ...
       });
```

---

## API Reference

### SupabaseRealtime
- `getInstance()`: Returns the singleton instance.
- `joinChannel(...)`: Connects to a new realtime topic.
- `closeAllChannel()`: Gracefully closes all active connections.

### SupabaseChannel
- `broadcast(event, payload)`: Sends a broadcast message immediately.
- `presenceTrack(payload)`: Starts tracking client presence immediately.
- `presenceUnTrack()`: Stops tracking client presence immediately.
- `listen(...)`: Registers a listener for specific events.
- `leave()`: Leaves the channel immediately.
- `close()`: Force-closes the channel resources.

### Event Models
- `Event.PhoenixMessage`: Base message class.
- `Event.ReplyMessage`: Server response to client actions.
- `Event.PresenceDiffMessage`: Incremental presence updates.
- `Event.PostgresChangesMessage`: Database change payload.

---

## Error Handling

The library uses `SupabaseError` to communicate failures. Common errors include:
- `Connection error`: WebSocket handshake failed.
- `Unauthorized`: Missing or invalid JWT for private channels.
- `Channel already joined`: Attempting to listen multiple times on the same channel instance.

---

## Thread Safety

- The library is designed to be thread-safe for multi-channel usage.
- All network writes are serialized through a single background thread per channel.
- Incoming messages are processed on a dedicated receiver thread.

---

## Performance Considerations

- **Memory**: Each channel creates its own executor services. Close channels when they are no longer needed to free resources.
- **CPU**: Parsing large JSON payloads from high-frequency broadcast events can be CPU intensive.
- **Battery**: Realtime connections keep the radio active. Use heartbeats judiciously and disconnect when the app is in the background if real-time updates aren't critical.

---

## Best Practices

1. **Clean Up**: Always call `channel.leave()` or `channel.close()` in your Activity's `onDestroy`.
2. **Filters**: Use server-side Postgres filters to minimize data transfer.
3. **UI Updates**: Use `Activity.runOnUiThread` or a `Handler` when updating views from callbacks.
4. **Token Refresh**: The library handles token refresh automatically if you use the core `Supabase` config for authentication.

---

## Full Example Project

```java
public class RealtimeChatActivity extends AppCompatActivity {

    private SupabaseChannel<JSONObject> chatChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatChannel = SupabaseRealtime.getInstance()
                .joinChannel("chat_room", true, true, true, false, null);

        chatChannel.addOnRealtimeCallback(new OnRealtimeCallback<JSONObject>() {
            @Override
            public void onConnected() {
                updateStatusUI("Connected");
            }

            @Override
            public void onClose() {
                updateStatusUI("Disconnected");
            }

            @Override
            public void onError(SupabaseError error) {
                showToast("Error: " + error.getMessage());
            }
        });

        chatChannel.listen("chat_message").addOnReceiveCallback(new OnReceiveCallback<Event.PhoenixMessage>() {
            @Override
            public void onReceive(Event.PhoenixMessage message) {
                if (message instanceof Event.GenericPhoenixMessage) {
                    JSONObject payload = ((Event.GenericPhoenixMessage) message).getPayload();
                    runOnUiThread(() -> addMessageToView(payload.optString("text")));
                }
            }

            @Override
            public void onError(SupabaseError error) {}
        });
    }

    public void onSendClicked(View v) {
        JSONObject msg = new JSONObject();
        try {
            msg.put("text", "Hello World!");
            // Broadcast is triggered immediately
            chatChannel.broadcast("chat_message", msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatChannel != null) {
            chatChannel.leave();
        }
    }
}
```

---

## License

Copyright (c) 2024 Udhai. All rights reserved.
Licensed under the MIT License.
