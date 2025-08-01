package com.vhn.doan.presentation.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.vhn.doan.R;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.data.repository.FavoriteRepositoryImpl;
import com.vhn.doan.data.repository.HealthTipRepositoryImpl;
import com.vhn.doan.presentation.base.BaseFragment;
import com.vhn.doan.presentation.favorite.FavoritePresenter;
import com.vhn.doan.presentation.favorite.FavoriteView;
import com.vhn.doan.presentation.profile.adapter.GridFavoriteAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị danh sách bài viết yêu thích của người dùng
 * Sử dụng GridLayoutManager để hiển thị dạng lưới
 */
public class FavoritePostsFragment extends BaseFragment implements FavoriteView {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private ConstraintLayout emptyStateLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyStateMessage;

    // MVP Components
    private FavoritePresenter presenter;
    private GridFavoriteAdapter adapter;

    public static FavoritePostsFragment newInstance() {
        return new FavoritePostsFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grid_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Gắn presenter với view
        presenter.attachView(this);

        // Thiết lập SwipeRefreshLayout nếu có
        setupSwipeRefresh();
    }

    @Override
    protected void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);

        // Tìm các view khác nếu có
        progressBar = view.findViewById(R.id.progressBar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Nếu không có empty state trong layout, tạm thời bỏ qua
        if (emptyStateLayout != null) {
            emptyStateMessage = emptyStateLayout.findViewById(R.id.emptyStateMessage);
        }
    }

    @Override
    protected void setupListeners() {
        // Thiết lập listener nếu cần
    }

    private void setupSwipeRefresh() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this::refreshFavorites);

            // Thiết lập màu sắc cho progress indicator
            swipeRefreshLayout.setColorSchemeResources(
                    R.color.primary_button_start,
                    R.color.secondary_button_start,
                    R.color.accent
            );
        }
    }

    private void setupRecyclerView() {
        // Sử dụng GridLayoutManager với 3 cột
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);

        // Tạo adapter mới với danh sách rỗng
        adapter = new GridFavoriteAdapter(
                requireContext(),
                new ArrayList<>(),
                new GridFavoriteAdapter.OnFavoriteItemClickListener() {
                    @Override
                    public void onItemClick(HealthTip healthTip) {
                        presenter.onHealthTipSelected(healthTip);
                    }

                    @Override
                    public void onRemoveFavorite(HealthTip healthTip) {
                        presenter.removeFromFavorites(healthTip);
                    }
                }
        );

        recyclerView.setAdapter(adapter);
    }

    private void refreshFavorites() {
        if (presenter != null) {
            presenter.refreshFavorites();
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
        if (recyclerView != null) {
            recyclerView.setVisibility(View.VISIBLE);
        }

        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.GONE);
        }

        // Cập nhật adapter với danh sách mới
        if (adapter == null) {
            setupRecyclerView(); // Khởi tạo adapter nếu chưa có
        }

        adapter.updateData(favoriteHealthTips);
    }

    @Override
    public void showEmptyFavorites() {
        if (recyclerView != null) {
            recyclerView.setVisibility(View.GONE);
        }

        if (emptyStateLayout != null) {
            emptyStateLayout.setVisibility(View.VISIBLE);

            if (emptyStateMessage != null) {
                emptyStateMessage.setText("Bạn chưa có bài viết yêu thích nào");
            }
        }
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
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(loading);
        }
    }

    @Override
    public void showMessage(String message) {
        if (isAdded() && getContext() != null) {
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
