package com.vhn.doan.presentation.support;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.vhn.doan.R;
import com.vhn.doan.data.SupportTicket;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter để hiển thị danh sách Support Tickets
 */
public class SupportTicketsAdapter extends RecyclerView.Adapter<SupportTicketsAdapter.TicketViewHolder> {

    private List<SupportTicket> tickets = new ArrayList<>();
    private OnTicketClickListener listener;

    public interface OnTicketClickListener {
        void onTicketClick(SupportTicket ticket);
    }

    public void setOnTicketClickListener(OnTicketClickListener listener) {
        this.listener = listener;
    }

    public void setTickets(List<SupportTicket> tickets) {
        this.tickets = tickets != null ? tickets : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addTickets(List<SupportTicket> newTickets) {
        if (newTickets != null && !newTickets.isEmpty()) {
            int startPosition = this.tickets.size();
            this.tickets.addAll(newTickets);
            notifyItemRangeInserted(startPosition, newTickets.size());
        }
    }

    public void clear() {
        this.tickets.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TicketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_support_ticket, parent, false);
        return new TicketViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TicketViewHolder holder, int position) {
        SupportTicket ticket = tickets.get(position);
        holder.bind(ticket);
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    class TicketViewHolder extends RecyclerView.ViewHolder {

        private TextView ticketIdText;
        private Chip statusChip;
        private TextView ticketTypeText;
        private TextView subjectText;
        private TextView descriptionText;
        private TextView createdAtText;
        private LinearLayout responseIndicator;

        public TicketViewHolder(@NonNull View itemView) {
            super(itemView);

            ticketIdText = itemView.findViewById(R.id.ticketIdText);
            statusChip = itemView.findViewById(R.id.statusChip);
            ticketTypeText = itemView.findViewById(R.id.ticketTypeText);
            subjectText = itemView.findViewById(R.id.subjectText);
            descriptionText = itemView.findViewById(R.id.descriptionText);
            createdAtText = itemView.findViewById(R.id.createdAtText);
            responseIndicator = itemView.findViewById(R.id.responseIndicator);
        }

        public void bind(SupportTicket ticket) {
            // Ticket ID
            ticketIdText.setText("#" + (ticket.getTicketId() != null ?
                                       ticket.getTicketId().substring(0, Math.min(8, ticket.getTicketId().length())) :
                                       "Unknown"));

            // Status
            updateStatusChip(ticket.getStatus());

            // Ticket Type
            if (ticket.getType() != null) {
                ticketTypeText.setText(ticket.getType().getDisplayName());
            }

            // Subject
            subjectText.setText(ticket.getSubject());

            // Description preview
            if (ticket.getDescription() != null && !ticket.getDescription().isEmpty()) {
                descriptionText.setText(ticket.getDescription());
                descriptionText.setVisibility(View.VISIBLE);
            } else {
                descriptionText.setVisibility(View.GONE);
            }

            // Created date
            createdAtText.setText(formatDate(ticket.getCreatedAt()));

            // Response indicator
            if (ticket.getAdminResponse() != null && !ticket.getAdminResponse().isEmpty()) {
                responseIndicator.setVisibility(View.VISIBLE);
            } else {
                responseIndicator.setVisibility(View.GONE);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTicketClick(ticket);
                }
            });
        }

        private void updateStatusChip(SupportTicket.TicketStatus status) {
            if (status == null) {
                status = SupportTicket.TicketStatus.OPEN;
            }

            statusChip.setText(status.getDisplayName());

            // Set color based on status
            int chipColor;
            switch (status) {
                case OPEN:
                    chipColor = Color.parseColor("#2196F3"); // Blue
                    break;
                case IN_PROGRESS:
                    chipColor = Color.parseColor("#FF9800"); // Orange
                    break;
                case RESOLVED:
                    chipColor = Color.parseColor("#4CAF50"); // Green
                    break;
                case CLOSED:
                    chipColor = Color.parseColor("#757575"); // Grey
                    break;
                default:
                    chipColor = Color.parseColor("#2196F3");
            }

            statusChip.setChipBackgroundColorResource(android.R.color.transparent);
            statusChip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(chipColor));
        }

        private String formatDate(Date date) {
            if (date == null) {
                return "";
            }

            long diff = System.currentTimeMillis() - date.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            if (days > 7) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                return sdf.format(date);
            } else if (days > 0) {
                return days + " ngày trước";
            } else if (hours > 0) {
                return hours + " giờ trước";
            } else if (minutes > 0) {
                return minutes + " phút trước";
            } else {
                return "Vừa xong";
            }
        }
    }
}

