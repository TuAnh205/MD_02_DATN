package com.anhnvt_ph55017.md_02_datn.screens;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.appcompat.widget.AppCompatButton;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Voucher;

import java.util.List;

public class VoucherAdapter extends RecyclerView.Adapter<VoucherAdapter.ViewHolder> {
    public interface OnVoucherClickListener {
        void onVoucherClick(Voucher voucher);
    }
    private List<Voucher> list;
    private OnVoucherClickListener listener;

    public VoucherAdapter(List<Voucher> list, OnVoucherClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_voucher, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Voucher v = list.get(position);
        holder.tvCode.setText(v.getCode());
        holder.tvName.setText(v.getName());
        holder.tvDesc.setText(v.getDescription());
        holder.tvType.setText(v.getType().equals("percentage") ? "Giảm %" : "Giảm tiền");
        holder.tvValue.setText(v.getType().equals("percentage") ? (v.getValue() + "%") : (v.getValue() + "đ"));

        // Hạn sử dụng
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat viewFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            Date end = isoFormat.parse(v.getEndDate());
            holder.tvExpire.setText("HSD: " + viewFormat.format(end));
        } catch (Exception e) {
            holder.tvExpire.setText("");
        }

        // Trạng thái
        holder.tvStatus.setVisibility(View.GONE);
        holder.btnUse.setEnabled(true);
        holder.btnUse.setText("Dùng ngay");
        holder.btnUse.setAlpha(1f);

        // Sắp hết hạn (còn <= 2 ngày)
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            Date now = new Date();
            Date end = isoFormat.parse(v.getEndDate());
            long diff = end.getTime() - now.getTime();
            long days = diff / (1000 * 60 * 60 * 24);
            if (days >= 0 && days <= 2) {
                holder.tvStatus.setText("Sắp hết hạn");
                holder.tvStatus.setVisibility(View.VISIBLE);
            }
            if (diff < 0) {
                holder.tvStatus.setText("Đã hết hạn");
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.btnUse.setEnabled(false);
                holder.btnUse.setAlpha(0.5f);
                holder.btnUse.setText("Hết hạn");
            }
        } catch (Exception ignored) {}

        // Nếu muốn kiểm tra hết lượt, thêm logic ở đây (dựa vào usageLimit, usedCount)
        // Ví dụ:
        // if (v.getUsageLimit() > 0 && v.getUsedCount() >= v.getUsageLimit()) {
        //     holder.tvStatus.setText("Hết lượt");
        //     holder.tvStatus.setVisibility(View.VISIBLE);
        //     holder.btnUse.setEnabled(false);
        //     holder.btnUse.setAlpha(0.5f);
        //     holder.btnUse.setText("Hết lượt");
        // }

        // Icon động nếu muốn (hoặc để mặc định)
        // holder.imgIcon.setImageResource(R.drawable.ic_voucher);

        holder.btnUse.setOnClickListener(view -> {
            if (listener != null && holder.btnUse.isEnabled()) listener.onVoucherClick(v);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCode, tvName, tvDesc, tvType, tvValue, tvExpire, tvStatus;
        ImageView imgIcon;
        AppCompatButton btnUse;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvName = itemView.findViewById(R.id.tvName);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvType = itemView.findViewById(R.id.tvType);
            tvValue = itemView.findViewById(R.id.tvValue);
            tvExpire = itemView.findViewById(R.id.tvExpire);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            btnUse = itemView.findViewById(R.id.btnUse);
        }
    }
}
