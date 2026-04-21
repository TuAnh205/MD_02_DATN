package com.anhnvt_ph55017.md_02_datn.screens;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Voucher;
import com.anhnvt_ph55017.md_02_datn.utils.VoucherApiService;

import java.util.ArrayList;
import java.util.List;

public class VoucherSelectActivity extends AppCompatActivity {
    private RecyclerView rvVouchers;
    private ProgressBar progressBar;
    private VoucherAdapter adapter;
    private List<Voucher> voucherList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher_select);

        rvVouchers = findViewById(R.id.rvVouchers);
        progressBar = findViewById(R.id.progressBar);
        rvVouchers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VoucherAdapter(voucherList, voucher -> {
            Intent result = new Intent();
            result.putExtra("voucher", voucher);
            setResult(Activity.RESULT_OK, result);
            finish();
        });
        rvVouchers.setAdapter(adapter);
        loadVouchers();
    }

    private void loadVouchers() {
        progressBar.setVisibility(View.VISIBLE);
        VoucherApiService.getVouchers(this, new VoucherApiService.VoucherListCallback() {
            @Override
            public void onSuccess(List<Voucher> vouchers) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    voucherList.clear();
                    voucherList.addAll(vouchers);
                    adapter.notifyDataSetChanged();
                });
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(VoucherSelectActivity.this, "Lỗi tải voucher: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
