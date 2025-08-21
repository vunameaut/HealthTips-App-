package com.vhn.doan.presentation.search;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter cho ViewPager2 hiển thị các tab kết quả tìm kiếm
 */
public class SearchResultsPagerAdapter extends FragmentStateAdapter {
    private final HealthTipSearchResultsFragment mHealthTipFragment;
    private final VideoSearchResultsFragment mVideoFragment;

    public SearchResultsPagerAdapter(@NonNull FragmentActivity fragmentActivity,
                                    HealthTipSearchResultsFragment healthTipFragment,
                                    VideoSearchResultsFragment videoFragment) {
        super(fragmentActivity);
        mHealthTipFragment = healthTipFragment;
        mVideoFragment = videoFragment;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Trả về fragment tương ứng với vị trí tab
        switch (position) {
            case 0:
                return mHealthTipFragment;
            case 1:
                return mVideoFragment;
            default:
                return mHealthTipFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2; // 2 tab: Bài viết và Video
    }
}
