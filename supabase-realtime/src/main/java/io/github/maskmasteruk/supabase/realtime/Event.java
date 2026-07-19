package io.github.maskmasteruk.supabase.realtime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.github.maskmasteruk.supabase.realtime.Enum.PhoenixEvent;

/**
 * Container class for various types of Phoenix and Supabase Realtime messages and events.
 *
 * <p>This class provides nested static classes that represent different message structures
 * received over the websocket. It also includes a factory method to parse raw JSON into
 * specific message types.</p>
 */
public class Event {

    /**
     * Base class for all Phoenix protocol messages.
     *
     * <p>Contains common fields like reference ID, event name, and topic.</p>
     */
    public static abstract class PhoenixMessage {
        protected String ref;
        protected String event;
        protected String topic;

        /**
         * Constructs a PhoenixMessage from a JSON object.
         *
         * @param json The raw JSON message.
         */
        public PhoenixMessage(JSONObject json) {
            this.ref = json.isNull("ref") ? null : json.optString("ref", null);
            this.event = json.optString("event", null);
            this.topic = json.optString("topic", null);
        }

        /** @return The message reference ID, used for matching replies to requests. */
        public String getRef() {
            return ref;
        }

        /** @return The name of the event. */
        public String getEvent() {
            return event;
        }

        /** @return The topic (e.g., channel name) this message belongs to. */
        public String getTopic() {
            return topic;
        }

        @Override
        public String toString() {
            return "PhoenixMessage{" +
                    "ref='" + ref + '\'' +
                    ", event='" + event + '\'' +
                    ", topic='" + topic + '\'' +
                    '}';
        }
    }

    /**
     * Represents a system-level message from the Phoenix server.
     */
    public static class SystemMessage extends PhoenixMessage {

        private final String message;
        private final String status;
        private final String extension;
        private final String channel;

        /**
         * Constructs a SystemMessage from JSON.
         *
         * @param json The raw JSON.
         * @throws JSONException if payload is missing.
         */
        public SystemMessage(JSONObject json) throws JSONException {
            super(json);

            JSONObject payload = json.getJSONObject("payload");

            this.message = payload.optString("message", null);
            this.status = payload.optString("status", null);
            this.extension = payload.optString("extension", null);
            this.channel = payload.optString("channel", null);
        }

        /** @return The system message text. */
        public String getMessage() {
            return message;
        }

        /** @return The status of the system event. */
        public String getStatus() {
            return status;
        }

        /** @return Any extension data. */
        public String getExtension() {
            return extension;
        }

        /** @return The channel associated with the system message. */
        public String getChannel() {
            return channel;
        }

        @Override
        public String toString() {
            return "SystemMessage{" +
                    "ref='" + ref + '\'' +
                    ", event='" + event + '\'' +
                    ", topic='" + topic + '\'' +
                    ", message='" + message + '\'' +
                    ", status='" + status + '\'' +
                    ", extension='" + extension + '\'' +
                    ", channel='" + channel + '\'' +
                    '}';
        }
    }

    /**
     * Represents a reply message from the server in response to a client request.
     */
    public static class ReplyMessage extends PhoenixMessage {

        private final String status;
        private final ReplyResponse response;

        /**
         * Constructs a ReplyMessage from JSON.
         *
         * @param json The raw JSON.
         * @throws JSONException if payload is missing.
         */
        public ReplyMessage(JSONObject json) throws JSONException {
            super(json);

            JSONObject payload = json.getJSONObject("payload");

            this.status = payload.optString("status", null);
            this.response = new ReplyResponse(payload.optJSONObject("response"));
        }

        /** @return The status of the reply (e.g., "ok", "error"). */
        public String getStatus() {
            return status;
        }

        /** @return The detailed response data. */
        public ReplyResponse getResponse() {
            return response;
        }

        @Override
        public String toString() {
            return "ReplyMessage{" +
                    "ref='" + ref + '\'' +
                    ", event='" + event + '\'' +
                    ", topic='" + topic + '\'' +
                    ", status='" + status + '\'' +
                    ", response=" + response +
                    '}';
        }
    }

    /**
     * Represents the content of a Phoenix reply.
     */
    public static class ReplyResponse {

        private final List<PostgresChange> postgresChanges = new ArrayList<>();
        private final JSONObject raw;

        private final String reason;
        private final String message;
        private final String code;

        /**
         * Constructs a ReplyResponse from JSON.
         *
         * @param json The response JSON object.
         */
        public ReplyResponse(JSONObject json) {
            this.raw = json;

            if (json == null) {
                this.reason = null;
                this.message = null;
                this.code = null;
                return;
            }

            this.reason = json.optString("reason", null);
            this.message = json.optString("message", null);
            this.code = json.optString("code", null);

            JSONArray changes = json.optJSONArray("postgres_changes");
            if (changes != null) {
                for (int i = 0; i < changes.length(); i++) {
                    JSONObject obj = changes.optJSONObject(i);
                    if (obj != null) {
                        postgresChanges.add(new PostgresChange(obj));
                    }
                }
            }
        }

        /** @return A list of database subscription IDs if applicable. */
        public List<PostgresChange> getPostgresChanges() {
            return postgresChanges;
        }

        /** @return The raw JSON of the response. */
        public JSONObject getRaw() {
            return raw;
        }

        /** @return The reason for failure, if any. */
        public String getReason() {
            return reason;
        }

        /** @return The error message, if any. */
        public String getMessage() {
            return message;
        }

        /** @return The error code, if any. */
        public String getCode() {
            return code;
        }

        /** @return true if the response indicates an error. */
        public boolean hasError() {
            return reason != null || message != null || code != null;
        }

        @Override
        public String toString() {
            return "ReplyResponse{" +
                    "postgresChanges=" + postgresChanges +
                    ", reason='" + reason + '\'' +
                    ", message='" + message + '\'' +
                    ", code='" + code + '\'' +
                    ", raw=" + (raw != null ? raw.toString() : "null") +
                    '}';
        }
    }

    /**
     * Represents metadata for a Postgres change subscription.
     */
    public static class PostgresChange {

        private final long id;
        private final String event;
        private final String schema;
        private final String table;

        /**
         * Constructs a PostgresChange from JSON.
         *
         * @param json The change metadata JSON.
         */
        public PostgresChange(JSONObject json) {
            this.id = json.optLong("id");
            this.event = json.optString("event");
            this.schema = json.optString("schema");
            this.table = json.optString("table");
        }

        /** @return The internal ID of the change subscription. */
        public long getId() {
            return id;
        }

        /** @return The database event (e.g., INSERT, UPDATE, DELETE). */
        public String getEvent() {
            return event;
        }

        /** @return The database schema name. */
        public String getSchema() {
            return schema;
        }

        /** @return The database table name. */
        public String getTable() {
            return table;
        }

        @Override
        public String toString() {
            return "PostgresChange{" +
                    "id=" + id +
                    ", event='" + event + '\'' +
                    ", schema='" + schema + '\'' +
                    ", table='" + table + '\'' +
                    '}';
        }
    }

    /**
     * Message containing the full initial presence state of a channel.
     */
    public static class PresenceStateMessage extends PhoenixMessage {

        private final Map<String, PresenceInfo> presences;

        /**
         * Constructs a PresenceStateMessage from JSON.
         *
         * @param json The raw message JSON.
         * @throws JSONException if payload is missing.
         */
        public PresenceStateMessage(JSONObject json) throws JSONException {
            super(json);

            this.presences = PresenceParser.parse(json.getJSONObject("payload"));
        }

        /** @return A map of presence keys to presence information. */
        public Map<String, PresenceInfo> getPresences() {
            return presences;
        }

        @Override
        public String toString() {
            return "PresenceStateMessage{" +
                    "ref='" + ref + '\'' +
                    ", event='" + event + '\'' +
                    ", topic='" + topic + '\'' +
                    ", presences=" + presences +
                    '}';
        }
    }

    /**
     * Message containing incremental updates to the presence state.
     */
    public static class PresenceDiffMessage extends PhoenixMessage {

        private final Map<String, PresenceInfo> joins;
        private final Map<String, PresenceInfo> leaves;

        /**
         * Constructs a PresenceDiffMessage from JSON.
         *
         * @param json The raw message JSON.
         * @throws JSONException if payload is missing.
         */
        public PresenceDiffMessage(JSONObject json) throws JSONException {
            super(json);

            JSONObject payload = json.getJSONObject("payload");

            joins = PresenceParser.parse(payload.optJSONObject("joins"));
            leaves = PresenceParser.parse(payload.optJSONObject("leaves"));
        }

        /** @return Map of new presences joined since the last update. */
        public Map<String, PresenceInfo> getJoins() {
            return joins;
        }

        /** @return Map of presences that have left since the last update. */
        public Map<String, PresenceInfo> getLeaves() {
            return leaves;
        }

        @Override
        public String toString() {
            return "PresenceDiffMessage{" +
                    "ref='" + ref + '\'' +
                    ", event='" + event + '\'' +
                    ", topic='" + topic + '\'' +
                    ", joins=" + joins +
                    ", leaves=" + leaves +
                    '}';
        }
    }

    /**
     * Utility class for parsing presence JSON structures.
     */
    public final static class PresenceParser {

        private PresenceParser() {
        }

        /**
         * Parses a JSON object into a map of presence information.
         *
         * @param json The presence state or diff JSON.
         * @return A map of keys to {@link PresenceInfo}.
         */
        public static Map<String, PresenceInfo> parse(JSONObject json) {
            Map<String, PresenceInfo> map = new HashMap<>();

            if (json == null) {
                return map;
            }

            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();

                JSONObject obj = json.optJSONObject(key);
                if (obj != null) {
                    map.put(key, new PresenceInfo(obj));
                }
            }

            return map;
        }
    }

    /**
     * Information about a specific presence key.
     */
    public static class PresenceInfo {

        private final List<Meta> metas = new ArrayList<>();

        /**
         * Constructs PresenceInfo from JSON.
         *
         * @param json The presence entry JSON.
         */
        public PresenceInfo(JSONObject json) {
            JSONArray array = json.optJSONArray("metas");

            if (array != null) {
                for (int i = 0; i < array.length(); i++) {
                    metas.add(new Meta(array.optJSONObject(i)));
                }
            }
        }

        /** @return The metadata list for this presence. */
        public List<Meta> getMetas() {
            return metas;
        }

        @Override
        public String toString() {
            return "PresenceInfo{" +
                    "metas=" + metas +
                    '}';
        }
    }

    /**
     * Metadata associated with a presence entry.
     */
    public static class Meta {

        private final JSONObject json;

        /**
         * Constructs Meta from JSON.
         *
         * @param json The metadata JSON.
         */
        public Meta(JSONObject json) {
            this.json = json;
        }

        /**
         * Gets a value from the metadata.
         *
         * @param key The key to look up.
         * @return The value, or null if not found.
         */
        public Object get(String key) {
            return json.opt(key);
        }

        /**
         * Gets a value from the metadata with a default fallback.
         *
         * @param key The key to look up.
         * @param defaultValue The value to return if key is missing.
         * @return The value or default.
         */
        public Object get(String key, Object defaultValue) {
            Object value = json.opt(key);
            return value != null ? value : defaultValue;
        }

        @Override
        public String toString() {
            return "Meta{" +
                    "json=" + json +
                    '}';
        }
    }

    /**
     * Message received when a Postgres change event occurs.
     */
    public static class PostgresChangesMessage extends PhoenixMessage {

        private final PostgresData data;
        private final List<Long> ids = new ArrayList<>();

        /**
         * Constructs PostgresChangesMessage from JSON.
         *
         * @param json The raw message JSON.
         * @throws JSONException if payload is missing.
         */
        public PostgresChangesMessage(JSONObject json) throws JSONException {
            super(json);

            JSONObject payload = json.getJSONObject("payload");

            this.data = new PostgresData(payload.optJSONObject("data"));

            JSONArray idsArray = payload.optJSONArray("ids");
            if (idsArray != null) {
                for (int i = 0; i < idsArray.length(); i++) {
                    ids.add(idsArray.optLong(i));
                }
            }
        }

        /** @return The actual database change data. */
        public PostgresData getData() {
            return data;
        }

        /** @return The IDs of the subscriptions this message satisfies. */
        public List<Long> getIds() {
            return ids;
        }

        @Override
        public String toString() {
            return "PostgresChangesMessage{" +
                    "ref='" + ref + '\'' +
                    ", event='" + event + '\'' +
                    ", topic='" + topic + '\'' +
                    ", data=" + data +
                    ", ids=" + ids +
                    '}';
        }
    }

    /**
     * Contains the details of a database change.
     */
    public static class PostgresData {

        private final String schema;
        private final String table;
        private final String type;
        private final JSONObject record;
        private final JSONObject oldRecord;
        private final List<Column> columns = new ArrayList<>();
        private final JSONArray errors;
        private final String commitTimestamp;

        /**
         * Constructs PostgresData from JSON.
         *
         * @param json The change data JSON object.
         */
        public PostgresData(JSONObject json) {

            if (json == null) {
                schema = null;
                table = null;
                type = null;
                record = null;
                oldRecord = null;
                errors = null;
                commitTimestamp = null;
                return;
            }

            schema = json.optString("schema", null);
            table = json.optString("table", null);
            type = json.optString("type", null);

            record = json.optJSONObject("record");
            oldRecord = json.optJSONObject("old_record");

            errors = json.optJSONArray("errors");

            commitTimestamp = json.optString("commit_timestamp", null);

            JSONArray cols = json.optJSONArray("columns");
            if (cols != null) {
                for (int i = 0; i < cols.length(); i++) {
                    JSONObject obj = cols.optJSONObject(i);
                    if (obj != null) {
                        columns.add(new Column(obj));
                    }
                }
            }
        }

        /** @return The database schema. */
        public String getSchema() {
            return schema;
        }

        /** @return The database table. */
        public String getTable() {
            return table;
        }

        /** @return The change type (INSERT, UPDATE, DELETE). */
        public String getType() {
            return type;
        }

        /** @return The new data record (for INSERT and UPDATE). */
        public JSONObject getRecord() {
            return record;
        }

        /** @return The old data record (for UPDATE and DELETE). */
        public JSONObject getOldRecord() {
            return oldRecord;
        }

        /** @return Information about the columns in the table. */
        public List<Column> getColumns() {
            return columns;
        }

        /** @return Any errors encountered during CDC processing. */
        public JSONArray getErrors() {
            return errors;
        }

        /** @return The timestamp when the change was committed. */
        public String getCommitTimestamp() {
            return commitTimestamp;
        }

        @Override
        public String toString() {
            return "PostgresData{" +
                    "schema='" + schema + '\'' +
                    ", table='" + table + '\'' +
                    ", type='" + type + '\'' +
                    ", record=" + record +
                    ", oldRecord=" + oldRecord +
                    ", columns=" + columns +
                    ", errors=" + errors +
                    ", commitTimestamp='" + commitTimestamp + '\'' +
                    '}';
        }
    }

    /**
     * Information about a database column.
     */
    public static class Column {

        private final String name;
        private final String type;

        /**
         * Constructs Column from JSON.
         *
         * @param json The column metadata JSON.
         */
        public Column(JSONObject json) {
            this.name = json.optString("name", null);
            this.type = json.optString("type", null);
        }

        /** @return The column name. */
        public String getName() {
            return name;
        }

        /** @return The database data type of the column. */
        public String getType() {
            return type;
        }

        @Override
        public String toString() {
            return "Column{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    /**
     * A message with a custom or unknown structure.
     */
    public static class GenericPhoenixMessage extends PhoenixMessage {

        private final JSONObject payload;

        /**
         * Constructs GenericPhoenixMessage from JSON.
         *
         * @param json The raw message JSON.
         * @throws JSONException if payload parsing fails.
         */
        public GenericPhoenixMessage(JSONObject json) throws JSONException {
            super(json);
            this.payload = json.optJSONObject("payload");
        }

        /** @return The generic payload as a JSONObject. */
        public JSONObject getPayload() {
            return payload;
        }

        @Override
        public String toString() {
            return "GenericPhoenixMessage{" +
                    "ref='" + ref + '\'' +
                    ", event='" + event + '\'' +
                    ", topic='" + topic + '\'' +
                    ", payload=" + payload +
                    '}';
        }
    }

    /**
     * Factory method to parse a raw JSON message into the appropriate {@link PhoenixMessage} subclass.
     *
     * @param json The raw JSON message from the websocket.
     * @return A specialized subclass of {@link PhoenixMessage}.
     * @throws JSONException if the JSON is malformed.
     *
     * <pre>{@code
     * JSONObject json = ...;
     * PhoenixMessage message = Event.from(json);
     * if (message instanceof ReplyMessage) {
     *     // handle reply
     * }
     * }</pre>
     */
    public static PhoenixMessage from(JSONObject json) throws JSONException {
        String event = json.optString("event");

        if (PhoenixEvent.REPLY.getEventValue().equals(event)) {
            return new ReplyMessage(json);
        } else if (PhoenixEvent.PRESENCE_DIFF.getEventValue().equals(event)) {
            return new PresenceDiffMessage(json);
        } else if (PhoenixEvent.PRESENCE_STATE.getEventValue().equals(event)) {
            return new PresenceStateMessage(json);
        } else if (PhoenixEvent.SYSTEM.getEventValue().equals(event)) {
            return new SystemMessage(json);
        } else if (PhoenixEvent.POSTGRES_CHANGES.getEventValue().equals(event)) {
            return new PostgresChangesMessage(json);
        } else {
            return new GenericPhoenixMessage(json);
        }
    }

}
