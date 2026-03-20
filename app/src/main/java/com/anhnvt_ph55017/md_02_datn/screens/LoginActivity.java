package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.util.Log;
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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import org.json.JSONException;
import org.json.JSONObject;

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
    RequestQueue requestQueue;

    // adjust to your backend address (for emulator: 10.0.2.2)
    private static final String BACKEND_BASE_URL = "http://10.0.2.2:5000/api";

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
        requestQueue = Volley.newRequestQueue(this);

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

        // Try backend login first
        String url = BACKEND_BASE_URL + "/auth/login";
        JSONObject body = new JSONObject();
        try {
            body.put("email", identifier);
            body.put("password", pass);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    try {
                        String token = response.getString("token");
                        JSONObject userObj = response.getJSONObject("user");
                        String email = userObj.getString("email");
                        String name = userObj.optString("name", "");

                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                                .putString("token", token)
                                .putString("user_email", email)
                                .putString("user_name", name)
                                .apply();

                        if (checkRemember.isChecked()) {
                            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                                    .putString(PREF_EMAIL, identifier)
                                    .putString(PREF_PASS, pass)
                                    .putBoolean(PREF_REMEMBER, true)
                                    .apply();
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

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errMsg = "Sai email/số điện thoại hoặc mật khẩu";

                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String errorBody = new String(error.networkResponse.data);
                            JSONObject errJson = new JSONObject(errorBody);
                            if (errJson.has("message")) {
                                errMsg = errJson.getString("message");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // ALWAYS fallback to local for normal (email/password) accounts
                    boolean localOk = userDAO.login(identifier, pass);
                    if (localOk) {
                        Toast.makeText(this, "Đăng nhập thành công (offline)", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        return;
                    }

                    if (errMsg.contains("Google") || errMsg.contains("Firebase")) {
                        Toast.makeText(this, errMsg + " (ngày đã login backend?)", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
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

    private static final String DEFAULT_WEB_CLIENT_ID_PLACEHOLDER = "YOUR_DEFAULT_WEB_CLIENT_ID_HERE";

    private void configureGoogle() {

        String webClientId = getString(R.string.default_web_client_id);

        if (webClientId == null || webClientId.isEmpty() || webClientId.equals(DEFAULT_WEB_CLIENT_ID_PLACEHOLDER)) {
            Toast.makeText(this, "Thiết lập Google Sign-In không hợp lệ, cập nhật default_web_client_id trong strings.xml", Toast.LENGTH_LONG).show();
            Log.e("LoginActivity", "default_web_client_id is invalid: " + webClientId);
            btnGoogle.setEnabled(false);
            return;
        }

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestIdToken(webClientId)
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
                String idToken = account.getIdToken();

                if (idToken == null || idToken.isEmpty()) {
                    Toast.makeText(this, "Không lấy được Google token. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                    Log.e("LoginActivity", "Google account idToken is null/empty");
                    loginGoogle(); // rerun the flow so user can choose account again
                    return;
                }

                String email = account.getEmail();
                String name = account.getDisplayName();
                sendGoogleTokenToBackend(idToken, email, name);

            } catch (ApiException e) {
                Toast.makeText(this, "Google login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("LoginActivity", "Google sign in failed", e);
            } catch (Exception e) {
                Toast.makeText(this, "Google login failed", Toast.LENGTH_SHORT).show();
                Log.e("LoginActivity", "Unknown sign in error", e);
            }
        }
    }

    // ================= FIREBASE AUTH =================

    private void firebaseAuth(String idToken) {

        AuthCredential credential =
                GoogleAuthProvider.getCredential(idToken, null);

        // Clear stale session before signing in:
        mAuth.signOut();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(this, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show();

                        if (mAuth.getCurrentUser() != null) {
                            String firebaseUid = mAuth.getCurrentUser().getUid();
                            String email = mAuth.getCurrentUser().getEmail();
                            String name = mAuth.getCurrentUser().getDisplayName();

                            Log.d("LoginActivity", "Google user done: uid=" + firebaseUid + ", email=" + email + ", name=" + name);

                            if (firebaseUid == null || firebaseUid.isEmpty() || email == null || email.isEmpty()) {
                                Toast.makeText(this, "Firebase user data incomplete", Toast.LENGTH_LONG).show();
                                proceedToMain();
                            } else {
                                syncFirebaseUserWithServer(firebaseUid, email, name);
                            }
                        } else {
                            Toast.makeText(this, "Firebase user null after Google sign-in", Toast.LENGTH_LONG).show();
                            proceedToMain();
                        }

                    } else {
                        Exception ex = task.getException();
                        String message = "Google login thất bại";
                        if (ex != null) {
                            message += ": " + ex.getMessage();
                            Log.e("LoginActivity", "signInWithCredential error", ex);
                        }

                        if (ex instanceof FirebaseAuthInvalidCredentialsException || ex instanceof FirebaseAuthInvalidUserException) {
                            Toast.makeText(this, "Mã xác thực Google đã hết hạn hoặc không hợp lệ. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                            googleSignInClient.signOut().addOnCompleteListener(logoutTask -> loginGoogle());
                        } else if (ex != null && ex.getMessage() != null && ex.getMessage().contains("recaptcha")) {
                            Toast.makeText(this, "Xác thực reCAPTCHA bị lỗi. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                            googleSignInClient.signOut().addOnCompleteListener(logoutTask -> loginGoogle());
                        } else {
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void sendGoogleTokenToBackend(String idToken, String email, String name) {
        String url = BACKEND_BASE_URL + "/auth/google-login";

        JSONObject body = new JSONObject();
        try {
            body.put("idToken", idToken);
            if (email != null) body.put("email", email);
            if (name != null) body.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Đã có lỗi khi tạo payload đăng nhập Google", Toast.LENGTH_LONG).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    try {
                        String token = response.getString("token");
                        JSONObject userObj = response.getJSONObject("user");
                        String emailFromServer = userObj.optString("email");
                        String nameFromServer = userObj.optString("name");

                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                                .putString("token", token)
                                .putString("user_email", emailFromServer)
                                .putString("user_name", nameFromServer)
                                .apply();

                        Toast.makeText(this, "Google login thành công", Toast.LENGTH_SHORT).show();
                        proceedToMain();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Lỗi đọc phản hồi server", Toast.LENGTH_LONG).show();
                    }
                },
                error -> {
                    String errMsg = "Google login server failed";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String errorBody = new String(error.networkResponse.data);
                            JSONObject errJson = new JSONObject(errorBody);
                            errMsg = errJson.optString("message", errMsg);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    Toast.makeText(this, "Google login thất bại: " + errMsg, Toast.LENGTH_LONG).show();

                    // fallback: clear local firebase if exists and re-run login
                    mAuth.signOut();
                    googleSignInClient.signOut();

                }
        );

        requestQueue.add(request);
    }

    private void proceedToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void syncFirebaseUserWithServer(String firebaseUid, String email, String name) {
        String url = BACKEND_BASE_URL + "/auth/firebase-sync";

        Log.d("LoginActivity", "syncFirebaseUserWithServer: url=" + url + " uid=" + firebaseUid + " email=" + email + " name=" + name);

        JSONObject body = new JSONObject();
        try {
            body.put("firebaseUid", firebaseUid);
            body.put("email", email);
            body.put("name", name);
        } catch (JSONException ex) {
            ex.printStackTrace();
            Toast.makeText(this, "Đã có lỗi khi tạo dữ liệu sync", Toast.LENGTH_LONG).show();
            proceedToMain();
            return;
        }

        Log.d("LoginActivity", "syncFirebaseUserWithServer payload: " + body.toString());

        JsonObjectRequest syncRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    try {
                        String token = response.getString("token");
                        JSONObject userObj = response.getJSONObject("user");
                        String emailFromServer = userObj.optString("email");
                        String nameFromServer = userObj.optString("name");

                        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                                .putString("token", token)
                                .putString("user_email", emailFromServer)
                                .putString("user_name", nameFromServer)
                                .apply();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    proceedToMain();
                },
                error -> {
                    Log.e("LoginActivity", "syncFirebaseUserWithServer failed", error);

                    if (error.networkResponse != null) {
                        String serverResponse = new String(error.networkResponse.data);
                        Log.e("LoginActivity", "syncFirebaseUserWithServer response: " + serverResponse);
                        try {
                            JSONObject err = new JSONObject(serverResponse);
                            String msg = err.optString("message", "Lỗi server khi sync.");
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Toast.makeText(this, "syncFirebaseUserWithServer: lỗi không xác định", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "syncFirebaseUserWithServer: không thể kết nối server", Toast.LENGTH_LONG).show();
                    }

                    proceedToMain();
                }
        );

        requestQueue.add(syncRequest);
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
    }

    private void loadAndShowSavedAccounts() {
        // Load all saved accounts from SharedPreferences
        // We'll store them as email1,password1;email2,password2;...
        String savedAccountsStr = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getString("saved_accounts", "");

        if (!savedAccountsStr.isEmpty()) {
            String[] accounts = savedAccountsStr.split(";");
            java.util.List<SavedAccount> accountList = new java.util.ArrayList<>();

            for (String account : accounts) {
                String[] parts = account.split(",");
                if (parts.length == 2) {
                    accountList.add(new SavedAccount(parts[0], parts[1]));
                }
            }

            if (!accountList.isEmpty()) {
                savedAccountAdapter.setAccounts(accountList);
                rvSavedAccounts.setVisibility(View.VISIBLE);
            }
        }
    }
}