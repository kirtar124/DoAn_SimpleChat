package com.example.doanchat;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FriendActivity extends AppCompatActivity {
    private EditText etSearchFriend;
    private Button btnSearchFriend;
    private RecyclerView rvFriends;
    private TextView tvNoFriendsMessage;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        // Lấy userId từ SharedPreferences
        currentUserId = getSharedPreferences("MyPrefs", MODE_PRIVATE).getInt("userId", -1);
        if (currentUserId == -1) {
            finish(); // Thoát nếu không có userId
            return;
        }

        etSearchFriend = findViewById(R.id.etSearchFriend);
        btnSearchFriend = findViewById(R.id.btnSearchFriend);
        rvFriends = findViewById(R.id.rvFriends);
        tvNoFriendsMessage = findViewById(R.id.tvNoFriendsMessage);

        rvFriends.setLayoutManager(new LinearLayoutManager(this));
        rvFriends.setVisibility(View.GONE);

        btnSearchFriend.setOnClickListener(v -> {
            Intent intent = new Intent(FriendActivity.this, Findfriend.class);
            startActivity(intent);
        });

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

        // Hiển thị danh sách bạn bè ban đầu
        loadFriends();
    }

    private void searchFriends() {
        String keyword = etSearchFriend.getText().toString().trim();
        new connect().searchUsersNotFriends(currentUserId, keyword, users -> {
            runOnUiThread(() -> {
                if (users.isEmpty()) {
                    tvNoFriendsMessage.setVisibility(View.VISIBLE);
                    rvFriends.setVisibility(View.GONE);
                } else {
                    tvNoFriendsMessage.setVisibility(View.GONE);
                    rvFriends.setVisibility(View.VISIBLE);
                    SearchFriendAdapter adapter = new SearchFriendAdapter(users, currentUserId);
                    rvFriends.setAdapter(adapter);
                }
            });
        });
    }

    private void loadFriends() {
        new connect().getFriends(currentUserId, friends -> {
            runOnUiThread(() -> {
                if (friends.isEmpty()) {
                    tvNoFriendsMessage.setVisibility(View.VISIBLE);
                    rvFriends.setVisibility(View.GONE);
                } else {
                    tvNoFriendsMessage.setVisibility(View.GONE);
                    rvFriends.setVisibility(View.VISIBLE);
                    FriendAdapter adapter = new FriendAdapter(friends, user -> {
                        Intent intent = new Intent(FriendActivity.this, ChatActivity.class);
                        intent.putExtra("friendId", user.getUserId());
                        intent.putExtra("friendName", user.getUsername());
                        startActivity(intent);
                    });
                    rvFriends.setAdapter(adapter);
                }
            });
        });
    }
}