package com.anhnvt_ph55017.md_02_datn.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.Intent;

import com.anhnvt_ph55017.md_02_datn.Adapters.OrderAdapter;
import com.anhnvt_ph55017.md_02_datn.DAO.OrderDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Order;
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
    OrderDAO orderDAO;
    
    TextView tvAll, tvPending, tvProcessing, tvShipping, tvDelivered, tvCancelled;
    String selectedStatus = "ALL";
    String[] statusValues = {"Chưa thanh toán", "Đang xử lý", "Đang giao hàng", "Đã nhận", "Đã hủy"};

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
        tvDelivered = view.findViewById(R.id.tvDelivered);
        tvCancelled = view.findViewById(R.id.tvCancelled);

        // Initialize OrderDAO and load orders from database
        if (getContext() != null) {
            orderDAO = new OrderDAO(getContext());
            
            if (orderList == null) {
                orderList = new ArrayList<>();
            }
            
            // Load orders from database for current user
            int userId = SessionManager.getUserId(getContext());
            if (userId <= 0) userId = 1;  // Fallback to user 1 if not logged in
            
            List<Order> dbOrders = orderDAO.getOrdersByUserId(userId);
            orderList.clear();
            orderList.addAll(dbOrders);
            
            // If no orders in database, add sample data for demo
            if (orderList.isEmpty()) {
                orderList.add(new Order("OD-9021", "Oct 20 2023", 1499, "Đang giao hàng", "Oct 24", 2,
                        "123 Đường ABC, Phường XYZ, Quận 1, TP.HCM", null, "Thanh toán khi nhận hàng"));
                orderList.add(new Order("OD-8955", "Oct 18 2023", 899, "Đang giao hàng", "Oct 26", 1,
                        "456 Đường DEF, Phường UVW, Quận 2, TP.HCM", null, "Thanh toán khi nhận hàng"));
                orderList.add(new Order("OD-8842", "Oct 15 2023", 450, "Đã nhận", "Oct 21", 2,
                        "789 Đường GHI, Phường RST, Quận 3, TP.HCM", null, "Thanh toán khi nhận hàng"));
                orderList.add(new Order("OD-8700", "Sep 28 2023", 2140, "Đã nhận", "Oct 2", 1,
                        "321 Đường JKL, Phường OPQ, Quận 4, TP.HCM", null, "Thanh toán khi nhận hàng"));
                orderList.add(new Order("OD-8600", "Nov 1 2023", 1200, "Chưa thanh toán", "Nov 5", 3,
                        "654 Đường MNO, Phường XYZ, Quận 5, TP.HCM", null, "Thanh toán khi nhận hàng"));
                orderList.add(new Order("OD-8501", "Oct 25 2023", 750, "Đang xử lý", "Oct 30", 1,
                        "987 Đường PQR, Phường ABC, Quận 6, TP.HCM", null, "Thanh toán khi nhận hàng"));
            }
        }

        filteredList = new ArrayList<>(orderList);
        
        // supply listener so we can start activity for result
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
            startActivityForResult(intent, REQUEST_CODE_DETAIL);
        });

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);
        
        // Set up tab click listeners
        setupTabListeners();
        setTabActive(tvAll);
        filterByStatus("ALL");

        return view;
    }
    
    // Thiết lập sự kiện click cho các tab trạng thái
    private void setupTabListeners() {
        tvAll.setOnClickListener(v -> filterByStatus("ALL"));
        tvPending.setOnClickListener(v -> filterByStatus("Chưa thanh toán"));
        tvProcessing.setOnClickListener(v -> filterByStatus("Đang xử lý"));
        tvShipping.setOnClickListener(v -> filterByStatus("Đang giao hàng"));
        tvDelivered.setOnClickListener(v -> filterByStatus("Đã nhận"));
        tvCancelled.setOnClickListener(v -> filterByStatus("Đã hủy"));
    }
    
    // Lọc danh sách đơn theo trạng thái được chọn
    private void filterByStatus(String status) {
        selectedStatus = status;
        filteredList.clear();
        if (status.equals("ALL")) {
            filteredList.addAll(orderList);
            // push cancelled orders to bottom
            filteredList.sort((o1, o2) -> {
                if (o1.getStatus().equals("Đã hủy") && !o2.getStatus().equals("Đã hủy")) return 1;
                if (!o1.getStatus().equals("Đã hủy") && o2.getStatus().equals("Đã hủy")) return -1;
                return 0;
            });
        } else {
            filteredList.addAll(orderList.stream()
                    .filter(order -> order.getStatus().equals(status))
                    .collect(Collectors.toList()));
        }
        adapter.notifyDataSetChanged();
        
        // Update tab highlights
        if (status.equals("ALL")) setTabActive(tvAll);
        else if (status.equals("Chưa thanh toán")) setTabActive(tvPending);
        else if (status.equals("Đang xử lý")) setTabActive(tvProcessing);
        else if (status.equals("Đang giao hàng")) setTabActive(tvShipping);
        else if (status.equals("Đã nhận")) setTabActive(tvDelivered);
        else if (status.equals("Đã hủy")) setTabActive(tvCancelled);
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
                if (orderDAO != null) {
                    List<Order> dbOrders = orderDAO.getAllOrders();
                    orderList.clear();
                    orderList.addAll(dbOrders);
                }
                filterByStatus(selectedStatus);
            }
        }
    }

    // provide static helper for other activities


    @Override
    public void onResume() {
        super.onResume();
        // Reload orders from database when fragment resumes
        if (orderDAO != null) {
            List<Order> dbOrders = orderDAO.getAllOrders();
            orderList.clear();
            orderList.addAll(dbOrders);
        }
        // refresh in case orders were added while away
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
        tvDelivered.setTextColor(getResources().getColor(R.color.white, null));
        tvDelivered.setAlpha(0.6f);
        tvCancelled.setTextColor(getResources().getColor(R.color.white, null));
        tvCancelled.setAlpha(0.6f);
        
        // đánh dấu tab đang hoạt động
        activeTab.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
        activeTab.setAlpha(1f);
    }
}