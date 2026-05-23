package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Customer;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder> {
    private List<Customer> customers;
    private OnCustomerClickListener listener;

    public interface OnCustomerClickListener {
        void onCustomerClick(Customer customer);
    }

    public CustomerAdapter(List<Customer> customers, OnCustomerClickListener listener) {
        this.customers = customers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_customer_v2, parent, false);
        return new CustomerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerViewHolder holder, int position) {
        Customer customer = customers.get(position);
        holder.tvAvatar.setText(customer.getInitials());
        holder.tvCustomerName.setText(customer.getName());
        holder.tvCustomerEmail.setText(customer.getEmail());
        holder.tvCustomerPhone.setText(customer.getPhone());
        holder.tvOrderCount.setText(customer.getOrderCount() + " đơn");
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCustomerClick(customer);
        });
    }

    @Override
    public int getItemCount() {
        return customers != null ? customers.size() : 0;
    }

    static class CustomerViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvCustomerName, tvCustomerEmail, tvCustomerPhone, tvOrderCount;
        public CustomerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerEmail = itemView.findViewById(R.id.tvCustomerEmail);
            tvCustomerPhone = itemView.findViewById(R.id.tvCustomerPhone);
            tvOrderCount = itemView.findViewById(R.id.tvOrderCount);
        }
    }
}
