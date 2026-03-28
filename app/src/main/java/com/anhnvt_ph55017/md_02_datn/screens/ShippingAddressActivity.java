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
import com.anhnvt_ph55017.md_02_datn.DAO.AddressDAO;
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
    private AddressDAO addressDAO;
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

        addressDAO = new AddressDAO(this);
        userId = SessionManager.getUserId(this);
        if (userId <= 0) userId = 1;  // Fallback to user 1 if not logged in

        btnBack.setOnClickListener(v -> onBackPressed());
        tvHeaderTitle.setText("Địa chỉ giao hàng");

        loadAddresses();
        loadDefaultAddress();

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

    private void loadAddresses() {
        // Load addresses for current user
        addresses = addressDAO.getAddresses(userId);

        // if first run, create sample addresses
        if (addresses.isEmpty()) {
            addressDAO.addAddress(userId, "Nhà riêng", "0123 456 789", "Số 1, Đường A, Quận 1", true);
            addressDAO.addAddress(userId, "Công ty", "0987 654 321", "Tầng 7, Tòa nhà B, Quận 3", false);
            addresses = addressDAO.getAddresses(userId);
        }

        adapter = new AddressAdapter(this, addresses, new AddressAdapter.Listener() {
            @Override
            public void onSelect(Address address) {
                selectedAddress = address;
                updateSelectedAddressDisplay();
                addressDAO.setDefault(userId, address.getId());
                refreshList();
            }

            @Override
            public void onEdit(Address address) {
                showAddressDialog(address);
            }

            @Override
            public void onDelete(Address address) {
                new AlertDialog.Builder(ShippingAddressActivity.this)
                        .setTitle("Xóa địa chỉ")
                        .setMessage("Bạn có chắc muốn xóa địa chỉ này không?")
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            addressDAO.deleteAddress(address.getId());
                            refreshList();
                            loadDefaultAddress();
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        rvAddresses.setLayoutManager(new LinearLayoutManager(this));
        rvAddresses.setAdapter(adapter);
    }

    private void refreshList() {
        addresses.clear();
        addresses.addAll(addressDAO.getAddresses(userId));
        adapter.notifyDataSetChanged();
    }

    private void showAddressDialog(Address editing) {
        boolean isEdit = editing != null;

        // Inflate custom dialog layout
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
        
        // Style dialog
        dialog.setOnShowListener(d -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
                dialog.getWindow().getDecorView().setBackgroundResource(R.drawable.bg_card);
                
                // Set dialog width to 90% of screen width
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

                if (name.isEmpty() || phone.isEmpty() || addr.isEmpty()) {
                    Toast.makeText(ShippingAddressActivity.this, "Vui lòng điền đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isEdit) {
                    addressDAO.updateAddress(editing.getId(), name, phone, addr, cbDefault.isChecked());
                } else {
                    addressDAO.addAddress(userId, name, phone, addr, cbDefault.isChecked());
                }

                refreshList();
                loadDefaultAddress();
                dialog.dismiss();
            });
        });

        dialog.show();
    }
}
