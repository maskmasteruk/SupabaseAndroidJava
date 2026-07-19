package io.github.maskmasteruk.supabase.storage.Enum;

/**
 * Enumeration of available bucket types in Supabase Storage.
 */
public enum BucketType {
    /** Standard storage bucket. */
    STANDARD("STANDARD"),
    /** Analytics storage bucket. */
    ANALYTICS("ANALYTICS"),
    /** Vector storage bucket. */
    VECTOR("VECTOR");

    private final String value;

    /**
     * Constructor for BucketType enum.
     *
     * @param value The string value of the bucket type.
     */
    BucketType(String value) {
        this.value = value;
    }

    /** @return The string representation used by the API. */
    public String getValue() {
        return value;
    }

    /**
     * Converts a string value to its corresponding {@code BucketType}.
     *
     * @param value The string value to convert.
     * @return The matching {@code BucketType}, or {@code null} if input is null.
     * @throws IllegalArgumentException If the value does not match any known {@code BucketType}.
     */
    public static BucketType fromValue(String value) {
        if (value == null) {
            return null;
        }

        for (BucketType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Unknown bucket type: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
