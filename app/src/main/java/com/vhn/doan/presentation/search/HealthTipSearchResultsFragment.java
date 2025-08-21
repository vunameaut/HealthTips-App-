package com.vhn.doan.presentation.search;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.HealthTip;
import com.vhn.doan.presentation.home.adapter.HealthTipAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị kết quả tìm kiếm bài viết (HealthTip)
 */
public class HealthTipSearchResultsFragment extends Fragment {
    private RecyclerView rvHealthTipResults;
    private View layoutNoArticleResults;
    private HealthTipAdapter adapter;
    private List<HealthTip> healthTipResults = new ArrayList<>();

    public static HealthTipSearchResultsFragment newInstance() {
        return new HealthTipSearchResultsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_health_tip_search_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo view
        rvHealthTipResults = view.findViewById(R.id.rv_health_tip_results);
        layoutNoArticleResults = view.findViewById(R.id.layout_no_article_results);

        // Thiết lập RecyclerView
        rvHealthTipResults.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HealthTipAdapter(
                getContext(),
                healthTipResults,
                new HealthTipAdapter.HealthTipClickListener() {
                    @Override
                    public void onHealthTipClick(HealthTip healthTip) {
                        if (getActivity() != null && healthTip != null && healthTip.getId() != null) {
                            Intent intent = com.vhn.doan.presentation.healthtip.detail.HealthTipDetailActivity.createIntent(
                                    getContext(),
                                    healthTip.getId()
                            );
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onFavoriteClick(HealthTip healthTip, boolean isFavorite) {
                        // Xử lý khi người dùng thích/bỏ thích bài viết
                        // Có thể thêm code cập nhật trạng thái yêu thích ở đây nếu cần
                    }
                },
                new com.vhn.doan.data.repository.FavoriteRepositoryImpl());
        rvHealthTipResults.setAdapter(adapter);

        // Hiển thị trạng thái ban đầu
        updateUI();
    }

    /**
     * Cập nhật danh sách kết quả tìm kiếm bài viết
     * @param results Danh sách kết quả tìm kiếm mới
     */
    public void updateResults(List<HealthTip> results) {
        healthTipResults.clear();
        if (results != null) {
            healthTipResults.addAll(results);
        }
        updateUI();
    }

    /**
     * Cập nhật giao diện dựa trên kết quả tìm kiếm
     */
    private void updateUI() {
        // Nếu fragment chưa được gắn view hoặc đã bị detach, không thực hiện cập nhật UI
        if (!isAdded() || getView() == null) {
            return;
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        try {
            // Phải đảm bảo các View đã được khởi tạo trước khi sử dụng
            if (rvHealthTipResults == null) {
                rvHealthTipResults = getView().findViewById(R.id.rv_health_tip_results);
            }

            if (layoutNoArticleResults == null) {
                layoutNoArticleResults = getView().findViewById(R.id.layout_no_article_results);
            }

            // Kiểm tra null cho các view trước khi thiết lập trạng thái hiển thị
            if (rvHealthTipResults != null && layoutNoArticleResults != null) {
                // Hiển thị thông báo khi không có kết quả
                if (healthTipResults.isEmpty()) {
                    rvHealthTipResults.setVisibility(View.GONE);
                    layoutNoArticleResults.setVisibility(View.VISIBLE);
                } else {
                    rvHealthTipResults.setVisibility(View.VISIBLE);
                    layoutNoArticleResults.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            // Xử lý bất kỳ ngoại lệ nào có thể xảy ra khi truy cập các view
            e.printStackTrace();
        }
    }

    /**
     * Xóa tất cả kết quả hiện tại
     */
    public void clearResults() {
        healthTipResults.clear();
        updateUI();
    }

    /**
     * Kiểm tra danh sách kết quả tìm kiếm có trống hay không
     * @return true nếu không có kết quả tìm kiếm, false nếu có
     */
    public boolean isResultsEmpty() {
        return healthTipResults == null || healthTipResults.isEmpty();
    }
}
