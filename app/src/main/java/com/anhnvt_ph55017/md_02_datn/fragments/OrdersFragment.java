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

import com.anhnvt_ph55017.md_02_datn.Adapters.OrderAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Order;


import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    RecyclerView rvOrders;
    OrderAdapter adapter;
    List<Order> orderList;

    public OrdersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        rvOrders = view.findViewById(R.id.rvOrders);

        orderList = new ArrayList<>();

        orderList.add(new Order("CT-9021", "Oct 20 2023", 1499));
        orderList.add(new Order("CT-8955", "Oct 18 2023", 899));
        orderList.add(new Order("CT-8842", "Oct 15 2023", 450));
        orderList.add(new Order("CT-8700", "Sep 28 2023", 2140));

        adapter = new OrderAdapter(getContext(), orderList);

        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrders.setAdapter(adapter);

        return view;
    }
}