package com.vhn.doan.presentation.profile;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter cho ViewPager2 trong profile fragment để chuyển đổi giữa các tab
 */
public class ProfileViewPagerAdapter extends FragmentStateAdapter {

    private static final int TAB_COUNT = 2;
    private static final int TAB_FAVORITE_POSTS = 0;
    private static final int TAB_LIKED_VIDEOS = 1;

    public ProfileViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case TAB_FAVORITE_POSTS:
                return FavoritePostsFragment.newInstance();
            case TAB_LIKED_VIDEOS:
                return LikedVideosFragment.newInstance();
            default:
                return FavoritePostsFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return TAB_COUNT;
    }
}
