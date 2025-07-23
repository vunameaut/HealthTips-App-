package com.vhn.doan.data.repository;

import com.vhn.doan.data.Favorite;
import com.vhn.doan.data.HealthTip;

import java.util.List;

/**
 * Interface Repository cho quản lý dữ liệu yêu thích
 * Tuân theo mô hình Repository Pattern trong kiến trúc MVP
 */
public interface FavoriteRepository {

    /**
     * Callback interface cho các thao tác với danh sách yêu thích
     */
    interface FavoriteListCallback {
        void onSuccess(List<HealthTip> favoriteHealthTips);
        void onError(String error);
    }

    /**
     * Callback interface cho thao tác thêm/xóa yêu thích
     */
    interface FavoriteActionCallback {
        void onSuccess();
        void onError(String error);
    }

    /**
     * Callback interface kiểm tra trạng thái yêu thích
     */
    interface FavoriteCheckCallback {
        void onResult(boolean isFavorite);
        void onError(String error);
    }

    /**
     * Thêm mẹo sức khỏe vào danh sách yêu thích
     * @param userId ID người dùng
     * @param healthTipId ID mẹo sức khỏe
     * @param callback Callback xử lý kết quả
     */
    void addToFavorites(String userId, String healthTipId, FavoriteActionCallback callback);

    /**
     * Xóa mẹo sức khỏe khỏi danh sách yêu thích
     * @param userId ID người dùng
     * @param healthTipId ID mẹo sức khỏe
     * @param callback Callback xử lý kết quả
     */
    void removeFromFavorites(String userId, String healthTipId, FavoriteActionCallback callback);

    /**
     * Lấy danh sách mẹo sức khỏe yêu thích của người dùng
     * @param userId ID người dùng
     * @param callback Callback trả về danh sách mẹo sức khỏe yêu thích
     */
    void getFavoriteHealthTips(String userId, FavoriteListCallback callback);

    /**
     * Kiểm tra xem mẹo sức khỏe có được yêu thích hay không
     * @param userId ID người dùng
     * @param healthTipId ID mẹo sức khỏe
     * @param callback Callback trả về kết quả kiểm tra
     */
    void isFavorite(String userId, String healthTipId, FavoriteCheckCallback callback);

    /**
     * Lấy danh sách ID các mẹo sức khỏe yêu thích của người dùng
     * @param userId ID người dùng
     * @param callback Callback trả về danh sách ID
     */
    void getFavoriteHealthTipIds(String userId, FavoriteListCallback callback);

    /**
     * Xóa tất cả yêu thích của người dùng
     * @param userId ID người dùng
     * @param callback Callback xử lý kết quả
     */
    void clearAllFavorites(String userId, FavoriteActionCallback callback);
}
