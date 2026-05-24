package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Order;
import com.anhnvt_ph55017.md_02_datn.utils.NetworkConstants;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public static final String ACTION_ORDER_STATUS_CHANGED = "com.anhnvt_ph55017.md_02_datn.ACTION_ORDER_STATUS_CHANGED";

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public interface OnOrderStatusChangeListener {
        void onOrderStatusChanged();
    }

    private final Context context;
    private final List<Order> orders;
    private final OnOrderClickListener listener;
    private final OnOrderStatusChangeListener statusChangeListener;

    // Danh sách trạng thái hiển thị
    private static final String[] STATUS_LABELS = {
            "Chờ xác nhận", "Xác nhận", "Đang giao", "Đã giao", "Hủy đơn"
    };
    private static final String[] STATUS_VALUES = {
            "pending", "confirmed", "shipped", "delivered", "cancelled"
    };

    public OrderAdapter(Context context, List<Order> orders, OnOrderClickListener listener) {
        this(context, orders, listener, null);
    }

    public OrderAdapter(Context context, List<Order> orders, OnOrderClickListener listener, OnOrderStatusChangeListener statusChangeListener) {
        this.context = context;
        this.orders = orders;
        this.listener = listener;
        this.statusChangeListener = statusChangeListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shop_order_manage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);

        // Mã đơn
        holder.tvOrderId.setText(order.getOrderCode());

        // Ngày tạo
        holder.tvOrderDate.setText(order.getFormattedDate());

        // Badge trạng thái
        String status = order.getStatus();
        holder.tvOrderBadge.setText(getBadgeLabel(status));
        holder.tvOrderBadge.setBackgroundResource(getBadgeBackground(status));

        // Tên khách hàng
        holder.tvCustomerName.setText(order.getCustomerName());

        // Tóm tắt sản phẩm
        holder.tvOrderSummary.setText(order.getItemSummary());

        // Tổng tiền
        holder.tvOrderTotal.setText(formatPrice(order.getTotal()));

        // Dropdown trạng thái
        holder.tvOrderStatus.setText(getStatusLabel(status));

        // Ảnh sản phẩm đầu tiên
        bindProductImage(holder, order.getProductImageUrl());

        // Click toàn bộ item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOrderClick(order);
        });

        // Click dropdown → hiện dialog chọn trạng thái
        holder.layoutStatusDropdown.setOnClickListener(v -> showStatusPicker(order, holder, position));
    }

    // ── Hiện dialog chọn trạng thái ──────────────────────────────────────────
    private void showStatusPicker(Order order, ViewHolder holder, int position) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
        builder.setTitle("Cập nhật trạng thái");
        builder.setItems(STATUS_LABELS, (dialog, which) -> {
            String newStatus = STATUS_VALUES[which];
            updateOrderStatus(order, newStatus, holder, position);
        });
        builder.show();
    }

    // ── Gọi API cập nhật trạng thái ──────────────────────────────────────────
    private void updateOrderStatus(Order order, String newStatus, ViewHolder holder, int position) {
        String token = SessionManager.getToken(context);
        String url   = NetworkConstants.getApiBaseUrl() + "/api/shop/orders/" + order.getId() + "/status";

        JSONObject body = new JSONObject();
        try { body.put("status", newStatus); } catch (Exception ignored) {}

        JsonObjectRequest req = new JsonObjectRequest(Request.Method.PUT, url, body,
                response -> {
                    String updatedStatus = newStatus;
                    JSONObject orderJson = response.optJSONObject("order");
                    if (orderJson != null) {
                        updatedStatus = orderJson.optString("status", newStatus);
                        String updatedImageUrl = extractImageUrlFromOrder(orderJson);
                        if (updatedImageUrl != null && !updatedImageUrl.isEmpty()) {
                            order.setProductImageUrl(updatedImageUrl);
                            bindProductImage(holder, updatedImageUrl);
                        }
                    }

                    order.setStatus(updatedStatus);
                    holder.tvOrderStatus.setText(getStatusLabel(updatedStatus));
                    holder.tvOrderBadge.setText(getBadgeLabel(updatedStatus));
                    holder.tvOrderBadge.setBackgroundResource(getBadgeBackground(updatedStatus));
                    notifyItemChanged(position);
                    notifyStatusChanged();
                    Toast.makeText(context, "Đã cập nhật trạng thái", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(context, "Lỗi cập nhật trạng thái", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + token);
                h.put("Content-Type", "application/json");
                return h;
            }
        };

        Volley.newRequestQueue(context).add(req);
    }

    private String extractImageUrlFromOrder(JSONObject orderJson) {
        if (orderJson == null) return null;
        if (orderJson.optJSONArray("items") == null || orderJson.optJSONArray("items").length() == 0) {
            return null;
        }

        JSONObject firstItem = orderJson.optJSONArray("items").optJSONObject(0);
        if (firstItem == null) return null;

        JSONObject product = firstItem.optJSONObject("product");
        if (product != null) {
            String imageUrl = product.optString("image", "");
            if (!imageUrl.isEmpty()) return imageUrl;
            if (product.optJSONArray("images") != null && product.optJSONArray("images").length() > 0) {
                return product.optJSONArray("images").optString(0, null);
            }
        }

        String itemImage = firstItem.optString("image", null);
        return itemImage != null && !itemImage.isEmpty() ? itemImage : null;
    }

    private void notifyStatusChanged() {
        if (statusChangeListener != null) {
            statusChangeListener.onOrderStatusChanged();
        }

        Intent intent = new Intent(ACTION_ORDER_STATUS_CHANGED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
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

    @Override
    public int getItemCount() {
        return orders == null ? 0 : orders.size();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
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
            default:           return "Chờ xác nhận";
        }
    }

    private String getBadgeLabel(String status) {
        if (status == null) return "CHỜ XÁC NHẬN";
        switch (status.toLowerCase()) {
            case "pending":
            case "processing":
            case "chờ xác nhận":
            case "cho xac nhan":
                return "CHỜ XÁC NHẬN";
            case "confirmed":
            case "đã xác nhận":
            case "da xac nhan":
                return "XÁC NHẬN";
            case "shipping":
            case "shipped":
            case "đang giao":
            case "dang giao":
                return "ĐANG GIAO";
            case "delivered":
            case "đã giao":
            case "da giao":
            case "đã nhận":
            case "da nhan":
                return "ĐÃ GIAO";
            case "cancelled":
            case "đã hủy":
            case "da huy":
                return "HỦY ĐƠN";
            default:           return "CHỜ XÁC NHẬN";
        }
    }

    private int getBadgeBackground(String status) {
        if (status == null) return R.drawable.bg_badge_new;
        switch (status.toLowerCase()) {
            case "delivered":
            case "đã nhận":
            case "da nhan":
                return R.drawable.bg_badge_delivered;
            case "cancelled":
            case "đã hủy":
            case "da huy":
                return R.drawable.bg_badge_cancelled;
            case "shipping":
            case "đang giao":
            case "dang giao":
                return R.drawable.bg_badge_shipping;
            default:           return R.drawable.bg_badge_new;
        }
    }

    private String formatPrice(double price) {
        java.text.NumberFormat fmt = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        return fmt.format((long) price) + "đ";
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView       tvOrderId, tvOrderDate, tvOrderBadge;
        TextView       tvCustomerName, tvOrderSummary, tvOrderTotal;
        TextView       tvOrderStatus;
        ImageView      imgOrderProduct;
        LinearLayout   layoutStatusDropdown;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId           = itemView.findViewById(R.id.tvOrderId);
            tvOrderDate         = itemView.findViewById(R.id.tvOrderDate);
            tvOrderBadge        = itemView.findViewById(R.id.tvOrderBadge);
            tvCustomerName      = itemView.findViewById(R.id.tvCustomerName);
            tvOrderSummary      = itemView.findViewById(R.id.tvOrderSummary);
            tvOrderTotal        = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderStatus       = itemView.findViewById(R.id.tvOrderStatus);
            imgOrderProduct     = itemView.findViewById(R.id.imgOrderProduct);
            layoutStatusDropdown = itemView.findViewById(R.id.layoutStatusDropdown);
        }
    }
}