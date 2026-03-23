package com.anhnvt_ph55017.md_02_datn.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;

import java.util.List;

public class SavedAccountAdapter extends RecyclerView.Adapter<SavedAccountAdapter.AccountViewHolder> {

    private List<SavedAccount> accounts;
    private final OnAccountClickListener listener;

    public interface OnAccountClickListener {
        void onAccountClick(SavedAccount account);
    }

    public SavedAccountAdapter(OnAccountClickListener listener) {
        this.listener = listener;
        this.accounts = new java.util.ArrayList<>();
    }

    public void setAccounts(List<SavedAccount> accounts) {
        this.accounts = accounts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_account, parent, false);
        return new AccountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        SavedAccount account = accounts.get(position);
        holder.txtAccount.setText(account.getEmail());
        holder.itemView.setOnClickListener(v -> listener.onAccountClick(account));
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    static class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView txtAccount;

        AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            txtAccount = itemView.findViewById(R.id.txtAccount);
        }
    }
    public static class SavedAccount {
        private final String email;
        private final String password;

        public SavedAccount(String email, String password) {
            this.email = email;
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }
    }}