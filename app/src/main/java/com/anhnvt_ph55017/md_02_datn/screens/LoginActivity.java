package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.Adapters.SavedAccountAdapter;
import com.anhnvt_ph55017.md_02_datn.Adapters.SavedAccountAdapter.SavedAccount;
import com.anhnvt_ph55017.md_02_datn.DAO.UserDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 1001;

    EditText edtEmail, edtPass;
    Button btnLogin, btnGoogle;
    TextView tvSignUp, tvReset;
    CheckBox checkRemember;
    RecyclerView rvSavedAccounts;
    SavedAccountAdapter savedAccountAdapter;

    FirebaseAuth mAuth;
    GoogleSignInClient googleSignInClient;
    UserDAO userDAO;

    boolean isPasswordVisible = false;

    private static final String PREFS_NAME = "auth_prefs";
    private static final String PREF_EMAIL = "pref_email";
    private static final String PREF_PASS = "pref_pass";
    private static final String PREF_REMEMBER = "pref_remember";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edt_Email);
        edtPass = findViewById(R.id.edt_Pass);
        checkRemember = findViewById(R.id.checkRemember);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogle = findViewById(R.id.btn_google_sign_in);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvReset = findViewById(R.id.tvResetPass);
        rvSavedAccounts = findViewById(R.id.rvSavedAccounts);

        mAuth = FirebaseAuth.getInstance();
        userDAO = new UserDAO(this);

        configureGoogle();

        loadSavedCredentials();
        prefillFromRegister();

        // Setup saved accounts RecyclerView
        rvSavedAccounts.setLayoutManager(new LinearLayoutManager(this));
        savedAccountAdapter = new SavedAccountAdapter(account -> {
            // Auto-fill email and password when account is selected
            edtEmail.setText(account.getEmail());
            edtPass.setText(account.getPassword());
            rvSavedAccounts.setVisibility(View.GONE);
        });
        rvSavedAccounts.setAdapter(savedAccountAdapter);

        // Show saved accounts when email field is focused and empty
        edtEmail.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && edtEmail.getText().toString().trim().isEmpty()) {
                loadAndShowSavedAccounts();
            } else {
                rvSavedAccounts.setVisibility(View.GONE);
            }
        });

        btnLogin.setOnClickListener(v -> loginEmail());

        btnGoogle.setOnClickListener(v -> loginGoogle());

        tvSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));

        tvReset.setOnClickListener(v ->
                startActivity(new Intent(this, ResetPass.class)));

        togglePassword();
    }

    // ================= EMAIL LOGIN =================

    private void loginEmail() {

        String identifier = edtEmail.getText().toString().trim();
        String pass = edtPass.getText().toString().trim();

        if (identifier.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            boolean ok = userDAO.login(identifier, pass);

            if (ok) {
                Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                // Lưu / xóa thông tin theo checkbox
                if (checkRemember.isChecked()) {
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                            .putString(PREF_EMAIL, identifier)
                            .putString(PREF_PASS, pass)
                            .putBoolean(PREF_REMEMBER, true)
                            .apply();

                    // Also save to saved accounts list
                    saveToSavedAccounts(identifier, pass);
                } else {
                    getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                            .remove(PREF_EMAIL)
                            .remove(PREF_PASS)
                            .putBoolean(PREF_REMEMBER, false)
                            .apply();
                }

                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

            } else {
                Toast.makeText(this, "Sai email/số điện thoại hoặc mật khẩu", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi đăng nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSavedCredentials() {
        android.content.SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean remembered = prefs.getBoolean(PREF_REMEMBER, false);
        checkRemember.setChecked(remembered);

        if (remembered) {
            String savedEmail = prefs.getString(PREF_EMAIL, "");
            String savedPass = prefs.getString(PREF_PASS, "");
            edtEmail.setText(savedEmail);
            edtPass.setText(savedPass);
        }
    }

    private void prefillFromRegister() {
        Intent intent = getIntent();
        if (intent != null) {
            String email = intent.getStringExtra("prefill_email");
            String pass = intent.getStringExtra("prefill_pass");
            if (email != null && pass != null) {
                edtEmail.setText(email);
                edtPass.setText(pass);
                checkRemember.setChecked(true);
            }
        }
    }

    // ================= GOOGLE CONFIG =================

    private void configureGoogle() {

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestIdToken(getString(R.string.default_web_client_id))
                        .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    // ================= GOOGLE LOGIN =================

    private void loginGoogle() {

        // logout google cache để chọn lại account
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

                firebaseAuth(account.getIdToken());

            } catch (Exception e) {

                Toast.makeText(this, "Google login failed", Toast.LENGTH_SHORT).show();
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

                        Toast.makeText(this, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    } else {

                        Toast.makeText(this, "Google login thất bại", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ================= SHOW / HIDE PASSWORD =================

    private void togglePassword() {

        edtPass.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_UP) {

                int drawableEnd = 2;

                if (event.getRawX() >= (edtPass.getRight()
                        - edtPass.getCompoundDrawables()[drawableEnd].getBounds().width())) {

                    if (isPasswordVisible) {

                        edtPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    } else {

                        edtPass.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    }

                    edtPass.setSelection(edtPass.length());

                    isPasswordVisible = !isPasswordVisible;

                    return true;
                }
            }

            return false;
        });
    }

    // ================= SAVED ACCOUNTS =================

    private void saveToSavedAccounts(String email, String password) {
        try {
            String savedAccountsStr = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .getString("saved_accounts", "");

            // Check if this account is already saved
            String newAccount = email + "," + password;
            if (savedAccountsStr.contains(newAccount)) {
                return; // Already saved
            }

            // Add to the list (limit to 5 accounts)
            String[] accounts = savedAccountsStr.isEmpty() ? new String[0] : savedAccountsStr.split(";");
            java.util.List<String> accountList = new java.util.ArrayList<>(java.util.Arrays.asList(accounts));

            // Remove if already exists (update password)
            accountList.removeIf(acc -> acc.startsWith(email + ","));

            // Add new account at the beginning
            accountList.add(0, newAccount);

            // Keep only last 5 accounts
            if (accountList.size() > 5) {
                accountList = accountList.subList(0, 5);
            }

            // Save back
            String updatedAccounts = String.join(";", accountList);
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                    .putString("saved_accounts", updatedAccounts)
                    .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadAndShowSavedAccounts() {
        try {
            // Load all saved accounts from SharedPreferences
            // We'll store them as email1,password1;email2,password2;...
            String savedAccountsStr = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .getString("saved_accounts", "");

            if (!savedAccountsStr.isEmpty()) {
                String[] accounts = savedAccountsStr.split(";");
                java.util.List<SavedAccountAdapter.SavedAccount> accountList = new java.util.ArrayList<>();

                for (String account : accounts) {
                    String[] parts = account.split(",");
                    if (parts.length == 2) {
                        accountList.add(new SavedAccountAdapter.SavedAccount(parts[0], parts[1]));
                    }
                }

                if (!accountList.isEmpty()) {
                    savedAccountAdapter.setAccounts(accountList);
                    rvSavedAccounts.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}