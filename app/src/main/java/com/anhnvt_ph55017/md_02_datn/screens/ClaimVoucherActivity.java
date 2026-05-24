package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Voucher;
import com.anhnvt_ph55017.md_02_datn.screens.VoucherClaimAdapter;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;
import com.anhnvt_ph55017.md_02_datn.utils.VoucherApiService;

public class ClaimVoucherActivity extends AppCompatActivity {
    private EditText edtVoucherCode;
    private Button btnClaimVoucher;
    private ProgressBar pbLoading;
    private RecyclerView rvAvailableVouchers, rvMyVouchers;
    private VoucherClaimAdapter adapter;
    private VoucherListAdapter myAdapter;
    private java.util.List<Voucher> availableList = new java.util.ArrayList<>();
    private java.util.List<Voucher> myList = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_voucher);

        edtVoucherCode = findViewById(R.id.edtVoucherCode);
        btnClaimVoucher = findViewById(R.id.btnClaimVoucher);
        pbLoading = findViewById(R.id.pbLoading);
        rvMyVouchers = findViewById(R.id.rvMyVouchers);
        rvAvailableVouchers = findViewById(R.id.rvAvailableVouchers);

        adapter = new VoucherClaimAdapter(availableList, voucher -> claimVoucher(voucher.getCode()));
        myAdapter = new VoucherListAdapter(myList);

        rvMyVouchers.setLayoutManager(new LinearLayoutManager(this));
        rvMyVouchers.setAdapter(myAdapter);

        rvAvailableVouchers.setLayoutManager(new LinearLayoutManager(this));
        rvAvailableVouchers.setAdapter(adapter);

        btnClaimVoucher.setOnClickListener(v -> claimVoucher());

        loadAvailableVouchers();
    }

    private void loadAvailableVouchers() {
        setLoading(true);
        // Fetch both lists: my vouchers and available vouchers
        VoucherApiService.getMyVouchers(this, new VoucherApiService.VoucherListCallback() {
            @Override
            public void onSuccess(java.util.List<Voucher> myVouchers) {
                runOnUiThread(() -> {
                    myList.clear();
                    myList.addAll(myVouchers);
                    myAdapter.notifyDataSetChanged();
                });

                // After loading myVouchers, load global vouchers to compute available ones
                VoucherApiService.getVouchers(ClaimVoucherActivity.this, new VoucherApiService.VoucherListCallback() {
                    @Override
                    public void onSuccess(java.util.List<Voucher> vouchers) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            java.util.Set<String> claimedCodes = new java.util.HashSet<>();
                            for (Voucher mv : myList) {
                                if (mv.getCode() != null) claimedCodes.add(mv.getCode());
                            }
                            availableList.clear();
                            for (Voucher v : vouchers) {
                                if (v.getCode() == null) continue;
                                if (!claimedCodes.contains(v.getCode())) {
                                    availableList.add(v);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(ClaimVoucherActivity.this, "Lỗi tải voucher: " + error, Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(ClaimVoucherActivity.this, "Lỗi tải voucher của bạn: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void claimVoucher(String code) {
        if (code == null || code.trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mã voucher", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!SessionManager.isLoggedIn(this)) {
            Toast.makeText(this, "Vui lòng đăng nhập để nhận voucher", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        setLoading(true);
        VoucherApiService.claimVoucher(this, code, new VoucherApiService.ClaimCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(ClaimVoucherActivity.this, message, Toast.LENGTH_LONG).show();
                    // remove claimed voucher from list if present
                    for (int i = availableList.size() - 1; i >= 0; i--) {
                        Voucher v = availableList.get(i);
                        if (v.getCode() != null && v.getCode().equalsIgnoreCase(code)) {
                            availableList.remove(i);
                        }
                    }
                    adapter.notifyDataSetChanged();

                    // Refresh user's claimed vouchers from server so UI and other flows are up-to-date
                    VoucherApiService.getMyVouchers(ClaimVoucherActivity.this, new VoucherApiService.VoucherListCallback() {
                        @Override
                        public void onSuccess(java.util.List<Voucher> myVouchers) {
                            runOnUiThread(() -> {
                                myList.clear();
                                myList.addAll(myVouchers);
                                myAdapter.notifyDataSetChanged();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            // ignore silently; we've already shown success
                        }
                    });
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(ClaimVoucherActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void claimVoucher() {
        claimVoucher(edtVoucherCode.getText().toString().trim());
    }

    private void setLoading(boolean loading) {
        btnClaimVoucher.setEnabled(!loading);
        pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
