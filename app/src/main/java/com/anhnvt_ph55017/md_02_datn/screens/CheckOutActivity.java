package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.List;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.anhnvt_ph55017.md_02_datn.DAO.OrderDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.HomeActivity;
import com.anhnvt_ph55017.md_02_datn.fragments.HomeFragment;
import com.anhnvt_ph55017.md_02_datn.models.Address;
import com.anhnvt_ph55017.md_02_datn.models.Order;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;
import com.anhnvt_ph55017.md_02_datn.utils.NotificationManager;

public class CheckOutActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADDRESS = 2001;

    RadioGroup paymentGroup;
    EditText edVoucher;
    Button btnOrder;
    TextView tvSubtotal, tvTax, tvTotal;
    androidx.recyclerview.widget.RecyclerView rvSummary;
    List<com.anhnvt_ph55017.md_02_datn.models.Product> cartList;
    com.anhnvt_ph55017.md_02_datn.Adapters.SummaryAdapter summaryAdapter;

    // shipping
    TextView tvShipName, tvShipPhone, tvShipAddress;
    Button btnChangeAddress;
    Address selectedAddress;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

        // Check if user is logged in
        userId = SessionManager.getUserId(this);
        if (userId <= 0) {
            Toast.makeText(this, "Please login to checkout", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
            return;
        }

        paymentGroup = findViewById(R.id.paymentGroup);
        edVoucher = findViewById(R.id.edVoucher);
        btnOrder = findViewById(R.id.btnOrder);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTax = findViewById(R.id.tvTax);
        tvTotal = findViewById(R.id.tvTotal);
        rvSummary = findViewById(R.id.rvSummary);

        tvShipName = findViewById(R.id.tvShipName);
        tvShipPhone = findViewById(R.id.tvShipPhone);
        tvShipAddress = findViewById(R.id.tvShipAddress);
        btnChangeAddress = findViewById(R.id.btnChangeAddress);

        // Không dùng DAO, lấy địa chỉ từ intent hoặc backend
        selectedAddress = null;
        // Nếu có địa chỉ được chọn từ ShippingAddressActivity, lấy từ intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("selectedAddressName")) {
            String name = intent.getStringExtra("selectedAddressName");
            String phone = intent.getStringExtra("selectedAddressPhone");
            String addressStr = intent.getStringExtra("selectedAddressDetail");
            String id = "";
            String userIdStr = String.valueOf(userId);
            selectedAddress = new Address(id, userIdStr, name, phone, addressStr, true);
        }
        updateAddressDisplay();

        cartList = (List<com.anhnvt_ph55017.md_02_datn.models.Product>) getIntent().getSerializableExtra("cart");
        if(cartList == null) cartList = new java.util.ArrayList<>();

        summaryAdapter = new com.anhnvt_ph55017.md_02_datn.Adapters.SummaryAdapter(this, cartList);
        rvSummary.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        rvSummary.setAdapter(summaryAdapter);

        calculateTotals();

        btnChangeAddress.setOnClickListener(v -> {
            Intent addressIntent = new Intent(CheckOutActivity.this, ShippingAddressActivity.class);
            startActivityForResult(addressIntent, REQUEST_CODE_ADDRESS);
        });

        btnOrder.setOnClickListener(v -> {

            int checkedId = paymentGroup.getCheckedRadioButtonId();

            if (checkedId == -1) {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            String voucher = edVoucher.getText().toString();

            if (!cartList.isEmpty()) {
                double subtotal = 0;
                int itemCount = 0;

                for (com.anhnvt_ph55017.md_02_datn.models.Product p : cartList) {
                    subtotal += p.getPrice() * p.getQty();
                    itemCount += p.getQty();
                }

                String date = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());
                String status = "Đang xử lý";
                String arrival = "Chưa xác định";

                int image = cartList.get(0).getImage();
                String name = cartList.get(0).getName();
                double price = cartList.get(0).getPrice();
                String desc = cartList.get(0).getDescription();

                // ===== FIX PAYMENT METHOD =====
                String paymentMethod;

                if (checkedId == R.id.payCOD) {
                    paymentMethod = "Thanh toán sau khi nhận hàng";
                } else if (checkedId == R.id.payCard) {
                    paymentMethod = "Sử dụng thẻ";
                } else {
                    paymentMethod = "Khác";
                }

                    // ===== ADDRESS =====
                    String address = selectedAddress != null
                        ? selectedAddress.getAddress()
                        : "Chưa chọn địa chỉ";
                    // TODO: Gọi API đặt hàng backend ở đây, truyền address, paymentMethod, cartList, ...

            }

            // Increment notification count for successful order
            NotificationManager.incrementNotification(this);

            Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();

            Intent home = new Intent(CheckOutActivity.this, MainActivity.class);
            home.putExtra("openFragment", "orders");
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(home);
            finish();
        });

    }

    private void calculateTotals(){
        double subtotal = 0;
        for(com.anhnvt_ph55017.md_02_datn.models.Product p : cartList){
            subtotal += p.getPrice() * p.getQty();
        }
        double tax = subtotal * 0.1;
        double total = subtotal + tax;

        tvSubtotal.setText(String.format("Subtotal: $%.2f", subtotal));
        tvTax.setText(String.format("Tax: $%.2f", tax));
        tvTotal.setText(String.format("Total: $%.2f", total));
    }

    private void loadDefaultAddress() {
        // Đã loại bỏ DAO, không làm gì ở đây
    }

    private void updateAddressDisplay() {
        if (selectedAddress != null) {
            tvShipName.setText(selectedAddress.getName());
            tvShipPhone.setText(selectedAddress.getPhone());
            tvShipAddress.setText(selectedAddress.getAddress());
        } else {
            tvShipName.setText("");
            tvShipPhone.setText("");
            tvShipAddress.setText("Chưa chọn địa chỉ");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADDRESS && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra("selectedAddressName");
            String phone = data.getStringExtra("selectedAddressPhone");
            String addressStr = data.getStringExtra("selectedAddressDetail");
            String id = "";
            String userIdStr = String.valueOf(userId);
            selectedAddress = new Address(id, userIdStr, name, phone, addressStr, true);
            updateAddressDisplay();
        }
    }
}
