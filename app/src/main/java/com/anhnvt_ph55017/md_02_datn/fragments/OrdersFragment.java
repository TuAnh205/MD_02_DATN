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


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OrdersFragment extends Fragment {

    private static final int REQUEST_CODE_DETAIL = 1001;

    RecyclerView rvOrders;
    OrderAdapter adapter;
    static List<Order> orderList;    // shared history
    List<Order> filteredList;
    
    TextView tvAll, tvPending, tvProcessing, tvShipping, tvCancelled;
    String selectedStatus = "ALL";
    // Các trạng thái mới
    // Tất cả, Chờ xác nhận, Xác nhận, Chưa thanh toán, Đã hủy

    // keep single reference for callbacks


    public OrdersFragment() {
        // Required empty public constructor

    }

    // Hàm khởi tạo giao diện fragment, thiết lập recycler và dữ liệu
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        rvOrders = view.findViewById(R.id.rvOrders);
        
        // Initialize status tabs
        tvAll = view.findViewById(R.id.tvAll);
        tvPending = view.findViewById(R.id.tvPending);
        tvProcessing = view.findViewById(R.id.tvProcessing);
        tvShipping = view.findViewById(R.id.tvShipping);
        // Không còn tab Đã nhận
        tvCancelled = view.findViewById(R.id.tvCancelled);

        // Lấy token từ SessionManager
        filteredList = new ArrayList<>();
        adapter = new OrderAdapter(getContext(), filteredList, order -> {
            Intent intent = new Intent(getContext(), com.anhnvt_ph55017.md_02_datn.screens.OrderDetailActivity.class);
            intent.putExtra("orderId", order.getId());
            intent.putExtra("orderDate", order.getDate());
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
            // Truyền list<OrderItem> qua intent
            if (order.getItems() != null) {
                java.io.Serializable itemsSerializable = (java.io.Serializable) order.getItems();
                intent.putExtra("orderItems", itemsSerializable);
            }
            startActivityForResult(intent, REQUEST_CODE_DETAIL);
        });

        // Gọi API lấy đơn hàng từ backend
        String token = SessionManager.getToken(getContext());
        com.anhnvt_ph55017.md_02_datn.utils.OrderApiService.getOrders(getContext(), token, new com.anhnvt_ph55017.md_02_datn.utils.OrderApiService.OrdersCallback() {
            @Override
            public void onSuccess(org.json.JSONArray ordersJson) {
                if (orderList == null) orderList = new ArrayList<>();
                orderList.clear();
                filteredList.clear();
                for (int i = 0; i < ordersJson.length(); i++) {
                    try {
                        org.json.JSONObject obj = ordersJson.getJSONObject(i);
                        Log.d("ORDER_PARSE_DEBUG", "Raw order json: " + obj.toString());
                        String id = obj.optString("_id");
                        String date = obj.optString("createdAt");
                        double total = obj.optDouble("total");
                        String status = obj.optString("status");
                        String paymentMethod = obj.optJSONObject("payment") != null ? obj.optJSONObject("payment").optString("method", "") : "";
                        String shippingAddress = "";
                        if (obj.has("shipping")) {
                            org.json.JSONObject ship = obj.optJSONObject("shipping");
                            if (ship != null && ship.has("address")) {
                                org.json.JSONObject addr = ship.optJSONObject("address");
                                if (addr != null) {
                                    shippingAddress = addr.optString("address", "");
                                }
                            }
                        }
                        int itemCount = obj.has("items") ? obj.getJSONArray("items").length() : 0;
                        // Lấy imageUrl của sản phẩm đầu tiên trong đơn hàng (nếu có)
                        String imageUrl = null;
                        if (obj.has("items")) {
                            org.json.JSONArray itemsArr = obj.getJSONArray("items");
                            if (itemsArr.length() > 0) {
                                org.json.JSONObject firstItem = itemsArr.getJSONObject(0);
                                // Ưu tiên lấy trường "image" trực tiếp
                                imageUrl = firstItem.optString("image", null);
                                // Nếu không có, thử lấy từ product.images[0]
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
                        // Parse danh sách hàng hóa
                        List<OrderItem> orderItems = new ArrayList<>();
                        if (obj.has("items")) {
                            org.json.JSONArray itemsArr = obj.getJSONArray("items");
                            for (int j = 0; j < itemsArr.length(); j++) {
                                org.json.JSONObject itemObj = itemsArr.getJSONObject(j);
                                String productName = itemObj.optString("name", "");
                                double price = itemObj.optDouble("price", 0);
                                int quantity = itemObj.optInt("quantity", 1);
                                int imageRes = R.drawable.bg_image;
                                // Lấy image từ item hoặc product.images[0]
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
                                // Nếu muốn truyền imageUrl, cần sửa OrderItem cho phù hợp
                                OrderItem orderItem = new OrderItem(productName, price, quantity, imageRes, itemImageUrl);
                                orderItems.add(orderItem);
                            }
                        }
                        Order order = new Order(id, date, total, status, "", itemCount, shippingAddress, orderItems, paymentMethod, imageUrl);
                        Log.d("ORDER_PARSE_DEBUG", "Parsed order: id=" + id + ", date=" + date + ", total=" + total + ", status=" + status + ", itemCount=" + itemCount + ", imageUrl=" + imageUrl);
                        orderList.add(order);
                        Log.d("ORDER_PARSE_DEBUG", "Order added to orderList: id=" + id);
                    } catch (Exception e) {
                        Log.e("ORDER_PARSE", e.getMessage(), e);
                    }
                }
                filteredList.addAll(orderList);
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String error) {
                Log.e("ORDER_API_ERROR", error);
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    // Hiển thị thông báo lỗi nếu muốn
                });
            }
        });

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);
        
        // Set up tab click listeners
        setupTabListeners();
        setTabActive(tvAll);
        if (orderList == null) orderList = new ArrayList<>();
        filterByStatus("ALL");

        return view;
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
                    .filter(order -> statusMatch(order.getStatus(), status))
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
    private boolean statusMatch(String backendStatus, String tabStatus) {
        backendStatus = backendStatus.trim().toLowerCase();
        if (tabStatus.equals("Chờ xác nhận")) {
            // Các trạng thái chờ xác nhận phổ biến
            return backendStatus.equals("pending") || backendStatus.equals("chờ xác nhận") || backendStatus.equals("chua xac nhan") || backendStatus.equals("cho xac nhan");
        }
        if (tabStatus.equals("Xác nhận")) {
            // Các trạng thái đã xác nhận phổ biến
            return backendStatus.equals("processing") || backendStatus.equals("xác nhận") || backendStatus.equals("da xac nhan") || backendStatus.equals("xac nhan") || backendStatus.equals("confirmed");
        }
        if (tabStatus.equals("Chưa thanh toán")) {
            // Các trạng thái chưa thanh toán phổ biến
            return backendStatus.equals("unpaid") || backendStatus.equals("chưa thanh toán") || backendStatus.equals("chua thanh toan");
        }
        if (tabStatus.equals("Đã hủy")) return isCancelled(backendStatus);
        return backendStatus.equals(tabStatus);
    }

    private boolean isCancelled(String status) {
        return status.equalsIgnoreCase("cancelled") || status.equals("Đã hủy");
    }
    
    // Nhận kết quả trả về từ OrderDetailActivity (thay đổi trạng thái)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DETAIL && resultCode == getActivity().RESULT_OK && data != null) {
            String id = data.getStringExtra("orderId");
            String newStatus = data.getStringExtra("newStatus");
            if (id != null && newStatus != null) {
                // Reload orders from database to get updated status
                // Không reload từ local DB nữa, chỉ filter lại danh sách đã lấy từ backend
                filterByStatus(selectedStatus);
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
        // reset tất cả
        tvAll.setTextColor(getResources().getColor(R.color.white, null));
        tvAll.setAlpha(0.6f);
        tvPending.setTextColor(getResources().getColor(R.color.white, null));
        tvPending.setAlpha(0.6f);
        tvProcessing.setTextColor(getResources().getColor(R.color.white, null));
        tvProcessing.setAlpha(0.6f);
        tvShipping.setTextColor(getResources().getColor(R.color.white, null));
        tvShipping.setAlpha(0.6f);
        tvCancelled.setTextColor(getResources().getColor(R.color.white, null));
        tvCancelled.setAlpha(0.6f);
        // đánh dấu tab đang hoạt động
        activeTab.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
        activeTab.setAlpha(1f);
    }
}