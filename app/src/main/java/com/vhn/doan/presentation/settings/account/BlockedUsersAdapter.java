package com.vhn.doan.presentation.settings.account;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vhn.doan.R;

import java.util.List;

/**
 * ✅ IMPLEMENTED: Adapter cho danh sách blocked users
 */
public class BlockedUsersAdapter extends RecyclerView.Adapter<BlockedUsersAdapter.BlockedUserViewHolder> {

    private List<BlockedUsersActivity.BlockedUser> blockedUsers;
    private OnUnblockListener listener;

    public interface OnUnblockListener {
        void onUnblock(BlockedUsersActivity.BlockedUser user);
    }

    public BlockedUsersAdapter(List<BlockedUsersActivity.BlockedUser> blockedUsers, OnUnblockListener listener) {
        this.blockedUsers = blockedUsers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public BlockedUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_blocked_user, parent, false);
        return new BlockedUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlockedUserViewHolder holder, int position) {
        BlockedUsersActivity.BlockedUser user = blockedUsers.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return blockedUsers.size();
    }

    static class BlockedUserViewHolder extends RecyclerView.ViewHolder {
        TextView tvDisplayName;
        TextView tvEmail;
        TextView tvBlockedDate;
        Button btnUnblock;

        public BlockedUserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDisplayName = itemView.findViewById(R.id.tvDisplayName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvBlockedDate = itemView.findViewById(R.id.tvBlockedDate);
            btnUnblock = itemView.findViewById(R.id.btnUnblock);
        }

        public void bind(BlockedUsersActivity.BlockedUser user, OnUnblockListener listener) {
            tvDisplayName.setText(user.getDisplayName());

            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                tvEmail.setVisibility(View.VISIBLE);
                tvEmail.setText(user.getEmail());
            } else {
                tvEmail.setVisibility(View.GONE);
            }

            tvBlockedDate.setText("Đã chặn: " + user.getFormattedDate());

            btnUnblock.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUnblock(user);
                }
            });
        }
    }
}
