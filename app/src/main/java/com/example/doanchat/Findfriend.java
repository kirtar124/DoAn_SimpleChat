package com.example.doanchat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Findfriend extends AppCompatActivity {
    private EditText etSearchFriend;
    private Button btnSearch;
    private RecyclerView rvSearchResults;
    private ProgressBar progressBar;
    private TextView tvNoResults;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_find_friend);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Lấy userId từ SharedPreferences
        currentUserId = getSharedPreferences("MyPrefs", MODE_PRIVATE).getInt("userId", -1);
        if (currentUserId == -1) {
            finish(); // Thoát nếu không có userId
            return;
        }

        etSearchFriend = findViewById(R.id.et_search_friend);
        btnSearch = findViewById(R.id.btn_search);
        rvSearchResults = findViewById(R.id.rv_search_results);
        progressBar = findViewById(R.id.progressBar);
        tvNoResults = findViewById(R.id.tvNoResults);

        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        btnSearch.setOnClickListener(v -> searchFriends());
        etSearchFriend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                searchFriends();
            }
        });
    }

    private void searchFriends() {
        String keyword = etSearchFriend.getText().toString().trim();
        if (keyword.isEmpty()) {
            tvNoResults.setVisibility(View.VISIBLE);
            rvSearchResults.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvNoResults.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.GONE);

        new connect().searchUsersNotFriends(currentUserId, keyword, users -> {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                if (users.isEmpty()) {
                    tvNoResults.setVisibility(View.VISIBLE);
                    rvSearchResults.setVisibility(View.GONE);
                } else {
                    tvNoResults.setVisibility(View.GONE);
                    rvSearchResults.setVisibility(View.VISIBLE);
                    SearchFriendAdapter adapter = new SearchFriendAdapter(users, currentUserId);
                    rvSearchResults.setAdapter(adapter);
                }
            });
        });
    }
}