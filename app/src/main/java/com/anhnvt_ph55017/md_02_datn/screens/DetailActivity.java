package com.anhnvt_ph55017.md_02_datn.screens;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.anhnvt_ph55017.md_02_datn.DAO.ProductDAO;
import com.anhnvt_ph55017.md_02_datn.R;

import com.anhnvt_ph55017.md_02_datn.models.Product;

public class DetailActivity extends AppCompatActivity {

    ImageView imgProduct;
    TextView tvName, tvPrice, tvDesc, tvStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        imgProduct = findViewById(R.id.imgProduct);
        tvName = findViewById(R.id.tvName);
        tvPrice = findViewById(R.id.tvPrice);
        tvDesc = findViewById(R.id.tvDesc);
        tvStock = findViewById(R.id.tvStock);

        int id = getIntent().getIntExtra("id", -1);

        ProductDAO productDAO = new ProductDAO(this);
        Product product = productDAO.getById(id);

        if (product != null) {
            imgProduct.setImageResource(product.getImage());
            tvName.setText(product.getName());
            tvPrice.setText("$" + product.getPrice());
            tvDesc.setText(product.getDescription());
            tvStock.setText("Stock: " + product.getStock());
        }
    }
}