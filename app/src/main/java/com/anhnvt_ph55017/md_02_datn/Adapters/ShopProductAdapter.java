package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.bumptech.glide.Glide;

import java.util.List;

public class ShopProductAdapter extends RecyclerView.Adapter<ShopProductAdapter.ViewHolder> {

    // ─── Interface ───────────────────────────────────────────────────────────
    public interface OnProductActionListener {
        void onEdit(Product product, int position);
        void onToggleVisibility(Product product, int position);
        void onDelete(Product product, int position);
    }

    // ─── Fields ──────────────────────────────────────────────────────────────
    private final Context context;
    private final List<Product> list;
    private final OnProductActionListener listener;

    // ─── Constructor ─────────────────────────────────────────────────────────
    public ShopProductAdapter(Context context, List<Product> list, OnProductActionListener listener) {
        this.context  = context;
        this.list     = list;
        this.listener = listener;
    }

    // ─── RecyclerView overrides ───────────────────────────────────────────────
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_shop_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = list.get(position);

        // Bind data
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(String.format("%,.0f đ", product.getPrice()));
        holder.tvStock.setText("Tồn kho : " + product.getStock());
        holder.tvCategory.setText("Danh mục : " +
                (product.getCategory() != null ? product.getCategory() : ""));

        // Badge trạng thái
        boolean visible = product.isVisible();   // thêm field isVisible vào model nếu chưa có
        holder.tvStatus.setText(visible ? "Đang hiện" : "Đã ẩn");

        // Icon ẩn/hiện đổi theo trạng thái
        holder.btnToggle.setImageResource(
                visible ? R.drawable.ic_eye_close : R.drawable.ic_eye
        );

        // Load ảnh
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.bg_image)
                    .error(R.drawable.bg_image)
                    .centerCrop()
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.bg_image);
        }

        // ─── Action listeners ─────────────────────────────────────────────
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(product, holder.getAdapterPosition());
        });

        holder.btnToggle.setOnClickListener(v -> {
            if (listener != null) listener.onToggleVisibility(product, holder.getAdapterPosition());
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(product, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    // ─── ViewHolder ───────────────────────────────────────────────────────────
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView  tvName, tvPrice, tvStock, tvStatus, tvCategory;
        ImageView btnEdit, btnToggle, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            tvName     = itemView.findViewById(R.id.tvProductName);
            tvPrice    = itemView.findViewById(R.id.tvPrice);
            tvStock    = itemView.findViewById(R.id.tvStock);
            tvStatus   = itemView.findViewById(R.id.tvBadgeStatus);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            btnEdit    = itemView.findViewById(R.id.btnEdit);
            btnToggle  = itemView.findViewById(R.id.btnToggleVisibility);
            btnDelete  = itemView.findViewById(R.id.btnDelete);
        }
    }
}