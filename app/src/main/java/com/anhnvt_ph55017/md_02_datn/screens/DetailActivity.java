package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.Adapters.ProductAdapter;
import com.anhnvt_ph55017.md_02_datn.DAO.CartDAO;
import com.anhnvt_ph55017.md_02_datn.DAO.ProductDAO;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.fragments.BottomSheetProductOptions;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import java.util.List;

public class DetailActivity extends AppCompatActivity {

    ImageView imgProduct;
    ImageButton btnBack;
    TextView tvName, tvPrice, tvRating, tvDesc;
    Button btnAddCart;
    CartDAO cartDAO;
    RecyclerView rvRelated;
    ProductDAO productDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        btnBack = findViewById(R.id.btnBack);
        imgProduct = findViewById(R.id.imgProduct);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvRating = findViewById(R.id.tvRating);
        tvDesc = findViewById(R.id.tvDesc);
        rvRelated = findViewById(R.id.rvRelated);
        btnAddCart = findViewById(R.id.btnAddCart);

        Intent intent = getIntent();

        // Validate intent data
        if (intent == null) {
            Toast.makeText(this, "Invalid product data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cartDAO = new CartDAO(this);

        // Get product ID
        int productId = intent.getIntExtra("id", 0);
        
        if (productId <= 0) {
            Toast.makeText(this, "Invalid product", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Build product object from intent data
        String productName = intent.getStringExtra("name");
        double productPrice = intent.getDoubleExtra("price", 0);
        int productImage = intent.getIntExtra("image", 0);
        String productDesc = intent.getStringExtra("desc");
        float productRating = intent.getFloatExtra("rating", 4.5f);
        int productReviewCount = intent.getIntExtra("reviewCount", 0);

        Product product = new Product(productId, productName, productPrice, productImage, productDesc, 100);
        product.setRating(productRating);
        product.setReviewCount(productReviewCount);

        // Add to cart - Show Bottom Sheet
        btnAddCart.setOnClickListener(v -> {
            BottomSheetProductOptions bottomSheet = 
                BottomSheetProductOptions.newInstance(product, selectedProduct -> {
                    int userId = SessionManager.getUserId(DetailActivity.this);
                    
                    if (userId <= 0) {
                        userId = 1;  // Guest cart
                    }

                    cartDAO.addToCart(userId, selectedProduct.getId());
                    showCustomToast("Added to cart");
                });
            
            bottomSheet.show(getSupportFragmentManager(), "product_options");
        });

        // Back button
        btnBack.setOnClickListener(v -> onBackPressed());

        // Set product details with null checks
        imgProduct.setImageResource(product.getImage());
        tvName.setText(product.getName() != null ? product.getName() : "Unknown Product");
        tvPrice.setText("$" + product.getPrice());
        tvDesc.setText(product.getDescription() != null ? product.getDescription() : "No description available");

        tvRating.setText("⭐ " + product.getRating() + " (" + product.getReviewCount() + " reviews)");

        /* RELATED PRODUCTS */

        productDAO = new ProductDAO(this);
        List<Product> list = productDAO.getAll();

        rvRelated.setLayoutManager(new GridLayoutManager(this,2));
        rvRelated.setAdapter(new ProductAdapter(this,list));
    }

    private void showCustomToast(String message) {
        Toast toast = new Toast(DetailActivity.this);
        
        LayoutInflater inflater = LayoutInflater.from(DetailActivity.this);
        View layout = inflater.inflate(R.layout.custom_toast_layout, null);
        
        TextView tvMessage = layout.findViewById(R.id.tvToastMessage);
        tvMessage.setText(message);
        
        toast.setView(layout);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL, 0, 100);
        toast.show();
    }
}