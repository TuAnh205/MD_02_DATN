package com.anhnvt_ph55017.md_02_datn.screens;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Voucher;

import java.util.List;

public class VoucherListAdapter extends RecyclerView.Adapter<VoucherListAdapter.Holder> {

    private List<Voucher> list;

    public VoucherListAdapter(List<Voucher> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher_simple, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Voucher v = list.get(position);
        holder.tvName.setText(v.getName() != null ? v.getName() : v.getCode());
        holder.tvDesc.setText(v.getDescription() != null ? v.getDescription() : "");
        holder.tvCode.setText(v.getCode() != null ? v.getCode() : "");
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvCode;

        public Holder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvVoucherName);
            tvDesc = itemView.findViewById(R.id.tvVoucherDesc);
            tvCode = itemView.findViewById(R.id.tvVoucherCode);
        }
    }
}
