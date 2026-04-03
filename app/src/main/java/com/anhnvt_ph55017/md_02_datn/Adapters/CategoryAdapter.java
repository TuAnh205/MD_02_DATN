package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Category;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    List<Category> list;
    OnCategoryClick listener;

    Set<Integer> selected = new HashSet<>();

    public interface OnCategoryClick{
        void onClick(Set<Integer> selectedCategory);
    }

    public CategoryAdapter(List<Category> list, OnCategoryClick listener){
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category,parent,false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Category category = list.get(position);

        holder.tvName.setText(category.getName());
        // Chỉ hiển thị đúng 6 danh mục, đúng thứ tự bạn yêu cầu
        int iconRes = -1;
        String name = category.getName().toLowerCase();
        // Loại bỏ máy tính bảng, microphone và các loại khác
        if ((name.contains("máy tính") || name.contains("may tinh") || name.contains("pc")) && !name.contains("bảng") && !name.contains("tablet")) {
            iconRes = R.drawable.ic_laptop;
        } else if (name.contains("điện thoại") || name.contains("dien thoai") || name.contains("phone")) {
            iconRes = R.drawable.ic_phone;
        } else if (name.contains("tai nghe") || name.contains("tainghe") || name.contains("headphone")) {
            iconRes = R.drawable.ic_headphone;
        } else if (name.contains("phụ kiện") || name.contains("phu kien") || name.contains("accessory")) {
            iconRes = R.drawable.ic_phukien;
        } else if (name.contains("màn hình") || name.contains("man hinh") || name.contains("monitor")) {
            iconRes = R.drawable.ic_pc;
        } else if (name.contains("loa")) {
            iconRes = R.drawable.volume_solid_full;
        }
        // Nếu không thuộc 6 loại trên thì ẩn item
        if (iconRes == -1 || name.contains("bảng") || name.contains("tablet") || name.contains("micro") || name.contains("microphone")) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            return;
        } else {
            holder.itemView.setVisibility(View.VISIBLE);
            // Đặt width các category bằng nhau (4 category trên 1 hàng)
            int equalWidth = holder.itemView.getResources().getDisplayMetrics().widthPixels / 4;
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(equalWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        holder.imgIcon.setImageResource(iconRes);

        int id = category.getId();

        // đổi màu khi chọn
        if(selected.contains(id)){
            holder.itemView.setBackgroundResource(R.drawable.bg_category_selected);
        }else{
            holder.itemView.setBackgroundResource(R.drawable.bg_category_normal);
        }

        holder.itemView.setOnClickListener(v -> {

            if(selected.contains(id)){
                selected.remove(id); // bỏ lọc
            }else{
                selected.add(id); // thêm lọc
            }

            notifyDataSetChanged();

            if(listener != null){
                listener.onClick(selected);
            }

        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imgIcon;
        TextView tvName;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            imgIcon = itemView.findViewById(R.id.imgCategoryIcon);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}