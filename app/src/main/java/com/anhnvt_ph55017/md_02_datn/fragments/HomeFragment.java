package com.anhnvt_ph55017.md_02_datn.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.anhnvt_ph55017.md_02_datn.Adapters.CategoryAdapter;
import com.anhnvt_ph55017.md_02_datn.Adapters.ProductAdapter;
import com.anhnvt_ph55017.md_02_datn.DAO.CategoryDAO;
import com.anhnvt_ph55017.md_02_datn.DAO.ProductDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.screens.CartActivity;

public class HomeFragment extends Fragment {

    RecyclerView rvCategory, rvProduct;
    ImageView imgCart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rvCategory = view.findViewById(R.id.rvCategory);
        rvProduct  = view.findViewById(R.id.rvProduct);
        imgCart = view.findViewById(R.id.imgCart);

        imgCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity(), CartActivity.class);
                startActivity(intent);

            }
        });
        CategoryDAO categoryDAO = new CategoryDAO(getContext());
        ProductDAO productDAO   = new ProductDAO(getContext());

        rvCategory.setLayoutManager(
                new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false)
        );
        rvCategory.setAdapter(
                new CategoryAdapter(categoryDAO.getAll())
        );

        rvProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProduct.setAdapter(
                new ProductAdapter(getContext(),productDAO.getAll())
        );

        return view;
    }
}