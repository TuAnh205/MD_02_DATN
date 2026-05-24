package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Review;
import com.bumptech.glide.Glide;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private final List<Review> reviews;
    private final Context context;
    private OnReviewClickListener onReviewClickListener;

    public interface OnReviewClickListener {
        void onReviewClick(Review review);
    }

    public ReviewAdapter(Context context, List<Review> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    public void setOnReviewClickListener(OnReviewClickListener listener) {
        this.onReviewClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);

        String productName = review.getProductName();
        if (TextUtils.isEmpty(productName)) {
            productName = "Sản phẩm";
        }
        holder.tvProductName.setText(productName);

        String userName = review.getUserName();
        if (TextUtils.isEmpty(userName)) {
            userName = "Ẩn danh";
        }
        holder.tvReviewer.setText("Đánh giá bởi " + userName);
        holder.tvReviewContent.setText(review.getContent());
        holder.ratingBarReview.setRating(review.getRating());
        holder.tvReviewDate.setText(review.getCreatedAt());

        Glide.with(context)
                .load(review.getProductImageUrl())
                .placeholder(R.drawable.bg_image_placeholder)
                .error(R.drawable.bg_image_placeholder)
                .centerCrop()
                .into(holder.ivProductImage);

        String responseText = review.getResponseText();
        if (responseText != null && !responseText.isEmpty()) {
            holder.layoutReplyContainer.setVisibility(View.VISIBLE);
            holder.tvReplyContent.setText(responseText);
            String responseBy = review.getResponseByName();
            String responseDate = review.getResponseDate();
            String info = "";
            if (responseBy != null && !responseBy.isEmpty()) {
                info = "Trả lời bởi " + responseBy;
            }
            if (responseDate != null && !responseDate.isEmpty()) {
                if (!info.isEmpty()) info += " • ";
                info += responseDate;
            }
            holder.tvReplyInfo.setText(info.isEmpty() ? "Đã trả lời" : info);
        } else {
            holder.layoutReplyContainer.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (onReviewClickListener != null) {
                onReviewClickListener.onReviewClick(review);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public void addReview(Review review) {
        reviews.add(0, review);
        notifyItemInserted(0);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvReviewer, tvReviewContent, tvReviewDate;
        TextView tvReplyContent, tvReplyInfo;
        LinearLayout layoutReplyContainer;
        RatingBar ratingBarReview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvReviewer = itemView.findViewById(R.id.tvReviewer);
            tvReviewContent = itemView.findViewById(R.id.tvReviewContent);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            tvReplyContent = itemView.findViewById(R.id.tvReplyContent);
            tvReplyInfo = itemView.findViewById(R.id.tvReplyInfo);
            layoutReplyContainer = itemView.findViewById(R.id.layoutReplyContainer);
            ratingBarReview = itemView.findViewById(R.id.ratingBarReview);
        }
    }
}
