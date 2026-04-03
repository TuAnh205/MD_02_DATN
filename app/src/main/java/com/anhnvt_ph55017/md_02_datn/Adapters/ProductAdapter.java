
package com.anhnvt_ph55017.md_02_datn.Adapters;
import com.anhnvt_ph55017.md_02_datn.utils.FavoriteApiService;
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
import com.anhnvt_ph55017.md_02_datn.utils.CartApiService;
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
            boolean newState = !product.isFavorite();
            product.setFavorite(newState);
            notifyItemChanged(holder.getAdapterPosition());

            // Gọi API thêm/xóa yêu thích
            String token = SessionManager.getToken(context);
            if (newState) {
                // Thêm vào danh sách yêu thích
                new Thread(() -> {
                    try {
                        java.net.URL url = new java.net.URL("http://10.0.2.2:5000/api/favorites");
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Authorization", "Bearer " + token);
                        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        conn.setDoOutput(true);
                        String body = "{\"productId\":\"" + product.getId() + "\"}";
                        conn.getOutputStream().write(body.getBytes("UTF-8"));
                        conn.getResponseCode(); // Đọc để gửi request
                        conn.disconnect();
                    } catch (Exception ignored) {}
                }).start();
            } else {
                // Xóa khỏi danh sách yêu thích
                new Thread(() -> {
                    try {
                        java.net.URL url = new java.net.URL("http://10.0.2.2:5000/api/favorites/" + product.getId());
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("DELETE");
                        conn.setRequestProperty("Authorization", "Bearer " + token);
                        conn.getResponseCode();
                        conn.disconnect();
                    } catch (Exception ignored) {}
                }).start();
            }
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
                // Mở bottom sheet chọn số lượng, sau đó gọi API addToCart
                BottomSheetProductOptions sheet =
                        BottomSheetProductOptions.newInstance(product, selectedProduct -> {
                            String token = SessionManager.getToken(context);
                            if (token == null || token.isEmpty()) {
                                Toast.makeText(context, "Bạn cần đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            CartApiService.addToCart(context, token, selectedProduct.getId(), selectedProduct.getQty(), new com.anhnvt_ph55017.md_02_datn.utils.CartApiService.CartCallback() {
                                @Override
                                public void onSuccess(org.json.JSONObject cartJson) {
                                    if (context instanceof AppCompatActivity) {
                                        ((AppCompatActivity) context).runOnUiThread(() ->
                                                Toast.makeText(context, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show()
                                        );
                                    }
                                }
                                @Override
                                public void onError(String error) {
                                    if (context instanceof AppCompatActivity) {
                                        ((AppCompatActivity) context).runOnUiThread(() ->
                                                Toast.makeText(context, "Lỗi thêm vào giỏ hàng: " + error, Toast.LENGTH_SHORT).show()
                                        );
                                    }
                                }
                            });
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