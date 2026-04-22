package com.anhnvt_ph55017.md_02_datn.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

import com.anhnvt_ph55017.md_02_datn.screens.*;
import com.anhnvt_ph55017.md_02_datn.utils.*;
import com.anhnvt_ph55017.md_02_datn.R;

public class ProfileFragment extends Fragment {

    Button btnLogin, btnRegister, btnLogout;
    ImageView imgAvatar;
    TextView tvName, tvEmail, tvOrderCount, tvWishlistCount, tvLanguage, tvDMK;
    Switch switchDark;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Ánh xạ
        imgAvatar       = view.findViewById(R.id.imgAvatar);
        tvName          = view.findViewById(R.id.tvName);
        tvEmail         = view.findViewById(R.id.tvEmail);
        tvOrderCount    = view.findViewById(R.id.tvOrderCount);
        tvWishlistCount = view.findViewById(R.id.tvWishlistCount);
        tvLanguage      = view.findViewById(R.id.tvLanguage);
        switchDark      = view.findViewById(R.id.switchDark);
        btnLogin        = view.findViewById(R.id.btnLogin);
        btnRegister     = view.findViewById(R.id.btnRegister);
        btnLogout       = view.findViewById(R.id.btnLogout);
        tvDMK           = view.findViewById(R.id.tvDMK);

        switchDark.setChecked(SessionManager.isDarkModeEnabled(getContext()));

        boolean isLoggedIn = SessionManager.isLoggedIn(getContext());

        // ================= ĐỔI MẬT KHẨU =================
        view.findViewById(R.id.rowPrivacy).setOnClickListener(v -> {
            if (!ensureLoggedIn()) return;

            Intent intent = new Intent(requireActivity(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        // ================= LOGIN STATE =================
        if (isLoggedIn) {
            btnLogin.setVisibility(View.GONE);
            btnRegister.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);
            view.findViewById(R.id.imgEdit).setVisibility(View.VISIBLE);

            String token = SessionManager.getToken(getContext());

            ProfileApiService.fetchProfile(getContext(), token, new ProfileApiService.ProfileCallback() {
                @Override
                public void onSuccess(org.json.JSONObject userJson) {
                    if (getActivity() != null) getActivity().runOnUiThread(() -> {
                        tvName.setText(userJson.optString("name", "Người dùng"));
                        tvEmail.setText(userJson.optString("email", ""));
                        String avatar = userJson.optString("avatar", "");
                        if (!avatar.isEmpty()) loadAvatar(avatar);
                    });
                }

                @Override
                public void onError(String error) {
                    if (getActivity() != null) getActivity().runOnUiThread(() -> {
                        tvName.setText("Người dùng");
                        tvEmail.setText("");
                    });
                }
            });

        } else {
            btnLogin.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
            view.findViewById(R.id.imgEdit).setVisibility(View.GONE);

            tvName.setText("Khách");
            tvEmail.setText("Vui lòng đăng nhập");
        }

        // ================= BUTTON =================
        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), LoginActivity.class)));

        btnRegister.setOnClickListener(v ->
                startActivity(new Intent(requireActivity(), RegisterActivity.class)));

        btnLogout.setOnClickListener(v -> {
            SessionManager.clearSession(getContext());
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        // ================= ACTION =================
        view.findViewById(R.id.imgEdit).setOnClickListener(v -> {
            if (!ensureLoggedIn()) return;
            startActivity(new Intent(requireActivity(), EditProfileActivity.class));
        });

        view.findViewById(R.id.rowAddress).setOnClickListener(v -> {
            if (!ensureLoggedIn()) return;
            startActivity(new Intent(requireActivity(), ShippingAddressActivity.class));
        });

        view.findViewById(R.id.rowWishlist).setOnClickListener(v -> {
            if (!ensureLoggedIn()) return;
            startActivity(new Intent(requireActivity(), FavoriteActivity.class));
        });

        view.findViewById(R.id.rowNotifications).setOnClickListener(v -> {
            if (!ensureLoggedIn()) return;
            startActivity(new Intent(requireActivity(), NotificationActivity.class));
        });

        view.findViewById(R.id.rowOrders).setOnClickListener(v -> {
            if (!ensureLoggedIn()) return;
            Toast.makeText(getContext(), "Đơn hàng của tôi", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.rowLanguage).setOnClickListener(v ->
                Toast.makeText(getContext(), "Đổi ngôn ngữ", Toast.LENGTH_SHORT).show());

        // ================= DARK MODE =================
        switchDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SessionManager.saveDarkModeEnabled(getContext(), isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            requireActivity().recreate();
        });

        return view;
    }

    private boolean ensureLoggedIn() {
        if (!SessionManager.isLoggedIn(getContext())) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            return false;
        }
        return true;
    }

    private void loadAvatar(String url) {
        new Thread(() -> {
            try {
                Bitmap bmp = BitmapFactory.decodeStream(new java.net.URL(url).openStream());
                if (getActivity() != null && bmp != null) {
                    getActivity().runOnUiThread(() -> imgAvatar.setImageBitmap(bmp));
                }
            } catch (Exception ignored) {}
        }).start();
    }
}