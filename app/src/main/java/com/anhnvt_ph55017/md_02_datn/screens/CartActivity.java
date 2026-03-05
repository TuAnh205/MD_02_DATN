package com.anhnvt_ph55017.md_02_datn.screens;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.Adapters.CartAdapter;
import com.anhnvt_ph55017.md_02_datn.DAO.CartDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;

import java.util.ArrayList;
import java.util.List;


public class CartActivity extends AppCompatActivity {

    RecyclerView rvCart;
    TextView tvSubtotal,tvTax,tvTotal;

    List<Product> cartList;
    CartAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        rvCart = findViewById(R.id.rvCart);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvTax = findViewById(R.id.tvTax);
        tvTotal = findViewById(R.id.tvTotal);

        cartList = new ArrayList<>();

        // demo data
        cartList.add(new Product(1,"MacBook Pro",2500,R.drawable.anh1,"",10));
        cartList.add(new Product(2,"iPhone 15",1200,R.drawable.anh2,"",10));

        adapter = new CartAdapter(this,cartList);

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(adapter);

        calculateTotal();
    }

    void calculateTotal(){

        double subtotal = 0;

        for(Product p : cartList){
            subtotal += p.getPrice();
        }

        double tax = subtotal * 0.1;

        double total = subtotal + tax;

        tvSubtotal.setText("Subtotal: $" + subtotal);
        tvTax.setText("Tax: $" + tax);
        tvTotal.setText("Order Total: $" + total);
    }
}

