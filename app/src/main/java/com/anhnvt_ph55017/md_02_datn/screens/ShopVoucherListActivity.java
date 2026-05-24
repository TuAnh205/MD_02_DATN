package com.anhnvt_ph55017.md_02_datn.screens;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.TranslateAnimation;
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

    private View sidebarContainer;
    private View dimOverlay;
    private View layoutShopUserCard;
    private androidx.recyclerview.widget.RecyclerView rvShopVouchers;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout swipeRefresh;
    private FloatingActionButton fabAddVoucher;
    private VoucherListAdapter adapter;
    private List<Voucher> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_voucher_list);

        sidebarContainer = findViewById(R.id.sidebarContainer);
        dimOverlay = findViewById(R.id.dimOverlay);
        layoutShopUserCard = findViewById(R.id.layoutShopUserCard);
        rvShopVouchers = findViewById(R.id.rvShopVouchers);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        fabAddVoucher = findViewById(R.id.fabAddVoucher);

        findViewById(R.id.ivToggleSidebar).setOnClickListener(v -> openSidebar());
        findViewById(R.id.ivCloseSidebar).setOnClickListener(v -> closeSidebar());
        dimOverlay.setOnClickListener(v -> closeSidebar());

        layoutShopUserCard.setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, ShopProfileActivity.class));
        });

        findViewById(R.id.menuDashboard).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, ShopMainActivity.class));
        });

        findViewById(R.id.menuCategories).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, ShopMainActivity.class));
        });

        findViewById(R.id.menuOrders).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, OrdersActivity.class));
        });

        findViewById(R.id.menuCustomers).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, ShopCustomersActivity.class));
        });

        findViewById(R.id.menuVoucher).setOnClickListener(v -> closeSidebar());

        findViewById(R.id.menuReviews).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, ShopReviewsActivity.class));
        });

        findViewById(R.id.menuRevenue).setOnClickListener(v -> {
            closeSidebar();
            startActivity(new Intent(this, RevenueActivity.class));
        });

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

    private void openSidebar() {
        sidebarContainer.setVisibility(View.VISIBLE);
        dimOverlay.setVisibility(View.VISIBLE);
        TranslateAnimation anim = new TranslateAnimation(-sidebarContainer.getWidth(), 0, 0, 0);
        anim.setDuration(250);
        sidebarContainer.startAnimation(anim);
    }

    private void closeSidebar() {
        TranslateAnimation anim = new TranslateAnimation(0, -sidebarContainer.getWidth(), 0, 0);
        anim.setDuration(200);
        anim.setAnimationListener(new android.view.animation.Animation.AnimationListener() {
            @Override public void onAnimationStart(android.view.animation.Animation animation) {}
            @Override public void onAnimationRepeat(android.view.animation.Animation animation) {}
            @Override public void onAnimationEnd(android.view.animation.Animation animation) {
                sidebarContainer.setVisibility(View.GONE);
                dimOverlay.setVisibility(View.GONE);
            }
        });
        sidebarContainer.startAnimation(anim);
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
