package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.Adapters.AddressAdapter;
import com.anhnvt_ph55017.md_02_datn.utils.AddressApiService;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Address;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import java.util.List;

public class ShippingAddressActivity extends AppCompatActivity {

    private RecyclerView rvAddresses;
    private Button btnAddAddress;
    private Button btnUseAddress;
    private android.widget.TextView tvSelectedAddressName;
    private android.widget.TextView tvSelectedAddressDetail;
    private List<Address> addresses;
    private AddressAdapter adapter;
    private Address selectedAddress;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_address);

        android.widget.ImageButton btnBack = findViewById(R.id.btnBack);
        android.widget.TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        rvAddresses = findViewById(R.id.rvAddresses);
        btnAddAddress = findViewById(R.id.btnAddAddress);
        btnUseAddress = findViewById(R.id.btnUseAddress);
        tvSelectedAddressName = findViewById(R.id.tvSelectedAddressName);
        tvSelectedAddressDetail = findViewById(R.id.tvSelectedAddressDetail);

        userId = SessionManager.getUserId(this);
        if (userId <= 0) userId = 1;  // Fallback to user 1 if not logged in

        btnBack.setOnClickListener(v -> onBackPressed());
        tvHeaderTitle.setText("Địa chỉ giao hàng");

        loadAddressesFromBackend();

        btnAddAddress.setOnClickListener(v -> showAddressDialog(null));
        
        btnUseAddress.setOnClickListener(v -> {
            if (selectedAddress != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedAddressId", selectedAddress.getId());
                resultIntent.putExtra("selectedAddressName", selectedAddress.getName());
                resultIntent.putExtra("selectedAddressPhone", selectedAddress.getPhone());
                resultIntent.putExtra("selectedAddressDetail", selectedAddress.getAddress());
                setResult(RESULT_OK, resultIntent);
                Toast.makeText(this, "Đã chọn địa chỉ: " + selectedAddress.getName(), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Vui lòng chọn một địa chỉ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDefaultAddress() {
        if (addresses == null) return;
        for (Address addr : addresses) {
            if (addr.isDefault()) {
                selectedAddress = addr;
                updateSelectedAddressDisplay();
                break;
            }
        }
    }

    private void updateSelectedAddressDisplay() {
        if (selectedAddress != null) {
            tvSelectedAddressName.setText(selectedAddress.getName());
            tvSelectedAddressDetail.setText(selectedAddress.getPhone() + "\n" + selectedAddress.getAddress());
        }
    }

    private void loadAddressesFromBackend() {
        String token = SessionManager.getToken(this);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        AddressApiService.getAddresses(token, new AddressApiService.AddressListCallback() {
            @Override
            public void onSuccess(org.json.JSONArray addressesJson) {
                runOnUiThread(() -> {
                    addresses = new java.util.ArrayList<>();

                    for (int i = 0; i < addressesJson.length(); i++) {
                        org.json.JSONObject obj = addressesJson.optJSONObject(i);
                        if (obj == null) continue;

                        // ✅ FIX CHUẨN STRING (KHÔNG PARSE INT)
                        String id = obj.optString("_id", "");
                        String userId = obj.optString("userId", "");
                        String name = obj.optString("name", "");
                        String phone = obj.optString("phone", "");
                        String addressStr = obj.optString("address", "");
                        boolean isDefault = obj.optBoolean("isDefault", false);

                        Address addr = new Address(id, userId, name, phone, addressStr, isDefault);
                        addresses.add(addr);
                    }

                    adapter = new AddressAdapter(ShippingAddressActivity.this, addresses, new AddressAdapter.Listener() {
                        @Override
                        public void onSelect(Address address) {
                            selectedAddress = address;
                            updateSelectedAddressDisplay();
                        }

                        @Override
                        public void onEdit(Address address) {
                            showAddressDialog(address);
                        }

                        @Override
                        public void onDelete(Address address) {
                            Toast.makeText(ShippingAddressActivity.this, "Chưa hỗ trợ xoá", Toast.LENGTH_SHORT).show();
                        }
                    });

                    rvAddresses.setLayoutManager(new LinearLayoutManager(ShippingAddressActivity.this));
                    rvAddresses.setAdapter(adapter);

                    loadDefaultAddress();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() ->
                        Toast.makeText(ShippingAddressActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void refreshList() {
        loadAddressesFromBackend();
    }

    private void showAddressDialog(Address editing) {
        boolean isEdit = editing != null;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_address, null);
        com.google.android.material.textfield.TextInputEditText etName = dialogView.findViewById(R.id.etName);
        com.google.android.material.textfield.TextInputEditText etPhone = dialogView.findViewById(R.id.etPhone);
        com.google.android.material.textfield.TextInputEditText etAddress = dialogView.findViewById(R.id.etAddress);
        com.google.android.material.checkbox.MaterialCheckBox cbDefault = dialogView.findViewById(R.id.cbDefault);

        if (isEdit) {
            etName.setText(editing.getName());
            etPhone.setText(editing.getPhone());
            etAddress.setText(editing.getAddress());
            cbDefault.setChecked(editing.isDefault());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogTheme);
        builder.setTitle(isEdit ? "Sửa địa chỉ" : "Thêm địa chỉ mới");
        builder.setView(dialogView);

        builder.setPositiveButton(isEdit ? "Cập nhật" : "Thêm", null);
        builder.setNegativeButton("Hủy", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.getWindow().getDecorView().setBackgroundResource(R.drawable.bg_card);
                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                dialog.getWindow().setLayout(
                        (int)(screenWidth * 0.9),
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                );
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = etName.getText().toString().trim();
                String phone = etPhone.getText().toString().trim();
                String addr = etAddress.getText().toString().trim();
                boolean isDefault = cbDefault.isChecked();

                if (name.isEmpty() || phone.isEmpty() || addr.isEmpty()) {
                    Toast.makeText(ShippingAddressActivity.this, "Vui lòng điền đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isEdit) {
                    // TODO: Gọi API update address nếu muốn
                    Toast.makeText(ShippingAddressActivity.this, "Chức năng sửa chưa hỗ trợ backend", Toast.LENGTH_SHORT).show();
                } else {
                    String token = SessionManager.getToken(ShippingAddressActivity.this);
                    String userIdStr = SessionManager.getUserIdString(ShippingAddressActivity.this);
                    org.json.JSONObject addressJson = new org.json.JSONObject();
                    try {
                        addressJson.put("userId", userIdStr);
                        addressJson.put("name", name);
                        addressJson.put("phone", phone);
                        addressJson.put("address", addr);
                        addressJson.put("isDefault", isDefault);
                    } catch (Exception e) {}
                    AddressApiService.addAddress(token, addressJson, new AddressApiService.AddressCallback() {
                        @Override
                        public void onSuccess(org.json.JSONObject addressJson) {
                            runOnUiThread(() -> {
                                Toast.makeText(ShippingAddressActivity.this, "Đã thêm địa chỉ mới", Toast.LENGTH_SHORT).show();
                                refreshList();
                                dialog.dismiss();
                            });
                        }
                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> Toast.makeText(ShippingAddressActivity.this, "Lỗi thêm địa chỉ: " + error, Toast.LENGTH_SHORT).show());
                        }
                    });
                }
            });
        });
        dialog.show();
    }
}
