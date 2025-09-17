package com.vhn.doan.presentation.healthtip.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.vhn.doan.R;
import com.vhn.doan.data.ContentBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter hiển thị các khối nội dung (ContentBlock) của bài viết
 */
public class ContentBlockAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Các loại view
    private static final int VIEW_TYPE_TEXT = 0;
    private static final int VIEW_TYPE_IMAGE = 1;
    private static final int VIEW_TYPE_HEADING = 2;
    private static final int VIEW_TYPE_QUOTE = 3;

    private List<ContentBlock> contentBlocks;

    public ContentBlockAdapter() {
        this.contentBlocks = new ArrayList<>();
    }

    public void setContentBlocks(List<ContentBlock> contentBlocks) {
        this.contentBlocks = contentBlocks;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        ContentBlock block = contentBlocks.get(position);
        switch (block.getType()) {
            case "image":
                return VIEW_TYPE_IMAGE;
            case "heading":
                return VIEW_TYPE_HEADING;
            case "quote":
                return VIEW_TYPE_QUOTE;
            case "text":
            default:
                return VIEW_TYPE_TEXT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_IMAGE:
                View imageView = inflater.inflate(R.layout.item_content_block_image, parent, false);
                return new ImageViewHolder(imageView);
            case VIEW_TYPE_HEADING:
                View headingView = inflater.inflate(R.layout.item_content_block_heading, parent, false);
                return new HeadingViewHolder(headingView);
            case VIEW_TYPE_QUOTE:
                View quoteView = inflater.inflate(R.layout.item_content_block_quote, parent, false);
                return new QuoteViewHolder(quoteView);
            case VIEW_TYPE_TEXT:
            default:
                View textView = inflater.inflate(R.layout.item_content_block_text, parent, false);
                return new TextViewHolder(textView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ContentBlock block = contentBlocks.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_TEXT:
                ((TextViewHolder) holder).bind(block);
                break;
            case VIEW_TYPE_IMAGE:
                ((ImageViewHolder) holder).bind(block);
                break;
            case VIEW_TYPE_HEADING:
                ((HeadingViewHolder) holder).bind(block);
                break;
            case VIEW_TYPE_QUOTE:
                ((QuoteViewHolder) holder).bind(block);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return contentBlocks.size();
    }

    /**
     * ViewHolder cho khối text
     */
    static class TextViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewContent;

        public TextViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewContent = itemView.findViewById(R.id.textViewContent);
        }

        public void bind(ContentBlock block) {
            textViewContent.setText(block.getValue());
        }
    }

    /**
     * ViewHolder cho khối image
     */
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewContent;
        private final TextView textViewCaption;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewContent = itemView.findViewById(R.id.imageViewContent);
            textViewCaption = itemView.findViewById(R.id.textViewCaption);
        }

        public void bind(ContentBlock block) {
            // Load hình ảnh với Glide
            Glide.with(itemView.getContext())
                    .load(block.getValue())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(imageViewContent);

            // Hiển thị caption nếu có
            if (block.getMetadata() != null && block.getMetadata().getCaption() != null) {
                textViewCaption.setText(block.getMetadata().getCaption());
                textViewCaption.setVisibility(View.VISIBLE);
            } else {
                textViewCaption.setVisibility(View.GONE);
            }

            // Set content description cho accessibility
            if (block.getMetadata() != null && block.getMetadata().getAlt() != null) {
                imageViewContent.setContentDescription(block.getMetadata().getAlt());
            } else {
                imageViewContent.setContentDescription(itemView.getContext().getString(R.string.health_tip_image));
            }
        }
    }

    /**
     * ViewHolder cho khối heading
     */
    static class HeadingViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewHeading;

        public HeadingViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewHeading = itemView.findViewById(R.id.textViewHeading);
        }

        public void bind(ContentBlock block) {
            textViewHeading.setText(block.getValue());

            // Set style dựa trên cấp độ heading
            int headingLevel = 1; // Default là H1
            if (block.getMetadata() != null && block.getMetadata().getLevel() != null) {
                headingLevel = block.getMetadata().getLevel();
            }

            // Thiết lập style dựa trên cấp độ heading
            switch (headingLevel) {
                case 1:
                    textViewHeading.setTextAppearance(itemView.getContext(), R.style.TextAppearance_App_Headline4);
                    break;
                case 2:
                    textViewHeading.setTextAppearance(itemView.getContext(), R.style.TextAppearance_App_Headline5);
                    break;
                case 3:
                    textViewHeading.setTextAppearance(itemView.getContext(), R.style.TextAppearance_App_Headline6);
                    break;
                case 4:
                    textViewHeading.setTextAppearance(itemView.getContext(), R.style.TextAppearance_App_Subtitle1);
                    break;
                case 5:
                    textViewHeading.setTextAppearance(itemView.getContext(), R.style.TextAppearance_App_Subtitle2);
                    break;
                case 6:
                default:
                    textViewHeading.setTextAppearance(itemView.getContext(), R.style.TextAppearance_App_Body1);
                    textViewHeading.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                    break;
            }
        }
    }

    /**
     * ViewHolder cho khối quote
     */
    static class QuoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewQuote;

        public QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewQuote = itemView.findViewById(R.id.textViewQuote);
        }

        public void bind(ContentBlock block) {
            textViewQuote.setText(block.getValue());
        }
    }
}
