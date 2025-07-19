package com.vhn.doan.presentation.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.vhn.doan.R;
import com.vhn.doan.presentation.auth.LoginActivity;
import com.vhn.doan.services.AuthManager;

/**
 * HomeActivity là màn hình chính của ứng dụng sau khi đăng nhập
 * Chứa HomeFragment và các Fragment khác thông qua BottomNavigationView
 */
public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Khởi tạo AuthManager
        authManager = new AuthManager(this);

        // Kiểm tra đăng nhập
        if (!authManager.isUserLoggedIn()) {
            // Chuyển về màn hình đăng nhập nếu chưa đăng nhập
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return;
        }

        // Khởi tạo và thiết lập BottomNavigationView
        setupBottomNavigation();

        // Mặc định hiển thị HomeFragment khi khởi động
        if (savedInstanceState == null) {
            loadFragment(HomeFragment.newInstance());
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    fragment = HomeFragment.newInstance();
                } else if (itemId == R.id.nav_categories) {
                    // Tạm thời vẫn dùng HomeFragment cho các tab khác
                    // Sẽ thay thế bằng Fragment tương ứng khi đã phát triển
                    fragment = HomeFragment.newInstance();
                    // fragment = CategoriesFragment.newInstance();
                } else if (itemId == R.id.nav_favorites) {
                    fragment = HomeFragment.newInstance();
                    // fragment = FavoritesFragment.newInstance();
                } else if (itemId == R.id.nav_profile) {
                    fragment = HomeFragment.newInstance();
                    // fragment = ProfileFragment.newInstance();
                }

                return loadFragment(fragment);
            }
        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
