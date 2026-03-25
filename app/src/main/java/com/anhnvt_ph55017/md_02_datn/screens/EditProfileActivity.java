package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.anhnvt_ph55017.md_02_datn.DAO.UserDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class EditProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView imgAvatar;
    private EditText etFullName, etEmail, etPhone, etBio;
    private Button btnSaveChanges, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        btnBack = findViewById(R.id.btnBack);
        imgAvatar = findViewById(R.id.imgAvatar);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etBio = findViewById(R.id.etBio);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);
        btnCancel = findViewById(R.id.btnCancel);

        loadCurrentProfile();

        btnBack.setOnClickListener(v -> onBackPressed());
        btnCancel.setOnClickListener(v -> onBackPressed());

        btnSaveChanges.setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentProfile() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            UserDAO userDAO = new UserDAO(this);
            User user = userDAO.getUserByEmail(firebaseUser.getEmail());
            if (user != null) {
                etFullName.setText(user.getFullname() != null ? user.getFullname() : "");
                etEmail.setText(user.getEmail() != null ? user.getEmail() : "");
                etPhone.setText(user.getPhone() != null ? user.getPhone() : "");
            } else {
                // Fallback to Firebase
                etFullName.setText(firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "");
                etEmail.setText(firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "");
                etPhone.setText("");
            }
        }
        etBio.setText(""); // Bio not stored yet
    }

    private void saveProfile() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String bio = etBio.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền tên và email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to local DB
        UserDAO userDAO = new UserDAO(this);
        boolean dbSuccess = userDAO.insertOrUpdateUser(fullName, email, phone);

        if (!dbSuccess) {
            Toast.makeText(this, "Lỗi lưu vào database", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update Firebase display name
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(fullName)
                    .build();

            firebaseUser.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    Intent result = new Intent();
                    result.putExtra("name", fullName);
                    result.putExtra("email", email);
                    result.putExtra("phone", phone);
                    result.putExtra("bio", bio);
                    setResult(RESULT_OK, result);
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật Firebase không thành công", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(EditProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
            Intent result = new Intent();
            result.putExtra("name", fullName);
            result.putExtra("email", email);
            result.putExtra("phone", phone);
            result.putExtra("bio", bio);
            setResult(RESULT_OK, result);
            finish();
        }
    }
}