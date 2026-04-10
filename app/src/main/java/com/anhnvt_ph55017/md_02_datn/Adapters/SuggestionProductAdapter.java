
package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.bumptech.glide.Glide;

import java.util.List;

public class SuggestionProductAdapter extends RecyclerView.Adapter<SuggestionProductAdapter.ViewHolder> {
    private Context context;
    private List<Product> productList;
    private OnProductListener listener;

    public interface OnProductListener {
        void onAddToCart(Product product);
        void onFavorite(Product product);
        void onProductClick(Product product);
    }
    public void setData(List<Product> list) {
        this.productList = list;
        notifyDataSetChanged();
    }
    public SuggestionProductAdapter(Context context, List<Product> productList, OnProductListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_suggestion_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvName.setText(product.getName());
        holder.tvDesc.setText(product.getDescription());
        // Định dạng giá tiền kiểu 39.990.000 đ
        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        String priceStr = formatter.format(product.getPrice()) + " đ";
        holder.tvPrice.setText(priceStr);
        // Tags demo, bạn có thể lấy từ product nếu có

        // Load ảnh
        Glide.with(context).load(product.getImageUrl()).placeholder(R.drawable.ic_launcher_background).into(holder.imgProduct);

        // Trái tim: đổi màu theo trạng thái yêu thích
        if (product.isFavorite()) {
            holder.imgFavorite.setImageResource(R.drawable.heart_solid_full);
        } else {
            holder.imgFavorite.setImageResource(R.drawable.heart_regular_full);
        }

        holder.imgFavorite.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFavorite(product);
                // Đảm bảo đổi màu ngay cả khi object khác reference
                notifyItemChanged(holder.getAdapterPosition());
            }
        });

        // Nút thêm giỏ hàng
        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCart(product);
        });

        // Click vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onProductClick(product);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, imgFavorite;
        TextView tvName, tvDesc, tvPrice;
        ImageButton btnAddToCart;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
            tvName = itemView.findViewById(R.id.tvName);
            tvDesc = itemView.findViewById(R.id.tvDesc);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }
}
