package com.anhnvt_ph55017.md_02_datn.screens;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.utils.NetworkConstants;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CreateVoucherActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    private boolean isSubmitting = false;

    private ImageView imgVoucherBanner;
    private TextView tvAddBanner;
    private EditText edtVoucherCode, edtProgramName, edtDiscountValue, edtDiscountMax, edtTotalUsage, edtUsagePerCustomer, edtStartDate, edtEndDate;
    private RadioGroup rgDiscountType;
    private Button btnCreateVoucher;
    private Uri bannerUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_voucher);
        initViews();
        setupListeners();
    }

    private void initViews() {
        imgVoucherBanner = findViewById(R.id.imgVoucherBanner);
        tvAddBanner = findViewById(R.id.tvAddBanner);
        edtVoucherCode = findViewById(R.id.edtVoucherCode);
        edtProgramName = findViewById(R.id.edtProgramName);
        rgDiscountType = findViewById(R.id.rgDiscountType);
        edtDiscountValue = findViewById(R.id.edtDiscountValue);

        edtTotalUsage = findViewById(R.id.edtTotalUsage);
        edtUsagePerCustomer = findViewById(R.id.edtUsagePerCustomer);
        edtStartDate = findViewById(R.id.edtStartDate);
        edtEndDate = findViewById(R.id.edtEndDate);
        btnCreateVoucher = findViewById(R.id.btnCreateVoucher);
    }

    private void setupListeners() {
        tvAddBanner.setOnClickListener(v -> openImagePicker());
        imgVoucherBanner.setOnClickListener(v -> openImagePicker());
        edtStartDate.setOnClickListener(v -> showDatePicker(edtStartDate));
        edtEndDate.setOnClickListener(v -> showDatePicker(edtEndDate));
        btnCreateVoucher.setOnClickListener(v -> submitVoucher());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            bannerUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), bannerUri);
                imgVoucherBanner.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Không thể tải ảnh đại diện", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDatePicker(EditText target) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
            target.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void submitVoucher() {
        if (isSubmitting) {
            return;
        }

        String code = edtVoucherCode.getText().toString().trim();
        String program = edtProgramName.getText().toString().trim();
        String discountValue = edtDiscountValue.getText().toString().trim();
        String discountMax = edtDiscountMax.getText().toString().trim();
        String totalUsage = edtTotalUsage.getText().toString().trim();
        String usagePerCustomer = edtUsagePerCustomer.getText().toString().trim();
        String startDate = edtStartDate.getText().toString().trim();
        String endDate = edtEndDate.getText().toString().trim();
        boolean isPercentage = rgDiscountType.getCheckedRadioButtonId() == R.id.rbPercent;

        if (TextUtils.isEmpty(code) || TextUtils.isEmpty(program) || TextUtils.isEmpty(discountValue)
                || TextUtils.isEmpty(totalUsage) || TextUtils.isEmpty(usagePerCustomer)
                || TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = SessionManager.getToken(this);
        if (TextUtils.isEmpty(token)) {
            Toast.makeText(this, "Bạn cần đăng nhập để tạo voucher", Toast.LENGTH_SHORT).show();
            return;
        }

        String isoStartDate;
        String isoEndDate;
        try {
            isoStartDate = toIsoDate(startDate);
            isoEndDate = toIsoDate(endDate);
        } catch (ParseException e) {
            Toast.makeText(this, "Ngày không hợp lệ, vui lòng chọn lại", Toast.LENGTH_SHORT).show();
            return;
        }

        double value;
        int usageLimit;
        int userLimit;
        try {
            value = Double.parseDouble(discountValue);
            usageLimit = Integer.parseInt(totalUsage);
            userLimit = Integer.parseInt(usagePerCustomer);
            if (value <= 0 || usageLimit <= 0 || userLimit <= 0) {
                throw new NumberFormatException("Giá trị phải lớn hơn 0");
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Vui lòng nhập số hợp lệ cho mức giảm và giới hạn sử dụng", Toast.LENGTH_SHORT).show();
            return;
        }

        Double maxDiscount = null;
        if (!TextUtils.isEmpty(discountMax)) {
            try {
                maxDiscount = Double.parseDouble(discountMax);
                if (maxDiscount <= 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Giảm tối đa phải là số lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final Double finalMaxDiscount = maxDiscount;
        isSubmitting = true;
        btnCreateVoucher.setEnabled(false);

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(NetworkConstants.getApiBaseUrl() + "/api/admin/vouchers");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("code", code.toUpperCase(Locale.ROOT));
                payload.put("name", program);
                payload.put("description", "Tạo từ ứng dụng");
                payload.put("type", isPercentage ? "percentage" : "fixed");
                payload.put("value", value);
                payload.put("minOrderValue", 0);
                payload.put("usageLimit", usageLimit);
                payload.put("userLimit", userLimit);
                payload.put("startDate", isoStartDate);
                payload.put("endDate", isoEndDate);
                payload.put("isActive", true);
                if (finalMaxDiscount != null) {
                    payload.put("maxDiscount", finalMaxDiscount);
                }

                byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body);
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                InputStream inputStream = responseCode >= 200 && responseCode < 300 ? conn.getInputStream() : conn.getErrorStream();
                String response = readStream(inputStream);

                runOnUiThread(() -> {
                    isSubmitting = false;
                    btnCreateVoucher.setEnabled(true);
                    if (responseCode >= 200 && responseCode < 300) {
                        Log.d("CreateVoucherActivity", "Tạo voucher thành công: " + code.toUpperCase(Locale.ROOT));
                        Toast.makeText(this, "Tạo voucher thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Tạo voucher thất bại: " + response, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    isSubmitting = false;
                    btnCreateVoucher.setEnabled(true);
                    Toast.makeText(this, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).start();
    }

    private String toIsoDate(String input) throws ParseException {
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        inputFormat.setLenient(false);
        java.util.Date parsed = inputFormat.parse(input);
        if (parsed == null) {
            throw new ParseException("Invalid date", 0);
        }
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        outputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return outputFormat.format(parsed);
    }

    private String readStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();
        return builder.toString();
    }
}
