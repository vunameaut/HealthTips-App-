package com.vhn.doan.utils;

/**
 * Utility class để tạo URL cho Cloudinary resources
 */
public class CloudinaryUrls {
    private static final String CLOUD = "dazo6ypwt";
    private static final String BASE_URL = "https://res.cloudinary.com/" + CLOUD;

    /**
     * Tạo URL cho video MP4
     * @param id Public ID của video
     * @param v Version của video
     * @return URL đầy đủ cho file MP4
     */
    public static String mp4(String id, long v) {
        return BASE_URL + "/video/upload/v" + v + "/" + id + ".mp4";
    }

    /**
     * Tạo URL cho poster/thumbnail của video
     * @param id Public ID của video
     * @param v Version của video
     * @return URL đầy đủ cho poster image
     */
    public static String poster(String id, long v) {
        return BASE_URL + "/video/upload/v" + v + "/" + id + ".jpg";
    }

    /**
     * Tạo URL cho thumbnail của video (alias cho poster)
     * @param id Public ID của video
     * @param v Version của video
     * @return URL đầy đủ cho thumbnail image
     */
    public static String videoThumbnail(String id, long v) {
        return poster(id, v);
    }

    /**
     * Tạo URL cho HLS streaming
     * @param id Public ID của video
     * @param v Version của video
     * @return URL đầy đủ cho HLS playlist
     */
    public static String hls(String id, long v) {
        return BASE_URL + "/video/upload/v" + v + "/" + id + ".m3u8";
    }
}
