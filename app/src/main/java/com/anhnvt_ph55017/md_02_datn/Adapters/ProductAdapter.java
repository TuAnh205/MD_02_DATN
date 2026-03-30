package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.DAO.CartDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.fragments.BottomSheetProductOptions;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.anhnvt_ph55017.md_02_datn.screens.DetailActivity;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

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

        // loading image bằng URL nếu có, else dùng resource local
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            ImageRequest imageRequest = new ImageRequest(
                    product.getImageUrl(),
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap response) {
                            holder.imgProduct.setImageBitmap(response);
                        }
                    },
                    0,
                    0,
                    ImageView.ScaleType.CENTER_CROP,
                    Bitmap.Config.RGB_565,
                    error -> {
                        if (product.getImage() != 0) {
                            holder.imgProduct.setImageResource(product.getImage());
                        } else {
                            holder.imgProduct.setImageResource(R.drawable.bg_image);
                        }
                    }
            );
            Volley.newRequestQueue(context).add(imageRequest);
        } else if (product.getImage() != 0) {
            holder.imgProduct.setImageResource(product.getImage());
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // rating
        holder.tvRating.setText(product.getRating() + " (" + product.getReviewCount() + ")");

        // favorite icon
        if(product.isFavorite()){
            holder.imgFavorite.setImageResource(R.drawable.heart_solid_full);
        }else{
            holder.imgFavorite.setImageResource(R.drawable.heart_regular_full);
        }

        // click favorite
        holder.imgFavorite.setOnClickListener(v -> {

            product.setFavorite(!product.isFavorite());
            notifyItemChanged(holder.getAdapterPosition());

        });

        // click item → mở detail
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

        // ADD TO CART - Show Bottom Sheet
        holder.btnAdd.setOnClickListener(v -> {
            // Show bottom sheet to select color, storage, and quantity
            if (context instanceof AppCompatActivity) {
                BottomSheetProductOptions bottomSheet = 
                    BottomSheetProductOptions.newInstance(product, selectedProduct -> {
                        // Callback: Add to cart with selected options
                        int userId = SessionManager.getUserId(context);
                        if (userId <= 0) {
                            userId = 1;  // Guest cart
                        }

                        CartDAO cartDAO = new CartDAO(context);
                        int productId = selectedProduct.getIntId();
                        if (productId >= 0) {
                            cartDAO.addToCart(userId, productId);
                        }
                        
                        // Show custom toast
                        showCustomToast(context, "Added to cart");
                    });
                
                bottomSheet.show(((AppCompatActivity) context).getSupportFragmentManager(), "product_options");
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // dùng cho search filter
    public void setData(List<Product> list){
        this.list = list;
        notifyDataSetChanged();
    }

    // Custom Toast helper
    private void showCustomToast(Context context, String message) {
        Toast toast = new Toast(context);
        
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast_layout, null);
        
        TextView tvMessage = layout.findViewById(R.id.tvToastMessage);
        tvMessage.setText(message);
        
        toast.setView(layout);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
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