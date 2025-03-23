package com.example.doanchat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private TextView tvFriendName;
    private RecyclerView rvMessages;
    private EditText etMessage;
    private Button btnSend;
    private List<Message> messageList;
    private ChatAdapter chatAdapter;
    private int currentUserId;
    private int friendId;
    private String friendName;
    private connect db;
    private Handler handler; // Handler để cập nhật tin nhắn định kỳ
    private Runnable updateMessagesRunnable; // Runnable để gọi loadMessages()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        // Kiểm tra view trước khi gọi setOnApplyWindowInsetsListener
        android.view.View rootView = findViewById(R.id.main);
        if (rootView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Lấy thông tin từ Intent
        currentUserId = getSharedPreferences("MyPrefs", MODE_PRIVATE).getInt("userId", -1);
        friendId = getIntent().getIntExtra("friendId", -1);
        friendName = getIntent().getStringExtra("friendName");

        if (currentUserId == -1 || friendId == -1) {
            finish(); // Thoát nếu không có userId hoặc friendId
            return;
        }

        // Khởi tạo database
        db = new connect();

        // Ánh xạ view
        tvFriendName = findViewById(R.id.tvFriendName);
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        // Hiển thị tên bạn bè
        tvFriendName.setText("Chat với " + friendName);

        // Khởi tạo danh sách tin nhắn
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, currentUserId);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(chatAdapter);

        // Khởi tạo Handler và Runnable để cập nhật tin nhắn
        handler = new Handler(Looper.getMainLooper());
        updateMessagesRunnable = new Runnable() {
            @Override
            public void run() {
                loadMessages(); // Tải tin nhắn
                handler.postDelayed(this, 3000); // Lặp lại sau 3 giây (3000ms)
            }
        };

        // Bắt đầu cập nhật tin nhắn
        handler.post(updateMessagesRunnable);

        // Xử lý gửi tin nhắn
        btnSend.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
                etMessage.setText(""); // Xóa ô nhập sau khi gửi
            }
        });
    }

    private void loadMessages() {
        db.getMessages(currentUserId, friendId, messages -> {
            runOnUiThread(() -> {
                messageList.clear();
                messageList.addAll(messages);
                chatAdapter.notifyDataSetChanged();
                rvMessages.scrollToPosition(messageList.size() - 1); // Cuộn xuống tin nhắn mới nhất
            });
        });
    }

    private void sendMessage(String messageText) {
        db.sendMessage(currentUserId, friendId, messageText, (success, errorMessage) -> {
            if (success) {
                loadMessages(); // Tải lại danh sách tin nhắn
            } else {
                // Hiển thị thông báo lỗi
                runOnUiThread(() -> {
                    Toast.makeText(ChatActivity.this, "Gửi tin nhắn thất bại: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dừng cập nhật tin nhắn khi activity bị hủy
        if (handler != null && updateMessagesRunnable != null) {
            handler.removeCallbacks(updateMessagesRunnable);
        }
    }
}