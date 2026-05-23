package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.fragments.BottomSheetCardPayment;
import com.anhnvt_ph55017.md_02_datn.models.Address;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.anhnvt_ph55017.md_02_datn.models.Voucher;
import com.anhnvt_ph55017.md_02_datn.utils.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CheckOutActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADDRESS = 2001;
    private static final int REQUEST_CODE_VOUCHER = 2002;

    RadioGroup paymentGroup;
    RadioButton payCard;
    AppCompatButton btnOrder, btnChangeAddress;
    LinearLayout layoutVoucherSelect;

    boolean cardInfoConfirmed = false;
    String cardHolderName = "";
    String cardNumber = "";
    String cardExpiry = "";
    String cardCVV = "";
    int previousPaymentId = -1;
    boolean isProgrammaticPaymentChange = false;

    TextView tvSubtotal, tvTax, tvTotal, tvVoucher;
    TextView tvShipName, tvShipPhone, tvShipAddress;
    TextView tvVoucherTitle, tvVoucherValue;

    androidx.recyclerview.widget.RecyclerView rvSummary;

    List<Product> cartList;
    com.anhnvt_ph55017.md_02_datn.Adapters.SummaryAdapter summaryAdapter;

    Address selectedAddress;
    Voucher selectedVoucher;

    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

        userId = SessionManager.getUserId(this);
        if (userId <= 0) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initView();
        setupCart();
        setupActions();
        loadDefaultAddress();
    }

    // ================= INIT =================
    private void initView() {
        paymentGroup = findViewById(R.id.paymentGroup);
        btnOrder = findViewById(R.id.btnOrder);
        btnChangeAddress = findViewById(R.id.btnChangeAddress);

        layoutVoucherSelect = findViewById(R.id.layoutVoucherSelect);
        tvVoucherTitle = findViewById(R.id.tvVoucherTitle);
        tvVoucherValue = findViewById(R.id.tvVoucherValue);

        payCard = findViewById(R.id.payCard);

        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTax = findViewById(R.id.tvTax);
        tvTotal = findViewById(R.id.tvTotal);
        tvVoucher = findViewById(R.id.tvVoucher);

        tvShipName = findViewById(R.id.tvShipName);
        tvShipPhone = findViewById(R.id.tvShipPhone);
        tvShipAddress = findViewById(R.id.tvShipAddress);

        rvSummary = findViewById(R.id.rvSummary);
    }

    private void setupCart() {
        cartList = (List<Product>) getIntent().getSerializableExtra("cart");
        if (cartList == null) cartList = new ArrayList<>();

        summaryAdapter = new com.anhnvt_ph55017.md_02_datn.Adapters.SummaryAdapter(this, cartList);
        rvSummary.setLayoutManager(new LinearLayoutManager(this));
        rvSummary.setAdapter(summaryAdapter);

        calculateTotals();
    }

    private void setupActions() {
        layoutVoucherSelect.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, VoucherSelectActivity.class), REQUEST_CODE_VOUCHER);
        });

        paymentGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (isProgrammaticPaymentChange) return;

            if (checkedId == R.id.payCard) {
                showCardPaymentSheet();
            } else {
                cardInfoConfirmed = false;
                payCard.setText("Thanh toán bằng thẻ");
                previousPaymentId = checkedId;
            }
        });

        btnChangeAddress.setOnClickListener(v -> {
            startActivityForResult(new Intent(this, ShippingAddressActivity.class), REQUEST_CODE_ADDRESS);
        });

        btnOrder.setOnClickListener(v -> handleOrder());
    }

    // ================= TÍNH TIỀN =================
    private void calculateTotals() {
        double subtotal = 0;

        for (Product p : cartList) {
            subtotal += p.getPrice() * p.getQty();
        }

        double discount = calculateDiscount(subtotal);
        double tax = subtotal * 0.1;
        double total = subtotal + tax - discount;

        String subtotalText = String.format("Tạm tính: %,.0f đ", subtotal);
        String taxText = String.format("Thuế: %,.0f đ", tax);
        String totalText = String.format("Tổng thanh toán: %,.0f đ", total);

        tvSubtotal.setText(subtotalText);
        tvTax.setText(taxText);
        tvTotal.setText(totalText);

        if (discount > 0) {
            tvVoucher.setVisibility(View.VISIBLE);
            tvVoucher.setText(String.format("Voucher: -%,.0f đ", discount));
        } else {
            tvVoucher.setVisibility(View.GONE);
        }
    }

    private double calculateDiscount(double subtotal) {
        if (selectedVoucher == null) return 0;

        if (subtotal < selectedVoucher.getMinOrderValue()) return 0;

        if ("percentage".equals(selectedVoucher.getType())) {
            double d = subtotal * selectedVoucher.getValue() / 100;
            return selectedVoucher.getMaxDiscount() > 0
                    ? Math.min(d, selectedVoucher.getMaxDiscount())
                    : d;
        } else {
            return selectedVoucher.getValue();
        }
    }

    // ================= ORDER =================
    private void handleOrder() {

        if (selectedAddress == null) {
            Toast.makeText(this, "Chọn địa chỉ trước", Toast.LENGTH_SHORT).show();
            return;
        }

        int checkedPaymentId = paymentGroup.getCheckedRadioButtonId();
        if (checkedPaymentId == -1) {
            Toast.makeText(this, "Chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isCardPayment = checkedPaymentId == R.id.payCard;
        if (isCardPayment && !cardInfoConfirmed) {
            Toast.makeText(this, "Vui lòng nhập thông tin thẻ qua BottomSheet", Toast.LENGTH_SHORT).show();
            showCardPaymentSheet();
            return;
        }

        try {
            JSONArray itemsArr = new JSONArray();
            double subtotal = 0;

            for (Product p : cartList) {
                subtotal += p.getPrice() * p.getQty();

                JSONObject item = new JSONObject();
                item.put("product", p.getId());
                item.put("name", p.getName());   // 👈 THÊM DÒNG NÀY
                item.put("price", p.getPrice()); // 👈 nên thêm luôn (tránh lỗi sau)
                item.put("qty", p.getQty());
                itemsArr.put(item);
            }

            double discount = calculateDiscount(subtotal);
            double total = subtotal - discount;

            // ===== SHIPPING =====
            JSONObject addressObj = new JSONObject();
            addressObj.put("name", selectedAddress.getName());
            addressObj.put("phone", selectedAddress.getPhone());
            addressObj.put("address", selectedAddress.getAddress());
            addressObj.put("city", selectedAddress.getCity());
            addressObj.put("district", selectedAddress.getDistrict());
            addressObj.put("ward", selectedAddress.getWard());

            JSONObject shippingObj = new JSONObject();
            shippingObj.put("address", addressObj);
            shippingObj.put("method", "standard");
            shippingObj.put("fee", 0);

            // ===== BODY =====
            JSONObject body = new JSONObject();
            body.put("items", itemsArr);
            body.put("subtotal", subtotal);
            body.put("total", total);
            body.put("shipping", shippingObj);

            JSONObject paymentObj = new JSONObject();
            paymentObj.put("method", isCardPayment ? "card" : "cod");
            paymentObj.put("status", "pending");
            if (isCardPayment) {
                paymentObj.put("cardholderName", cardHolderName);
                paymentObj.put("cardLastFour", cardNumber.substring(Math.max(0, cardNumber.length() - 4)));
            }
            body.put("payment", paymentObj);

            // ===== FIX DISCOUNT (QUAN TRỌNG NHẤT) =====
            JSONObject discountObj = new JSONObject();

            if (selectedVoucher != null) {
                String type = selectedVoucher.getType();

                // fallback tránh null hoặc ""
                if (type == null || type.isEmpty()) type = "fixed";

                discountObj.put("type", type);
                discountObj.put("value", selectedVoucher.getValue());
                discountObj.put("amount", discount);
                discountObj.put("code", selectedVoucher.getCode());

                body.put("voucherCode", selectedVoucher.getCode());
            } else {
                discountObj.put("type", "fixed");
                discountObj.put("value", 0);
                discountObj.put("amount", 0);
            }

            body.put("discount", discountObj);

            Log.d("ORDER_BODY", body.toString());

            String token = SessionManager.getToken(this);

            OrderApiService.createOrder(this, token, body, new OrderApiService.CreateOrderCallback() {
                @Override
                public void onSuccess(JSONObject res) {
                    if (isCardPayment) {
                        String orderId = res.optString("_id", res.optString("id", ""));
                        if (orderId.isEmpty()) {
                            runOnUiThread(() ->
                                    Toast.makeText(CheckOutActivity.this, "Không lấy được ID đơn hàng", Toast.LENGTH_SHORT).show()
                            );
                            return;
                        }

                        try {
                            JSONObject cardData = new JSONObject();
                            cardData.put("cardNumber", cardNumber);
                            cardData.put("cardholderName", cardHolderName);
                            cardData.put("expiryDate", cardExpiry);
                            cardData.put("cvv", cardCVV);

                            OrderApiService.processPayment(CheckOutActivity.this, token, orderId, "card", cardData,
                                    new OrderApiService.ProcessPaymentCallback() {
                                        @Override
                                        public void onSuccess(JSONObject paymentResponse) {
                                            runOnUiThread(() -> {
                                                Intent intent = new Intent(
                                                        CheckOutActivity.this,
                                                        ThanhCongScreen.class
                                                );
                                                removeBoughtItemsFromCart();
                                                startActivity(intent);
                                                finish();
                                            });
                                        }

                                        @Override
                                        public void onError(String err) {
                                            runOnUiThread(() ->
                                                    Toast.makeText(CheckOutActivity.this, "Lỗi thanh toán: " + err, Toast.LENGTH_LONG).show()
                                            );
                                        }
                                    });
                        } catch (Exception e) {
                            runOnUiThread(() ->
                                    Toast.makeText(CheckOutActivity.this, "Lỗi xử lý thông tin thẻ", Toast.LENGTH_SHORT).show()
                            );
                        }
                    } else {
                        runOnUiThread(() -> {
                            Intent intent = new Intent(
                                    CheckOutActivity.this,
                                    ThanhCongScreen.class
                            );
                            removeBoughtItemsFromCart();
                            startActivity(intent);
                            finish();
                        });
                    }
                }

                @Override
                public void onError(String err) {
                    runOnUiThread(() ->
                            Toast.makeText(CheckOutActivity.this, "Lỗi: " + err, Toast.LENGTH_SHORT).show()
                    );
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showCardPaymentSheet() {
        BottomSheetCardPayment sheet = BottomSheetCardPayment.newInstance(
                cardHolderName,
                cardNumber,
                cardExpiry,
                cardCVV,
                new BottomSheetCardPayment.CardPaymentListener() {
                    @Override
                    public void onCardPaymentConfirmed(String holder, String number, String expiry, String cvv) {
                        cardInfoConfirmed = true;
                        cardHolderName = holder;
                        cardNumber = number;
                        cardExpiry = expiry;
                        cardCVV = cvv;
                        payCard.setText("Thanh toán bằng thẻ (đã nhập)");
                        previousPaymentId = R.id.payCard;
                    }

                    @Override
                    public void onCardPaymentCancelled() {
                        if (!cardInfoConfirmed) {
                            isProgrammaticPaymentChange = true;
                            if (previousPaymentId != -1) {
                                paymentGroup.check(previousPaymentId);
                            } else {
                                paymentGroup.clearCheck();
                            }
                            isProgrammaticPaymentChange = false;
                        }
                    }
                }
        );
        sheet.show(getSupportFragmentManager(), "CardPaymentBottomSheet");
    }

    //load adress
    private void loadDefaultAddress() {
        String token = SessionManager.getToken(this);

        AddressApiService.getAddresses(token, new AddressApiService.AddressListCallback() {
            @Override
            public void onSuccess(JSONArray data) {
                runOnUiThread(() -> {
                    try {
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);

                            if (obj.optBoolean("isDefault", false)) {

                                selectedAddress = new Address(
                                        obj.getString("_id"),
                                        String.valueOf(userId),
                                        obj.getString("name"),
                                        obj.getString("phone"),
                                        obj.getString("address"),
                                        obj.getString("city"),
                                        obj.getString("district"),
                                        obj.getString("ward"),
                                        true
                                );

                                updateAddressDisplay();
                                return; // 👈 thấy default là dừng luôn
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(CheckOutActivity.this, "Không load được địa chỉ", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }
    // ================= XÓA CART =================
    private void removeBoughtItemsFromCart() {
        String token = SessionManager.getToken(this);

        for (Product p : cartList) {
            CartApiService.removeFromCart(this, token, p.getCartItemId(), null);
        }

        goToMain();
    }

    private void goToMain() {
        Intent intent = new Intent(CheckOutActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ================= ADDRESS =================
    private void updateAddressDisplay() {
        if (selectedAddress == null) return;

        tvShipName.setText(selectedAddress.getName());
        tvShipPhone.setText(selectedAddress.getPhone());

        String full = selectedAddress.getAddress()
                + ", " + selectedAddress.getWard()
                + ", " + selectedAddress.getDistrict()
                + ", " + selectedAddress.getCity();

        tvShipAddress.setText(full);
    }

    // ================= RESULT =================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK || data == null) return;

        if (requestCode == REQUEST_CODE_ADDRESS) {
            selectedAddress = new Address(
                    data.getStringExtra("selectedAddressId"),
                    String.valueOf(userId),
                    data.getStringExtra("selectedAddressName"),
                    data.getStringExtra("selectedAddressPhone"),
                    data.getStringExtra("selectedAddressDetail"),
                    data.getStringExtra("selectedAddressCity"),
                    data.getStringExtra("selectedAddressDistrict"),
                    data.getStringExtra("selectedAddressWard"),
                    true
            );
            updateAddressDisplay();
        }

        else if (requestCode == REQUEST_CODE_VOUCHER) {
            selectedVoucher = (Voucher) data.getSerializableExtra("voucher");

            if (selectedVoucher != null) {
                tvVoucherTitle.setText(selectedVoucher.getName());

                if ("percentage".equals(selectedVoucher.getType())) {
                    tvVoucherValue.setText("-" + (int) selectedVoucher.getValue() + "%");
                } else {
                    tvVoucherValue.setText("-" + (int) selectedVoucher.getValue() + "đ");
                }

                calculateTotals();
            }
        }
    }
}