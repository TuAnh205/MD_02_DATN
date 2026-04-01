
package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
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
    Runnable updateTotal;
    CartActionListener actionListener;

    public interface CartActionListener {
        void onUpdateQuantity(Product product, int newQty);
        void onRemove(Product product);
    }

    public CartAdapter(Context context, List<Product> list, Runnable updateTotal, CartActionListener actionListener) {
        this.context = context;
        this.list = list;
        this.updateTotal = updateTotal;
        this.actionListener = actionListener;
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
        // Đổi ký hiệu giá sang đ
        holder.tvPrice.setText(String.format("%,.0f đ", p.getPrice()));

        // Hiển thị ảnh: nếu có imageUrl thì load bằng Glide, không thì dùng resource
        if (p.getImageUrl() != null && !p.getImageUrl().isEmpty()) {
            try {
                android.util.Log.d("IMG_URL", p.getImageUrl());
                com.bumptech.glide.Glide.with(context)
                        .load(p.getImageUrl())
                        .placeholder(R.drawable.bg_image)
                        .error(R.drawable.bg_image)
                        .centerCrop()
                        .into(holder.img);
            } catch (Exception e) {
                holder.img.setImageResource(R.drawable.bg_image);
            }
        } else {
            holder.img.setImageResource(p.getImage());
        }



        holder.tvQty.setText(String.valueOf(p.getQty()));
        // compute and display line total
        holder.tvLineTotal.setText("Tổng: " + String.format("%,.0f đ", p.getPrice() * p.getQty()));

        holder.btnPlus.setOnClickListener(v -> {
            int newQty = p.getQty() + 1;
            if (actionListener != null) actionListener.onUpdateQuantity(p, newQty);
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (p.getQty() > 1 && actionListener != null) {
                actionListener.onUpdateQuantity(p, p.getQty() - 1);
            }
        });

        holder.tvRemove.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onRemove(p);
        });
        holder.cbItem.setChecked(p.isSelected());

        holder.cbItem.setOnCheckedChangeListener((buttonView, isChecked) -> {

            p.setSelected(isChecked);

            if(updateTotal != null){
                updateTotal.run();
            }

        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img;
        TextView tvName, tvPrice, tvLineTotal, tvQty, tvRemove;
        Button btnPlus, btnMinus;
        CheckBox cbItem;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);

            img = itemView.findViewById(R.id.imgProduct);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvLineTotal = itemView.findViewById(R.id.tvLineTotal);
            tvQty = itemView.findViewById(R.id.tvQty);
            tvRemove = itemView.findViewById(R.id.tvRemove);
            cbItem = itemView.findViewById(R.id.cbItem);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
        }
    }
    public void selectAll(boolean check){

        for(Product p : list){
            p.setSelected(check);
        }

        notifyDataSetChanged();
    }
}


