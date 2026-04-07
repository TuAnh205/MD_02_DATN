package com.anhnvt_ph55017.md_02_datn.screens;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anhnvt_ph55017.md_02_datn.R;
import com.bumptech.glide.Glide;
import com.anhnvt_ph55017.md_02_datn.utils.NetworkConstants;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddProductActivity extends AppCompatActivity {

    private EditText edtProductName, edtPrice, edtDescription, edtStock, edtImageUrl;
    private Spinner spinnerCategory;
    private TextView tvCharCount;
    private ImageView ivProductImage1, ivProductImage2, ivRemoveImage;
    private Button btnCancel, btnSave, btnAddPhoto;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int PERMISSION_REQUEST = 3;
    private String selectedImagePath = "";
    private String selectedImageUrl = "";
    private Bitmap selectedImageBitmap = null;
    private List<String> categories = new ArrayList<>();
    private boolean isFirstImage = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        // Map UI elements
        edtProductName = findViewById(R.id.edtProductName);
        edtPrice = findViewById(R.id.edtPrice);
        edtDescription = findViewById(R.id.edtDescription);
        edtStock = findViewById(R.id.edtStock);
        edtImageUrl = findViewById(R.id.edtImageUrl);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        tvCharCount = findViewById(R.id.tvCharCount);
        ivProductImage1 = findViewById(R.id.ivProductImage1);
        ivProductImage2 = findViewById(R.id.ivProductImage2);
        ivRemoveImage = findViewById(R.id.ivRemoveImage);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        btnCancel = findViewById(R.id.btnCancel);
        btnSave = findViewById(R.id.btnSave);

        // Back button
        findViewById(R.id.ivBack).setOnClickListener(v -> onBackPressed());

        // Load categories
        loadCategories();

        // Description character counter
        edtDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = s.length();
                tvCharCount.setText(length + "/500");
                if (length > 500) {
                    edtDescription.setText(s.subSequence(0, 500));
                    edtDescription.setSelection(500);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Add photo button
        btnAddPhoto.setOnClickListener(v -> chooseImage());

        // Image URL handler
        edtImageUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String url = s.toString().trim();
                if (!url.isEmpty()) {
                    selectedImageUrl = url;
                    // Load image from URL using Glide
                    try {
                        Glide.with(AddProductActivity.this)
                                .load(url)
                                .into(ivProductImage2);
                        ivRemoveImage.setVisibility(android.view.View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Cancel button
        btnCancel.setOnClickListener(v -> onBackPressed());

        // Save button
        btnSave.setOnClickListener(v -> saveProduct());

        // Remove image button
        ivRemoveImage.setOnClickListener(v -> {
            ivProductImage2.setImageResource(android.R.color.transparent);
            ivRemoveImage.setVisibility(android.view.View.GONE);
            selectedImagePath = "";
            selectedImageBitmap = null;
            selectedImageUrl = "";
            edtImageUrl.setText("");
        });
    }

    private void chooseImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    selectedImageBitmap = bitmap;
                    ivProductImage2.setImageBitmap(bitmap);
                    ivRemoveImage.setVisibility(android.view.View.VISIBLE);
                    selectedImagePath = imageUri.toString();
                } catch (IOException e) {
                    Toast.makeText(this, "Lỗi tải ảnh", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void saveProduct() {
        String name = edtProductName.getText().toString().trim();
        String price = edtPrice.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String stock = edtStock.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (name.isEmpty() || price.isEmpty() || stock.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if either image source is available
        if (selectedImageBitmap == null && selectedImageUrl.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ảnh sản phẩm hoặc nhập URL ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create JSON body
        JSONObject productData = new JSONObject();
        try {
            productData.put("name", name);
            productData.put("price", Double.parseDouble(price));
            productData.put("stock", Integer.parseInt(stock));
            productData.put("category", category);
            productData.put("description", description);

            // Handle both Bitmap (Base64) and URL image modes
            if (selectedImageBitmap != null) {
                String base64Image = bitmapToBase64(selectedImageBitmap);
                productData.put("image", "data:image/jpeg;base64," + base64Image);
            } else {
                // Use URL string directly
                productData.put("image", selectedImageUrl);
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Lỗi tạo dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get token
        String token = SessionManager.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Không tìm được token đăng nhập", Toast.LENGTH_LONG).show();
            return;
        }

        // Send request to server
        String url = NetworkConstants.getApiBaseUrl() + "/api/shop/products";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                productData,
                response -> {
                    Toast.makeText(this, "Lưu sản phẩm thành công", Toast.LENGTH_SHORT).show();
                    finish();
                },
                error -> {
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        Toast.makeText(this, "Lỗi lưu sản phẩm: " + statusCode, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Không thể kết nối server", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void loadCategories() {
        String url = NetworkConstants.getApiBaseUrl() + "/products/categories";

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    categories.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            categories.add(response.getString(i));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // Setup spinner adapter
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            categories
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategory.setAdapter(adapter);
                },
                error -> {
                    Toast.makeText(this, "Không thể tải danh mục", Toast.LENGTH_SHORT).show();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }
}
