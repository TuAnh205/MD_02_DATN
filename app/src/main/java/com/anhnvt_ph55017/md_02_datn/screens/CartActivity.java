package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.Adapters.CartAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    RecyclerView rvCart;
    TextView tvSubtotal,tvTax,tvTotal;
    CheckBox cbAll;
    AppCompatButton btnCheckOut;
    List<Product> cartList;
    CartAdapter adapter;
    com.anhnvt_ph55017.md_02_datn.DAO.CartDAO cartDAO;

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

        cartDAO = new com.anhnvt_ph55017.md_02_datn.DAO.CartDAO(this);

        // Get current user ID or use guest cart (userId = 1)
        int userId = SessionManager.getUserId(this);
        if (userId <= 0) {
            userId = 1;  // Guest cart
        }
        
        cartList = cartDAO.getCartProducts(userId);

        btnCheckOut.setOnClickListener(v -> {
            // Check if user is logged in
            int currentUserId = SessionManager.getUserId(CartActivity.this);
            if (currentUserId <= 0) {
                Toast.makeText(CartActivity.this, "Please login to checkout", Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(CartActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                return;
            }
            
            // only send items the user has selected
            ArrayList<com.anhnvt_ph55017.md_02_datn.models.Product> selected = new ArrayList<>();
            for(com.anhnvt_ph55017.md_02_datn.models.Product p : cartList){
                if(p.isSelected()) selected.add(p);
            }
            Intent intent = new Intent(this, CheckOutActivity.class);
            intent.putExtra("cart", selected);
            startActivity(intent);
        });

        adapter = new CartAdapter(this, cartList, cartDAO, this::calculateTotal);

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(adapter);

        cbAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adapter.selectAll(isChecked);
            calculateTotal();
        });

        calculateTotal();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cartList.clear();
        
        int userId = SessionManager.getUserId(this);
        if (userId <= 0) {
            userId = 1;  // Guest cart
        }
        
        cartList.addAll(cartDAO.getCartProducts(userId));
        adapter.notifyDataSetChanged();
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

        tvSubtotal.setText(String.format("Subtotal: $%.2f", subtotal));
        tvTax.setText(String.format("Tax: $%.2f", tax));
        tvTotal.setText(String.format("Order Total: $%.2f", total));
    }
}