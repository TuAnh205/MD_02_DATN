package com.anhnvt_ph55017.md_02_datn;

import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.anhnvt_ph55017.md_02_datn.DAO.UserDAO;

public class RegisterActivity extends AppCompatActivity {

    EditText edtFullName, edtEmail, edtPhone, edtPass1, edtPass2;
    CheckBox checkRegis;
    AppCompatButton btnRegister;
    TextView tvBack;

    UserDAO userDAO;

    boolean isPass1Visible = false;
    boolean isPass2Visible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        anhXa();
        eye();
        userDAO = new UserDAO(this);

        tvBack.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            String fullName = edtFullName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();
            String pass1 = edtPass1.getText().toString();
            String pass2 = edtPass2.getText().toString();

            if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty()
                    || pass1.isEmpty() || pass2.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass1.equals(pass2)) {
                Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!checkRegis.isChecked()) {
                Toast.makeText(this, "Bạn chưa đồng ý điều khoản", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userDAO.checkEmailExists(email)) {
                Toast.makeText(this, "Email đã tồn tại", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean success = userDAO.register(fullName, email, phone, pass1);
            if (success) {
                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                finish(); // quay về login
            } else {
                Toast.makeText(this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
            }
        });
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

}
