package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.Adapters.CartAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    RecyclerView rvCart;
    TextView tvSubtotal,tvTax,tvTotal;
    CheckBox cbAll;
    AppCompatButton btnCheckOut;
    List<Product> cartList;
    CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        btnCheckOut = findViewById(R.id.btnCheckOut);
        rvCart = findViewById(R.id.rvCart);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTax = findViewById(R.id.tvTax);
        tvTotal = findViewById(R.id.tvTotal);
        cbAll = findViewById(R.id.cbAll);

        cartList = new ArrayList<>();
        btnCheckOut.setOnClickListener(v ->
                startActivity(new Intent(this, CheckOutActivity.class))
        );
        cartList.add(new Product(1,"MacBook Pro",2500,R.drawable.anh1,"",10));
        cartList.add(new Product(2,"iPhone 15",1200,R.drawable.anh2,"",10));

        adapter = new CartAdapter(this,cartList,this::calculateTotal);

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(adapter);

        cbAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adapter.selectAll(isChecked);
            calculateTotal();
        });

        calculateTotal();
    }

    public void calculateTotal(){

        double subtotal = 0;

        for(Product p : cartList){
            if(p.isSelected()){
                subtotal += p.getPrice() * p.getQty();
            }
        }

        double tax = subtotal * 0.1;
        double total = subtotal + tax;

        tvSubtotal.setText("Subtotal: $" + subtotal);
        tvTax.setText("Tax: $" + tax);
        tvTotal.setText("Order Total: $" + total);
    }
}