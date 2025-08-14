package com.vhn.doan.services;

/**
 * Utility helper to build Cloudinary delivery URLs from a public id and
 * version returned by Firebase.
 *
 * Update the CLOUD constant with your Cloudinary cloud name before
 * building the application.
 */
public final class CloudinaryUrlBuilder {
    /**
     * Replace this value with your Cloudinary cloud name.
     */
    private static final String CLOUD = "<cloud_name>";

    private CloudinaryUrlBuilder() {
        // Utility class
    }

    /**
     * Build an MP4 delivery URL from Cloudinary identifiers.
     *
     * @param publicId Cloudinary public id of the video
     * @param version  Cloudinary version of the video asset
     * @return fully qualified MP4 URL
     */
    public static String mp4UrlFrom(String publicId, long version) {
        if (publicId == null || publicId.isEmpty()) {
            return "";
        }
        return "https://res.cloudinary.com/" + CLOUD + "/video/upload/v" + version + "/" + publicId + ".mp4";
    }

    /**
     * Build a poster image URL from the video identifiers.
     */
    public static String posterUrlFrom(String publicId, long version) {
        if (publicId == null || publicId.isEmpty()) {
            return "";
        }
        return "https://res.cloudinary.com/" + CLOUD + "/video/upload/so_3/v" + version + "/" + publicId + ".jpg";
    }

    /**
     * Build an HLS delivery URL (optional, not used yet).
     */
    public static String hlsUrlFrom(String publicId, long version) {
        if (publicId == null || publicId.isEmpty()) {
            return "";
        }
        return "https://res.cloudinary.com/" + CLOUD + "/video/upload/v" + version + "/" + publicId + ".m3u8";
    }
}

