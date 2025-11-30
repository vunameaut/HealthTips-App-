package com.vhn.doan.presentation.settings.support.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.vhn.doan.R;
import com.vhn.doan.model.SupportTicket;
import com.vhn.doan.presentation.support.TicketChatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SupportTicketAdapter extends RecyclerView.Adapter<SupportTicketAdapter.ViewHolder> {

    private Context context;
    private List<SupportTicket> tickets;
    private SimpleDateFormat dateFormat;

    public SupportTicketAdapter(Context context, List<SupportTicket> tickets) {
        this.context = context;
        this.tickets = tickets;
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_support_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SupportTicket ticket = tickets.get(position);

        // Ticket ID
        holder.tvTicketId.setText("#" + ticket.getId().substring(0, Math.min(8, ticket.getId().length())).toUpperCase());

        // Subject
        holder.tvSubject.setText(ticket.getSubject());

        // Type
        holder.tvType.setText(ticket.getIssueType());

        // Description preview
        if (ticket.getDescription() != null) {
            holder.tvDescription.setText(ticket.getDescription());
        }

        // Date
        holder.tvDate.setText(dateFormat.format(new Date(ticket.getTimestamp())));

        // Set status
        String status = ticket.getStatus();
        if ("resolved".equals(status)) {
            holder.tvStatus.setText(R.string.status_resolved);
        } else if ("in_progress".equals(status)) {
            holder.tvStatus.setText(R.string.status_in_progress);
        } else {
            holder.tvStatus.setText(R.string.status_pending);
        }

        // Show admin response indicator
        if (ticket.getAdminResponse() != null && !ticket.getAdminResponse().isEmpty()) {
            holder.responseIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.responseIndicator.setVisibility(View.GONE);
        }

        // Show image thumbnail if available
        if (ticket.getImageUrl() != null && !ticket.getImageUrl().isEmpty()) {
            holder.imageThumbnail.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(ticket.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.bg_input_field)
                    .into(holder.imageThumbnail);
        } else {
            holder.imageThumbnail.setVisibility(View.GONE);
        }

        // Click to open chat
        holder.itemView.setOnClickListener(v -> openTicketChat(ticket));
    }

    private void openTicketChat(SupportTicket ticket) {
        Intent intent = new Intent(context, TicketChatActivity.class);
        intent.putExtra(TicketChatActivity.EXTRA_TICKET_ID, ticket.getId());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    private void showTicketDetails(SupportTicket ticket) {
        StringBuilder details = new StringBuilder();
        details.append("Loại: ").append(ticket.getIssueType()).append("\n\n");
        details.append(context.getString(R.string.subject)).append(": ").append(ticket.getSubject()).append("\n\n");
        details.append("Mô tả: ").append(ticket.getDescription()).append("\n\n");
        details.append("Trạng thái: ").append(ticket.getStatus()).append("\n\n");
        details.append(context.getString(R.string.submitted_at)).append(": ")
                .append(dateFormat.format(new Date(ticket.getTimestamp())));

        if (ticket.getAdminResponse() != null && !ticket.getAdminResponse().isEmpty()) {
            details.append("\n\n--- ").append(context.getString(R.string.admin_response)).append(" ---\n");
            details.append(ticket.getAdminResponse());
            details.append("\n\n").append(context.getString(R.string.responded_at)).append(": ")
                    .append(dateFormat.format(new Date(ticket.getRespondedAt())));
        }

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.ticket_details))
                .setMessage(details.toString())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTicketId;
        TextView tvStatus;
        TextView tvType;
        TextView tvSubject;
        TextView tvDescription;
        TextView tvDate;
        View responseIndicator;
        ImageView imageThumbnail;

        ViewHolder(View itemView) {
            super(itemView);
            tvTicketId = itemView.findViewById(R.id.ticketIdText);
            tvStatus = itemView.findViewById(R.id.statusChip);
            tvType = itemView.findViewById(R.id.ticketTypeText);
            tvSubject = itemView.findViewById(R.id.subjectText);
            tvDescription = itemView.findViewById(R.id.descriptionText);
            tvDate = itemView.findViewById(R.id.createdAtText);
            responseIndicator = itemView.findViewById(R.id.responseIndicator);
            imageThumbnail = itemView.findViewById(R.id.imageThumbnail);
        }
    }
}
