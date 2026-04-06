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

import com.anhnvt_ph55017.md_02_datn.utils.ProfileApiService;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.User;
import com.anhnvt_ph55017.md_02_datn.screens.LoginActivity;
import com.anhnvt_ph55017.md_02_datn.screens.RegisterActivity;
import com.anhnvt_ph55017.md_02_datn.screens.ShippingAddressActivity;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    Button btnLogin, btnRegister, btnLogout;
    ImageView imgAvatar;
    TextView tvName, tvEmail, tvOrderCount, tvWishlistCount, tvLanguage;
    Switch switchDark;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Ánh xạ view
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

        switchDark.setChecked(SessionManager.isDarkModeEnabled(getContext()));

        boolean isLoggedIn = SessionManager.isLoggedIn(getContext());

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
                        String name = userJson.optString("name", "");
                        String email = userJson.optString("email", "");
                        String avatarUrl = userJson.optString("avatar", "");
                        tvName.setText(!name.isEmpty() ? name : "Người dùng");
                        tvEmail.setText(!email.isEmpty() ? email : "");
                        if (!avatarUrl.isEmpty()) {
                            loadAvatar(avatarUrl);
                        }
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
            tvOrderCount.setText("--");
            tvWishlistCount.setText("--");
            com.anhnvt_ph55017.md_02_datn.utils.OrderApiService.getOrders(getContext(), token, new com.anhnvt_ph55017.md_02_datn.utils.OrderApiService.OrdersCallback() {
                @Override
                public void onSuccess(org.json.JSONArray ordersJson) {
                    if (getActivity() != null) getActivity().runOnUiThread(() -> tvOrderCount.setText(String.valueOf(ordersJson.length())));
                }
                @Override
                public void onError(String error) {
                    if (getActivity() != null) getActivity().runOnUiThread(() -> tvOrderCount.setText("--"));
                }
            });
            com.anhnvt_ph55017.md_02_datn.utils.FavoriteApiService.getFavorites(getContext(), token, new com.anhnvt_ph55017.md_02_datn.utils.FavoriteApiService.FavoritesCallback() {
                @Override
                public void onSuccess(org.json.JSONArray favoritesJson) {
                    if (getActivity() != null) getActivity().runOnUiThread(() -> tvWishlistCount.setText(String.valueOf(favoritesJson.length())));
                }
                @Override
                public void onError(String error) {
                    if (getActivity() != null) getActivity().runOnUiThread(() -> tvWishlistCount.setText("--"));
                }
            });
        } else {
            btnLogin.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
            view.findViewById(R.id.imgEdit).setVisibility(View.GONE);
            tvName.setText("Khách");
            tvEmail.setText("Vui lòng đăng nhập");
            tvOrderCount.setText("--");
            tvWishlistCount.setText("--");
        }

        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), LoginActivity.class));
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), RegisterActivity.class));
        });

        // Các action khi chưa đăng nhập
        view.findViewById(R.id.rowOrders).setOnClickListener(v -> {
            if (!ensureLoggedIn()) return;
            Toast.makeText(getContext(), "Đơn hàng của tôi", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.imgEdit).setOnClickListener(v -> {
            if (!ensureLoggedIn()) return;
            Intent intent = new Intent(requireActivity(), com.anhnvt_ph55017.md_02_datn.screens.EditProfileActivity.class);
            startActivityForResult(intent, 3001);
        });

        view.findViewById(R.id.rowAddress).setOnClickListener(v -> {
            if (!ensureLoggedIn()) return;
            Intent intent = new Intent(requireActivity(), ShippingAddressActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.rowPayment).setOnClickListener(v -> {
            if (!ensureLoggedIn()) return;
            Toast.makeText(getContext(), "Phương thức thanh toán", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.rowWishlist).setOnClickListener(v -> {
            if (!ensureLoggedIn()) return;
            Toast.makeText(getContext(), "Sản phẩm yêu thích", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.rowNotifications).setOnClickListener(v -> {
            if (!ensureLoggedIn()) return;
            Toast.makeText(getContext(), "Thông báo", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.rowPrivacy).setOnClickListener(v -> {
            if (!ensureLoggedIn()) return;
            Toast.makeText(getContext(), "Quyền riêng tư & Bảo mật", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.rowLanguage).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Đổi ngôn ngữ", Toast.LENGTH_SHORT).show();
        });

        switchDark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SessionManager.saveDarkModeEnabled(getContext(), isChecked);
            AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            if (getActivity() != null) {
                getActivity().recreate();
            }
            Toast.makeText(getContext(), isChecked ? "Bật chế độ tối" : "Tắt chế độ tối", Toast.LENGTH_SHORT).show();
        });

        // Logout
        btnLogout.setOnClickListener(v -> {
            SessionManager.clearSession(getContext());
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            requireActivity().finish();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SessionManager.isLoggedIn(getContext())) {
            String token = SessionManager.getToken(getContext());
            ProfileApiService.fetchProfile(getContext(), token, new ProfileApiService.ProfileCallback() {
                @Override
                public void onSuccess(org.json.JSONObject userJson) {
                    if (getActivity() != null) getActivity().runOnUiThread(() -> {
                        String name = userJson.optString("name", "");
                        String email = userJson.optString("email", "");
                        String avatarUrl = userJson.optString("avatar", "");
                        tvName.setText(!name.isEmpty() ? name : "Người dùng");
                        tvEmail.setText(!email.isEmpty() ? email : "");
                        if (!avatarUrl.isEmpty()) {
                            loadAvatar(avatarUrl);
                        }
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
        }
    }

    private boolean ensureLoggedIn() {
        if (!SessionManager.isLoggedIn(getContext())) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(requireActivity(), LoginActivity.class));
            return false;
        }
        return true;
    }

    private void loadAvatar(String imageUrl) {
        new Thread(() -> {
            try {
                java.io.InputStream input = new java.net.URL(imageUrl).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                input.close();
                if (bitmap != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> imgAvatar.setImageBitmap(bitmap));
                }
            } catch (Exception ignored) {
            }
        }).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3001 && resultCode == getActivity().RESULT_OK && data != null) {
            String name = data.getStringExtra("name");
            String email = data.getStringExtra("email");
            if (name != null) tvName.setText(name);
            if (email != null) tvEmail.setText(email);
        }
    }
}
