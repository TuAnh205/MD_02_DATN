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
import androidx.appcompat.widget.AppCompatButton;
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

import org.json.JSONArray;
import org.json.JSONObject;

public class CheckOutActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADDRESS = 2001;

    RadioGroup paymentGroup;
    EditText edVoucher;
    AppCompatButton btnOrder;
    TextView tvSubtotal, tvTax, tvTotal;
    androidx.recyclerview.widget.RecyclerView rvSummary;
    List<com.anhnvt_ph55017.md_02_datn.models.Product> cartList;
    com.anhnvt_ph55017.md_02_datn.Adapters.SummaryAdapter summaryAdapter;

    // shipping
    TextView tvShipName, tvShipPhone, tvShipAddress;
    AppCompatButton btnChangeAddress;

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
            String city = intent.getStringExtra("selectedAddressCity");
            String district = intent.getStringExtra("selectedAddressDistrict");
            String ward = intent.getStringExtra("selectedAddressWard");
            String id = intent.getStringExtra("selectedAddressId");
            String userIdStr = String.valueOf(userId);
            selectedAddress = new Address(id != null ? id : "", userIdStr, name, phone, addressStr, city, district, ward, true);
            updateAddressDisplay();
        } else {
            // Nếu chưa chọn, tự động lấy địa chỉ mặc định từ backend
            String token = com.anhnvt_ph55017.md_02_datn.utils.SessionManager.getToken(this);
            if (token != null && !token.isEmpty()) {
                com.anhnvt_ph55017.md_02_datn.utils.AddressApiService.getAddresses(token, new com.anhnvt_ph55017.md_02_datn.utils.AddressApiService.AddressListCallback() {
                    @Override
                    public void onSuccess(org.json.JSONArray addressesJson) {
                        runOnUiThread(() -> {
                            for (int i = 0; i < addressesJson.length(); i++) {
                                org.json.JSONObject obj = addressesJson.optJSONObject(i);
                                if (obj == null) continue;
                                boolean isDefault = obj.optBoolean("isDefault", false);
                                if (isDefault) {
                                    String id = obj.optString("_id", "");
                                    String userIdStr = obj.optString("userId", "");
                                    String name = obj.optString("name", "");
                                    String phone = obj.optString("phone", "");
                                    String addressStr = obj.optString("address", "");
                                    String city = obj.optString("city", "");
                                    String district = obj.optString("district", "");
                                    String ward = obj.optString("ward", "");
                                    selectedAddress = new Address(id, userIdStr, name, phone, addressStr, city, district, ward, true);
                                    updateAddressDisplay();
                                    break;
                                }
                            }
                        });
                    }
                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> Toast.makeText(CheckOutActivity.this, "Lỗi tải địa chỉ: " + error, Toast.LENGTH_SHORT).show());
                    }
                });
            }
        }

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

            // Log thông tin địa chỉ giao hàng để debug
            if (selectedAddress != null) {
                android.util.Log.d("CHECKOUT_ADDRESS_DEBUG", "city=" + selectedAddress.getCity() + ", district=" + selectedAddress.getDistrict() + ", ward=" + selectedAddress.getWard());
            }

            int checkedId = paymentGroup.getCheckedRadioButtonId();
            if (checkedId == -1) {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }
            String voucher = edVoucher.getText().toString();
            if (!cartList.isEmpty()) {
                double subtotal = 0;
                double total = 0;
                JSONArray itemsArr = new JSONArray();
                for (com.anhnvt_ph55017.md_02_datn.models.Product p : cartList) {
                    subtotal += p.getPrice() * p.getQty();
                    total += p.getPrice() * p.getQty();
                    JSONObject itemObj = new JSONObject();
                    try {
                        itemObj.put("product", p.getId());
                        itemObj.put("name", p.getName());
                        itemObj.put("price", p.getPrice());
                        itemObj.put("qty", p.getQty());
                        itemObj.put("image", p.getImageUrl() != null ? p.getImageUrl() : "");
                    } catch (Exception e) {}
                    itemsArr.put(itemObj);
                }
                // Thông tin địa chỉ giao hàng
                JSONObject shippingObj = new JSONObject();
                JSONObject shippingAddressObj = new JSONObject();
                try {
                    if (selectedAddress != null) {
                        shippingAddressObj.put("name", selectedAddress.getName());
                        shippingAddressObj.put("phone", selectedAddress.getPhone());
                        shippingAddressObj.put("address", selectedAddress.getAddress());
                        shippingAddressObj.put("city", selectedAddress.getCity() != null ? selectedAddress.getCity() : "");
                        shippingAddressObj.put("district", selectedAddress.getDistrict() != null ? selectedAddress.getDistrict() : "");
                        shippingAddressObj.put("ward", selectedAddress.getWard() != null ? selectedAddress.getWard() : "");
                    }
                    shippingObj.put("address", shippingAddressObj);
                } catch (Exception e) {}

                // Thông tin thanh toán
                JSONObject paymentObj = new JSONObject();
                try {
                    if (checkedId == R.id.payCOD) {
                        paymentObj.put("method", "cod");
                    } else if (checkedId == R.id.payCard) {
                        paymentObj.put("method", "card");
                    } else {
                        paymentObj.put("method", "other");
                    }
                } catch (Exception e) {}

                JSONObject orderBody = new JSONObject();
                try {
                    orderBody.put("items", itemsArr);
                    orderBody.put("subtotal", subtotal);
                    orderBody.put("total", total);
                    orderBody.put("shipping", shippingObj);
                    orderBody.put("payment", paymentObj);
                } catch (Exception e) {}

                String token = com.anhnvt_ph55017.md_02_datn.utils.SessionManager.getToken(this);
                com.anhnvt_ph55017.md_02_datn.utils.OrderApiService.createOrder(this, token, orderBody, new com.anhnvt_ph55017.md_02_datn.utils.OrderApiService.CreateOrderCallback() {
                    @Override
                    public void onSuccess(org.json.JSONObject orderJson) {
                        runOnUiThread(() -> {
                            NotificationManager.incrementNotification(CheckOutActivity.this);
                            Toast.makeText(CheckOutActivity.this, "Thanh toán thành công!", Toast.LENGTH_LONG).show();
                            // Clear cart in backend
                            String token = com.anhnvt_ph55017.md_02_datn.utils.SessionManager.getToken(CheckOutActivity.this);
                            com.anhnvt_ph55017.md_02_datn.utils.CartApiService.clearCart(CheckOutActivity.this, token, new com.anhnvt_ph55017.md_02_datn.utils.CartApiService.CartCallback() {
                                @Override
                                public void onSuccess(org.json.JSONObject cartJson) {
                                    // Sau khi clear, chuyển về trang chủ
                                    Intent home = new Intent(CheckOutActivity.this, MainActivity.class);
                                    home.putExtra("openFragment", "orders");
                                    home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(home);
                                    finish();
                                }
                                @Override
                                public void onError(String error) {
                                    // Nếu lỗi vẫn chuyển về trang chủ
                                    Intent home = new Intent(CheckOutActivity.this, MainActivity.class);
                                    home.putExtra("openFragment", "orders");
                                    home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(home);
                                    finish();
                                }
                            });
                        });
                    }
                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> Toast.makeText(CheckOutActivity.this, "Lỗi đặt hàng: " + error, Toast.LENGTH_LONG).show());
                    }
                });
            }
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
            StringBuilder addressBuilder = new StringBuilder();
            addressBuilder.append(selectedAddress.getAddress());
            if (selectedAddress.getWard() != null && !selectedAddress.getWard().isEmpty()) {
                addressBuilder.append(", ").append(selectedAddress.getWard());
            }
            if (selectedAddress.getDistrict() != null && !selectedAddress.getDistrict().isEmpty()) {
                addressBuilder.append(", ").append(selectedAddress.getDistrict());
            }
            if (selectedAddress.getCity() != null && !selectedAddress.getCity().isEmpty()) {
                addressBuilder.append(", ").append(selectedAddress.getCity());
            }
            tvShipAddress.setText(addressBuilder.toString());
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
            String city = data.getStringExtra("selectedAddressCity");
            String district = data.getStringExtra("selectedAddressDistrict");
            String ward = data.getStringExtra("selectedAddressWard");
            String id = data.getStringExtra("selectedAddressId");
            String userIdStr = String.valueOf(userId);
            selectedAddress = new Address(id != null ? id : "", userIdStr, name, phone, addressStr, city, district, ward, true);
            updateAddressDisplay();
        }
    }
}
