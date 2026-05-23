package com.anhnvt_ph55017.md_02_datn.screens;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Customer;

public class CustomerManagementActivity extends AppCompatActivity {

    public static final String EXTRA_CUSTOMER = "extra_customer";

    private TextView tvCustomerName;
    private TextView tvCustomerEmail;
    private TextView tvCustomerPhone;
    private TextView tvCustomerOrderCount;
    private TextView tvCustomerId;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_management);

        tvCustomerName = findViewById(R.id.tvCustomerName);
        tvCustomerEmail = findViewById(R.id.tvCustomerEmail);
        tvCustomerPhone = findViewById(R.id.tvCustomerPhone);
        tvCustomerOrderCount = findViewById(R.id.tvCustomerOrderCount);
        tvCustomerId = findViewById(R.id.tvCustomerId);
        btnBack = findViewById(R.id.btnBack);

        Customer customer = (Customer) getIntent().getSerializableExtra(EXTRA_CUSTOMER);
        if (customer == null) {
            finish();
            return;
        }

        tvCustomerName.setText(customer.getName());
        tvCustomerEmail.setText(customer.getEmail());
        tvCustomerPhone.setText(customer.getPhone());
        tvCustomerOrderCount.setText(String.valueOf(customer.getOrderCount()));
        tvCustomerId.setText(customer.getId());

        btnBack.setOnClickListener(v -> finish());
    }
}
