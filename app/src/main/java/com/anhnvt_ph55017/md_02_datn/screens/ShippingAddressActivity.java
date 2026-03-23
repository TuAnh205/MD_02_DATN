package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import java.util.List;

public class ShippingAddressActivity extends AppCompatActivity {

    private RecyclerView rvAddresses;
    private Button btnAddAddress;
    private AddressDAO addressDAO;
    private List<Address> addresses;
    private AddressAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipping_address);

        android.widget.ImageButton btnBack = findViewById(R.id.btnBack);
        android.widget.TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        rvAddresses = findViewById(R.id.rvAddresses);
        btnAddAddress = findViewById(R.id.btnAddAddress);

        addressDAO = new AddressDAO(this);

        btnBack.setOnClickListener(v -> onBackPressed());
        tvHeaderTitle.setText("Địa chỉ");

        loadAddresses();

        btnAddAddress.setOnClickListener(v -> showAddressDialog(null));
    }

    private void loadAddresses() {
        // userId is currently hardcoded to 1
        addresses = addressDAO.getAddresses(1);

        // if first run, create 2 sample addresses
        if (addresses.isEmpty()) {
            addressDAO.addAddress(1, "Nhà riêng", "0123 456 789", "Số 1, Đường A, Quận 1", true);
            addressDAO.addAddress(1, "Công ty", "0987 654 321", "Tầng 7, Tòa nhà B, Quận 3", false);
            addresses = addressDAO.getAddresses(1);
        }

        adapter = new AddressAdapter(this, addresses, new AddressAdapter.Listener() {
            @Override
            public void onSelect(Address address) {
                addressDAO.setDefault(1, address.getId());
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
        addresses.addAll(addressDAO.getAddresses(1));
        adapter.notifyDataSetChanged();
    }

    private void showAddressDialog(Address editing) {
        boolean isEdit = editing != null;

        android.widget.LinearLayout container = new android.widget.LinearLayout(this);
        container.setOrientation(android.widget.LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        EditText etName = new EditText(this);
        etName.setHint("Tên người nhận");
        if (isEdit) etName.setText(editing.getName());
        container.addView(etName);

        EditText etPhone = new EditText(this);
        etPhone.setHint("Số điện thoại");
        etPhone.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        if (isEdit) etPhone.setText(editing.getPhone());
        container.addView(etPhone);

        EditText etAddress = new EditText(this);
        etAddress.setHint("Địa chỉ");
        if (isEdit) etAddress.setText(editing.getAddress());
        container.addView(etAddress);

        android.widget.CheckBox cbDefault = new android.widget.CheckBox(this);
        cbDefault.setText("Sử dụng làm địa chỉ mặc định");
        cbDefault.setTextColor(android.graphics.Color.WHITE);
        if (isEdit) cbDefault.setChecked(editing.isDefault());
        container.addView(cbDefault);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isEdit ? "Sửa địa chỉ" : "Thêm địa chỉ");
        builder.setView(container);

        builder.setPositiveButton(isEdit ? "Cập nhật" : "Thêm", null);
        builder.setNegativeButton("Huỷ", null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
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
                    addressDAO.addAddress(1, name, phone, addr, cbDefault.isChecked());
                }

                refreshList();
                dialog.dismiss();
            });
        });

        dialog.show();
    }
}
