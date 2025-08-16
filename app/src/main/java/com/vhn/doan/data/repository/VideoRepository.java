package com.vhn.doan.data.repository;

import com.vhn.doan.data.ShortVideo;

import java.util.List;

/**
 * Interface VideoRepository định nghĩa các phương thức để truy cập và quản lý dữ liệu video ngắn
 * Tuân theo nguyên tắc thiết kế repository trong kiến trúc MVP
 */
public interface VideoRepository {

    /**
     * Interface callback để nhận kết quả từ các thao tác async
     */
    interface VideoCallback {
        /**
         * Được gọi khi thao tác thành công
         * @param videos danh sách các video ngắn
         */
        void onSuccess(List<ShortVideo> videos);

        /**
         * Được gọi khi thao tác thất bại
         * @param errorMessage thông báo lỗi
         */
        void onError(String errorMessage);
    }

    /**
     * Lấy feed video cho người dùng dựa trên preferences và trending
     * @param userId ID của người dùng để lấy preferences
     * @param country Quốc gia để lấy trending videos
     * @param callback Callback để nhận kết quả
     */
    void getFeed(String userId, String country, VideoCallback callback);

    /**
     * Lấy feed video đồng bộ (blocking)
     * @param userId ID của người dùng để lấy preferences
     * @param country Quốc gia để lấy trending videos
     * @return Danh sách video đã được sắp xếp
     * @deprecated Khuyên dùng phương thức async với callback
     */
    @Deprecated
    List<ShortVideo> getFeed(String userId, String country);
}
