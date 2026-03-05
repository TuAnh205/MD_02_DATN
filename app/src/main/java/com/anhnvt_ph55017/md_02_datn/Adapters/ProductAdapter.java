package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.content.Context;
import android.content.Intent;
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
import com.anhnvt_ph55017.md_02_datn.screens.DetailActivity;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    List<Product> list;
    Context context;

    public ProductAdapter(Context context, List<Product> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Product product = list.get(position);

        holder.tvName.setText(product.getName());
        holder.tvPrice.setText("$" + product.getPrice());
        holder.imgProduct.setImageResource(product.getImage());

        // ⭐ rating
        holder.tvRating.setText(product.getRating() + " (" + product.getReviewCount() + ")");

        // ❤️ favorite icon
        if(product.isFavorite()){
            holder.imgFavorite.setImageResource(R.drawable.heart_solid_full);
        }else{
            holder.imgFavorite.setImageResource(R.drawable.heart_regular_full);
        }

        // ❤️ click tim
        holder.imgFavorite.setOnClickListener(v -> {

            product.setFavorite(!product.isFavorite());
            notifyItemChanged(holder.getAdapterPosition());

        });

        // 🔥 click item → mở detail
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, DetailActivity.class);

            intent.putExtra("id", product.getId());
            intent.putExtra("name", product.getName());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("image", product.getImage());
            intent.putExtra("desc", product.getDescription());
            intent.putExtra("rating", product.getRating());
            intent.putExtra("reviewCount", product.getReviewCount());

            context.startActivity(intent);

        });
    }
    public void setData(List<Product> list){
        this.list = list;
        notifyDataSetChanged();
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