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
 * Adapter mới cho ContentBlock với thứ tự hiển thị tuần tự
 * Hiển thị text, image, caption xen kẽ theo thứ tự trong dữ liệu
 */
public class NewContentBlockAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Các loại view
    private static final int VIEW_TYPE_TEXT = 1;
    private static final int VIEW_TYPE_IMAGE = 2;
    private static final int VIEW_TYPE_HEADING = 3;
    private static final int VIEW_TYPE_QUOTE = 4;
    private static final int VIEW_TYPE_CAPTION = 5; // Thêm type cho caption

    private List<ContentBlock> contentBlocks;

    public NewContentBlockAdapter() {
        this.contentBlocks = new ArrayList<>();
    }

    public void setContentBlocks(List<ContentBlock> contentBlocks) {
        this.contentBlocks = contentBlocks != null ? contentBlocks : new ArrayList<>();
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
            case "caption":
                return VIEW_TYPE_CAPTION;
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
                return new ImageViewHolder(inflater.inflate(R.layout.item_new_content_image, parent, false));
            case VIEW_TYPE_HEADING:
                return new HeadingViewHolder(inflater.inflate(R.layout.item_new_content_heading, parent, false));
            case VIEW_TYPE_QUOTE:
                return new QuoteViewHolder(inflater.inflate(R.layout.item_new_content_quote, parent, false));
            case VIEW_TYPE_CAPTION:
                return new CaptionViewHolder(inflater.inflate(R.layout.item_new_content_caption, parent, false));
            case VIEW_TYPE_TEXT:
            default:
                return new TextViewHolder(inflater.inflate(R.layout.item_new_content_text, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ContentBlock block = contentBlocks.get(position);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_IMAGE:
                ((ImageViewHolder) holder).bind(block);
                break;
            case VIEW_TYPE_HEADING:
                ((HeadingViewHolder) holder).bind(block);
                break;
            case VIEW_TYPE_QUOTE:
                ((QuoteViewHolder) holder).bind(block);
                break;
            case VIEW_TYPE_CAPTION:
                ((CaptionViewHolder) holder).bind(block);
                break;
            case VIEW_TYPE_TEXT:
            default:
                ((TextViewHolder) holder).bind(block);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return contentBlocks.size();
    }

    // ViewHolder cho Text Block
    static class TextViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        TextViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textViewContent);
        }

        void bind(ContentBlock block) {
            if (block.getValue() != null && !block.getValue().trim().isEmpty()) {
                textView.setText(block.getValue());
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
            }
        }
    }

    // ViewHolder cho Image Block - chỉ hiển thị hình ảnh
    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewContent);
        }

        void bind(ContentBlock block) {
            if (block.getValue() != null && !block.getValue().isEmpty()) {
                imageView.setVisibility(View.VISIBLE);

                Glide.with(imageView.getContext())
                    .load(block.getValue())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
            } else {
                imageView.setVisibility(View.GONE);
            }
        }
    }

    // ViewHolder cho Caption Block - chỉ hiển thị chú thích
    static class CaptionViewHolder extends RecyclerView.ViewHolder {
        TextView captionView;

        CaptionViewHolder(@NonNull View itemView) {
            super(itemView);
            captionView = itemView.findViewById(R.id.textViewCaption);
        }

        void bind(ContentBlock block) {
            if (block.getValue() != null && !block.getValue().trim().isEmpty()) {
                captionView.setText(block.getValue());
                captionView.setVisibility(View.VISIBLE);
            } else {
                captionView.setVisibility(View.GONE);
            }
        }
    }

    // ViewHolder cho Heading Block
    static class HeadingViewHolder extends RecyclerView.ViewHolder {
        TextView headingView;

        HeadingViewHolder(@NonNull View itemView) {
            super(itemView);
            headingView = itemView.findViewById(R.id.textViewHeading);
        }

        void bind(ContentBlock block) {
            if (block.getValue() != null && !block.getValue().trim().isEmpty()) {
                headingView.setText(block.getValue());
                headingView.setVisibility(View.VISIBLE);

                // Điều chỉnh style dựa trên level
                int level = 2; // Default H2
                if (block.getMetadata() != null && block.getMetadata().getLevel() != null) {
                    level = block.getMetadata().getLevel();
                }

                // Áp dụng style tùy theo level
                applyHeadingStyle(headingView, level);
            } else {
                headingView.setVisibility(View.GONE);
            }
        }

        private void applyHeadingStyle(TextView textView, int level) {
            float textSize = 20f; // Default size

            switch (level) {
                case 1:
                    textSize = 28f;
                    break;
                case 2:
                    textSize = 24f;
                    break;
                case 3:
                    textSize = 22f;
                    break;
                case 4:
                    textSize = 20f;
                    break;
                case 5:
                    textSize = 18f;
                    break;
                case 6:
                    textSize = 16f;
                    break;
            }

            textView.setTextSize(textSize);
        }
    }

    // ViewHolder cho Quote Block
    static class QuoteViewHolder extends RecyclerView.ViewHolder {
        TextView quoteView;

        QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            quoteView = itemView.findViewById(R.id.textViewQuote);
        }

        void bind(ContentBlock block) {
            if (block.getValue() != null && !block.getValue().trim().isEmpty()) {
                quoteView.setText(block.getValue());
                quoteView.setVisibility(View.VISIBLE);
            } else {
                quoteView.setVisibility(View.GONE);
            }
        }
    }
}
