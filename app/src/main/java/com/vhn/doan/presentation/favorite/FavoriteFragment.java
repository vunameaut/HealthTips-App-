package com.vhn.doan.presentation.favorite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.vhn.doan.R;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.repository.FavoriteRepositoryImpl;
import com.vhn.doan.data.repository.HealthTipRepositoryImpl;
import com.vhn.doan.presentation.favorite.adapter.FavoriteHealthTipAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị danh sách mẹo sức khỏe yêu thích
 * Tuân thủ kiến trúc MVP
 */
public class FavoriteFragment extends Fragment implements FavoriteView {

    // UI Components
    private RecyclerView recyclerViewFavorites;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyFavorites;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView textViewEmptyMessage;

    // MVP Components
    private FavoritePresenter presenter;
    private FavoriteHealthTipAdapter adapter;

    public FavoriteFragment() {
        // Constructor mặc định
    }

    /**
     * Phương thức factory để tạo instance mới của fragment
     */
    public static FavoriteFragment newInstance() {
        return new FavoriteFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Khởi tạo presenter với repositories
        presenter = new FavoritePresenter(
                requireContext(),
                new FavoriteRepositoryImpl(),
                new HealthTipRepositoryImpl()
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ các thành phần UI
        initViews(view);

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Thiết lập SwipeRefreshLayout
        setupSwipeRefresh();

        // Thiết lập sự kiện click cho empty state
        setupEmptyStateClick();

        // Gắn presenter với view
        presenter.attachView(this);
    }

    /**
     * Ánh xạ các thành phần UI từ layout
     */
    private void initViews(View view) {
        recyclerViewFavorites = view.findViewById(R.id.recyclerViewFavorites);
        progressBar = view.findViewById(R.id.progressBar);
        layoutEmptyFavorites = view.findViewById(R.id.layoutEmptyFavorites);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        textViewEmptyMessage = view.findViewById(R.id.textViewEmptyMessage);
    }

    /**
     * Thiết lập RecyclerView với adapter
     */
    private void setupRecyclerView() {
        recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new FavoriteHealthTipAdapter(
                requireContext(),
                new ArrayList<>(),
                new FavoriteHealthTipAdapter.FavoriteHealthTipClickListener() {
                    @Override
                    public void onHealthTipClick(HealthTip healthTip) {
                        presenter.onHealthTipSelected(healthTip);
                    }

                    @Override
                    public void onRemoveFromFavorites(HealthTip healthTip) {
                        presenter.removeFromFavorites(healthTip);
                    }
                }
        );

        recyclerViewFavorites.setAdapter(adapter);
    }

    /**
     * Thiết lập SwipeRefreshLayout
     */
    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(this::refreshFavorites);

        // Thiết lập màu sắc cho progress indicator
        swipeRefreshLayout.setColorSchemeResources(
                R.color.primary_button_start,
                R.color.secondary_button_start,
                R.color.accent
        );
    }

    /**
     * Refresh danh sách yêu thích
     */
    private void refreshFavorites() {
        if (presenter != null) {
            presenter.refreshFavorites();
        }
    }

    /**
     * Thiết lập sự kiện click cho nút "Khám phá mẹo sức khỏe"
     */
    private void setupEmptyStateClick() {
        View rootView = getView();
        if (rootView != null) {
            View cardViewGoToHome = rootView.findViewById(R.id.cardViewGoToHome);
            if (cardViewGoToHome != null) {
                cardViewGoToHome.setOnClickListener(v -> navigateToHome());
            }
        }
    }

    /**
     * Navigation về trang chủ
     */
    private void navigateToHome() {
        if (getActivity() instanceof com.vhn.doan.presentation.home.HomeActivity) {
            com.vhn.doan.presentation.home.HomeActivity homeActivity =
                (com.vhn.doan.presentation.home.HomeActivity) getActivity();
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                homeActivity.findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Tải lại dữ liệu khi fragment được hiển thị
        if (presenter != null) {
            presenter.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
    }

    // Triển khai các phương thức của FavoriteView

    @Override
    public void showFavoriteHealthTips(List<HealthTip> favoriteHealthTips) {
        recyclerViewFavorites.setVisibility(View.VISIBLE);
        layoutEmptyFavorites.setVisibility(View.GONE);

        adapter.updateFavoriteHealthTips(favoriteHealthTips);
    }

    @Override
    public void showEmptyFavorites() {
        recyclerViewFavorites.setVisibility(View.GONE);
        layoutEmptyFavorites.setVisibility(View.VISIBLE);

        textViewEmptyMessage.setText("Bạn chưa có mẹo sức khỏe yêu thích nào.\nHãy thêm yêu thích từ trang chủ!");
    }

    @Override
    public void navigateToHealthTipDetail(HealthTip healthTip) {
        if (healthTip != null && healthTip.getId() != null) {
            // Tạo Intent để chuyển đến HealthTipDetailActivity
            android.content.Intent intent = com.vhn.doan.presentation.healthtip.detail.HealthTipDetailActivity.createIntent(
                    requireContext(),
                    healthTip.getId()
            );
            startActivity(intent);
        } else {
            showError("Không thể mở chi tiết mẹo sức khỏe do thiếu thông tin");
        }
    }

    @Override
    public void showRemovedFromFavorites(String healthTipTitle) {
        showMessage("Đã xóa '" + healthTipTitle + "' khỏi danh sách yêu thích");
    }

    @Override
    public void refreshFavoritesList() {
        if (presenter != null) {
            presenter.refreshFavorites();
        }
    }

    @Override
    public void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        swipeRefreshLayout.setRefreshing(loading);
    }

    @Override
    public void showMessage(String message) {
        if (isAdded() && getView() != null) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showError(String errorMessage) {
        if (isAdded() && getView() != null) {
            Snackbar.make(getView(), errorMessage, Snackbar.LENGTH_LONG).show();
        }
    }
}
