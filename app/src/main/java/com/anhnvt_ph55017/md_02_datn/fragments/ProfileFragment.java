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

import com.anhnvt_ph55017.md_02_datn.DAO.UserDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.User;
import com.anhnvt_ph55017.md_02_datn.screens.LoginActivity;
import com.anhnvt_ph55017.md_02_datn.screens.ShippingAddressActivity;
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

        // Lấy thông tin user từ local DB, fallback về Firebase
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            UserDAO userDAO = new UserDAO(getContext());
            User user = userDAO.getUserByEmail(firebaseUser.getEmail());
            if (user != null) {
                tvName.setText(user.getFullname() != null ? user.getFullname() : "Người dùng");
                tvEmail.setText(user.getEmail() != null ? user.getEmail() : "");
            } else {
                // Fallback to Firebase
                tvName.setText(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "Người dùng");
                tvEmail.setText(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "");
            }
        } else {
            tvName.setText("Alex Johnson");
            tvEmail.setText("alex.johnson@coretech.io");
        }
        tvOrderCount.setText("24");
        tvWishlistCount.setText("12");

        // Menu clicks
        view.findViewById(R.id.rowOrders).setOnClickListener(v ->
                Toast.makeText(getContext(), "My Orders", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.imgEdit).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), com.anhnvt_ph55017.md_02_datn.screens.EditProfileActivity.class);
            startActivityForResult(intent, 3001);
        });

        view.findViewById(R.id.rowAddress).setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), ShippingAddressActivity.class);
            startActivity(intent);
        });

        view.findViewById(R.id.rowPayment).setOnClickListener(v ->
                Toast.makeText(getContext(), "Payment Methods", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.rowWishlist).setOnClickListener(v ->
                Toast.makeText(getContext(), "Wishlist", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.rowNotifications).setOnClickListener(v ->
                Toast.makeText(getContext(), "Notifications", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.rowPrivacy).setOnClickListener(v ->
                Toast.makeText(getContext(), "Privacy & Security", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.rowLanguage).setOnClickListener(v ->
                Toast.makeText(getContext(), "Change language", Toast.LENGTH_SHORT).show());

        switchDark.setOnCheckedChangeListener((buttonView, isChecked) ->
                Toast.makeText(getContext(), isChecked ? "Dark mode on" : "Dark mode off", Toast.LENGTH_SHORT).show());

        // Logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

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
