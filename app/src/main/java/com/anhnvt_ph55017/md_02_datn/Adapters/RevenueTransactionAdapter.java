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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
            Date parsedDate = parseDisplayDate(isoDate.trim());
            if (parsedDate == null) {
                return isoDate;
            }
            SimpleDateFormat out = new SimpleDateFormat("dd/MM HH:mm", new Locale("vi", "VN"));
            out.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
            return out.format(parsedDate);
        } catch (Exception e) {
            return isoDate;
        }
    }

    private Date parseDisplayDate(String rawDate) throws ParseException {
        String[] offsetPatterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ssX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        };
        String[] localPatterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd HH:mm"
        };

        boolean hasExplicitOffset = rawDate.matches(".*[Zz]|.*[+-]\\d{2}:?\\d{2}$");
        SimpleDateFormat formatter = new SimpleDateFormat();

        if (hasExplicitOffset) {
            for (String pattern : offsetPatterns) {
                try {
                    formatter = new SimpleDateFormat(pattern, Locale.US);
                    formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                    return formatter.parse(rawDate);
                } catch (ParseException ignored) {
                }
            }
        }

        for (String pattern : localPatterns) {
            try {
                formatter = new SimpleDateFormat(pattern, Locale.US);
                formatter.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
                return formatter.parse(rawDate);
            } catch (ParseException ignored) {
            }
        }

        return null;
    }
}
