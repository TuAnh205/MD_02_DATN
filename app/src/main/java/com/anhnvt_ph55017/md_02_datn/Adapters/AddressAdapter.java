package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Address;

import java.util.List;

public class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolder> {

    public interface Listener {
        void onSelect(Address address);

        void onEdit(Address address);

        void onDelete(Address address);
    }

    private final Context context;
    private final List<Address> list;
    private final Listener listener;

    public AddressAdapter(Context context, List<Address> list, Listener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_address, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Address a = list.get(position);

        holder.tvName.setText(a.getName());
        holder.tvPhone.setText(a.getPhone());
        holder.tvAddress.setText(a.getAddress());
        holder.rbDefault.setChecked(a.isDefault());
        holder.itemView.setBackgroundColor(a.isDefault()
                ? Color.parseColor("#2F80FF")
                : Color.parseColor("#162233"));

        holder.rbDefault.setOnClickListener(v -> {
            if (!a.isDefault()) {
                listener.onSelect(a);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (!a.isDefault()) listener.onSelect(a);
        });

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(a));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(a));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RadioButton rbDefault;
        TextView tvName, tvPhone, tvAddress;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rbDefault = itemView.findViewById(R.id.rbDefault);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            btnEdit.setImageResource(android.R.drawable.ic_menu_edit);
            btnDelete.setImageResource(android.R.drawable.ic_menu_delete);
        }
    }
}