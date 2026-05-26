package com.anhnvt_ph55017.md_02_datn.screens;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Voucher;

import java.util.List;

public class VoucherClaimAdapter extends RecyclerView.Adapter<VoucherClaimAdapter.Holder> {

    public interface OnClaimListener {
        void onClaim(Voucher voucher);
    }

    private List<Voucher> list;
    private OnClaimListener listener;
    private java.util.Set<String> claimedCodes = new java.util.HashSet<>();

    public VoucherClaimAdapter(List<Voucher> list, OnClaimListener listener) {
        this.list = list;
        this.listener = listener;
    }

    public void setClaimedCodes(java.util.Set<String> codes) {
        this.claimedCodes = codes != null ? codes : new java.util.HashSet<>();
        notifyDataSetChanged();
    }

    public void addClaimedCode(String code) {
        if (code == null) return;
        this.claimedCodes.add(code.toUpperCase());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voucher_claim, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Voucher v = list.get(position);
        holder.tvName.setText(v.getName() != null ? v.getName() : v.getCode());
        holder.tvDesc.setText(v.getDescription() != null ? v.getDescription() : "");
        holder.tvCode.setText(v.getCode() != null ? v.getCode() : "");
        boolean isClaimed = v.getCode() != null && claimedCodes.contains(v.getCode().toUpperCase());
        if (isClaimed) {
            holder.btnClaim.setText("Đã nhận");
            holder.btnClaim.setEnabled(false);
            holder.btnClaim.setAlpha(0.5f);
            holder.btnClaim.setOnClickListener(null);
        } else {
            holder.btnClaim.setText("NHẬN");
            holder.btnClaim.setEnabled(true);
            holder.btnClaim.setAlpha(1f);
            holder.btnClaim.setOnClickListener(view -> {
                if (listener != null) listener.onClaim(v);
            });
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvCode;
        AppCompatButton btnClaim;

        public Holder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvVoucherName);
            tvDesc = itemView.findViewById(R.id.tvVoucherDesc);
            tvCode = itemView.findViewById(R.id.tvVoucherCode);
            btnClaim = itemView.findViewById(R.id.btnClaim);
        }
    }
}
