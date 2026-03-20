package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.Adapters.SummaryAdapter;
import com.anhnvt_ph55017.md_02_datn.DAO.AddressDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.fragments.OrdersFragment;
import com.anhnvt_ph55017.md_02_datn.models.Address;
import com.anhnvt_ph55017.md_02_datn.models.Order;
import com.anhnvt_ph55017.md_02_datn.models.Product;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckOutActivity extends AppCompatActivity {

    RadioGroup paymentGroup;
    EditText edVoucher;
    Button btnOrder, btnChangeAddress;
    TextView tvSubtotal, tvTax, tvTotal;
    TextView tvShipName, tvShipPhone, tvShipAddress;
    RecyclerView rvSummary;

    List<Product> cartList;
    SummaryAdapter summaryAdapter;

    AddressDAO addressDAO;
    Address selectedAddress;

    // FIX: thay startActivityForResult (deprecated) bằng ActivityResultLauncher
    private final ActivityResultLauncher<Intent> addressLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            loadDefaultAddress();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

        // Ánh xạ view
        paymentGroup    = findViewById(R.id.paymentGroup);
        edVoucher       = findViewById(R.id.edVoucher);
        btnOrder        = findViewById(R.id.btnOrder);
        tvSubtotal      = findViewById(R.id.tvSubtotal);
        tvTax           = findViewById(R.id.tvTax);
        tvTotal         = findViewById(R.id.tvTotal);
        rvSummary       = findViewById(R.id.rvSummary);
        tvShipName      = findViewById(R.id.tvShipName);
        tvShipPhone     = findViewById(R.id.tvShipPhone);
        tvShipAddress   = findViewById(R.id.tvShipAddress);
        btnChangeAddress = findViewById(R.id.btnChangeAddress);

        addressDAO = new AddressDAO(this);
        loadDefaultAddress();

        // FIX: cast an toàn, tránh unchecked cast crash
        Object extra = getIntent().getSerializableExtra("cart");
        if (extra instanceof List) {
            cartList = (List<Product>) extra;
        } else {
            cartList = new ArrayList<>();
        }

        summaryAdapter = new SummaryAdapter(this, cartList);
        rvSummary.setLayoutManager(new LinearLayoutManager(this));
        rvSummary.setAdapter(summaryAdapter);

        calculateTotals();

        btnChangeAddress.setOnClickListener(v ->
                addressLauncher.launch(new Intent(this, ShippingAddressActivity.class))
        );

        btnOrder.setOnClickListener(v -> placeOrder());
    }

    // FIX: tách logic đặt hàng ra method riêng cho dễ đọc
    private void placeOrder() {
        if (paymentGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        // FIX: kiểm tra địa chỉ trước khi đặt hàng
        if (selectedAddress == null) {
            Toast.makeText(this, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cartList.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            return;
        }

        double subtotal = 0;
        int itemCount = 0;
        for (Product p : cartList) {
            subtotal += p.getPrice() * p.getQty();
            itemCount += p.getQty();
        }

        // FIX: dùng Locale.getDefault() tránh warning format
        String date = new SimpleDateFormat("MMM dd yyyy", Locale.getDefault()).format(new Date());
        String id = "OD-" + System.currentTimeMillis();

        Product first = cartList.get(0);
        Order newOrder = new Order(
                id, date, subtotal, "Đang xử lý",
                "TBD", itemCount,
                first.getImage(), first.getName(),
                first.getPrice(), first.getDescription()
        );

        OrdersFragment.addOrder(newOrder);

        Toast.makeText(this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();

        Intent home = new Intent(this, MainActivity.class);
        home.putExtra("openFragment", "home");
        home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(home);
        finish();
    }

    private void calculateTotals() {
        double subtotal = 0;
        for (Product p : cartList) {
            subtotal += p.getPrice() * p.getQty();
        }
        double tax   = subtotal * 0.1;
        double total = subtotal + tax;

        // FIX: dùng Locale.US để format số tiền nhất quán
        tvSubtotal.setText(String.format(Locale.US, "Subtotal: $%.2f", subtotal));
        tvTax.setText(String.format(Locale.US, "Tax: $%.2f", tax));
        tvTotal.setText(String.format(Locale.US, "Total: $%.2f", total));
    }

    private void loadDefaultAddress() {
        List<Address> all = addressDAO.getAddresses(1);
        if (all.isEmpty()) {
            addressDAO.addAddress(1, "Nhà riêng", "0123 456 789", "Số 1, Đường A, Quận 1", true);
            addressDAO.addAddress(1, "Công ty", "0987 654 321", "Tầng 7, Tòa nhà B, Quận 3", false);
            all = addressDAO.getAddresses(1);
        }

        selectedAddress = addressDAO.getDefaultAddress(1);
        if (selectedAddress == null && !all.isEmpty()) {
            selectedAddress = all.get(0);
        }

        if (selectedAddress != null) {
            tvShipName.setText(selectedAddress.getName());
            tvShipPhone.setText(selectedAddress.getPhone());
            tvShipAddress.setText(selectedAddress.getAddress());
        }
    }
}