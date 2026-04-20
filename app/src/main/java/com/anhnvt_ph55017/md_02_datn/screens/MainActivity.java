package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.fragments.BrowseFragment;
import com.anhnvt_ph55017.md_02_datn.fragments.HomeFragment;
import com.anhnvt_ph55017.md_02_datn.fragments.OrdersFragment;
import com.anhnvt_ph55017.md_02_datn.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private long lastBackPressed = 0;
    private static final int BACK_PRESS_INTERVAL = 2000; // 2s

    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager.applyTheme(this);
        super.onCreate(savedInstanceState);

        String role = SessionManager.getUserRole(this);
        if ("shop".equalsIgnoreCase(role)) {
            startActivity(new Intent(this, ShopMainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);

        // Mặc định Home
        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            if (item.getItemId() == R.id.nav_home) {
                fragment = new HomeFragment();
                return loadFragment(fragment);
            } else if (item.getItemId() == R.id.nav_browse) {
                fragment = new BrowseFragment();
                return loadFragment(fragment);
            } else if (item.getItemId() == R.id.nav_cart) {
                fragment = new com.anhnvt_ph55017.md_02_datn.fragments.CartFragment();
                return loadFragment(fragment);
            } else if (item.getItemId() == R.id.nav_orders) {
                fragment = new OrdersFragment();
                return loadFragment(fragment);
            } else if (item.getItemId() == R.id.nav_profile) {
                fragment = new ProfileFragment();
                return loadFragment(fragment);
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        // Nếu là activity root (không còn activity nào phía sau), xử lý double back để thoát app
        if (isTaskRoot()) {
            if (System.currentTimeMillis() - lastBackPressed < BACK_PRESS_INTERVAL) {
                finishAffinity(); // Thoát toàn bộ app
            } else {
                lastBackPressed = System.currentTimeMillis();
                android.widget.Toast.makeText(this, "Nhấn back lần nữa để thoát ứng dụng", android.widget.Toast.LENGTH_SHORT).show();
            }
        } else {
            // Nếu không phải activity root, back bình thường
            super.onBackPressed();
        }
    }

    // HƯỚNG DẪN: Khi chuyển về MainActivity từ các màn khác (đặt hàng, sửa profile, đăng nhập thành công), hãy dùng:
    // Intent intent = new Intent(context, MainActivity.class);
    // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    // context.startActivity(intent);
    // ((Activity) context).finish();

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}