package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
                Toast.makeText(ShopVoucherListActivity.this, "Xóa voucher chưa được triển khai", Toast.LENGTH_SHORT).show();
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
}
