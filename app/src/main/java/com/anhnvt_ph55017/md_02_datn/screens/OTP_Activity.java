package com.anhnvt_ph55017.md_02_datn.screens;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.anhnvt_ph55017.md_02_datn.R;

public class OTP_Activity extends AppCompatActivity {
    EditText otp1, otp2, otp3, otp4, otp5, otp6;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otp);
        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);

        setupOtpInputs();
    }
    private void setupOtpInputs() {

        EditText[] otp = {otp1, otp2, otp3, otp4, otp5, otp6};

        for (int i = 0; i < otp.length; i++) {
            final int index = i;

            otp[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < otp.length - 1) {
                        otp[index + 1].requestFocus();
                    }
                }

                @Override public void afterTextChanged(Editable s) {}
            });

            otp[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && otp[index].getText().toString().isEmpty()
                        && index > 0) {
                    otp[index - 1].requestFocus();
                }
                return false;
            });
        }
    }

}