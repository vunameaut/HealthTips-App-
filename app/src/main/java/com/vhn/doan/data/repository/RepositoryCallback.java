package com.vhn.doan.data.repository;

/**
 * Generic callback interface cho các thao tác repository
 * @param <T> Kiểu dữ liệu trả về
 */
public interface RepositoryCallback<T> {

    /**
     * Được gọi khi thao tác thành công
     * @param result Kết quả trả về
     */
    void onSuccess(T result);

    /**
     * Được gọi khi thao tác thất bại
     * @param error Thông tin lỗi
     */
    void onError(String error);
}
