package com.vhn.doan.presentation.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vhn.doan.R;

/**
 * Fragment hiển thị danh sách video đã like
 * Sẽ được triển khai lại sau khi có hệ thống video mới
 */
public class LikedVideosFragment extends Fragment {

    public static LikedVideosFragment newInstance() {
        return new LikedVideosFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_liked_videos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Tạm thời hiển thị thông báo cho người dùng
        TextView messageTextView = view.findViewById(R.id.textViewMessage);
        if (messageTextView != null) {
            messageTextView.setText("Chức năng video sẽ được triển khai lại trong phiên bản tiếp theo");
        }
    }
}
