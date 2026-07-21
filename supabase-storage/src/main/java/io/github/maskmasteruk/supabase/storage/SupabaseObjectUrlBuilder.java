package io.github.maskmasteruk.supabase.storage;

import java.util.HashMap;

/**
 * Builder for optional query parameters used when generating a public URL for a
 * Supabase Storage object.
 * <p>
 * This builder supports:
 * <ul>
 *     <li>Image transformation options (resize, width, height, quality, format)</li>
 *     <li>Cache-busting via a cache nonce</li>
 *     <li>Download behavior with an optional filename</li>
 * </ul>
 *
 * <p>Example:</p>
 * <pre>{@code
 * SupabaseObjectUrlBuilder builder = new SupabaseObjectUrlBuilder();
 *
 * SupabaseObjectUrlBuilder.Transformation transformation =
 *         new SupabaseObjectUrlBuilder.Transformation();
 * transformation.setWidth(400);
 * transformation.setHeight(300);
 * transformation.setResize(SupabaseObjectUrlBuilder.Resize.COVER);
 *
 * builder.setTransformation(transformation);
 * builder.setDownload(new SupabaseObjectUrlBuilder.Download("image.png"));
 *
 * HashMap<String, String> queryParams = builder.buildQueryParams();
 * }</pre>
 */
public class SupabaseObjectUrlBuilder {

    /**
     * Image transformation options.
     */
    private Transformation transformation;

    /**
     * Optional cache-busting nonce.
     */
    private String cacheNonce;

    /**
     * Download configuration.
     */
    private Download download;

    /**
     * Returns the image transformation configuration.
     *
     * @return the transformation configuration, or {@code null} if none is set
     */
    public Transformation getTransformation() {
        return transformation;
    }

    /**
     * Sets the image transformation configuration.
     *
     * @param transformation transformation options
     */
    public void setTransformation(Transformation transformation) {
        this.transformation = transformation;
    }

    /**
     * Returns the cache nonce.
     *
     * @return the cache nonce, or {@code null} if not set
     */
    public String getCacheNonce() {
        return cacheNonce;
    }

    /**
     * Sets a cache nonce to invalidate cached responses.
     *
     * @param cacheNonce cache nonce
     */
    public void setCacheNonce(String cacheNonce) {
        this.cacheNonce = cacheNonce;
    }

    /**
     * Returns the download configuration.
     *
     * @return download configuration, or {@code null} if not set
     */
    public Download getDownload() {
        return download;
    }

    /**
     * Sets the download configuration.
     *
     * @param download download configuration
     */
    public void setDownload(Download download) {
        this.download = download;
    }

    /**
     * Represents image transformation options supported by Supabase Storage.
     */
    public static class Transformation {

        /**
         * Desired image width in pixels.
         */
        private Integer width;

        /**
         * Desired image height in pixels.
         */
        private Integer height;

        /**
         * Resize mode.
         */
        private Resize resize;

        /**
         * Output image quality.
         */
        private Integer quality;

        /**
         * Output image format.
         */
        private Format format;

        /**
         * Returns the output width.
         *
         * @return width in pixels
         */
        public Integer getWidth() {
            return width;
        }

        /**
         * Sets the output width.
         *
         * @param width width in pixels
         */
        public void setWidth(Integer width) {
            this.width = width;
        }

        /**
         * Returns the output height.
         *
         * @return height in pixels
         */
        public Integer getHeight() {
            return height;
        }

        /**
         * Sets the output height.
         *
         * @param height height in pixels
         */
        public void setHeight(Integer height) {
            this.height = height;
        }

        /**
         * Returns the resize mode.
         *
         * @return resize mode
         */
        public Resize getResize() {
            return resize;
        }

        /**
         * Sets the resize mode.
         *
         * @param resize resize mode
         */
        public void setResize(Resize resize) {
            this.resize = resize;
        }

        /**
         * Returns the image quality.
         *
         * @return quality value
         */
        public Integer getQuality() {
            return quality;
        }

        /**
         * Sets the image quality.
         *
         * @param quality quality value
         */
        public void setQuality(Integer quality) {
            this.quality = quality;
        }

        /**
         * Returns the output format.
         *
         * @return output format
         */
        public Format getFormat() {
            return format;
        }

        /**
         * Sets the output format.
         *
         * @param format output format
         */
        public void setFormat(Format format) {
            this.format = format;
        }
    }

    /**
     * Download configuration for a public URL.
     * <p>
     * When present, the generated URL includes the {@code download} query parameter,
     * optionally specifying the filename suggested to the browser.
     */
    public static class Download {

        /**
         * Suggested download filename.
         */
        private String fileName;

        /**
         * Creates an empty download configuration.
         */
        public Download() {
        }

        /**
         * Creates a download configuration with a suggested filename.
         *
         * @param fileName suggested filename
         */
        public Download(String fileName) {
            this.fileName = fileName;
        }

        /**
         * Returns the suggested filename.
         *
         * @return filename, or {@code null}
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * Sets the suggested filename.
         *
         * @param fileName filename
         */
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }
    }

    /**
     * Supported image resize modes.
     */
    public enum Resize {

        /**
         * Crop the image to completely cover the requested dimensions.
         */
        COVER("cover"),

        /**
         * Scale the image to fit within the requested dimensions while preserving
         * aspect ratio.
         */
        CONTAIN("contain"),

        /**
         * Stretch the image to exactly match the requested dimensions.
         */
        FILL("fill");

        private final String value;

        Resize(String value) {
            this.value = value;
        }

        /**
         * Returns the API value for this resize mode.
         *
         * @return resize value
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * Supported output image formats.
     */
    public enum Format {

        /**
         * Preserve the original image format.
         */
        ORIGIN("origin");

        private final String value;

        Format(String value) {
            this.value = value;
        }

        /**
         * Returns the API value for this format.
         *
         * @return format value
         */
        public String getValue() {
            return value;
        }
    }

    /**
     * Builds the query parameters corresponding to the configured options.
     * <p>
     * Only non-null values are included in the returned map.
     *
     * @return a map of query parameter names and values
     */
    public HashMap<String, String> buildQueryParams() {
        HashMap<String, String> params = new HashMap<>();

        if (transformation != null) {
            if (transformation.getWidth() != null) {
                params.put("width", String.valueOf(transformation.getWidth()));
            }
            if (transformation.getHeight() != null) {
                params.put("height", String.valueOf(transformation.getHeight()));
            }
            if (transformation.getResize() != null) {
                params.put("resize", transformation.getResize().getValue());
            }
            if (transformation.getQuality() != null) {
                params.put("quality", String.valueOf(transformation.getQuality()));
            }
            if (transformation.getFormat() != null) {
                params.put("format", transformation.getFormat().getValue());
            }
        }

        if (getCacheNonce() != null) {
            params.put("cacheNonce", getCacheNonce());
        }

        if (getDownload() != null) {
            String fileName = getDownload().getFileName();
            params.put("download", fileName != null ? fileName : "");
        }

        return params;
    }
}