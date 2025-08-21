package com.vhn.doan.presentation.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;
import com.vhn.doan.data.SearchHistory;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị danh sách lịch sử tìm kiếm
 */
public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.SearchHistoryViewHolder> {
    private final Context mContext;
    private final List<SearchHistory> mAllSearchHistories;
    private final List<SearchHistory> mDisplayedSearchHistories;
    private OnSearchHistoryClickListener mListener;
    private OnSearchHistoryDeleteListener mDeleteListener;

    /**
     * Interface lắng nghe sự kiện click vào mục lịch sử tìm kiếm
     */
    public interface OnSearchHistoryClickListener {
        void onSearchHistoryClick(String keyword);
    }

    /**
     * Interface lắng nghe sự kiện xóa mục lịch sử tìm kiếm
     */
    public interface OnSearchHistoryDeleteListener {
        void onSearchHistoryDelete(SearchHistory searchHistory);
    }

    public SearchHistoryAdapter(Context context, List<SearchHistory> searchHistories) {
        mContext = context;
        mAllSearchHistories = searchHistories;
        mDisplayedSearchHistories = new ArrayList<>();
    }

    public void setOnSearchHistoryClickListener(OnSearchHistoryClickListener listener) {
        mListener = listener;
    }

    public void setOnSearchHistoryDeleteListener(OnSearchHistoryDeleteListener listener) {
        mDeleteListener = listener;
    }

    /**
     * Cập nhật danh sách lịch sử tìm kiếm hiển thị
     *
     * @param displayedHistories Danh sách lịch sử tìm kiếm sẽ được hiển thị
     */
    public void updateDisplayedHistory(List<SearchHistory> displayedHistories) {
        mDisplayedSearchHistories.clear();
        if (displayedHistories != null) {
            mDisplayedSearchHistories.addAll(displayedHistories);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SearchHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_search_history, parent, false);
        return new SearchHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchHistoryViewHolder holder, int position) {
        SearchHistory searchHistory = mDisplayedSearchHistories.get(position);
        holder.tvHistoryKeyword.setText(searchHistory.getKeyword());

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onSearchHistoryClick(searchHistory.getKeyword());
            }
        });

        holder.ivDeleteHistory.setOnClickListener(v -> {
            if (mDeleteListener != null) {
                mDeleteListener.onSearchHistoryDelete(searchHistory);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDisplayedSearchHistories != null ? mDisplayedSearchHistories.size() : 0;
    }

    /**
     * ViewHolder cho mục lịch sử tìm kiếm
     */
    static class SearchHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvHistoryKeyword;
        ImageView ivDeleteHistory;

        public SearchHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHistoryKeyword = itemView.findViewById(R.id.tv_history_keyword);
            ivDeleteHistory = itemView.findViewById(R.id.iv_delete_history);
        }
    }
}
