package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Category;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    List<Category> list;
    OnCategoryClick listener;
    OnCategoryActionListener actionListener;
    Context context;
    Set<Integer> selected = new HashSet<>();

    private final int[] iconColors = {
            0xFF6366F1, // Blue
            0xFFD946EF, // Purple
            0xFFFB923C, // Orange
            0xFFEC4899, // Pink
            0xFF06B6D4, // Cyan
            0xFF10B981  // Green
    };

    private final int[] icons = {
            R.drawable.ic_laptop,
            R.drawable.ic_phone,
            R.drawable.ic_headphone,
            R.drawable.ic_box,
            R.drawable.ic_pc,
            R.drawable.ic_phukien
    };

    public interface OnCategoryClick {
        void onClick(Set<Integer> selectedCategory);
    }

    public interface OnCategoryActionListener {
        void onAction(Category category, String action);
    }

    // Constructor for selection mode
    public CategoryAdapter(List<Category> list, OnCategoryClick listener) {
        this.list = list;
        this.listener = listener;
        this.actionListener = null;
        this.context = null;
    }

    // Constructor for management mode
    public CategoryAdapter(Context context, List<Category> list, OnCategoryActionListener listener) {
        this.context = context;
        this.list = list;
        this.actionListener = listener;
        this.listener = null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = list.get(position);

        // Set icon and color if management mode
        if (actionListener != null) {
            int iconIndex = position % icons.length;
            holder.ivIcon.setVisibility(View.VISIBLE);
            holder.ivIcon.setImageResource(icons[iconIndex]);
            holder.ivIcon.setBackgroundColor(iconColors[iconIndex]);

            holder.tvCategoryName.setText(category.getName());
            holder.tvProductCount.setText(category.getProductCount() + " sản phẩm");
            holder.tvProductCount.setVisibility(View.VISIBLE);

            // Edit button
            holder.ivEdit.setVisibility(View.VISIBLE);
            holder.ivEdit.setOnClickListener(v -> actionListener.onAction(category, "edit"));

            // Delete button
            holder.ivDelete.setVisibility(View.VISIBLE);
            holder.ivDelete.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Xóa danh mục")
                        .setMessage("Bạn chắc chắn muốn xóa danh mục này?")
                        .setPositiveButton("Xóa", (dialog, which) -> actionListener.onAction(category, "delete"))
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        } else {
            // Selection mode
            holder.tvCategoryName.setText(category.getName());
            holder.ivIcon.setVisibility(View.GONE);
            holder.tvProductCount.setVisibility(View.GONE);
            holder.ivEdit.setVisibility(View.GONE);
            holder.ivDelete.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivIcon;
        private final TextView tvCategoryName;
        private final TextView tvProductCount;
        private final ImageView ivEdit;
        private final ImageView ivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvProductCount = itemView.findViewById(R.id.tvProductCount);
            ivEdit = itemView.findViewById(R.id.ivEdit);
            ivDelete = itemView.findViewById(R.id.ivDelete);
        }
    }
}
