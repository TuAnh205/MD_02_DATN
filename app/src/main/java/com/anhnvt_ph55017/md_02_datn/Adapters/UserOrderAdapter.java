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
import com.anhnvt_ph55017.md_02_datn.models.Order;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class UserOrderAdapter extends RecyclerView.Adapter<UserOrderAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    private final Context context;
    private final List<Order> orders;
    private final OnOrderClickListener listener;

    public UserOrderAdapter(Context context, List<Order> orders, OnOrderClickListener listener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_history_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.tvOrderId.setText(order.getOrderCode());
        holder.tvOrderDate.setText(order.getFormattedDate());
        holder.tvOrderCustomerName.setText(order.getCustomerName());
        holder.tvOrderSummary.setText(order.getItemSummary());
        holder.tvOrderTotal.setText(formatPrice(order.getTotal()));
        holder.tvOrderBadge.setText(getStatusLabel(order.getStatus()));
        holder.tvOrderBadge.setBackgroundResource(getBadgeBackground(order.getStatus()));
        bindProductImage(holder, order.getProductImageUrl());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders == null ? 0 : orders.size();
    }

    private void bindProductImage(ViewHolder holder, String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .centerCrop()
                    .into(holder.imgOrderProduct);
        } else {
            holder.imgOrderProduct.setImageResource(R.drawable.bg_image_placeholder);
        }
    }

    private String formatPrice(double value) {
        NumberFormat formatter = NumberFormat.getInstance(Locale.getDefault());
        return formatter.format(value) + "đ";
    }

    private String getStatusLabel(String status) {
        if (status == null) return "Chờ xác nhận";
        switch (status.toLowerCase()) {
            case "pending":
            case "processing":
            case "chờ xác nhận":
            case "cho xac nhan":
                return "Chờ xác nhận";
            case "confirmed":
            case "đã xác nhận":
            case "da xac nhan":
                return "Xác nhận";
            case "shipping":
            case "shipped":
            case "đang giao":
            case "dang giao":
                return "Đang giao";
            case "delivered":
            case "đã giao":
            case "da giao":
            case "đã nhận":
            case "da nhan":
                return "Đã giao";
            case "cancelled":
            case "đã hủy":
            case "da huy":
                return "Hủy đơn";
            default:
                return status;
        }
    }

    private int getBadgeBackground(String status) {
        if (status == null) return R.drawable.bg_badge_active;
        switch (status.toLowerCase()) {
            case "pending":
            case "processing":
            case "chờ xác nhận":
            case "cho xac nhan":
                return R.drawable.bg_badge_active;
            case "confirmed":
            case "đã xác nhận":
            case "da xac nhan":
                return R.drawable.bg_badge_active;
            case "shipping":
            case "shipped":
            case "đang giao":
            case "dang giao":
                return R.drawable.bg_badge_shipping;
            case "delivered":
            case "đã giao":
            case "da giao":
            case "đã nhận":
            case "da nhan":
                return R.drawable.bg_badge_delivered;
            case "cancelled":
            case "đã hủy":
            case "da huy":
                return R.drawable.bg_badge_cancelled;
            default:
                return R.drawable.bg_badge_active;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvOrderId;
        final TextView tvOrderDate;
        final TextView tvOrderCustomerName;
        final TextView tvOrderSummary;
        final TextView tvOrderTotal;
        final TextView tvOrderBadge;
        final ImageView imgOrderProduct;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tvOrderHistoryId);
            tvOrderDate = itemView.findViewById(R.id.tvOrderHistoryDate);
            tvOrderCustomerName = itemView.findViewById(R.id.tvOrderHistoryCustomerName);
            tvOrderSummary = itemView.findViewById(R.id.tvOrderHistorySummary);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderHistoryTotal);
            tvOrderBadge = itemView.findViewById(R.id.tvOrderHistoryBadge);
            imgOrderProduct = itemView.findViewById(R.id.imgOrderHistoryProduct);
        }
    }
}
