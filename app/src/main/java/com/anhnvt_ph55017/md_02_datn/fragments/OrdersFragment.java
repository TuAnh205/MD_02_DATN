package com.anhnvt_ph55017.md_02_datn.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;

import com.anhnvt_ph55017.md_02_datn.Adapters.OrderAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Order;
import com.anhnvt_ph55017.md_02_datn.models.OrderItem;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class OrdersFragment extends Fragment {

    private static final int REQUEST_CODE_DETAIL = 1001;

    RecyclerView rvOrders;
    OrderAdapter adapter;
    List<Order> orderList;    // instance variable, avoid static
    List<Order> filteredList;

    TextView tvAll, tvPending, tvProcessing, tvShipping, tvCancelled, tvOrdersEmpty;
    String selectedStatus = "ALL";

    public OrdersFragment() {
        // Required empty public constructor
    }

    // Hàm khởi tạo giao diện fragment, thiết lập recycler và dữ liệu
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        rvOrders = view.findViewById(R.id.rvOrders);
        tvOrdersEmpty = view.findViewById(R.id.tvOrdersEmpty);
        // Always re-init orderList to avoid static bugs
        orderList = new ArrayList<>();

        // Initialize status tabs
        tvAll = view.findViewById(R.id.tvAll);
        tvPending = view.findViewById(R.id.tvPending);
        tvProcessing = view.findViewById(R.id.tvProcessing);
        tvShipping = view.findViewById(R.id.tvShipping);
        tvCancelled = view.findViewById(R.id.tvCancelled);

        filteredList = new ArrayList<>();
        adapter = new OrderAdapter(getContext(), filteredList, order -> {
            Intent intent = new Intent(getContext(), com.anhnvt_ph55017.md_02_datn.screens.OrderDetailActivity.class);
            intent.putExtra("orderId", order.getId());
            intent.putExtra("orderDate", order.getFormattedDate());
            intent.putExtra("orderTotal", order.getTotal());
            intent.putExtra("orderStatus", order.getStatus());
            intent.putExtra("arrivalDate", order.getArrivalDate());
            intent.putExtra("itemCount", order.getItemCount());
            intent.putExtra("imageRes", order.getImageRes());
            intent.putExtra("productName", order.getProductName());
            intent.putExtra("productPrice", order.getProductPrice());
            intent.putExtra("productDesc", order.getProductDesc());
            intent.putExtra("shippingAddress", order.getShippingAddress());
            intent.putExtra("paymentMethod", order.getPaymentMethod());
            double vDisc = order.getVoucherDiscount();
            String vDiscStr = vDisc > 0 ? String.format(Locale.getDefault(), "%,.0fđ", vDisc) : "0đ";
            intent.putExtra("voucherDiscount", vDiscStr);
            if (order.getItems() != null) {
                java.io.Serializable itemsSerializable = (java.io.Serializable) order.getItems();
                intent.putExtra("orderItems", itemsSerializable);
            }
            startActivityForResult(intent, REQUEST_CODE_DETAIL);
        });

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);

        setupTabListeners();
        setTabActive(tvAll);
        filterByStatus("ALL");
        loadOrders();

        return view;
    }

    private void loadOrders() {
        String token = SessionManager.getToken(getContext());
        com.anhnvt_ph55017.md_02_datn.utils.OrderApiService.getOrders(getContext(), token, new com.anhnvt_ph55017.md_02_datn.utils.OrderApiService.OrdersCallback() {
            @Override
            public void onSuccess(org.json.JSONArray ordersJson) {
                orderList.clear();
                for (int i = 0; i < ordersJson.length(); i++) {
                    try {
                        org.json.JSONObject obj = ordersJson.getJSONObject(i);
                        Order order = parseOrder(obj);
                        if (order != null) {
                            orderList.add(order);
                        }
                    } catch (Exception e) {
                        Log.e("ORDER_PARSE", e.getMessage(), e);
                    }
                }
                sortOrdersByDate(orderList);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (tvOrdersEmpty != null) {
                            tvOrdersEmpty.setVisibility(orderList.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                        filterByStatus(selectedStatus);
                    });
                }
            }

            @Override
            public void onError(String error) {
                Log.e("ORDER_API_ERROR", error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (tvOrdersEmpty != null) {
                            tvOrdersEmpty.setVisibility(View.VISIBLE);
                            tvOrdersEmpty.setText("Không thể tải lịch sử đơn hàng. Vui lòng thử lại.");
                        }
                    });
                }
            }
        });
    }

    private Order parseOrder(org.json.JSONObject obj) throws Exception {
        Log.d("ORDER_PARSE_DEBUG", "Raw order json: " + obj.toString());
        String id = obj.optString("_id");
        String date = obj.optString("createdAt");
        double total = obj.optDouble("total");
        String status = obj.optString("status");
        org.json.JSONObject paymentObj = obj.optJSONObject("payment");
        String paymentMethod = mapPaymentMethod(paymentObj != null ? paymentObj.optString("method", "") : "");
        String paymentStatus = paymentObj != null ? paymentObj.optString("status", "") : "";

        String shippingAddress = "";
        if (obj.has("shipping")) {
            org.json.JSONObject ship = obj.optJSONObject("shipping");
            if (ship != null && ship.has("address")) {
                org.json.JSONObject addr = ship.optJSONObject("address");
                if (addr != null) {
                    String street = addr.optString("address", "");
                    String district = addr.optString("district", "");
                    String city = addr.optString("city", "");
                    String ward = addr.optString("ward", "");
                    StringBuilder addressBuilder = new StringBuilder();
                    if (!street.isEmpty()) addressBuilder.append(street);
                    if (!ward.isEmpty()) addressBuilder.append(addressBuilder.length() > 0 ? ", " : "").append(ward);
                    if (!district.isEmpty()) addressBuilder.append(addressBuilder.length() > 0 ? ", " : "").append(district);
                    if (!city.isEmpty()) addressBuilder.append(addressBuilder.length() > 0 ? ", " : "").append(city);
                    shippingAddress = addressBuilder.toString();
                }
            }
        }

        int itemCount = obj.has("items") ? obj.getJSONArray("items").length() : 0;
        String imageUrl = null;
        if (obj.has("items")) {
            org.json.JSONArray itemsArr = obj.getJSONArray("items");
            if (itemsArr.length() > 0) {
                org.json.JSONObject firstItem = itemsArr.getJSONObject(0);
                imageUrl = firstItem.optString("image", null);
                if ((imageUrl == null || imageUrl.isEmpty()) && firstItem.has("product")) {
                    org.json.JSONObject productObj = firstItem.optJSONObject("product");
                    if (productObj != null && productObj.has("images")) {
                        org.json.JSONArray imagesArr = productObj.optJSONArray("images");
                        if (imagesArr != null && imagesArr.length() > 0) {
                            imageUrl = imagesArr.optString(0, null);
                        }
                    }
                }
            }
        }

        List<OrderItem> orderItems = new ArrayList<>();
        if (obj.has("items")) {
            org.json.JSONArray itemsArr = obj.getJSONArray("items");
            for (int j = 0; j < itemsArr.length(); j++) {
                org.json.JSONObject itemObj = itemsArr.getJSONObject(j);
                String productName = itemObj.optString("name", "");
                double price = itemObj.optDouble("price", 0);
                int quantity = itemObj.optInt("quantity", 1);
                int imageRes = R.drawable.bg_image;
                String itemImageUrl = itemObj.optString("image", null);
                if ((itemImageUrl == null || itemImageUrl.isEmpty()) && itemObj.has("product")) {
                    org.json.JSONObject productObj = itemObj.optJSONObject("product");
                    if (productObj != null && productObj.has("images")) {
                        org.json.JSONArray imagesArr = productObj.optJSONArray("images");
                        if (imagesArr != null && imagesArr.length() > 0) {
                            itemImageUrl = imagesArr.optString(0, null);
                        }
                    }
                }
                orderItems.add(new OrderItem(productName, price, quantity, imageRes, itemImageUrl));
            }
        }

        Order order = new Order(id, date, total, status, "", itemCount, shippingAddress, orderItems, paymentMethod, imageUrl);
        order.setPaymentStatus(paymentStatus);

        double voucherDiscount = 0;
        if (obj.has("voucher")) {
            org.json.JSONObject v = obj.optJSONObject("voucher");
            if (v != null) {
                voucherDiscount = v.optDouble("discount", v.optDouble("amount", 0));
            }
        }
        if (voucherDiscount == 0 && obj.has("discount")) {
            org.json.JSONObject discountObj = obj.optJSONObject("discount");
            if (discountObj != null) {
                voucherDiscount = discountObj.optDouble("amount", discountObj.optDouble("discount", 0));
            } else {
                voucherDiscount = obj.optDouble("discount", 0);
            }
        }
        if (voucherDiscount == 0) {
            voucherDiscount = obj.optDouble("voucherDiscount", 0);
        }
        order.setVoucherDiscount(voucherDiscount);
        Log.d("ORDER_PARSE_DEBUG", "Parsed order: id=" + id + ", date=" + date + ", total=" + total + ", status=" + status + ", paymentStatus=" + paymentStatus + ", itemCount=" + itemCount + ", imageUrl=" + imageUrl);
        return order;
    }

    private void sortOrdersByDate(List<Order> orders) {
        Collections.sort(orders, (first, second) -> Long.compare(parseOrderTimestamp(second.getDate()), parseOrderTimestamp(first.getDate())));
    }

    private long parseOrderTimestamp(String date) {
        if (date == null || date.isEmpty()) {
            return 0L;
        }

        String normalized = date.replace("Z", "+00:00");
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
                Date parsed = format.parse(normalized);
                if (parsed != null) {
                    return parsed.getTime();
                }
            } catch (Exception ignored) {
            }
        }

        return 0L;
    }

    private String mapPaymentMethod(String rawMethod) {
        if (rawMethod == null || rawMethod.trim().isEmpty()) {
            return "Thanh toán khi nhận hàng";
        }

        String value = rawMethod.trim();
        if (value.equalsIgnoreCase("COD")
                || value.equalsIgnoreCase("cod")
                || value.equalsIgnoreCase("cash_on_delivery")
                || value.equalsIgnoreCase("cash on delivery")
                || value.equalsIgnoreCase("cashondelivery")) {
            return "Thanh toán khi nhận hàng";
        }

        return value;
    }

    // Thiết lập sự kiện click cho các tab trạng thái
    private void setupTabListeners() {
        tvAll.setOnClickListener(v -> filterByStatus("ALL"));
        tvPending.setOnClickListener(v -> filterByStatus("Chờ xác nhận"));
        tvProcessing.setOnClickListener(v -> filterByStatus("Xác nhận"));
        tvShipping.setOnClickListener(v -> filterByStatus("Chưa thanh toán"));
        tvCancelled.setOnClickListener(v -> filterByStatus("Đã hủy"));
    }

    // Lọc danh sách đơn theo trạng thái được chọn
    private void filterByStatus(String status) {
        selectedStatus = status;
        filteredList.clear();
        if (orderList == null) orderList = new ArrayList<>();
        if (status.equals("ALL")) {
            filteredList.addAll(orderList);
        } else {
            filteredList.addAll(orderList.stream()
                    .filter(order -> statusMatch(order, status))
                    .collect(Collectors.toList()));
        }
        adapter.notifyDataSetChanged();

        if (status.equals("ALL")) setTabActive(tvAll);
        else if (status.equals("Chờ xác nhận")) setTabActive(tvPending);
        else if (status.equals("Xác nhận")) setTabActive(tvProcessing);
        else if (status.equals("Chưa thanh toán")) setTabActive(tvShipping);
        else if (status.equals("Đã hủy")) setTabActive(tvCancelled);
    }

    // So khớp trạng thái tiếng Anh/Việt
    private boolean statusMatch(Order order, String tabStatus) {
        String backendStatus = order.getStatus() != null ? order.getStatus().trim().toLowerCase() : "";
        String paymentStatus = order.getPaymentStatus() != null ? order.getPaymentStatus().trim().toLowerCase() : "";
        if (tabStatus.equals("Chờ xác nhận")) {
            // Các trạng thái chờ xác nhận phổ biến
            return backendStatus.equals("pending") || backendStatus.equals("chờ xác nhận") || backendStatus.equals("chua xac nhan") || backendStatus.equals("cho xac nhan");
        }
        if (tabStatus.equals("Xác nhận")) {
            // Các trạng thái đã xác nhận phổ biến
            return backendStatus.equals("processing") || backendStatus.equals("xác nhận") || backendStatus.equals("da xac nhan") || backendStatus.equals("xac nhan") || backendStatus.equals("confirmed") || backendStatus.equals("đã xác nhận");
        }
        if (tabStatus.equals("Chưa thanh toán")) {
            // Các đơn chưa thanh toán nghĩa là payment.pending hoặc trạng thái pending/chưa thanh toán
            return paymentStatus.equals("pending") || backendStatus.equals("unpaid") || backendStatus.equals("chưa thanh toán") || backendStatus.equals("chua thanh toan") || backendStatus.equals("pending");
        }
        if (tabStatus.equals("Đã hủy")) return isCancelled(backendStatus);
        return backendStatus.equals(tabStatus);
    }

    private boolean isCancelled(String status) {
        status = status.trim().toLowerCase();
        // Các biến thể phổ biến của trạng thái đã hủy
        return status.equals("canceled") || status.equals("cancelled") || status.equals("đã hủy") || status.equals("da huy") || status.equals("huy") || status.equals("cancel") || status.equals("đã bị hủy") || status.equals("da bi huy");
    }

    // Nhận kết quả trả về từ OrderDetailActivity (thay đổi trạng thái)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DETAIL && resultCode == getActivity().RESULT_OK && data != null) {
            boolean cancelled = data.getBooleanExtra("orderCancelled", false);
            if (cancelled) {
                loadOrders();
            }
        }
    }

    // provide static helper for other activities


    @Override
    public void onResume() {
        super.onResume();
        // Reload orders from database when fragment resumes
        // refresh in case orders were added while away (chỉ cần filter lại, không reload local)
        filterByStatus(selectedStatus);
    }

    // trạng thái đã là tiếng Việt, trả trực tiếp
    private String getStatusVietnamese(String status) {
        return status;
    }

    // Cập nhật kiểu hiển thị khi tab được chọn, tất cả tab khác mờ đi
    private void setTabActive(TextView activeTab) {
        // reset tất cả về màu đen mờ
        int black = getResources().getColor(R.color.black, null);
        tvAll.setTextColor(black);
        tvAll.setAlpha(0.6f);
        tvPending.setTextColor(black);
        tvPending.setAlpha(0.6f);
        tvProcessing.setTextColor(black);
        tvProcessing.setAlpha(0.6f);
        tvShipping.setTextColor(black);
        tvShipping.setAlpha(0.6f);
        tvCancelled.setTextColor(black);
        tvCancelled.setAlpha(0.6f);
        // đánh dấu tab đang hoạt động: màu xanh, đậm
        activeTab.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
        activeTab.setAlpha(1f);
    }
}