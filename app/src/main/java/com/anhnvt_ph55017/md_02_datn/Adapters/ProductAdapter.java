package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.DAO.CartDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.fragments.BottomSheetProductOptions;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.anhnvt_ph55017.md_02_datn.screens.DetailActivity;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;
import com.bumptech.glide.Glide;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private List<Product> list;
    private Context context;

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

        // ===== TEXT =====
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText("$" + product.getPrice());
        holder.tvRating.setText(product.getRating() + " (" + product.getReviewCount() + ")");

        // ===== IMAGE (GLIDE) =====
        String imageUrl = product.getImageUrl();

        if (imageUrl != null && !imageUrl.isEmpty()) {

            android.util.Log.d("IMG_URL", imageUrl);

            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.bg_image)
                    .error(R.drawable.bg_image)
                    .centerCrop()
                    .into(holder.imgProduct);

        } else {
            holder.imgProduct.setImageResource(R.drawable.bg_image);
        }

        // ===== FAVORITE =====
        if (product.isFavorite()) {
            holder.imgFavorite.setImageResource(R.drawable.heart_solid_full);
        } else {
            holder.imgFavorite.setImageResource(R.drawable.heart_regular_full);
        }

        holder.imgFavorite.setOnClickListener(v -> {
            product.setFavorite(!product.isFavorite());
            notifyItemChanged(holder.getAdapterPosition());
        });

        // ===== CLICK ITEM → DETAIL =====
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("id", product.getId());
            intent.putExtra("name", product.getName());
            intent.putExtra("price", product.getPrice());
            intent.putExtra("imageUrl", product.getImageUrl());
            intent.putExtra("desc", product.getDescription());
            intent.putExtra("rating", product.getRating());
            intent.putExtra("reviewCount", product.getReviewCount());

            context.startActivity(intent);
        });

        // ===== ADD TO CART =====
        holder.btnAdd.setOnClickListener(v -> {

            if (context instanceof AppCompatActivity) {

                BottomSheetProductOptions sheet =
                        BottomSheetProductOptions.newInstance(product, selectedProduct -> {

                            int userId = SessionManager.getUserId(context);
                            if (userId <= 0) userId = 1;

                            CartDAO cartDAO = new CartDAO(context);
                            cartDAO.addToCart(userId, selectedProduct.getIntId());

                            Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show();
                        });

                sheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "product_options");
            }
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public void setData(List<Product> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    // ===== VIEW HOLDER =====
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