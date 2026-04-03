package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
                .inflate(R.layout.item_order,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,int position){

        Order o = list.get(position);

        // ⚠️ LUÔN dùng status gốc từ backend (KHÔNG sửa)
        String rawStatus = o.getStatus();

        holder.tvOrderId.setText("Đơn #" + o.getId());
        holder.tvDate.setText("Đặt hàng vào " + o.getDate());
        holder.tvPrice.setText("$" + o.getTotal());
        holder.tvStatus.setText(getStatusVietnamese(rawStatus));
        holder.tvArrivalDate.setText("Dự kiến giao: " + o.getArrivalDate());
        holder.tvItemCount.setText(o.getItemCount() + " sản phẩm");

        // ⭐ Rating
        if (o.getRating() > 0) {
            holder.tvRatingBadge.setText("⭐ " + o.getRating() + " | ✅ Đã đánh giá");
            holder.tvRatingBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvRatingBadge.setVisibility(View.GONE);
        }

        // 🎨 Set màu đúng theo STATUS GỐC
        setStatusColor(holder.tvStatus, rawStatus);

        // 🖼️ Load ảnh
        String imageUrl = o.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_image)
                    .error(R.drawable.bg_image)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.bg_image);
        }

        // 👉 Click detail
        holder.btnDetail.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetail(o);
            } else {
                Intent intent = new Intent(context, OrderDetailActivity.class);
                intent.putExtra("orderId", o.getId());
                intent.putExtra("orderDate", o.getDate());
                intent.putExtra("orderTotal", o.getTotal());
                intent.putExtra("orderStatus", rawStatus); // ⚠️ vẫn là EN
                intent.putExtra("arrivalDate", o.getArrivalDate());
                intent.putExtra("shippingAddress", o.getShippingAddress());
                intent.putExtra("itemCount", o.getItemCount());
                // Luôn truyền đủ 3 trường address, district, city (nếu không có thì truyền rỗng)
                String address = null, district = null, city = null;
                try {
                    java.lang.reflect.Method getAddress = o.getClass().getMethod("getAddress");
                    address = (String) getAddress.invoke(o);
                } catch (Exception ignored) {}
                try {
                    java.lang.reflect.Method getDistrict = o.getClass().getMethod("getDistrict");
                    district = (String) getDistrict.invoke(o);
                } catch (Exception ignored) {}
                try {
                    java.lang.reflect.Method getCity = o.getClass().getMethod("getCity");
                    city = (String) getCity.invoke(o);
                } catch (Exception ignored) {}
                intent.putExtra("address", address != null ? address : "");
                intent.putExtra("district", district != null ? district : "");
                intent.putExtra("city", city != null ? city : "");
                if (o.getItems() != null && o.getItems() instanceof java.io.Serializable) {
                    intent.putExtra("orderItems", (java.io.Serializable) o.getItems());
                }
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    // 🎨 SET MÀU (CHỈ DÙNG TIẾNG ANH)
    private void setStatusColor(TextView tvStatus, String status) {

        if (status == null) return;

        switch (status) {
            case "pending":
                tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_light, null));
                break;

            case "processing":
                tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark, null));
                break;

            case "shipping":
                tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_blue_light, null));
                break;

            case "delivered":
                tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_light, null));
                break;

            case "cancelled":
                tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_light, null));
                break;

            default:
                tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_light, null));
                break;
        }
    }

    // 🌍 HIỂN THỊ TIẾNG VIỆT (KHÔNG ẢNH HƯỞNG LOGIC)
    private String getStatusVietnamese(String status) {

        if (status == null) return "";

        switch (status) {
            case "pending":
                return "Chưa thanh toán";
            case "confirmed":
                return "Đã xác nhận";
            case "processing":
                return "Đang xử lý";

            case "shipping":
                return "Đang giao hàng";

            case "delivered":
                return "Đã nhận";

            case "cancelled":
                return "Đã hủy";

            default:
                return status;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView tvOrderId, tvDate, tvPrice, tvStatus, tvArrivalDate, tvItemCount, tvRatingBadge;
        Button btnDetail;
        ImageView imgProduct;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvArrivalDate = itemView.findViewById(R.id.tvArrivalDate);
            tvItemCount = itemView.findViewById(R.id.tvItemCount);
            tvRatingBadge = itemView.findViewById(R.id.tvRatingBadge);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            imgProduct = itemView.findViewById(R.id.imgProduct);
        }
    }
}