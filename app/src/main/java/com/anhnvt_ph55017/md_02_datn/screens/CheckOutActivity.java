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

import com.anhnvt_ph55017.md_02_datn.DAO.AddressDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.HomeActivity;
import com.anhnvt_ph55017.md_02_datn.fragments.HomeFragment;
import com.anhnvt_ph55017.md_02_datn.models.Address;
import com.anhnvt_ph55017.md_02_datn.models.Order;

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
    AddressDAO addressDAO;
    Address selectedAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

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

        addressDAO = new AddressDAO(this);
        loadDefaultAddress();

        cartList = (List<com.anhnvt_ph55017.md_02_datn.models.Product>) getIntent().getSerializableExtra("cart");
        if(cartList == null) cartList = new java.util.ArrayList<>();

        summaryAdapter = new com.anhnvt_ph55017.md_02_datn.Adapters.SummaryAdapter(this, cartList);
        rvSummary.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        rvSummary.setAdapter(summaryAdapter);

        calculateTotals();

        btnChangeAddress.setOnClickListener(v -> {
            startActivityForResult(new Intent(CheckOutActivity.this, ShippingAddressActivity.class), REQUEST_CODE_ADDRESS);
        });

        btnOrder.setOnClickListener(v -> {

            int checked = paymentGroup.getCheckedRadioButtonId();

            if(checked == -1){
                Toast.makeText(this,"Chọn phương thức thanh toán",Toast.LENGTH_SHORT).show();
                return;
            }

            String voucher = edVoucher.getText().toString();

            // create order object and add to history
            if(!cartList.isEmpty()){
                double subtotal = 0;
                int itemCount = 0;
                for(com.anhnvt_ph55017.md_02_datn.models.Product p : cartList){
                    subtotal += p.getPrice() * p.getQty();
                    itemCount += p.getQty();
                }
                String id = "OD-" + System.currentTimeMillis();
                String date = new java.text.SimpleDateFormat("MMM dd yyyy").format(new java.util.Date());
                String status = "Đang xử lý";
                String arrival = "TBD";
                int image = cartList.get(0).getImage();
                String name = cartList.get(0).getName();
                double price = cartList.get(0).getPrice();
                String desc = cartList.get(0).getDescription();

                com.anhnvt_ph55017.md_02_datn.models.Order newOrder =
                        new com.anhnvt_ph55017.md_02_datn.models.Order(id, date, subtotal, status,
                                arrival, itemCount, image, name, price, desc);

                com.anhnvt_ph55017.md_02_datn.fragments.OrdersFragment.addOrder(newOrder);
            }

            Intent home = new Intent(CheckOutActivity.this, MainActivity.class);
            Toast.makeText(this,"Thanh toán thành công!",Toast.LENGTH_LONG).show();
            home.putExtra("openFragment", "home");
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
        // ensure there are at least two sample addresses
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADDRESS && resultCode == RESULT_OK && data != null) {
            loadDefaultAddress();
        }
    }
}
