package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.bumptech.glide.Glide;

import java.util.List;

public class ShopProductAdapter extends RecyclerView.Adapter<ShopProductAdapter.ViewHolder> {

    private final Context context;
    private final List<Product> list;

    public ShopProductAdapter(Context context, List<Product> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_shop_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = list.get(position);
        holder.tvName.setText(product.getName());
        holder.tvPrice.setText(String.format("%,.0f₫", product.getPrice()));
        holder.tvStock.setText("Còn " + product.getStock() + " sản phẩm");
        holder.tvDescription.setText(product.getDescription() != null ? product.getDescription() : "");
        holder.tvStatus.setText("Đang hiển thị");

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.bg_image)
                    .error(R.drawable.bg_image)
                    .centerCrop()
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.bg_image);
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvName;
        TextView tvPrice;
        TextView tvStock;
        TextView tvStatus;
        TextView tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgShopProduct);
            tvName = itemView.findViewById(R.id.tvShopProductName);
            tvPrice = itemView.findViewById(R.id.tvShopProductPrice);
            tvStock = itemView.findViewById(R.id.tvShopProductStock);
            tvStatus = itemView.findViewById(R.id.tvShopProductStatus);
            tvDescription = itemView.findViewById(R.id.tvShopProductDescription);
        }
    }
}
