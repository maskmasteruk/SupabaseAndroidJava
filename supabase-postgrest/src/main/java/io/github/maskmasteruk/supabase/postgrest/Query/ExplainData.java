package io.github.maskmasteruk.supabase.postgrest.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Configuration for the PostgREST 'Explain' header.
 *
 * <p>This class allows obtaining information about the execution plan of a statement.
 * It maps to the 'Accept: application/vnd.pgrst.plan' header.</p>
 */
public class ExplainData {
    /** The format of the execution plan. */
    public Format format;
    /** The options for the execution plan analysis. */
    public ArrayList<ExplainOption> options = new ArrayList<>();

    /**
     * Default constructor for ExplainData.
     */
    public ExplainData() {
    }

    /**
     * Sets the format of the execution plan.
     *
     * @param format The desired format (TEXT or JSON).
     * @return This {@link ExplainData} instance for chaining.
     */
    public ExplainData setFormat(Format format) {
        this.format = format;
        return this;
    }

    /**
     * Gets the list of explain options.
     *
     * @return The list of options.
     */
    public List<ExplainOption> getOptions() {
        return options;
    }

    /**
     * Sets the explain options.
     *
     * @param options The options to set.
     * @return This {@link ExplainData} instance for chaining.
     */
    public ExplainData setOptions(ExplainOption... options) {
        this.options = new ArrayList<>(Arrays.asList(options));
        return this;
    }

    /**
     * Adds an explain option.
     *
     * @param options The option to add.
     * @return This {@link ExplainData} instance for chaining.
     */
    public ExplainData addOptions(ExplainOption options) {
        this.options.add(options);
        return this;
    }

    /**
     * Builds the 'Accept' header value for the explain request.
     *
     * @param mediaType The underlying media type (e.g., application/json).
     * @return The constructed header value.
     */
    public String get(String mediaType) {
        return "application/vnd.pgrst.plan" + (format != null ? "+" + format.getValue() : "") + "; for=\"" + mediaType + "\"; options=" + options.stream().map(ExplainOption::getValue).collect(Collectors.joining("|")) + ";";
    }

    /**
     * Enum representing options for the execution plan analysis.
     */
    public static enum ExplainOption {
        /** Shows the actual execution times and other statistics. */
        ANALYZE("analyze"),
        /** Shows additional information about the plan. */
        VERBOSE("verbose"),
        /** Includes information about modified configuration parameters. */
        SETTINGS("settings"),
        /** Includes information on buffer usage. */
        BUFFERS("buffers"),
        /** Includes information on Write Ahead Log usage. */
        WAL("wal");

        private final String value;

        ExplainOption(String value) {
            this.value = value;
        }

        /**
         * Gets the string value of the option.
         * @return The option value.
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * Enum representing the format of the execution plan.
     */
    public static enum Format {
        /** Plain text format. */
        TEXT("text"),
        /** JSON format. */
        JSON("json");

        private final String value;

        Format(String value) {
            this.value = value;
        }

        /**
         * Gets the string value of the format.
         * @return The format value.
         */
        public String getValue() {
            return value;
        }
    }

}
