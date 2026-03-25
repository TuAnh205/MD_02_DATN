package com.anhnvt_ph55017.md_02_datn.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomSheetProductOptions extends BottomSheetDialogFragment {

    private Product product;
    private OnAddToCartListener listener;

    private RadioGroup rgColor, rgStorage;
    private ImageButton btnMinus, btnPlus;
    private TextView tvQty, tvProductName, tvPrice, tvStock;
    private Button btnAdd;

    private int quantity = 1;

    public static BottomSheetProductOptions newInstance(Product product, OnAddToCartListener listener) {
        BottomSheetProductOptions fragment = new BottomSheetProductOptions();
        fragment.product = product;
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_product_options, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        // 🔥 QUAN TRỌNG: để bo góc hiển thị
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ánh xạ view
        tvProductName = view.findViewById(R.id.tvProductName);
        tvPrice = view.findViewById(R.id.tvPrice);
        tvStock = view.findViewById(R.id.tvStock);
        rgColor = view.findViewById(R.id.rgColor);
        rgStorage = view.findViewById(R.id.rgStorage);
        btnMinus = view.findViewById(R.id.btnMinus);
        btnPlus = view.findViewById(R.id.btnPlus);
        tvQty = view.findViewById(R.id.tvQty);
        btnAdd = view.findViewById(R.id.btnAddToCart);

        // set data sản phẩm
        if (product != null) {
            tvProductName.setText(product.getName());
            tvPrice.setText("$" + String.format("%.2f", product.getPrice()));
            tvStock.setText("Stock: " + product.getStock());
        }

        // chọn mặc định
        if (rgColor.getChildCount() > 0) {
            ((RadioButton) rgColor.getChildAt(0)).setChecked(true);
        }

        if (rgStorage.getChildCount() > 0) {
            ((RadioButton) rgStorage.getChildAt(0)).setChecked(true);
        }

        updateQtyDisplay();

        // listener cho màu
        rgColor.setOnCheckedChangeListener((group, checkedId) -> {
            for (int i = 0; i < group.getChildCount(); i++) {
                ((RadioButton) group.getChildAt(i)).refreshDrawableState();
            }
        });

        // listener cho GB
        rgStorage.setOnCheckedChangeListener((group, checkedId) -> {
            for (int i = 0; i < group.getChildCount(); i++) {
                ((RadioButton) group.getChildAt(i)).refreshDrawableState();
            }
        });

        // xử lý tăng giảm số lượng
        btnPlus.setOnClickListener(v -> {
            if (product != null && quantity < product.getStock() && quantity < 10) {
                quantity++;
                updateQtyDisplay();
            }
        });

        btnMinus.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                updateQtyDisplay();
            }
        });

        // thêm vào giỏ
        btnAdd.setOnClickListener(v -> addToCart(view));
    }

    private void updateQtyDisplay() {
        tvQty.setText(String.format("%02d", quantity));
    }

    private void addToCart(View view) {
        if (product == null) return;

        // lấy màu
        int colorId = rgColor.getCheckedRadioButtonId();
        RadioButton colorBtn = view.findViewById(colorId);
        String color = (colorBtn != null) ? colorBtn.getText().toString() : "Black";

        // lấy dung lượng
        int storageId = rgStorage.getCheckedRadioButtonId();
        RadioButton storageBtn = view.findViewById(storageId);
        String storageStr = (storageBtn != null) ? storageBtn.getText().toString() : "64GB";

        int storage = 64;
        try {
            storage = Integer.parseInt(storageStr.replace("GB", ""));
        } catch (Exception ignored) {}

        // set dữ liệu
        product.setColor(color);
        product.setStorage(storage);
        product.setQty(quantity);

        // callback
        if (listener != null) {
            listener.onAddToCart(product);
        }

        dismiss();
    }

    public interface OnAddToCartListener {
        void onAddToCart(Product product);
    }
}