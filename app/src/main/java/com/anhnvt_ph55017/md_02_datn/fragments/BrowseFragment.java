package com.anhnvt_ph55017.md_02_datn.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.anhnvt_ph55017.md_02_datn.Adapters.ProductAdapter;
import com.anhnvt_ph55017.md_02_datn.DAO.ProductDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;

import java.util.ArrayList;
import java.util.List;

public class BrowseFragment extends Fragment {

    RecyclerView rvProducts;
    ProductDAO productDAO;
    List<Product> listProduct;
    ProductAdapter adapter;
    EditText editSearch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_browse, container, false);

        rvProducts = view.findViewById(R.id.rvSearchProducts);
        editSearch = view.findViewById(R.id.edtSearch);

        productDAO = new ProductDAO(getContext());

        listProduct = productDAO.getAll();

        adapter = new ProductAdapter(getContext(), listProduct);

        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setAdapter(adapter);

        // tìm kiếm theo chữ
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

        return view;
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