package com.example.doanchat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Connection con;
    private String str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        connect connect = new connect();
        connect.CONN((conn, errorMessage) -> runOnUiThread(() -> {
            this.con = conn;
            ketnoi();
            btnlogin();
            btnreg();
        }));
    }

    public void btnlogin() {
        Button btn = findViewById(R.id.btndk);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView tk = findViewById(R.id.txttk);
                TextView pw = findViewById(R.id.txtpw);
                String username = tk.getText().toString().trim();
                String password = pw.getText().toString().trim();
                connect db = new connect();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.checkLogin(username, password, (success, userId, errorMessage) -> runOnUiThread(() -> {
                    if (success) {
                        // Lưu userId vào SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt("userId", userId);
                        editor.apply();

                        // Chuyển sang FriendActivity
                        Toast.makeText(MainActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, FriendActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        String message = "Đăng nhập thất bại";
                        if (errorMessage != null) {
                            message += ": " + errorMessage;
                        }
                        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                }));
            }
        });
    }

    public void ketnoi() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            try {
                if (con == null) {
                    str = "Kết nối server thất bại";
                } else {
                    str = "Kết nối server thành công";
                }
            } catch (Exception e) {
                str = "Lỗi kết nối: " + e.getMessage();
            }
            runOnUiThread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
            });
        });
    }

    public void btnreg() {
        Button btnrg = findViewById(R.id.btnreg);
        btnrg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Dangki.class);
                startActivity(intent);
                finish();
            }
        });
    }
}