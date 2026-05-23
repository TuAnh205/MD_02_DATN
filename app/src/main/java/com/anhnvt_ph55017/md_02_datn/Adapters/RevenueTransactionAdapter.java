package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.RevenueTransaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RevenueTransactionAdapter extends RecyclerView.Adapter<RevenueTransactionAdapter.ViewHolder> {

    private final Context context;
    private final List<RevenueTransaction> transactions;

    public RevenueTransactionAdapter(Context context, List<RevenueTransaction> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_revenue_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        RevenueTransaction transaction = transactions.get(position);

        holder.tvRevenueProductName.setText(transaction.getProductName());
        holder.tvRevenueOrderInfo.setText(String.format("%s • %s",
                transaction.getOrderNumber(), formatDate(transaction.getPaidAt())));
        holder.tvRevenueProductMeta.setText(String.format("SKU %s · %d sản phẩm",
                transaction.getSku() == null || transaction.getSku().isEmpty() ? "-" : transaction.getSku(), transaction.getQuantity()));
        holder.tvRevenueProductAmount.setText(formatPrice(transaction.getNetAmount()));
        holder.tvRevenueProductSubtitle.setText(String.format("Thu về %s · Phí %s", formatPrice(transaction.getNetAmount()), formatPrice(transaction.getPlatformFee())));
        holder.tvRevenueTransactionStatus.setText(getStatusLabel(transaction.getStatus()));

        if (transaction.getProductImage() != null && !transaction.getProductImage().isEmpty()) {
            Glide.with(context)
                    .load(transaction.getProductImage())
                    .placeholder(R.drawable.bg_image_placeholder)
                    .error(R.drawable.bg_image_placeholder)
                    .centerCrop()
                    .into(holder.imgRevenueProduct);
        } else {
            holder.imgRevenueProduct.setImageResource(R.drawable.bg_image_placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return transactions == null ? 0 : transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgRevenueProduct;
        TextView tvRevenueOrderInfo;
        TextView tvRevenueProductName;
        TextView tvRevenueProductMeta;
        TextView tvRevenueProductAmount;
        TextView tvRevenueProductSubtitle;
        TextView tvRevenueTransactionStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            imgRevenueProduct = itemView.findViewById(R.id.imgRevenueProduct);
            tvRevenueOrderInfo = itemView.findViewById(R.id.tvRevenueOrderInfo);
            tvRevenueProductName = itemView.findViewById(R.id.tvRevenueProductName);
            tvRevenueProductMeta = itemView.findViewById(R.id.tvRevenueProductMeta);
            tvRevenueProductAmount = itemView.findViewById(R.id.tvRevenueProductAmount);
            tvRevenueProductSubtitle = itemView.findViewById(R.id.tvRevenueProductSubtitle);
            tvRevenueTransactionStatus = itemView.findViewById(R.id.tvRevenueTransactionStatus);
        }
    }

    private String getStatusLabel(String status) {
        if (status == null || status.isEmpty()) {
            return "Đang xử lý";
        }
        switch (status.toLowerCase()) {
            case "delivered":
            case "đã nhận":
            case "da nhan":
            case "paid":
            case "charged":
                return "Thành công";
            case "cancelled":
            case "đã hủy":
            case "da huy":
                return "Hủy";
            default:
                return "Đang xử lý";
        }
    }

    private String formatPrice(double price) {
        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format((long) price) + "đ";
    }

    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isEmpty()) {
            return "-";
        }
        try {
            SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date date = iso.parse(isoDate);
            SimpleDateFormat out = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
            return date != null ? out.format(date) : "-";
        } catch (Exception e) {
            return isoDate;
        }
    }
}
