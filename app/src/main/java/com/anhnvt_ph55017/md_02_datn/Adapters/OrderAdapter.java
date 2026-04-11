package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Order;
import com.anhnvt_ph55017.md_02_datn.screens.OrderDetailActivity;
import com.bumptech.glide.Glide;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder>{

    public interface OnOrderClickListener {
        void onDetail(Order order);
    }

    Context context;
    List<Order> list;
    OnOrderClickListener listener;

    public OrderAdapter(Context context, List<Order> list, OnOrderClickListener listener){
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        Order o = list.get(position);

        String rawStatus = o.getStatus();
        holder.tvOrderId.setText(o.getId());
        holder.tvDate.setText(o.getDate());
        holder.tvPrice.setText(o.getItemCount() + " sản phẩm");
        holder.tvStatus.setText(getStatusText(rawStatus));
        // Đổi màu nền trạng thái
        if (rawStatus != null) {
            switch (rawStatus.toLowerCase()) {
                case "cancelled":
                case "hủy":
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_status_red);
                    holder.tvStatus.setTextColor(0xFFFFFFFF);
                    break;
                case "confirmed":
                case "đã xác nhận":
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_status_green);
                    holder.tvStatus.setTextColor(0xFFFFFFFF);
                    break;
                case "pending":
                case "chờ xác nhận":
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_status_yellow);
                    holder.tvStatus.setTextColor(0xFF222222);
                    break;
                default:
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_status_gray);
                    holder.tvStatus.setTextColor(0xFF222222);
            }
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_gray);
            holder.tvStatus.setTextColor(0xFF222222);
        }
        holder.tvArrivalDate.setText(formatPrice(o.getTotal()));
        holder.tvRatingBadge.setText(getStatusText(rawStatus));
        holder.tvItemCount.setText(o.getProductName() != null ? o.getProductName() : "Admin Shop");

        String imageUrl = o.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_image)
                    .error(R.drawable.bg_image)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_user);
        }

        // Click on item to view detail
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetail(o);
            }
        });
    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    private String getStatusText(String status) {
        if (status == null) return "Chờ xử lý";
        switch (status.toLowerCase()) {
            case "pending":
                return "MỚI";
            case "processing":
                return "CHUẨN BỊ";
            case "shipping":
                return "ĐANG GIAO";
            case "delivered":
                return "ĐÃ GIAO";
            case "cancelled":
                return "HỦY";
            default:
                return status;
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return 0xFFFF9800;
        switch (status.toLowerCase()) {
            case "pending":
                return 0xFF22C55E; // Green
            case "processing":
                return 0xFFFF9800; // Orange
            case "shipping":
                return 0xFF0A6ED8; // Blue
            case "delivered":
                return 0xFF22C55E; // Green
            case "cancelled":
                return 0xFFEF4444; // Red
            default:
                return 0xFF64748B;
        }
    }

    private String formatPrice(double price) {
        return String.format("%.0f", price) + "đ";
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvDate, tvStatus, tvPrice, tvArrivalDate, tvItemCount, tvRatingBadge;
        ImageView imgProduct;

        public ViewHolder(View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvArrivalDate = itemView.findViewById(R.id.tvArrivalDate);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            tvRatingBadge = itemView.findViewById(R.id.tvRatingBadge);
            imgProduct = itemView.findViewById(R.id.imgProduct);
        }
    }
}