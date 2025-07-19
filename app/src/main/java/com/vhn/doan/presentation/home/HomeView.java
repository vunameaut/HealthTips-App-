package com.vhn.doan.presentation.home;

import com.vhn.doan.data.Category;
import com.vhn.doan.data.HealthTip;

import java.util.List;

/**
 * Interface HomeView định nghĩa các phương thức UI mà màn hình Home cần triển khai
 * Tuân thủ kiến trúc MVP
 */
public interface HomeView {

    /**
     * Hiển thị danh sách các danh mục
     * @param categories Danh sách danh mục
     */
    void showCategories(List<Category> categories);

    /**
     * Hiển thị danh sách mẹo sức khỏe mới nhất
     * @param healthTips Danh sách mẹo sức khỏe mới nhất
     */
    void showLatestHealthTips(List<HealthTip> healthTips);

    /**
     * Hiển thị danh sách mẹo sức khỏe được xem nhiều nhất
     * @param healthTips Danh sách mẹo sức khỏe xem nhiều
     */
    void showMostViewedHealthTips(List<HealthTip> healthTips);

    /**
     * Hiển thị danh sách mẹo sức khỏe được thích nhiều nhất
     * @param healthTips Danh sách mẹo sức khỏe được thích nhiều
     */
    void showMostLikedHealthTips(List<HealthTip> healthTips);

    /**
     * Hiển thị chế độ offline khi không có kết nối internet
     */
    void showOfflineMode();

    /**
     * Chuyển đến trang chi tiết danh mục
     * @param category Danh mục đã chọn
     */
    void navigateToCategoryDetail(Category category);

    /**
     * Chuyển đến trang chi tiết mẹo sức khỏe
     * @param healthTip Mẹo sức khỏe đã chọn
     */
    void navigateToHealthTipDetail(HealthTip healthTip);

    /**
     * Chuyển đến trang tìm kiếm
     */
    void navigateToSearch();

    /**
     * Hiển thị loading khi đang tải dữ liệu
     * @param loading true để hiển thị loading, false để ẩn
     */
    void showLoading(boolean loading);

    /**
     * Hiển thị thông báo cho người dùng
     * @param message Nội dung thông báo
     */
    void showMessage(String message);

    /**
     * Hiển thị thông báo lỗi
     * @param errorMessage Nội dung lỗi
     */
    void showError(String errorMessage);
}
