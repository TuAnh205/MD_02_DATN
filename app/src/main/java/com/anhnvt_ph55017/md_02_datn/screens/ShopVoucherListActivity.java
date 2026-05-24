package com.anhnvt_ph55017.md_02_datn.screens;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Voucher;
import com.anhnvt_ph55017.md_02_datn.utils.VoucherApiService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ShopVoucherListActivity extends AppCompatActivity {

    private androidx.recyclerview.widget.RecyclerView rvShopVouchers;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fabAddVoucher;
    private VoucherListAdapter adapter;
    private List<Voucher> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_voucher_list);

        rvShopVouchers = findViewById(R.id.rvShopVouchers);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        fabAddVoucher = findViewById(R.id.fabAddVoucher);

        adapter = new VoucherListAdapter(list, new VoucherListAdapter.VoucherActionListener() {
            @Override
            public void onEdit(Voucher voucher) {
                Intent i = new Intent(ShopVoucherListActivity.this, CreateVoucherActivity.class);
                i.putExtra("editVoucher", voucher);
                startActivity(i);
            }

            @Override
            public void onDelete(Voucher voucher) {
                confirmDeleteVoucher(voucher);
            }
        });

        rvShopVouchers.setLayoutManager(new LinearLayoutManager(this));
        rvShopVouchers.setAdapter(adapter);

        fabAddVoucher.setOnClickListener(v -> startActivity(new Intent(this, CreateVoucherActivity.class)));

        swipeRefresh.setOnRefreshListener(this::loadVouchers);

        loadVouchers();
    }

    private void loadVouchers() {
        swipeRefresh.setRefreshing(true);
        VoucherApiService.getMyCreatedVouchers(this, new VoucherApiService.VoucherListCallback() {
            @Override
            public void onSuccess(java.util.List<Voucher> vouchers) {
                runOnUiThread(() -> {
                    list.clear();
                    list.addAll(vouchers);
                    adapter.notifyDataSetChanged();
                    swipeRefresh.setRefreshing(false);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(ShopVoucherListActivity.this, "Lỗi tải voucher: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void confirmDeleteVoucher(Voucher voucher) {
        if (voucher == null || voucher.get_id() == null || voucher.get_id().trim().isEmpty()) {
            Toast.makeText(this, "Voucher không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        String voucherName = voucher.getName() != null && !voucher.getName().isEmpty()
                ? voucher.getName()
                : voucher.getCode();

        new AlertDialog.Builder(this)
                .setTitle("Xóa voucher")
                .setMessage("Bạn có chắc muốn xóa voucher \"" + voucherName + "\"?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteVoucher(voucher))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteVoucher(Voucher voucher) {
        swipeRefresh.setRefreshing(true);
        VoucherApiService.deleteShopVoucher(this, voucher.get_id(), new VoucherApiService.DeleteCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(ShopVoucherListActivity.this, message, Toast.LENGTH_SHORT).show();
                    loadVouchers();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(ShopVoucherListActivity.this, "Lỗi xóa voucher: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}
