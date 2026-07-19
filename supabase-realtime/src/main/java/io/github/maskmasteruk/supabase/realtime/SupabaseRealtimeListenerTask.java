package io.github.maskmasteruk.supabase.realtime;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.maskmasteruk.supabase.core.Objects.SupabaseError;
import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;
import io.github.maskmasteruk.supabase.realtime.Callback.OnReceiveCallback;

/**
 * Represents a long-running task that listens for incoming messages on a websocket.
 *
 * <p>This task continuously reads from the input stream and notifies registered
 * callbacks when messages matching the filter criteria (if any) arrive.</p>
 *
 * <pre>{@code
 * channel.listen(PostgresChangeEvent.INSERT)
 *        .addOnReceiveCallback(new OnReceiveCallback<Event.PhoenixMessage>() {
 *            @Override
 *            public void onReceive(Event.PhoenixMessage message) {
 *                if (message instanceof Event.PostgresChangesMessage) {
 *                    System.out.println("New row: " + ((Event.PostgresChangesMessage) message).getData().getRecord());
 *                }
 *            }
 *            // ...
 *        });
 * }</pre>
 */
public class SupabaseRealtimeListenerTask {
    private final ArrayList<OnReceiveCallback<Event.PhoenixMessage>> onReceiveCallbacks;
    private final ExecutorService executorService;
    private final InputStream inputStream;

    private final boolean ALL;

    private final ArrayList<String> events;

    private final AtomicBoolean isTerminated = new AtomicBoolean(false);

    /**
     * Constructs a listener for specific events.
     *
     * @param executorService Executor to run the listening loop.
     * @param inputStream The socket's input stream.
     * @param events List of event names to filter.
     */
    public SupabaseRealtimeListenerTask(ExecutorService executorService, InputStream inputStream, ArrayList<String> events) {
        this.executorService = executorService;
        this.inputStream = inputStream;
        this.events = events;
        ALL = false;
        onReceiveCallbacks = new ArrayList<>();
        listen();
    }

    /**
     * Constructs a listener for all events.
     *
     * @param executorService Executor to run the listening loop.
     * @param inputStream The socket's input stream.
     */
    public SupabaseRealtimeListenerTask(ExecutorService executorService, InputStream inputStream) {
        this.executorService = executorService;
        this.inputStream = inputStream;
        this.events = null;
        ALL = true;
        onReceiveCallbacks = new ArrayList<>();
        listen();

    }

    /**
     * Adds a callback to be invoked when a matching message is received.
     *
     * @param onReceiveCallback The callback to add.
     * @return This task instance for chaining.
     */
    public SupabaseRealtimeListenerTask addOnReceiveCallback(OnReceiveCallback<Event.PhoenixMessage> onReceiveCallback) {
        onReceiveCallbacks.add(onReceiveCallback);
        return this;
    }

    /**
     * Internal method to trigger error callbacks.
     *
     * @param supabaseError The error that occurred.
     */
    private void onError(SupabaseError supabaseError) {
        onReceiveCallbacks.forEach(tOnCompleteCallback -> tOnCompleteCallback.onError(supabaseError));
    }

    /**
     * Internal method to trigger receive callbacks.
     *
     * @param phoenixMessage The received message.
     */
    private void onReceive(Event.PhoenixMessage phoenixMessage) {
        onReceiveCallbacks.forEach(tOnCompleteCallback -> tOnCompleteCallback.onReceive(phoenixMessage));
    }

    /**
     * Starts the listening loop on a background thread.
     */
    private void listen() {
        executorService.execute(() -> {
            while (!Thread.currentThread().isInterrupted() && !isTerminated.get()) {
                try {
                    if (isTerminated.get()) {
                        break;
                    }

                    String json = Websocket.readMessage(inputStream);

                    if (isTerminated.get()) {
                        break;
                    }

                    if (json == null) break;
                    if (json.isEmpty()) continue;


                    Event.PhoenixMessage phoenixMessage = Event.from(JsonUtils.getJsonObject(json));

                    if (phoenixMessage.getRef() != null && (phoenixMessage.getRef().startsWith("HEARTBEAT_") || phoenixMessage.getRef().startsWith("ACCESS_TOKEN_"))) {
                        continue;
                    }

                    if (ALL || events.contains(phoenixMessage.getEvent())) {
                        onReceive(phoenixMessage);
                    }
                } catch (Exception e) {
                    onError(new SupabaseError(e));
                }
            }
        });
    }

    /**
     * Terminates the listening task and shuts down the associated executor.
     */
    public void terminate() {
        isTerminated.set(true);
        executorService.shutdownNow();
    }
}
