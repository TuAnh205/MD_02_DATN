package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.anhnvt_ph55017.md_02_datn.Adapters.OrderAdapter;
import com.anhnvt_ph55017.md_02_datn.Adapters.RevenueTransactionAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.RevenueTransaction;
import com.anhnvt_ph55017.md_02_datn.utils.NetworkConstants;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

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
    private TextView tvPlatformFeeAmount;
    private RecyclerView rvRevenueTransactions;
    private LineChart revenueChart;

    private final List<RevenueTransaction> revenueTransactions = new ArrayList<>();
    private RevenueTransactionAdapter revenueAdapter;
    private String currentPeriod = "month";

    private final BroadcastReceiver orderStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (OrderAdapter.ACTION_ORDER_STATUS_CHANGED.equals(intent.getAction())) {
                loadRevenue(currentPeriod);
            }
        }
    };

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
        tvPlatformFeeAmount = findViewById(R.id.tvPlatformFeeAmount);
        layoutRevenueSummary = findViewById(R.id.layoutRevenueSummary);
        rvRevenueTransactions = findViewById(R.id.rvRevenueTransactions);
        revenueChart = findViewById(R.id.revenueChart);

        btnBack.setOnClickListener(v -> onBackPressed());
        initTransactionList();
        setupPeriodButtons();
        loadRevenue(currentPeriod);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                orderStatusReceiver,
                new IntentFilter(OrderAdapter.ACTION_ORDER_STATUS_CHANGED)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(orderStatusReceiver);
        super.onStop();
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
        layoutRevenueSummary.setVisibility(View.VISIBLE);

        JSONArray chartValues = null;
        JSONObject chartObject = response.optJSONObject("chart");
        if (chartObject != null) {
            chartValues = chartObject.optJSONArray("values");
        }

        boolean hasNegativeValues = updateChart(chartValues);
        String chartNote = response.optString("chartNote", "").trim();
        if (hasNegativeValues) {
            String note = "Biểu đồ đã được làm tròn về 0 để tránh đường cong âm do điều chỉnh tháng trước.";
            tvRevenueChartNote.setText(chartNote.isEmpty() ? note : chartNote + " " + note);
        } else if (!chartNote.isEmpty()) {
            tvRevenueChartNote.setText(chartNote);
        }

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

        double totalPlatformFee = calculatePlatformFee(productDetails);
        updatePlatformFeeAmount(totalPlatformFee);

        revenueAdapter.notifyDataSetChanged();
        tvRevenueEmpty.setVisibility(revenueTransactions.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private double calculatePlatformFee(JSONArray productDetails) {
        double total = 0;

        if (productDetails == null) {
            return 0;
        }

        for (int i = 0; i < productDetails.length(); i++) {
            JSONObject item = productDetails.optJSONObject(i);
            if (item == null) {
                continue;
            }

            String status = item.optString("feeStatus", item.optString("status", ""));
            if (!isDeliveredStatus(status)) {
                continue;
            }

            double platformFee = item.optDouble("platformFee", 0);
            if (platformFee > 0) {
                total += platformFee;
            } else {
                total += item.optDouble("grossAmount", 0) * 0.05;
            }
        }

        return Math.max(0, total);
    }

    private boolean isDeliveredStatus(String status) {
        if (status == null) {
            return false;
        }

        String normalized = status.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("delivered")
                || normalized.equals("đã giao")
                || normalized.equals("da giao")
                || normalized.equals("paid")
                || normalized.equals("charged")
                || normalized.equals("đã nhận")
                || normalized.equals("da nhan");
    }

    private void updatePlatformFeeAmount(double totalPlatformFee) {
        if (tvPlatformFeeAmount == null) {
            return;
        }
        tvPlatformFeeAmount.setText(formatPrice(totalPlatformFee));
    }

    private boolean updateChart(JSONArray values) {
        if (revenueChart == null) {
            return false;
        }

        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = getChartLabels();

        if (values == null || values.length() == 0) {
            revenueChart.clear();
            revenueChart.setNoDataText("Chưa có dữ liệu doanh thu");
            revenueChart.invalidate();
            return false;
        }

        boolean hasNegativeValues = false;
        double max = 0;
        for (int i = 0; i < values.length(); i++) {
            double rawValue = values.optDouble(i, 0);
            double visualValue = Math.max(0, rawValue);
            hasNegativeValues |= rawValue < 0;
            entries.add(new Entry(i, (float) visualValue));
            max = Math.max(max, visualValue);
        }
        if (max <= 0) max = 1;

        LineDataSet dataSet = new LineDataSet(entries, "Doanh thu");
        dataSet.setColor(Color.parseColor("#0A6ED8"));
        dataSet.setLineWidth(2.8f);
        dataSet.setCircleColor(Color.parseColor("#0A6ED8"));
        dataSet.setCircleRadius(4.5f);
        dataSet.setDrawCircleHole(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCubicIntensity(0.2f);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#DCEBFF"));
        dataSet.setFillAlpha(120);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        revenueChart.setData(lineData);
        revenueChart.getDescription().setEnabled(false);
        revenueChart.getLegend().setEnabled(false);
        revenueChart.setTouchEnabled(true);
        revenueChart.setDragEnabled(false);
        revenueChart.setScaleEnabled(false);
        revenueChart.setPinchZoom(false);
        revenueChart.setDrawGridBackground(false);
        revenueChart.getAxisRight().setEnabled(false);
        revenueChart.getAxisLeft().setDrawGridLines(true);
        revenueChart.getAxisLeft().setGridColor(Color.parseColor("#E5E7EB"));
        revenueChart.getAxisLeft().setTextColor(Color.parseColor("#6B7280"));
        revenueChart.getAxisLeft().setTextSize(10f);
        revenueChart.getAxisLeft().setAxisMinimum(0f);
        revenueChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        revenueChart.getXAxis().setGranularity(1f);
        revenueChart.getXAxis().setDrawGridLines(false);
        revenueChart.getXAxis().setTextColor(Color.parseColor("#6B7280"));
        revenueChart.getXAxis().setTextSize(10f);
        revenueChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        revenueChart.getXAxis().setLabelCount(Math.min(labels.size(), 5), true);
        revenueChart.animateX(600);
        revenueChart.invalidate();
        return hasNegativeValues;
    }

    private ArrayList<String> getChartLabels() {
        switch (currentPeriod) {
            case "week":
                return new ArrayList<>(List.of("T2", "T3", "T4", "T5", "CN"));
            case "year":
                return new ArrayList<>(List.of("Q1", "Q2", "Q3", "Q4"));
            case "month":
            default:
                return new ArrayList<>(List.of("Tuần 1", "Tuần 2", "Tuần 3", "Tuần 4", "Tuần 5"));
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
