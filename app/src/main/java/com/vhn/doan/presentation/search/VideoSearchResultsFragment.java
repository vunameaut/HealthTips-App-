package com.vhn.doan.presentation.search;

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
import com.vhn.doan.data.ShortVideo;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị kết quả tìm kiếm video
 */
public class VideoSearchResultsFragment extends Fragment {
    private RecyclerView rvVideoResults;
    private View layoutNoVideoResults;
    private VideoSearchResultAdapter adapter;
    private List<ShortVideo> videoResults = new ArrayList<>();

    // Interface lắng nghe sự kiện click vào video
    private VideoItemClickListener listener;

    public static VideoSearchResultsFragment newInstance() {
        return new VideoSearchResultsFragment();
    }

    public void setVideoItemClickListener(VideoItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_video_search_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo view
        rvVideoResults = view.findViewById(R.id.rv_video_results);
        layoutNoVideoResults = view.findViewById(R.id.layout_no_video_results);

        // Thiết lập RecyclerView
        rvVideoResults.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new VideoSearchResultAdapter(getContext(), videoResults);
        adapter.setOnVideoClickListener(position -> {
            if (listener != null && position < videoResults.size()) {
                listener.onVideoClicked(videoResults.get(position));
            }
        });
        rvVideoResults.setAdapter(adapter);

        // Hiển thị trạng thái ban đầu
        updateUI();
    }

    /**
     * Cập nhật danh sách kết quả tìm kiếm video
     * @param results Danh sách kết quả tìm kiếm mới
     */
    public void updateResults(List<ShortVideo> results) {
        videoResults.clear();
        if (results != null) {
            videoResults.addAll(results);
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
            if (rvVideoResults == null) {
                rvVideoResults = getView().findViewById(R.id.rv_video_results);
            }

            if (layoutNoVideoResults == null) {
                layoutNoVideoResults = getView().findViewById(R.id.layout_no_video_results);
            }

            // Kiểm tra null cho các view trước khi thiết lập trạng thái hiển thị
            if (rvVideoResults != null && layoutNoVideoResults != null) {
                // Hiển thị thông báo khi không có kết quả
                if (videoResults.isEmpty()) {
                    rvVideoResults.setVisibility(View.GONE);
                    layoutNoVideoResults.setVisibility(View.VISIBLE);
                } else {
                    rvVideoResults.setVisibility(View.VISIBLE);
                    layoutNoVideoResults.setVisibility(View.GONE);
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
        videoResults.clear();
        updateUI();
    }

    /**
     * Kiểm tra danh sách kết quả tìm kiếm có trống hay không
     * @return true nếu không có kết quả tìm kiếm, false nếu có
     */
    public boolean isResultsEmpty() {
        return videoResults == null || videoResults.isEmpty();
    }

    /**
     * Interface để lắng nghe sự kiện click vào video
     */
    public interface VideoItemClickListener {
        void onVideoClicked(ShortVideo video);
    }
}
