package com.vhn.doan.presentation.base;

/**
 * Interface để lắng nghe sự kiện fragment được show/hide
 * Giúp fragment biết khi nào nó thực sự hiển thị cho người dùng
 */
public interface FragmentVisibilityListener {

    /**
     * Được gọi khi fragment được hiển thị (visible to user)
     */
    void onFragmentVisible();

    /**
     * Được gọi khi fragment bị ẩn (hidden from user)
     */
    void onFragmentHidden();
}

