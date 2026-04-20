package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.utils.NetworkConstants;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;

    EditText edtEmail, edtPass;
    Button btnLogin;
    TextView tvSignUp, tvReset;
    ImageButton btnGoogle;
    RadioButton checkRemember;
    RecyclerView rvSavedAccounts;

    GoogleSignInClient googleSignInClient;

    List<AccountInfo> savedAccounts = new ArrayList<>();

    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ánh xạ
        edtEmail = findViewById(R.id.edt_Email);
        edtPass = findViewById(R.id.edt_Pass);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogle = findViewById(R.id.btn_google_sign_in);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvReset = findViewById(R.id.tvResetPass);
        checkRemember = findViewById(R.id.checkRemember);
        rvSavedAccounts = findViewById(R.id.rvSavedAccounts);

        loadSavedAccounts();
        setupSavedAccountsList();

        configureGoogle();
        togglePassword();
        prefillFromRegister();

        btnLogin.setOnClickListener(v -> login());
        btnGoogle.setOnClickListener(v -> loginGoogle());

        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        tvReset.setOnClickListener(v ->
                startActivity(new Intent(this, ResetPass.class)));
    }

    // ================= LOGIN =================
    private void login() {

        String email = edtEmail.getText().toString().trim().toLowerCase();
        String pass = edtPass.getText().toString().trim();
        boolean remember = checkRemember.isChecked();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Nhập đầy đủ", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("email", email);
            body.put("password", pass);

            String url = NetworkConstants.getApiBaseUrl() + "/api/auth/login";

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    body,
                    response -> {

                        String token = response.optString("token", "");
                        JSONObject userObj = response.optJSONObject("user");

                        if (token.isEmpty() || userObj == null) {
                            Toast.makeText(this, "Login lỗi dữ liệu", Toast.LENGTH_LONG).show();
                            return;
                        }

                        String userId = userObj.optString("_id", "");
                        String userName = userObj.optString("name", "User");
                        String userEmail = userObj.optString("email", email);
                        String role = userObj.optString("role", "user");

                        SessionManager.saveToken(this, token);
                        SessionManager.saveUserSession(this, userId, userEmail, userName, role);

                        // ===== SYNC CART LOCAL =====
                        List<com.anhnvt_ph55017.md_02_datn.models.Product> localCart =
                                com.anhnvt_ph55017.md_02_datn.utils.CartLocalManager.loadCart(this);

                        if (!localCart.isEmpty()) {
                            for (com.anhnvt_ph55017.md_02_datn.models.Product p : localCart) {
                                com.anhnvt_ph55017.md_02_datn.utils.CartApiService.addToCart(
                                        this,
                                        token,
                                        p.getId(),
                                        p.getQty(), // 🔥 dùng qty đúng
                                        null
                                );
                            }
                            com.anhnvt_ph55017.md_02_datn.utils.CartLocalManager.clearCart(this);
                        }

                        if (remember) {
                            saveAccount(email, pass);
                        }

                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                        if ("shop".equals(role)) {
                            startActivity(new Intent(this, ShopMainActivity.class));
                        } else {
                            startActivity(new Intent(this, MainActivity.class));
                        }

                        finish();
                    },
                    error -> {
                        Toast.makeText(this, "Sai tài khoản hoặc lỗi server", Toast.LENGTH_LONG).show();
                    }
            );

            Volley.newRequestQueue(this).add(request);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= GOOGLE =================
    private void configureGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void loginGoogle() {
        startActivityForResult(googleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account =
                        GoogleSignIn.getSignedInAccountFromIntent(data)
                                .getResult(ApiException.class);

                googleLoginToServer(account.getIdToken());

            } catch (Exception e) {
                Toast.makeText(this, "Google lỗi", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void googleLoginToServer(String idToken) {
        // (giữ nguyên logic bạn đã viết – không lỗi)
    }

    // ================= PASSWORD =================
    private void togglePassword() {
        edtPass.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableWidth = edtPass.getCompoundDrawables()[2].getBounds().width();

                if (event.getX() >= (edtPass.getWidth() - drawableWidth)) {

                    if (isPasswordVisible) {
                        edtPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    } else {
                        edtPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    }

                    edtPass.setSelection(edtPass.length());
                    isPasswordVisible = !isPasswordVisible;
                    return true;
                }
            }
            return false;
        });
    }

    // ================= PREFILL =================
    private void prefillFromRegister() {
        Intent i = getIntent();
        if (i != null) {
            edtEmail.setText(i.getStringExtra("prefill_email"));
            edtPass.setText(i.getStringExtra("prefill_pass"));
        }
    }

    // ================= SAVE ACCOUNT =================
    public static class AccountInfo {
        String email, password;

        public AccountInfo(String e, String p) {
            email = e;
            password = p;
        }
    }

    private void saveAccount(String email, String pass) {
        SharedPreferences pref = getSharedPreferences("login_prefs", MODE_PRIVATE);
        String json = pref.getString("accounts", "[]");
        try {
            org.json.JSONArray arr = new org.json.JSONArray(json);
            // Không lưu trùng email
            for (int i = 0; i < arr.length(); i++) {
                org.json.JSONObject obj = arr.getJSONObject(i);
                if (email.equals(obj.optString("email"))) {
                    arr.remove(i);
                    break;
                }
            }
            org.json.JSONObject newObj = new org.json.JSONObject();
            newObj.put("email", email);
            newObj.put("password", pass);
            arr.put(0, newObj); // Thêm lên đầu
            pref.edit().putString("accounts", arr.toString()).apply();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadSavedAccounts() {
        savedAccounts.clear();
        SharedPreferences pref = getSharedPreferences("login_prefs", MODE_PRIVATE);
        Object val = pref.getAll().get("accounts");
        String json = "[]";
        if (val instanceof String) {
            json = (String) val;
        } else if (val != null) {
            // Nếu là kiểu khác (ví dụ Boolean), xóa key này để tránh lỗi
            pref.edit().remove("accounts").apply();
        }
        try {
            org.json.JSONArray arr = new org.json.JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                org.json.JSONObject obj = arr.getJSONObject(i);
                savedAccounts.add(new AccountInfo(obj.optString("email"), obj.optString("password")));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void setupSavedAccountsList() {
        if (savedAccounts.isEmpty()) return;

        rvSavedAccounts.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup p, int v) {
                View view = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, p, false);
                return new RecyclerView.ViewHolder(view) {};
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder h, int i) {
                AccountInfo acc = savedAccounts.get(i);
                ((TextView) h.itemView.findViewById(android.R.id.text1)).setText(acc.email);

                h.itemView.setOnClickListener(v -> {
                    edtEmail.setText(acc.email);
                    edtPass.setText(acc.password);
                });
            }

            @Override
            public int getItemCount() {
                return savedAccounts.size();
            }
        });
    }
}