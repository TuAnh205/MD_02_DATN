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

    private ProgressBar pbLoading;
    private RecyclerView rvAvailableVouchers, rvMyVouchers;
    private VoucherClaimAdapter adapter;
    private VoucherListAdapter myAdapter;
    private java.util.List<Voucher> availableList = new java.util.ArrayList<>();
    private java.util.List<Voucher> myList = new java.util.ArrayList<>();
    private java.util.Set<String> claimedCodes = new java.util.HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim_voucher);


        pbLoading = findViewById(R.id.pbLoading);
        rvMyVouchers = findViewById(R.id.rvMyVouchers);
        rvAvailableVouchers = findViewById(R.id.rvAvailableVouchers);

        adapter = new VoucherClaimAdapter(availableList, voucher -> claimVoucher(voucher.getCode()));
        myAdapter = new VoucherListAdapter(myList);

        rvMyVouchers.setLayoutManager(new LinearLayoutManager(this));
        rvMyVouchers.setAdapter(myAdapter);

        rvAvailableVouchers.setLayoutManager(new LinearLayoutManager(this));
        rvAvailableVouchers.setAdapter(adapter);



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
                    // Filter out consumed vouchers and vouchers đã hết lượt
                    for (Voucher v : myVouchers) {
                        if (v == null) continue;
                        boolean userLimitOk = v.getUserLimit() <= 0 || v.getUsedCount() < v.getUserLimit();
                        boolean usageLimitOk = v.getUsageLimit() <= 0 || v.getUsedCount() < v.getUsageLimit();
                        if (!v.isConsumed() && userLimitOk && usageLimitOk) {
                            myList.add(v);
                        }
                    }
                    myAdapter.notifyDataSetChanged();
                });

                // After loading myVouchers, load global vouchers to compute available ones
                VoucherApiService.getVouchers(ClaimVoucherActivity.this, new VoucherApiService.VoucherListCallback() {
                    @Override
                    public void onSuccess(java.util.List<Voucher> vouchers) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            claimedCodes.clear();
                            for (Voucher mv : myVouchers) {
                                if (mv != null && mv.getCode() != null) {
                                    claimedCodes.add(mv.getCode().toUpperCase());
                                }
                            }
                            availableList.clear();
                            for (Voucher v : vouchers) {
                                if (v.getCode() == null) continue;
                                String codeUpper = v.getCode().toUpperCase();
                                // skip vouchers already claimed by this user (received or used)
                                if (claimedCodes.contains(codeUpper)) continue;
                                // skip vouchers that have exhausted usage limits
                                if (v.getUsageLimit() > 0 && v.getUsedCount() >= v.getUsageLimit()) continue;
                                availableList.add(v);
                            }
                            adapter.setClaimedCodes(claimedCodes);
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

                    Voucher claimedVoucher = null;
                    int removedIndex = -1;
                    for (int i = 0; i < availableList.size(); i++) {
                        Voucher v = availableList.get(i);
                        if (v.getCode() != null && v.getCode().equalsIgnoreCase(code)) {
                            claimedVoucher = availableList.remove(i);
                            removedIndex = i;
                            break;
                        }
                    }
                    if (removedIndex >= 0) {
                        adapter.notifyItemRemoved(removedIndex);
                    } else {
                        adapter.notifyDataSetChanged();
                    }

                    if (claimedVoucher != null) {
                        boolean alreadyInMyList = false;
                        for (Voucher v : myList) {
                            if (v.getCode() != null && v.getCode().equalsIgnoreCase(code)) {
                                alreadyInMyList = true;
                                break;
                            }
                        }
                        if (!alreadyInMyList) {
                            myList.add(0, claimedVoucher);
                        }
                        myAdapter.notifyDataSetChanged();
                    }
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



    private void setLoading(boolean loading) {

        pbLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Auto-refresh to detect consumed vouchers when returning from checkout
        loadAvailableVouchers();
    }
}
