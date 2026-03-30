package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anhnvt_ph55017.md_02_datn.DAO.UserDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.User;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;

    EditText edtEmail, edtPass;
    Button btnLogin, btnGoogle;
    TextView tvSignUp, tvReset;

    FirebaseAuth mAuth;
    GoogleSignInClient googleSignInClient;

    // ✅ Cập nhật đúng IP máy chủ
    private static final String BASE_URL = "http://192.168.1.13:5000/api";

    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ánh xạ
        edtEmail = findViewById(R.id.edt_Email);
        edtPass = findViewById(R.id.edt_Pass);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogle = findViewById(R.id.btn_google_sign_in);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvReset = findViewById(R.id.tvResetPass);

        mAuth = FirebaseAuth.getInstance();

        configureGoogle();
        togglePassword();
        prefillFromRegister();

        // login thường
        btnLogin.setOnClickListener(v -> login());

        // google
        btnGoogle.setOnClickListener(v -> loginGoogle());

        // chuyển sang register
        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        // quên mật khẩu
        tvReset.setOnClickListener(v ->
                startActivity(new Intent(this, ResetPass.class)));
    }

    // ================= LOGIN EMAIL =================
    private void login() {

        String email = edtEmail.getText().toString().trim().toLowerCase();
        String pass = edtPass.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Nhập đầy đủ", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", pass);

            String url = BASE_URL + "/auth/login";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        String token = response.optString("token");

                        // Lưu token
                        getSharedPreferences("auth", MODE_PRIVATE)
                                .edit()
                                .putString("token", token)
                                .apply();

                        // ✅ Lưu session tối thiểu để isLoggedIn() = true
                        SessionManager.saveUserSession(this, 1, email, "User");

                        Toast.makeText(this, "Login thành công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    },
                    error -> {
                        Toast.makeText(this, "Sai tài khoản hoặc mật khẩu", Toast.LENGTH_LONG).show();
                    }
            );

            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= GOOGLE CONFIG =================
    private void configureGoogle() {

        String webClientId = getString(R.string.default_web_client_id);

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestIdToken(webClientId)
                        .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // ================= GOOGLE LOGIN =================
    private void loginGoogle() {

        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(data);

            try {

                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();

                if (idToken == null) {
                    Toast.makeText(this, "Không lấy được token", Toast.LENGTH_SHORT).show();
                    return;
                }

                firebaseAuth(idToken);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Google login fail", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ================= FIREBASE AUTH =================
    private void firebaseAuth(String idToken) {

        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {

                            String uid   = user.getUid();
                            String email = user.getEmail();
                            String name  = user.getDisplayName();

                            syncServer(uid, email, name);
                        }

                    } else {
                        Toast.makeText(this, "Firebase fail", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ================= SYNC SERVER =================
    private void syncServer(String uid, String email, String name) {

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Google account không có email", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("firebaseUid", uid);
            body.put("email", email);
            body.put("name", name != null ? name : "User");

            String url = BASE_URL + "/auth/firebase-sync";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {
                        String token = response.optString("token");

                        // Lưu token
                        getSharedPreferences("auth", MODE_PRIVATE)
                                .edit()
                                .putString("token", token)
                                .apply();

                        // ✅ Lưu session tối thiểu để isLoggedIn() = true
                        SessionManager.saveUserSession(
                                this, 1, email, name != null ? name : "User"
                        );

                        Toast.makeText(this, "Google login thành công", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    },
                    error -> {
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String bodyStr = new String(error.networkResponse.data);
                            int statusCode = error.networkResponse.statusCode;
                            Log.e("SYNC_ERROR", "Status: " + statusCode + " | Body: " + bodyStr);
                            Toast.makeText(this, "Sync server lỗi " + statusCode + ": " + bodyStr, Toast.LENGTH_LONG).show();
                        } else {
                            Log.e("SYNC_ERROR", error.toString());
                            Toast.makeText(this, "Sync server lỗi: " + error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }
            );

            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo request sync server", Toast.LENGTH_LONG).show();
        }
    }

    // ================= SHOW PASSWORD =================
    private void togglePassword() {

        edtPass.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_UP) {

                if (edtPass.getCompoundDrawables()[2] == null) return false;

                int drawableWidth = edtPass.getCompoundDrawables()[2].getBounds().width();

                if (event.getX() >= (edtPass.getWidth() - drawableWidth - edtPass.getPaddingEnd())) {

                    if (isPasswordVisible) {

                        // Ẩn mật khẩu
                        edtPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        edtPass.setCompoundDrawablesWithIntrinsicBounds(
                                0, 0, R.drawable.ic_eye_close, 0);

                    } else {

                        // Hiện mật khẩu
                        edtPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        edtPass.setCompoundDrawablesWithIntrinsicBounds(
                                0, 0, R.drawable.ic_eye, 0);
                    }

                    edtPass.setSelection(edtPass.getText().length());
                    isPasswordVisible = !isPasswordVisible;

                    return true;
                }
            }

            return false;
        });
    }

    // ================= PREFILL =================
    private void prefillFromRegister() {

        Intent intent = getIntent();

        if (intent != null) {
            String email = intent.getStringExtra("prefill_email");
            String pass  = intent.getStringExtra("prefill_pass");

            if (email != null && pass != null) {
                edtEmail.setText(email);
                edtPass.setText(pass);
            }
        }
    }
}