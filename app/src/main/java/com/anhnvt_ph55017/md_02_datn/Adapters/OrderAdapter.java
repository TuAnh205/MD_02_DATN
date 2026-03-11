package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Order;

import org.jspecify.annotations.NonNull;

import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder>{

    Context context;
    List<Order> list;

    public OrderAdapter(Context context, List<Order> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){

        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_order,parent,false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,int position){

        Order o = list.get(position);

        holder.tvOrderId.setText("Order #"+o.getId());
        holder.tvDate.setText(o.getDate());
        holder.tvPrice.setText("$"+o.getTotal());

    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView tvOrderId,tvDate,tvPrice;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            tvOrderId=itemView.findViewById(R.id.tvOrderId);
            tvDate=itemView.findViewById(R.id.tvDate);
            tvPrice=itemView.findViewById(R.id.tvPrice);
        }
    }
}