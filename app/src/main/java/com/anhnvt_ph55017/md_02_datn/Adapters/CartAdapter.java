package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;

import java.util.List;


public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    Context context;
    List<Product> list;

    public CartAdapter(Context context, List<Product> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_cart, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Product p = list.get(position);

        holder.tvName.setText(p.getName());
        holder.tvPrice.setText("$" + p.getPrice());
        holder.img.setImageResource(p.getImage());

        holder.btnPlus.setOnClickListener(v -> {

            int qty = Integer.parseInt(holder.tvQty.getText().toString());
            qty++;
            holder.tvQty.setText(String.valueOf(qty));

        });

        holder.btnMinus.setOnClickListener(v -> {

            int qty = Integer.parseInt(holder.tvQty.getText().toString());

            if (qty > 1) {
                qty--;
                holder.tvQty.setText(String.valueOf(qty));
            }

        });

        holder.tvRemove.setOnClickListener(v -> {

            list.remove(position);
            notifyDataSetChanged();

        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        TextView tvName, tvPrice, tvQty, tvRemove;
        Button btnPlus, btnMinus;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            img = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQty = itemView.findViewById(R.id.tvQty);
            tvRemove = itemView.findViewById(R.id.tvRemove);

            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
        }
    }
}


