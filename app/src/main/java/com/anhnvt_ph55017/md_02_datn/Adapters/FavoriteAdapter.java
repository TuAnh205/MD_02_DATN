package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.anhnvt_ph55017.md_02_datn.utils.CartApiService;
import com.anhnvt_ph55017.md_02_datn.utils.FavoriteApiService;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    Context context;
    List<Product> list;
    OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public FavoriteAdapter(Context context, List<Product> list, OnProductClickListener listener) {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Product p = list.get(position);

        holder.tvName.setText(p.getName());
        holder.tvPrice.setText("$" + p.getPrice());

        Glide.with(context)
                .load(p.getImageUrl())
                .into(holder.imgProduct);

        // ❌ Xóa khỏi yêu thích
        holder.imgDelete.setOnClickListener(v -> {
            String token = SessionManager.getToken(context);

            FavoriteApiService.removeFavorite(context, token, p.getId());

            list.remove(position);
            notifyItemRemoved(position);

            Toast.makeText(context, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
        });

        // 👉 Click item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(p);
        });

        // 🛒 Thêm vào giỏ hàng (hiện bottom sheet chọn số lượng)
        holder.imgAddCart.setOnClickListener(v -> {
            if (context instanceof androidx.appcompat.app.AppCompatActivity) {
                com.anhnvt_ph55017.md_02_datn.fragments.BottomSheetProductOptions sheet =
                        com.anhnvt_ph55017.md_02_datn.fragments.BottomSheetProductOptions.newInstance(p, (selectedProduct, qty) -> {
                            String token = SessionManager.getToken(context);
                            if (token == null || token.isEmpty()) {
                                Toast.makeText(context, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            CartApiService.addToCart(context, token, selectedProduct.getId(), qty, new CartApiService.CartCallback() {
                                @Override
                                public void onSuccess(JSONObject cartJson) {
                                    ((Activity) context).runOnUiThread(() ->
                                            Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                                    );
                                }
                                @Override
                                public void onError(String error) {
                                    ((Activity) context).runOnUiThread(() ->
                                            Toast.makeText(context, "Lỗi: " + error, Toast.LENGTH_SHORT).show()
                                    );
                                }
                            });
                        });
                sheet.show(((androidx.appcompat.app.AppCompatActivity) context).getSupportFragmentManager(), "BottomSheetProductOptions");
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct, imgDelete, imgAddCart;
        TextView tvName, tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProduct = itemView.findViewById(R.id.imgProduct);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            imgAddCart = itemView.findViewById(R.id.imgAddCart);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}