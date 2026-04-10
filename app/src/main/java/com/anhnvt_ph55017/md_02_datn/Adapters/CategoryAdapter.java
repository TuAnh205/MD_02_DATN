package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
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
    int selectedPosition = -1;

    private final int[] iconColors = {
            0xFF6366F1, // Blue
            0xFFD946EF, // Purple
            0xFFFB923C, // Orange
            0xFFEC4899, // Pink
            0xFF06B6D4, // Cyan
            0xFF10B981  // Green
    };

    private final int[] icons = {
            R.drawable.ic_camera,
            R.drawable.volume_solid_full,
            R.drawable.ic_box,
            R.drawable.ic_mycrophone,
            R.drawable.ic_pc,
            R.drawable.ic_laptop,
            R.drawable.ic_laptop,
            R.drawable.ic_wifi,
            R.drawable.ic_phukien,

            R.drawable.ic_headphone,
            R.drawable.ic_phone,
            R.drawable.ic_oclock,



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
        int iconIndex = position % icons.length;
        holder.ivIcon.setVisibility(View.VISIBLE);
        holder.ivIcon.setImageResource(icons[iconIndex]);
        holder.tvCategoryName.setText(category.getName());

        // Xử lý đổi màu khi chọn
        if (selectedPosition == position) {
            holder.layout.setBackgroundResource(R.drawable.bg_category_selected);
            holder.tvCategoryName.setTextColor(Color.WHITE);
            holder.ivIcon.setColorFilter(Color.WHITE);
        } else {
            holder.layout.setBackgroundResource(R.drawable.bg_category_unselected);
            holder.tvCategoryName.setTextColor(Color.parseColor("#64748B"));
            holder.ivIcon.setColorFilter(Color.parseColor("#64748B"));
        }

        holder.layout.setOnClickListener(v -> {
            int oldPos = selectedPosition;
            if (selectedPosition == position) {
                // Bỏ chọn nếu nhấn lần 2
                selectedPosition = -1;
                notifyItemChanged(oldPos);
                if (listener != null) {
                    listener.onClick(new HashSet<>()); // Gửi set rỗng để báo bỏ lọc
                }
            } else {
                selectedPosition = position;
                notifyItemChanged(oldPos);
                notifyItemChanged(selectedPosition);
                if (listener != null) {
                    Set<Integer> sel = new HashSet<>();
                    sel.add(position);
                    listener.onClick(sel);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View layout;
        public final ImageView ivIcon;
        public final TextView tvCategoryName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView;
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}
