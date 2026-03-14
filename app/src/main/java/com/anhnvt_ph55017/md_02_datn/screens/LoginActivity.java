package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

    FirebaseAuth mAuth;
    GoogleSignInClient googleSignInClient;

    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtEmail = findViewById(R.id.edt_Email);
        edtPass = findViewById(R.id.edt_Pass);
        btnLogin = findViewById(R.id.btn_login);
        btnGoogle = findViewById(R.id.btn_google_sign_in);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvReset = findViewById(R.id.tvResetPass);

        mAuth = FirebaseAuth.getInstance();

        configureGoogle();

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

        String email = edtEmail.getText().toString().trim();
        String pass = edtPass.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    } else {

                        Toast.makeText(this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                    }
                });
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
}