package com.vhn.doan.presentation.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.presentation.base.BaseFragment;

/**
 * Fragment hiển thị danh sách video đã like của người dùng
 */
public class LikedVideosFragment extends BaseFragment {

    private RecyclerView recyclerView;

    public static LikedVideosFragment newInstance() {
        return new LikedVideosFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grid_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        // TODO: Load dữ liệu video đã like từ Firebase
    }

    @Override
    protected void initViews(View view) {
        recyclerView = view.findViewById(R.id.recyclerView);
    }

    @Override
    protected void setupListeners() {
        // Không cần thiết lập listener ở đây
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);

        // Tạm thời sử dụng adapter rỗng, sẽ cập nhật sau khi có dữ liệu thực tế
        // GridContentAdapter adapter = new GridContentAdapter(new ArrayList<>());
        // recyclerView.setAdapter(adapter);
    }
}
