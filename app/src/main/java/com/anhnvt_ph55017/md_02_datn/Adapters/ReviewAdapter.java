package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
    private final List<Review> reviews;
    private final Context context;

    public ReviewAdapter(Context context, List<Review> reviews) {
        this.context = context;
        this.reviews = reviews;
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
        holder.tvReviewer.setText(review.getUserName());
        holder.tvReviewContent.setText(review.getContent());
        holder.ratingBarReview.setRating(review.getRating());
        holder.tvReviewDate.setText(review.getCreatedAt());
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
        TextView tvReviewer, tvReviewContent, tvReviewDate;
        RatingBar ratingBarReview;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReviewer = itemView.findViewById(R.id.tvReviewer);
            tvReviewContent = itemView.findViewById(R.id.tvReviewContent);
            tvReviewDate = itemView.findViewById(R.id.tvReviewDate);
            ratingBarReview = itemView.findViewById(R.id.ratingBarReview);
        }
    }
}
