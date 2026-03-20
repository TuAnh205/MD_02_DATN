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

import com.anhnvt_ph55017.md_02_datn.Adapters.ProductAdapter;
import com.anhnvt_ph55017.md_02_datn.Adapters.SearchHistoryAdapter;
import com.anhnvt_ph55017.md_02_datn.DAO.ProductDAO;
import com.anhnvt_ph55017.md_02_datn.DAO.SearchHistoryDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;

import java.util.ArrayList;
import java.util.List;

public class BrowseFragment extends Fragment {

    RecyclerView rvProducts, rvSearchHistory;
    ProductDAO productDAO;
    SearchHistoryDAO searchHistoryDAO;
    List<Product> listProduct;
    ProductAdapter adapter;
    SearchHistoryAdapter searchHistoryAdapter;
    EditText editSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_browse, container, false);

        rvProducts = view.findViewById(R.id.rvSearchProducts);
        rvSearchHistory = view.findViewById(R.id.rvSearchHistory);
        editSearch = view.findViewById(R.id.edtSearch);

        // Kiểm tra context
        if (getContext() == null) {
            return view;
        }

        productDAO = new ProductDAO(getContext());
        searchHistoryDAO = new SearchHistoryDAO(getContext());

        // Load products
        listProduct = productDAO.getAll();
        adapter = new ProductAdapter(getContext(), listProduct);
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setAdapter(adapter);

        // Load search history (10 gần nhất)
        loadSearchHistory();

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
}