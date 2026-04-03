package com.anhnvt_ph55017.md_02_datn.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.anhnvt_ph55017.md_02_datn.utils.ProfileApiService;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.User;
import com.anhnvt_ph55017.md_02_datn.screens.LoginActivity;
import com.anhnvt_ph55017.md_02_datn.screens.ShippingAddressActivity;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {

    Button btnLogout;
    TextView tvName, tvEmail, tvOrderCount, tvWishlistCount, tvLanguage;
    Switch switchDark;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Ánh xạ view
        tvName          = view.findViewById(R.id.tvName);
        tvEmail         = view.findViewById(R.id.tvEmail);
        tvOrderCount    = view.findViewById(R.id.tvOrderCount);
        tvWishlistCount = view.findViewById(R.id.tvWishlistCount);
        tvLanguage      = view.findViewById(R.id.tvLanguage);
        switchDark      = view.findViewById(R.id.switchDark);
        btnLogout       = view.findViewById(R.id.btnLogout);

                // Lấy thông tin user từ SessionManager (backend)
                                // Luôn lấy thông tin user mới nhất từ backend
                                String token = SessionManager.getToken(getContext());
                                ProfileApiService.fetchProfile(getContext(), token, new ProfileApiService.ProfileCallback() {
                                        @Override
                                        public void onSuccess(org.json.JSONObject userJson) {
                                                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                                                        String name = userJson.optString("name", "");
                                                        String email = userJson.optString("email", "");
                                                        tvName.setText(!name.isEmpty() ? name : "Người dùng");
                                                        tvEmail.setText(!email.isEmpty() ? email : "");
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
                // Lấy số đơn hàng
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
                // Lấy số sản phẩm yêu thích
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

        // Menu clicks
        view.findViewById(R.id.rowOrders).setOnClickListener(v ->
                Toast.makeText(getContext(), "Đơn hàng của tôi", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.imgEdit).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), com.anhnvt_ph55017.md_02_datn.screens.EditProfileActivity.class);
            startActivityForResult(intent, 3001);
        });

        view.findViewById(R.id.rowAddress).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ShippingAddressActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.rowPayment).setOnClickListener(v ->
                Toast.makeText(getContext(), "Phương thức thanh toán", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.rowWishlist).setOnClickListener(v ->
                Toast.makeText(getContext(), "Sản phẩm yêu thích", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.rowNotifications).setOnClickListener(v ->
                Toast.makeText(getContext(), "Thông báo", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.rowPrivacy).setOnClickListener(v ->
                Toast.makeText(getContext(), "Quyền riêng tư & Bảo mật", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.rowLanguage).setOnClickListener(v ->
                Toast.makeText(getContext(), "Đổi ngôn ngữ", Toast.LENGTH_SHORT).show());

        switchDark.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(getContext(), isChecked ? "Bật chế độ tối" : "Tắt chế độ tối", Toast.LENGTH_SHORT).show());

        // Logout
        btnLogout.setOnClickListener(v -> {
              // Clear local session
              SessionManager.clearSession(getContext());
              Intent intent = new Intent(requireActivity(), LoginActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
              startActivity(intent);
              // Không gọi Toast sau startActivity vì Context đã bị destroy
        });

        return view;
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
