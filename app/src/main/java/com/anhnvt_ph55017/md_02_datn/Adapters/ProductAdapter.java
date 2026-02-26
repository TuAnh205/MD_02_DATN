package com.anhnvt_ph55017.md_02_datn.Adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    List<Product> list;

    public ProductAdapter(List<Product> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = list.get(position);

        holder.tvName.setText(product.getName());
        holder.tvPrice.setText("$" + product.getPrice());
        holder.imgProduct.setImageResource(product.getImage());
        // ❤️ set icon theo trạng thái
        holder.imgFavorite.setImageResource(
                product.isFavorite()
                        ? R.drawable.heart_solid_full
                        : R.drawable.heart_regular_full
        );

        // ❤️ CLICK TIM
        holder.imgFavorite.setOnClickListener(v -> {
            product.setFavorite(!product.isFavorite());

            holder.imgFavorite.setImageResource(
                    product.isFavorite()
                            ? R.drawable.heart_solid_full
                            : R.drawable.heart_regular_full
            );
        });
// ⭐ demo rating cứng (sau này lấy từ DB)
        holder.tvRating.setText("4.8 (210)");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct, imgFavorite;
        TextView tvName, tvPrice, tvRating;
        ImageButton btnAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnAdd = itemView.findViewById(R.id.btnAdd);
        }
    }
}
