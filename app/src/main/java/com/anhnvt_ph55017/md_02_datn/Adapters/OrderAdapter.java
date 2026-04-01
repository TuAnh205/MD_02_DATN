package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Order;
import com.anhnvt_ph55017.md_02_datn.screens.OrderDetailActivity;

import org.jspecify.annotations.NonNull;

import java.util.List;

// Adapter hiển thị danh sách đơn hàng trong RecyclerView
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

    // Tạo ViewHolder mới khi cần
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_order,parent,false);

        return new ViewHolder(v);
    }

    // Gán dữ liệu đơn hàng vào các view của ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,int position){

        Order o = list.get(position);

        holder.tvOrderId.setText("Đơn #"+o.getId());
        holder.tvDate.setText("Đặt hàng vào " + o.getDate());
        holder.tvPrice.setText("$"+o.getTotal());
        holder.tvStatus.setText(getStatusVietnamese(o.getStatus()));
        holder.tvArrivalDate.setText("Dự kiến giao: " + o.getArrivalDate());
        holder.tvItemCount.setText(o.getItemCount() + " sản phẩm");

        // Show rating badge if order is reviewed
        if (o.getRating() > 0) {
            holder.tvRatingBadge.setText(String.format("⭐ %.1f | ✅ Đã đánh giá", o.getRating()));
            holder.tvRatingBadge.setVisibility(View.VISIBLE);
        } else {
            holder.tvRatingBadge.setVisibility(View.GONE);
        }

        // Đặt màu cho trạng thái đơn hàng
        setStatusColor(holder.tvStatus, o.getStatus());

        // Hiển thị ảnh sản phẩm: nếu có imageUrl thì load bằng Glide, không thì dùng resource mặc định
        if (holder.imgProduct != null) {
            String imageUrl = o.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                com.bumptech.glide.Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.bg_image)
                        .error(R.drawable.bg_image)
                        .into(holder.imgProduct);
            } else {
                holder.imgProduct.setImageResource(R.drawable.bg_image);
            }
        }

        // Sự kiện khi nhấn nút xem chi tiết đơn hàng
        holder.btnDetail.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDetail(o);
            } else {

                Intent intent = new Intent(context, OrderDetailActivity.class);
                intent.putExtra("orderId", o.getId());
                intent.putExtra("orderDate", o.getDate());
                intent.putExtra("orderTotal", o.getTotal());
                intent.putExtra("orderStatus", o.getStatus());
                intent.putExtra("arrivalDate", o.getArrivalDate());
                intent.putExtra("itemCount", o.getItemCount());
                intent.putExtra("imageRes", o.getImageRes());
                context.startActivity(intent);
            }
        });

    }

    // Trả về số lượng mục trong danh sách
    @Override
    public int getItemCount(){
        return list.size();
    }
    
    private void setStatusColor(TextView tvStatus, String status) {
        // set màu cho trạng thái
        if (status.equals("Chưa thanh toán")) {
            tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_light, null));
        } else if (status.equals("Đang xử lý")) {
            tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark, null));
        } else if (status.equals("Đang giao hàng")) {
            tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_blue_light, null));
        } else if (status.equals("Đã nhận")) {
            tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_light, null));
        } else if (status.equals("Đã hủy")) {
            tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_light, null));
        }
    }
    
    private String getStatusVietnamese(String status) {

        return status;

    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView tvOrderId, tvDate, tvPrice, tvStatus, tvArrivalDate, tvItemCount, tvRatingBadge;
        Button btnDetail;
        android.widget.ImageView imgProduct;

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