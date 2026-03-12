package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
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
import com.anhnvt_ph55017.md_02_datn.models.Product;

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

        cartDAO = new CartDAO(this);

// lấy id sản phẩm
        int productId = intent.getIntExtra("id",0);

        btnAddCart.setOnClickListener(v -> {

            cartDAO.addToCart(1, productId); // userId tạm = 1

            Toast.makeText(DetailActivity.this,
                    "Added to cart",
                    Toast.LENGTH_SHORT).show();

        });

        // back icon
        btnBack.setOnClickListener(v -> onBackPressed());



        imgProduct.setImageResource(intent.getIntExtra("image",0));
        tvName.setText(intent.getStringExtra("name"));
        tvPrice.setText("$" + intent.getDoubleExtra("price",0));
        tvDesc.setText(intent.getStringExtra("desc"));

        float rating = intent.getFloatExtra("rating",4.5f);
        int review = intent.getIntExtra("reviewCount",100);

        tvRating.setText("⭐ " + rating + " (" + review + " reviews)");

        /* RELATED PRODUCTS */

        productDAO = new ProductDAO(this);
        List<Product> list = productDAO.getAll();

        rvRelated.setLayoutManager(new GridLayoutManager(this,2));
        rvRelated.setAdapter(new ProductAdapter(this,list));
    }
}