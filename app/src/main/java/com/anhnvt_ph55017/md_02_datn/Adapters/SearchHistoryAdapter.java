package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;

import java.util.List;

public class SearchHistoryAdapter extends RecyclerView.Adapter<SearchHistoryAdapter.HistoryViewHolder> {

    private List<String> history;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String keyword);
    }

    public SearchHistoryAdapter(List<String> history, OnItemClickListener listener) {
        this.history = history;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        String keyword = history.get(position);
        holder.txtKeyword.setText(keyword);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(keyword));
    }

    @Override
    public int getItemCount() {
        return history.size();
    }

    public void setData(List<String> newHistory) {
        this.history = newHistory;
        notifyDataSetChanged();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView txtKeyword;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtKeyword = itemView.findViewById(R.id.txtKeyword);
        }
    }
}
