package com.anhnvt_ph55017.md_02_datn.fragments;

import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anhnvt_ph55017.md_02_datn.Adapters.SuggestionProductAdapter;
import com.anhnvt_ph55017.md_02_datn.Adapters.SearchHistoryAdapter;
import com.anhnvt_ph55017.md_02_datn.DAO.SearchHistoryDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.anhnvt_ph55017.md_02_datn.utils.ProductApiService;

import java.util.ArrayList;
import java.util.List;

public class BrowseFragment extends Fragment {
    TextView tvSeeAllSuggestion;
    boolean isShowAll = false;
    RecyclerView rvProducts, rvSearchHistory;
    SearchHistoryDAO searchHistoryDAO;
    List<Product> listProduct;
    SuggestionProductAdapter adapter;
    SearchHistoryAdapter searchHistoryAdapter;
    EditText editSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_browse, container, false);

        tvSeeAllSuggestion = view.findViewById(R.id.tvSeeAllSuggestion);
        tvSeeAllSuggestion.setOnClickListener(v -> {
            isShowAll = !isShowAll;
            updateSuggestionList();
            tvSeeAllSuggestion.setText(isShowAll ? "Thu gọn" : "Xem tất cả");
        });

        rvProducts = view.findViewById(R.id.rvSearchProducts);
        rvSearchHistory = view.findViewById(R.id.rvSearchHistory);
        editSearch = view.findViewById(R.id.edtSearch);

        // Kiểm tra context
        if (getContext() == null) {
            return view;
        }

        searchHistoryDAO = new SearchHistoryDAO(getContext());

        listProduct = new ArrayList<>();
        adapter = new SuggestionProductAdapter(getContext(), listProduct, new SuggestionProductAdapter.OnProductListener() {
            @Override
            public void onAddToCart(Product product) {
                // Mở bottom sheet chọn số lượng, giống Home
                BottomSheetProductOptions bottomSheet = BottomSheetProductOptions.newInstance(product, (selectedProduct, qty) -> {
                    String token = com.anhnvt_ph55017.md_02_datn.utils.SessionManager.getToken(getContext());
                    com.anhnvt_ph55017.md_02_datn.utils.CartApiService.addToCart(getContext(), token, selectedProduct.getId(), qty, new com.anhnvt_ph55017.md_02_datn.utils.CartApiService.CartCallback() {
                        @Override
                        public void onSuccess(org.json.JSONObject cartJson) {
                            if (getActivity() != null) getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show()
                            );
                        }
                        @Override
                        public void onError(String error) {
                            if (getActivity() != null) getActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "Lỗi thêm giỏ hàng!", Toast.LENGTH_SHORT).show()
                            );
                        }
                    });
                });
                bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
            }
            @Override
            public void onFavorite(Product product) {
                int pos = listProduct.indexOf(product);
                boolean newState = !product.isFavorite();
                product.setFavorite(newState);
                if (pos >= 0) adapter.notifyItemChanged(pos);
                String token = com.anhnvt_ph55017.md_02_datn.utils.SessionManager.getToken(getContext());
                new Thread(() -> {
                    try {
                        java.net.URL url = new java.net.URL("http://10.0.2.2:5000/api/favorites");
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                        conn.setRequestMethod(newState ? "POST" : "DELETE");
                        conn.setRequestProperty("Authorization", "Bearer " + token);
                        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                        conn.setDoOutput(true);
                        String body = "{\"productId\":\"" + product.getId() + "\"}";
                        conn.getOutputStream().write(body.getBytes("UTF-8"));
                        conn.getResponseCode();
                        conn.disconnect();
                    } catch (Exception ignored) {}
                }).start();
            }
            @Override
            public void onProductClick(Product product) {
                // TODO: Mở DetailActivity
                Toast.makeText(getContext(), "Xem chi tiết: " + product.getName(), Toast.LENGTH_SHORT).show();
            }
        });
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 1));
        rvProducts.setAdapter(adapter);

        loadProductsFromApi();

        // Load search history (10 gần nhất)
        loadSearchHistory();

        // Load trending keywords


        // Tìm kiếm theo chữ
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle search action on keyboard
        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                String searchKeyword = editSearch.getText().toString().trim();
                
                // Lưu keyword vào lịch sử tìm kiếm nếu không trống
                if (!searchKeyword.isEmpty()) {
                    searchHistoryDAO.add(searchKeyword);
                    loadSearchHistory(); // Reload search history
                }
                
                hideKeyboard();
                return true;
            }
            return false;
        });

        return view;
    }

    /**
     * Load 10 lịch sử tìm kiếm gần nhất và hiển thị theo chiều ngang
     */
    private void loadProductsFromApi() {
        if (getContext() == null) {
            return;
        }
        ProductApiService.fetchProducts(getContext(), "", new ProductApiService.ProductCallback() {
            @Override
            public void onSuccess(List<Product> products) {
                listProduct = products;
                isShowAll = false;
                updateSuggestionList();
                tvSeeAllSuggestion.setText("Xem tất cả");
            }
            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Lấy sản phẩm thất bại: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateSuggestionList() {
        if (isShowAll || listProduct.size() <= 5) {
            adapter.setData(listProduct);
        } else {
            adapter.setData(listProduct.subList(0, 5));
        }
    }

    private void loadSearchHistory() {
        if (getContext() == null) {
            return;
        }

        List<String> recentKeywords = searchHistoryDAO.getTopRecent(10);

        searchHistoryAdapter = new SearchHistoryAdapter(recentKeywords, keyword -> {
            // Khi click vào một item, set text vào EditText
            editSearch.setText(keyword);
            editSearch.setSelection(keyword.length()); // Đặt cursor ở cuối
        });

        rvSearchHistory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvSearchHistory.setAdapter(searchHistoryAdapter);
    }

    private void hideKeyboard() {
        if (getContext() == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
        }
    }

    private void filter(String text){
        List<Product> filtered = new ArrayList<>();
        for(Product p : listProduct){
            if(p.getName().toLowerCase().contains(text.toLowerCase())){
                filtered.add(p);
            }
        }
        adapter.setData(filtered);
    }

    /**
     * Load và hiển thị xu hướng tìm kiếm
     */

}

