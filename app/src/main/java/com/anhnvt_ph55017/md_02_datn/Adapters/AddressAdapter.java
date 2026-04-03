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

    private int selectedPosition = RecyclerView.NO_POSITION;

    public void setSelectedAddress(Address address) {
        int oldPosition = selectedPosition;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(address.getId())) {
                selectedPosition = i;
                break;
            }
        }
        notifyItemChanged(oldPosition);
        notifyItemChanged(selectedPosition);
    }

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
        // Mặc định chọn địa chỉ mặc định đầu tiên nếu có
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).isDefault()) {
                selectedPosition = i;
                break;
            }
        }
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
        holder.rbDefault.setChecked(position == selectedPosition);

        // Set tag visibility and text
        if (a.isDefault()) {
            holder.tvTag.setVisibility(View.VISIBLE);
            holder.tvTag.setText("MẶC ĐỊNH");
        } else {
            holder.tvTag.setVisibility(View.GONE);
        }

        holder.itemView.setBackgroundColor(position == selectedPosition
                ? Color.parseColor("#2F80FF")
                : Color.parseColor("#162233"));


        View.OnClickListener selectListener = v -> {
            int oldPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(oldPosition);
            notifyItemChanged(selectedPosition);
            listener.onSelect(a);
        };

        holder.rbDefault.setOnClickListener(selectListener);
        holder.itemView.setOnClickListener(selectListener);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(a));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(a));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        RadioButton rbDefault;
        TextView tvName, tvPhone, tvAddress, tvTag;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rbDefault = itemView.findViewById(R.id.rbDefault);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvTag = itemView.findViewById(R.id.tvTag);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            btnEdit.setImageResource(android.R.drawable.ic_menu_edit);
            btnDelete.setImageResource(android.R.drawable.ic_menu_delete);
        }
    }
}