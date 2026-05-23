package com.anhnvt_ph55017.md_02_datn.screens;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anhnvt_ph55017.md_02_datn.Adapters.RevenueTransactionAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.RevenueTransaction;
import com.anhnvt_ph55017.md_02_datn.utils.NetworkConstants;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RevenueActivity extends AppCompatActivity {

    private ProgressBar progressRevenue;
    private TextView tvRevenuePeriod;
    private TextView tvRevenueUpdatedAt;
    private View layoutRevenueSummary;
    private TextView tvRevenueMessage;
    private TextView tvRevenueEmpty;
    private TextView btnRevenueWeek;
    private TextView btnRevenueMonth;
    private TextView btnRevenueYear;
    private TextView tvRevenueChartAmount;
    private TextView tvRevenueChartNote;
    private TextView tvCardRevenueTotal;
    private TextView tvCardOrders;
    private TextView tvCardNewCustomers;
    private TextView tvCardProducts;
    private RecyclerView rvRevenueTransactions;

    private View[] chartBars;

    private final List<RevenueTransaction> revenueTransactions = new ArrayList<>();
    private RevenueTransactionAdapter revenueAdapter;
    private String currentPeriod = "month";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SessionManager.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue);

        ImageView btnBack = findViewById(R.id.btnBackRevenue);
        progressRevenue = findViewById(R.id.progressRevenue);
        tvRevenuePeriod = findViewById(R.id.tvRevenuePeriod);
        tvRevenueUpdatedAt = findViewById(R.id.tvRevenueUpdatedAt);
        tvRevenueMessage = findViewById(R.id.tvRevenueMessage);
        tvRevenueEmpty = findViewById(R.id.tvRevenueEmpty);
        btnRevenueWeek = findViewById(R.id.btnRevenueWeek);
        btnRevenueMonth = findViewById(R.id.btnRevenueMonth);
        btnRevenueYear = findViewById(R.id.btnRevenueYear);
        tvRevenueChartAmount = findViewById(R.id.tvRevenueChartAmount);
        tvRevenueChartNote = findViewById(R.id.tvRevenueChartDetails);
        tvCardRevenueTotal = findViewById(R.id.tvCardRevenueTotal);
        tvCardOrders = findViewById(R.id.tvCardOrders);
        tvCardNewCustomers = findViewById(R.id.tvCardNewCustomers);
        tvCardProducts = findViewById(R.id.tvCardProducts);
        layoutRevenueSummary = findViewById(R.id.layoutRevenueSummary);
        rvRevenueTransactions = findViewById(R.id.rvRevenueTransactions);

        chartBars = new View[]{
                findViewById(R.id.barRevenue1),
                findViewById(R.id.barRevenue2),
                findViewById(R.id.barRevenue3),
                findViewById(R.id.barRevenue4),
                findViewById(R.id.barRevenue5)
        };

        btnBack.setOnClickListener(v -> onBackPressed());
        initTransactionList();
        setupPeriodButtons();
        loadRevenue(currentPeriod);
    }

    private void initTransactionList() {
        revenueAdapter = new RevenueTransactionAdapter(this, revenueTransactions);
        rvRevenueTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvRevenueTransactions.setAdapter(revenueAdapter);
    }

    private void setupPeriodButtons() {
        View.OnClickListener periodClick = v -> {
            if (v == btnRevenueWeek) {
                loadRevenue("week");
            } else if (v == btnRevenueMonth) {
                loadRevenue("month");
            } else if (v == btnRevenueYear) {
                loadRevenue("year");
            }
        };

        btnRevenueWeek.setOnClickListener(periodClick);
        btnRevenueMonth.setOnClickListener(periodClick);
        btnRevenueYear.setOnClickListener(periodClick);
    }

    private void loadRevenue(String period) {
        currentPeriod = period;
        updatePeriodButtons(period);
        progressRevenue.setVisibility(View.VISIBLE);
        tvRevenueMessage.setVisibility(View.GONE);
        tvRevenueEmpty.setVisibility(View.GONE);

        String token = SessionManager.getToken(this);
        if (token == null || token.isEmpty()) {
            progressRevenue.setVisibility(View.GONE);
            showError("Không tìm được token đăng nhập");
            return;
        }

        String url = NetworkConstants.getApiBaseUrl() + "/api/shop/revenue?period=" + period;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    progressRevenue.setVisibility(View.GONE);
                    updateRevenueUi(response);
                },
                error -> {
                    progressRevenue.setVisibility(View.GONE);
                    showError("Không thể tải dữ liệu doanh thu");
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

    private void updateRevenueUi(JSONObject response) {
        if (response == null) {
            showError("Dữ liệu doanh thu không hợp lệ");
            return;
        }

        JSONObject summary = response.optJSONObject("summary");
        if (summary == null) {
            showError("Không có dữ liệu tóm tắt");
            return;
        }

        String period = response.optString("period", currentPeriod);
        tvRevenuePeriod.setText(getPeriodLabel(period));
        tvRevenueUpdatedAt.setText("CẬP NHẬT LÚC " + getCurrentTimeLabel());
        tvCardRevenueTotal.setText(formatPrice(summary.optDouble("totalGrossRevenue", 0)));
        tvCardOrders.setText(String.valueOf(summary.optInt("totalOrders", 0)));
        tvCardNewCustomers.setText(String.valueOf(summary.optInt("newCustomers", 0)));
        tvCardProducts.setText(String.valueOf(summary.optInt("totalProducts", 0)));
        tvRevenueChartAmount.setText(formatPrice(summary.optDouble("totalGrossRevenue", 0)));
        tvRevenueChartNote.setText(response.optString("chartNote", ""));
        layoutRevenueSummary.setVisibility(View.VISIBLE);

        JSONArray chartValues = null;
        JSONObject chartObject = response.optJSONObject("chart");
        if (chartObject != null) {
            chartValues = chartObject.optJSONArray("values");
        }
        updateChart(chartValues);

        JSONArray productDetails = response.optJSONArray("productDetails");
        revenueTransactions.clear();
        if (productDetails != null) {
            for (int i = 0; i < productDetails.length(); i++) {
                JSONObject item = productDetails.optJSONObject(i);
                if (item == null) continue;

                RevenueTransaction transaction = new RevenueTransaction(
                        item.optString("orderNumber", "#"),
                        item.optString("productName", "Sản phẩm"),
                        item.optString("productImage", ""),
                        item.optString("sku", ""),
                        item.optInt("quantity", 0),
                        item.optDouble("grossAmount", 0),
                        item.optDouble("platformFee", 0),
                        item.optDouble("netAmount", 0),
                        item.optString("paidAt", ""),
                        item.optString("feeStatus", item.optString("status", ""))
                );
                revenueTransactions.add(transaction);
            }
        }

        revenueAdapter.notifyDataSetChanged();
        tvRevenueEmpty.setVisibility(revenueTransactions.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateChart(JSONArray values) {
        if (values == null || values.length() == 0) {
            for (View bar : chartBars) {
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bar.getLayoutParams();
                params.height = dpToPx(32);
                bar.setLayoutParams(params);
            }
            return;
        }

        double max = 0;
        for (int i = 0; i < values.length() && i < chartBars.length; i++) {
            max = Math.max(max, values.optDouble(i, 0));
        }
        if (max <= 0) max = 1;

        for (int i = 0; i < chartBars.length; i++) {
            double value = values.optDouble(i, 0);
            int height = dpToPx(32) + (int) ((dpToPx(80) * value) / max);
            if (height < dpToPx(32)) {
                height = dpToPx(32);
            }
            View bar = chartBars[i];
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bar.getLayoutParams();
            params.height = height;
            bar.setLayoutParams(params);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void updatePeriodButtons(String period) {
        boolean isWeek = "week".equals(period);
        boolean isMonth = "month".equals(period);
        boolean isYear = "year".equals(period);

        setPeriodButtonStyle(btnRevenueWeek, isWeek);
        setPeriodButtonStyle(btnRevenueMonth, isMonth);
        setPeriodButtonStyle(btnRevenueYear, isYear);
    }

    private void setPeriodButtonStyle(TextView button, boolean selected) {
        button.setBackgroundResource(selected ? R.drawable.bg_btn_blue_rounded : R.drawable.bg_btn_outline);
        button.setTextColor(selected ? Color.WHITE : Color.parseColor("#0A6ED8"));
    }

    private String getPeriodLabel(String period) {
        switch (period) {
            case "week":
                return "Tuần này";
            case "year":
                return "Năm nay";
            case "month":
            default:
                return "Tháng này";
        }
    }

    private String getCurrentTimeLabel() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm, dd/MM", Locale.getDefault());
        return format.format(new Date());
    }

    private void showError(String message) {
        tvRevenueMessage.setText(message);
        tvRevenueMessage.setVisibility(View.VISIBLE);
        tvRevenueEmpty.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private String formatPrice(double price) {
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
        return formatter.format((long) price) + "đ";
    }
}
