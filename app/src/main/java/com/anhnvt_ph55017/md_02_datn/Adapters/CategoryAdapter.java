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
        holder.imgIcon.setImageResource(category.getImage());

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