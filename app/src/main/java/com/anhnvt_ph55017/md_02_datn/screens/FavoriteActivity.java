package com.anhnvt_ph55017.md_02_datn.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.anhnvt_ph55017.md_02_datn.Adapters.FavoriteAdapter;
import com.anhnvt_ph55017.md_02_datn.R;
import com.anhnvt_ph55017.md_02_datn.models.Product;
import com.anhnvt_ph55017.md_02_datn.utils.FavoriteApiService;
import com.anhnvt_ph55017.md_02_datn.utils.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity {

    private RecyclerView rvFavorite;
    private List<Product> list;
    private FavoriteAdapter adapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        rvFavorite = findViewById(R.id.rvFavorite);
        progressBar = findViewById(R.id.progressBar);

        // Xử lý nút back
        android.widget.ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvFavorite.setLayoutManager(new GridLayoutManager(this, 1));

        list = new ArrayList<>();
        adapter = new FavoriteAdapter(this, list, this::openProductDetail);
        rvFavorite.setAdapter(adapter);

        loadFavorites();
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void finishLoad() {
        runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            showLoading(false);

            if (list.isEmpty()) {
                Toast.makeText(this, "Chưa có sản phẩm yêu thích", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFavorites() {
        String token = SessionManager.getToken(this);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        FavoriteApiService.getFavorites(this, token, new FavoriteApiService.FavoritesCallback() {
            @Override
            public void onSuccess(JSONArray favoritesJson) {

                list.clear();

                int total = favoritesJson.length();
                if (total == 0) {
                    finishLoad();
                    return;
                }

                final int[] loaded = {0};

                for (int i = 0; i < total; i++) {
                    try {
                        JSONObject obj = favoritesJson.getJSONObject(i);

                        Log.d("FAVORITE_JSON", obj.toString());

                        // 🔥 ĐÚNG KEY
                        JSONObject productObj = obj.optJSONObject("product");

                        if (productObj == null) {
                            loaded[0]++;
                            if (loaded[0] == total) finishLoad();
                            continue;
                        }

                        Product p = new Product(
                                productObj.optString("_id"),
                                productObj.optString("name"),
                                productObj.optDouble("price"),

                                // ⚠️ images là array → lấy phần tử đầu
                                productObj.optJSONArray("images") != null &&
                                        productObj.optJSONArray("images").length() > 0
                                        ? productObj.optJSONArray("images").optString(0)
                                        : "",

                                productObj.optString("description", ""),
                                productObj.optInt("stock", 0)
                        );

                        p.setFavorite(true);
                        list.add(p);

                        loaded[0]++;
                        if (loaded[0] == total) finishLoad();

                    } catch (Exception e) {
                        e.printStackTrace();

                        loaded[0]++;
                        if (loaded[0] == total) finishLoad();
                    }
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(FavoriteActivity.this,
                            "Lỗi load favorite: " + error,
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void openProductDetail(Product product) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("productId", product.getId());
        startActivity(intent);
    }
}