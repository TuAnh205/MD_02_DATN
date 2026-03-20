package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.MotionEvent;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.anhnvt_ph55017.md_02_datn.DAO.UserDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    EditText edtFullName, edtEmail, edtPhone, edtPass1, edtPass2;
    CheckBox checkRegis;
    AppCompatButton btnRegister;
    TextView tvBack;

    UserDAO userDAO;
    RequestQueue requestQueue;

    // emulator: 10.0.2.2 (host machine)
    private static final String BACKEND_BASE_URL = "http://10.0.2.2:5000/api";

    boolean isPass1Visible = false;
    boolean isPass2Visible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        anhXa();
        eye();
        userDAO = new UserDAO(this);
        requestQueue = Volley.newRequestQueue(this);

        tvBack.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> validateAndRegister());
    }

    // ================= VALIDATE & REGISTER =================
    private void validateAndRegister() {

        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String pass1 = edtPass1.getText().toString();
        String pass2 = edtPass2.getText().toString();

        // Clear error cũ
        clearError();

        // 1️⃣ Full name
        if (fullName.isEmpty()) {
            edtFullName.setError("Không được để trống");
            edtFullName.requestFocus();
            return;
        }

        if (fullName.length() < 3) {
            edtFullName.setError("Tên phải ít nhất 3 ký tự");
            edtFullName.requestFocus();
            return;
        }

        // 2️⃣ Email
        if (email.isEmpty()) {
            edtEmail.setError("Email không được để trống");
            edtEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Email không đúng định dạng");
            edtEmail.requestFocus();
            return;
        }

        if (userDAO.checkEmailExists(email)) {
            edtEmail.setError("Email đã tồn tại");
            edtEmail.requestFocus();
            return;
        }

        // 3️⃣ Phone
        if (phone.isEmpty()) {
            edtPhone.setError("Số điện thoại không được để trống");
            edtPhone.requestFocus();
            return;
        }

        if (!phone.matches("^0[0-9]{9}$")) {
            edtPhone.setError("Số điện thoại phải có 10 số và bắt đầu bằng 0");
            edtPhone.requestFocus();
            return;
        }

        // 4️⃣ Password
        if (pass1.isEmpty()) {
            edtPass1.setError("Mật khẩu không được để trống");
            edtPass1.requestFocus();
            return;
        }

        if (pass1.length() < 6) {
            edtPass1.setError("Mật khẩu tối thiểu 6 ký tự");
            edtPass1.requestFocus();
            return;
        }

        if (!pass1.matches(".*[A-Z].*") ||
                !pass1.matches(".*[a-z].*") ||
                !pass1.matches(".*[0-9].*")) {

            edtPass1.setError("Mật khẩu phải có chữ hoa, chữ thường và số");
            edtPass1.requestFocus();
            return;
        }

        // 5️⃣ Confirm password
        if (!pass1.equals(pass2)) {
            edtPass2.setError("Mật khẩu không khớp");
            edtPass2.requestFocus();
            return;
        }

        // 6️⃣ Checkbox
        if (!checkRegis.isChecked()) {
            Toast.makeText(this, "Bạn phải đồng ý điều khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        // 7️⃣ Register
        attemptBackendRegister(fullName, email, phone, pass1);
    }

    private void attemptBackendRegister(String fullName, String email, String phone, String password) {
        String url = BACKEND_BASE_URL + "/auth/register";

        JSONObject body = new JSONObject();
        try {
            body.put("name", fullName);
            body.put("email", email);
            body.put("phone", phone);
            body.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi tạo yêu cầu đăng ký", Toast.LENGTH_LONG).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    // If backend registration success, ensure local DB has record for offline mode
                    if (!userDAO.checkEmailExists(email)) {
                        userDAO.register(fullName, email, phone, password);
                    }

                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra("prefill_email", email);
                    intent.putExtra("prefill_pass", password);
                    startActivity(intent);
                    finish();
                },
                error -> {
                    String message = "Đăng ký thất bại";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            String errorBody = new String(error.networkResponse.data);
                            JSONObject errJson = new JSONObject(errorBody);
                            message = errJson.optString("message", message);
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }

                    Toast.makeText(this, "Backend: " + message + ". Sẽ thử đăng ký offline...", Toast.LENGTH_LONG).show();

                    // Fallback local registration if backend unavailable or conflict
                    if (userDAO.checkEmailExists(email)) {
                        Toast.makeText(this, "Email đã tồn tại (offline)", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    boolean localSuccess = userDAO.register(fullName, email, phone, password);
                    if (localSuccess) {
                        Toast.makeText(this, "Đăng ký offline thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.putExtra("prefill_email", email);
                        intent.putExtra("prefill_pass", password);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Đăng ký thất bại vừa online vừa offline", Toast.LENGTH_LONG).show();
                    }
                }
        );

        requestQueue.add(request);
    }

    // ================= ICON MẮT =================
    private void eye() {

        edtPass1.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (edtPass1.getRight()
                        - edtPass1.getCompoundDrawables()[2].getBounds().width())) {

                    isPass1Visible = !isPass1Visible;
                    togglePassword(edtPass1, isPass1Visible);
                    return true;
                }
            }
            return false;
        });

        edtPass2.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (edtPass2.getRight()
                        - edtPass2.getCompoundDrawables()[2].getBounds().width())) {

                    isPass2Visible = !isPass2Visible;
                    togglePassword(edtPass2, isPass2Visible);
                    return true;
                }
            }
            return false;
        });
    }

    private void togglePassword(EditText edt, boolean isVisible) {
        if (isVisible) {
            edt.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            edt.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_eye, 0);
        } else {
            edt.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
            edt.setCompoundDrawablesWithIntrinsicBounds(
                    0, 0, R.drawable.ic_eye_close, 0);
        }
        edt.setSelection(edt.getText().length());
    }

    // ================= UTILS =================
    private void clearError() {
        edtFullName.setError(null);
        edtEmail.setError(null);
        edtPhone.setError(null);
        edtPass1.setError(null);
        edtPass2.setError(null);
    }

    private void anhXa() {
        edtFullName = findViewById(R.id.edtFullName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPhone = findViewById(R.id.edtPhoneNumber);
        edtPass1 = findViewById(R.id.edtPass1);
        edtPass2 = findViewById(R.id.edtPass2);
        checkRegis = findViewById(R.id.checkRegis);
        btnRegister = findViewById(R.id.btnRegister);
        tvBack = findViewById(R.id.tvBack);
    }
}