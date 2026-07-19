package io.github.maskmasteruk.supabase.realtime;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.github.maskmasteruk.supabase.core.Utils.JsonUtils;
import io.github.maskmasteruk.supabase.realtime.Enum.PhoenixEvent;
import io.github.maskmasteruk.supabase.realtime.Enum.PostgresChangeEvent;
import io.github.maskmasteruk.supabase.realtime.Enum.PresenceTypes;

/**
 * Utility class for building JSON request payloads for the Phoenix/Supabase Realtime protocol.
 *
 * <p>This class contains nested classes for different types of requests such as
 * joining a channel, updating access tokens, broadcasting messages, updating
 * presence, and sending heartbeats.</p>
 */
public class EventBuilder {

    /**
     * Represents a request to join a Phoenix channel.
     *
     * <p>Configures various features like broadcast, presence, and database subscriptions
     * for the channel being joined.</p>
     */
    public static class JoinRequest {
        private final String topic;
        private final PhoenixEvent event;
        private final Payload payload;
        private final String ref;
        private final String join_ref;

        private JoinRequest(Builder builder) {
            this.topic = builder.topic;
            this.event = builder.event;
            this.ref = builder.ref;
            this.join_ref = builder.join_ref;

            Broadcast broadcast = new Broadcast(builder.broadcastAck, builder.broadcastSelf);
            Presence presence = new Presence(builder.presenceEnabled);
            Config config = new Config(broadcast, presence, builder.postgresChanges, builder.isPrivate);
            this.payload = new Payload(config, builder.accessToken, builder.customPayload);
        }

        /**
         * Serializes the join request to a JSON string.
         *
         * @return The JSON string representation of the join request.
         */
        public String toJsonString() {
            return new JsonUtils.JsonObjectStringBuilder()
                    .append("topic", this.topic)
                    .append("event", this.event != null ? this.event.getEventValue() : null)
                    .append("payload", this.payload.toBuilderObject())
                    .append("ref", this.ref)
                    .append("join_ref", this.join_ref)
                    .build();
        }

        /**
         * Builder class for creating {@link JoinRequest} instances.
         */
        public static class Builder {
            private String topic;
            private final PhoenixEvent event = PhoenixEvent.JOIN;
            private String ref;
            private String join_ref;

            private String accessToken;

            private boolean isPrivate;
            private boolean broadcastAck;
            private boolean broadcastSelf;
            private boolean presenceEnabled;
            private final List<PostgresChange> postgresChanges = new ArrayList<>();
            private final HashMap<String, Object> customPayload = new HashMap<>();

            /** Sets the topic (channel name) to join. */
            public Builder topic(String topic) {
                this.topic = topic;
                return this;
            }

            /** Sets the message reference ID. */
            public Builder ref(int ref) {
                this.ref = String.valueOf(ref);
                return this;
            }

            /** Sets the join reference ID. */
            public Builder join_ref(long join_ref) {
                this.join_ref = String.valueOf(join_ref);
                return this;
            }

            /** Sets the JWT access token for authentication. */
            public Builder accessToken(String accessToken) {
                this.accessToken = accessToken;
                return this;
            }

            /** Sets whether the channel is private. */
            public Builder isPrivate(boolean isPrivate) {
                this.isPrivate = isPrivate;
                return this;
            }

            /** Sets whether to request acknowledgments for broadcasts. */
            public Builder broadcastAck(boolean ack) {
                this.broadcastAck = ack;
                return this;
            }

            /** Sets whether to receive broadcasts sent by this client. */
            public Builder broadcastSelf(boolean self) {
                this.broadcastSelf = self;
                return this;
            }

            /** Sets whether presence features are enabled for this channel. */
            public Builder presenceEnabled(boolean enabled) {
                this.presenceEnabled = enabled;
                return this;
            }

            /** Adds a custom key-value pair to the join payload. */
            private Builder addCustomPayload(String key, Object value) {
                customPayload.put(key, value);
                return this;
            }

            /**
             * Adds a single Postgres change filter to the internal list.
             *
             * @param event The database event type.
             * @param schema The database schema.
             * @param table The table name.
             * @param postgresChangeFilter The filter configuration.
             * @return This builder instance.
             */
            public Builder addPostgresChange(PostgresChangeEvent event, String schema, String table, PostgresChange.PostgresChangeFilter postgresChangeFilter) {
                this.postgresChanges.add(new PostgresChange(event, schema, table, postgresChangeFilter));
                return this;
            }

            /**
             * Adds a list of Postgres change subscriptions.
             *
             * @param postgresChanges The list of changes to subscribe to.
             * @return This builder instance.
             */
            public Builder setPostgresChange(ArrayList<PostgresChange> postgresChanges) {
                this.postgresChanges.addAll(postgresChanges);
                return this;
            }

            /**
             * Builds the {@link JoinRequest}.
             *
             * @return A new {@link JoinRequest} instance.
             */
            public JoinRequest build() {
                return new JoinRequest(this);
            }
        }

        /** Internal payload structure for the join request. */
        private static class Payload {
            private final Config config;
            private final String accessToken;
            private final HashMap<String, Object> customPayload;

            public Payload(Config config, String accessToken, HashMap<String, Object> customPayload) {
                this.config = config;
                this.accessToken = accessToken;
                this.customPayload = customPayload;
            }

            public JSONObject toBuilderObject() {
                JsonUtils.JsonObjectBuilder jsonObjectBuilder = new JsonUtils.JsonObjectBuilder()
                        .append("config", this.config != null ? this.config.toBuilderObject() : null)
                        .append("access_token", this.accessToken);
                customPayload.forEach(jsonObjectBuilder::append);
                return jsonObjectBuilder
                        .build();
            }
        }

        /** Internal configuration structure for the join request. */
        private static class Config {
            private final Broadcast broadcast;
            private final Presence presence;
            private final List<PostgresChange> postgresChanges;
            private final boolean isPrivate;

            public Config(Broadcast broadcast, Presence presence, List<PostgresChange> postgresChanges, boolean isPrivate) {
                this.broadcast = broadcast;
                this.presence = presence;
                this.postgresChanges = postgresChanges;
                this.isPrivate = isPrivate;
            }

            public JSONObject toBuilderObject() {
                JSONArray changesArray = new JSONArray();
                if (this.postgresChanges != null) {
                    for (PostgresChange change : this.postgresChanges) {
                        changesArray.put(change.toBuilderObject());
                    }
                }

                return new JsonUtils.JsonObjectBuilder()
                        .append("broadcast", this.broadcast != null ? this.broadcast.toBuilderObject() : null)
                        .append("presence", this.presence != null ? this.presence.toBuilderObject() : null)
                        .append("postgres_changes", changesArray)
                        .append("private", this.isPrivate)
                        .build();
            }
        }

        /** Internal broadcast configuration. */
        private static class Broadcast {
            private final boolean ack;
            private final boolean self;

            public Broadcast(boolean ack, boolean self) {
                this.ack = ack;
                this.self = self;
            }

            public JSONObject toBuilderObject() {
                return new JsonUtils.JsonObjectBuilder()
                        .append("ack", this.ack)
                        .append("self", this.self)
                        .build();
            }
        }

        /** Internal presence configuration. */
        private static class Presence {
            private final boolean enabled;

            public Presence(boolean enabled) {
                this.enabled = enabled;
            }

            public JSONObject toBuilderObject() {
                return new JsonUtils.JsonObjectBuilder()
                        .append("enabled", this.enabled)
                        .build();
            }
        }

    }

    /**
     * Represents a request to update the access token for a channel.
     */
    public static class AccessTokenRequest {

        private final String topic;
        private final PhoenixEvent event;
        private final Payload payload;
        private final String ref;

        /**
         * Constructs an AccessTokenRequest.
         *
         * @param topic       The channel topic (e.g., "realtime:public:todos")
         * @param accessToken The JWT token
         * @param ref         The message reference ID
         */
        public AccessTokenRequest(String topic, String accessToken, int ref) {
            this.topic = topic;
            this.event = PhoenixEvent.ACCESS_TOKEN;
            this.payload = new Payload(accessToken);
            this.ref = String.valueOf(ref);
        }

        /**
         * Serializes the request to a JSON string.
         *
         * @return The JSON string representation.
         */
        public String toJsonString() {
            return new JsonUtils.JsonObjectStringBuilder()
                    .append("topic", this.topic)
                    .append("event", this.event != null ? this.event.getEventValue() : null)
                    .append("payload", this.payload != null ? this.payload.toBuilderObject() : null)
                    .append("ref", "ACCESS_TOKEN_" + this.ref)
                    .build();
        }

        /** Internal payload for AccessTokenRequest. */
        public static class Payload {
            private String access_token;

            public Payload() {}

            public Payload(String accessToken) {
                this.access_token = accessToken;
            }

            /**
             * Helper method to convert payload fields into a builder object.
             *
             * @return A builder object for serialization.
             */
            public JsonUtils.JsonObjectStringBuilder toBuilderObject() {
                return new JsonUtils.JsonObjectStringBuilder()
                        .append("access_token", this.access_token);
            }
        }
    }

    /**
     * Represents a request to broadcast a message to a channel.
     */
    public static class BroadcastRequest {

        private final String topic;
        private final String event;
        private final JSONObject payload;
        private final String ref;
        private final String join_ref;

        /**
         * Constructs a BroadcastRequest.
         *
         * @param topic    The channel topic.
         * @param event    The name of the broadcast event.
         * @param payload  The message payload.
         * @param ref      The message reference ID.
         * @param join_ref The join reference ID.
         */
        public BroadcastRequest(String topic, String event, JSONObject payload, int ref, long join_ref) {
            this.topic = topic;
            this.event = event;
            this.payload = payload;
            this.ref = String.valueOf(ref);
            this.join_ref = String.valueOf(join_ref);
        }

        /**
         * Serializes the request to a JSON string.
         *
         * @return The JSON string representation.
         */
        public String toJsonString() {
            return new JsonUtils.JsonObjectStringBuilder()
                    .append("topic", this.topic)
                    .append("event", PhoenixEvent.BROADCAST.getEventValue())
                    .append("payload", new JsonUtils.JsonObjectBuilder().append("event", event).append("payload", payload).build())
                    .append("ref", this.ref)
                    .append("join_ref", join_ref)
                    .build();
        }

    }

    /**
     * Represents a request to update presence state in a channel.
     */
    public static class PresenceRequest {

        private final String topic;
        private final PresenceTypes type;
        private final JSONObject payload;
        private final String ref;
        private final String join_ref;

        /**
         * Constructs a PresenceRequest.
         *
         * @param topic    The channel topic.
         * @param type     The type of presence action (TRACK or UNTRACK).
         * @param payload  The presence metadata.
         * @param ref      The message reference ID.
         * @param join_ref The join reference ID.
         */
        public PresenceRequest(String topic, PresenceTypes type, JSONObject payload, int ref, long join_ref) {
            this.topic = topic;
            this.type = type;
            this.payload = payload;
            this.ref = String.valueOf(ref);
            this.join_ref = String.valueOf(join_ref);
        }

        /**
         * Serializes the request to a JSON string.
         *
         * @return The JSON string representation.
         */
        public String toJsonString() {
            JsonUtils.JsonObjectBuilder payloadJson = new JsonUtils.JsonObjectBuilder()
                    .append("type", PhoenixEvent.PRESENCE.getEventValue())
                    .append("event", type.getValue());
            if (payload != null) {
                payloadJson.append("payload", payload);
            }
            return new JsonUtils.JsonObjectStringBuilder()
                    .append("topic", this.topic)
                    .append("event", PhoenixEvent.PRESENCE.getEventValue())
                    .append("payload", payloadJson.build())
                    .append("ref", this.ref)
                    .append("join_ref", join_ref)
                    .build();
        }

    }

    /**
     * Represents a heartbeat request to keep the websocket connection alive.
     */
    public static class HeartbeatRequest {
        private final String topic = "phoenix";
        private final PhoenixEvent event = PhoenixEvent.HEARTBEAT;
        private final Object payload = new JSONObject();
        private final int ref;

        /**
         * Constructs a HeartbeatRequest.
         *
         * @param ref The message reference ID.
         */
        public HeartbeatRequest(int ref) {
            this.ref = ref;
        }

        /**
         * Serializes the request to a JSON string.
         *
         * @return The JSON string representation.
         */
        public String toJsonString() {
            return new JsonUtils.JsonObjectStringBuilder()
                    .append("topic", this.topic)
                    .append("event", this.event.getEventValue())
                    .append("payload", this.payload)
                    .append("ref", "HEARTBEAT_" + this.ref)
                    .build();
        }

    }

    /**
     * Represents a request to leave a channel.
     */
    public static class LeaveRequest {
        private final String topic;
        private final PhoenixEvent event = PhoenixEvent.LEAVE;
        private final Object payload = new JSONObject();
        private final int ref;
        private final Long join_ref;

        /**
         * Constructs a LeaveRequest.
         *
         * @param topic    The channel topic to leave.
         * @param ref      The message reference ID.
         * @param join_ref The join reference ID associated with the current join session.
         */
        public LeaveRequest(String topic, int ref, Long join_ref) {
            this.topic = topic;
            this.ref = ref;
            this.join_ref = join_ref;
        }

        /**
         * Serializes the request to a JSON string.
         *
         * @return The JSON string representation.
         */
        public String toJsonString() {
            return new JsonUtils.JsonObjectStringBuilder()
                    .append("topic", this.topic)
                    .append("event", this.event.getEventValue())
                    .append("payload", this.payload)
                    .append("ref", String.valueOf(this.ref))
                    .append("join_ref", String.valueOf(this.join_ref))
                    .build();
        }

    }
}
