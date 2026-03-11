package com.anhnvt_ph55017.md_02_datn.screens;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.anhnvt_ph55017.md_02_datn.R;

public class CheckOutActivity extends AppCompatActivity {

    RadioGroup paymentGroup;
    EditText edVoucher;
    Button btnOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

        paymentGroup = findViewById(R.id.paymentGroup);
        edVoucher = findViewById(R.id.edVoucher);
        btnOrder = findViewById(R.id.btnOrder);

        btnOrder.setOnClickListener(v -> {

            int checked = paymentGroup.getCheckedRadioButtonId();

            if(checked == -1){
                Toast.makeText(this,"Choose payment method",Toast.LENGTH_SHORT).show();
                return;
            }

            String voucher = edVoucher.getText().toString();

            Toast.makeText(this,"Order placed!",Toast.LENGTH_LONG).show();
        });

    }
}