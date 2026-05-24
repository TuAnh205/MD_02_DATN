package com.anhnvt_ph55017.md_02_datn.screens;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Voucher;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VoucherListAdapter extends RecyclerView.Adapter<VoucherListAdapter.Holder> {

    public interface VoucherActionListener {
        void onEdit(Voucher voucher);
        void onDelete(Voucher voucher);
    }

    private final List<Voucher> list;
    private final VoucherActionListener listener;

    public VoucherListAdapter(List<Voucher> list, VoucherActionListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public VoucherListAdapter(List<Voucher> list) {
        this(list, new VoucherActionListener() {
            @Override
            public void onEdit(Voucher voucher) {
            }

            @Override
            public void onDelete(Voucher voucher) {
            }
        });
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher_simple, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Voucher voucher = list.get(position);

        String name = voucher.getName() != null && !voucher.getName().isEmpty() ? voucher.getName() : voucher.getCode();
        holder.tvName.setText(name != null ? name : "Voucher");
        holder.tvDesc.setText(voucher.getDescription() != null && !voucher.getDescription().isEmpty()
                ? voucher.getDescription()
                : "Không có mô tả");
        holder.tvCode.setText(voucher.getCode() != null ? voucher.getCode() : "");
        holder.tvDiscount.setText(formatDiscount(voucher));
        holder.tvDate.setText(formatDateRange(voucher));
        holder.tvUsage.setText(buildUsageText(voucher));

        String status = buildStatus(voucher);
        holder.tvStatus.setText(status);
        holder.tvStatus.setTextColor(getStatusColor(status));

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(voucher));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(voucher));
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    private String formatDiscount(Voucher voucher) {
        if (voucher == null) return "";

        String type = voucher.getType();
        if (type == null) type = "percentage";

        if (type.equalsIgnoreCase("percentage") || type.equalsIgnoreCase("percent")) {
            String display = formatDisplayValue(voucher.getValue()) + "%";
            if (voucher.getMaxDiscount() > 0) {
                display += " • Tối đa " + formatCurrency(voucher.getMaxDiscount());
            }
            return display;
        }

        String display = "Giảm " + formatCurrency(voucher.getValue());
        if (voucher.getMinOrderValue() > 0) {
            display += " • Áp dụng từ " + formatCurrency(voucher.getMinOrderValue());
        }
        return display;
    }

    private String formatDisplayValue(double value) {
        if (Math.abs(value - Math.round(value)) < 0.0001) {
            return String.format(Locale.getDefault(), "%.0f", value);
        }
        return String.format(Locale.getDefault(), "%.1f", value);
    }

    private String formatCurrency(double value) {
        return String.format(Locale.getDefault(), "%,.0f ₫", value);
    }

    private String formatDateRange(Voucher voucher) {
        String start = formatSingleDate(voucher.getStartDate());
        String end = formatSingleDate(voucher.getEndDate());
        if (start.isEmpty() && end.isEmpty()) return "Chưa có hạn dùng";
        if (start.isEmpty()) return "Đến " + end;
        if (end.isEmpty()) return "Từ " + start;
        return start + " - " + end;
    }

    private String buildUsageText(Voucher voucher) {
        int used = voucher.getUsedCount();
        int limit = voucher.getUsageLimit();
        if (limit <= 0) {
            return "Đã dùng " + used + " lượt";
        }
        return "Đã dùng " + used + "/" + limit + " lượt";
    }

    private String buildStatus(Voucher voucher) {
        if (!voucher.isActive()) {
            return "Tạm dừng";
        }

        Date end = parseDate(voucher.getEndDate());
        if (end != null && end.before(new Date())) {
            return "Hết hạn";
        }

        Date start = parseDate(voucher.getStartDate());
        if (start != null && start.after(new Date())) {
            return "Sắp mở";
        }

        return "Đang hoạt động";
    }

    private int getStatusColor(String status) {
        if ("Hết hạn".equals(status)) {
            return Color.parseColor("#E53935");
        }
        if ("Tạm dừng".equals(status)) {
            return Color.parseColor("#F59E0B");
        }
        if ("Sắp mở".equals(status)) {
            return Color.parseColor("#2563EB");
        }
        return Color.parseColor("#43A047");
    }

    private Date parseDate(String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd",
                "dd/MM/yyyy"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                sdf.setLenient(false);
                return sdf.parse(raw);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private String formatSingleDate(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        Date date = parseDate(raw);
        if (date == null) {
            return raw;
        }
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date);
    }

    public static class Holder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvCode, tvDiscount, tvDate, tvUsage, tvStatus;
        ImageView btnEdit, btnDelete;

        public Holder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvVoucherName);
            tvDesc = itemView.findViewById(R.id.tvVoucherDesc);
            tvCode = itemView.findViewById(R.id.tvVoucherCode);
            tvDiscount = itemView.findViewById(R.id.tvVoucherDiscount);
            tvDate = itemView.findViewById(R.id.tvVoucherDate);
            tvUsage = itemView.findViewById(R.id.tvVoucherUsage);
            tvStatus = itemView.findViewById(R.id.tvVoucherStatus);
            btnEdit = itemView.findViewById(R.id.btnEditVoucher);
            btnDelete = itemView.findViewById(R.id.btnDeleteVoucher);
        }
    }
}
