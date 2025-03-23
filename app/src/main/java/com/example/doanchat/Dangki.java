package com.example.doanchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText; // Thay TextView bằng EditText
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Dangki extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dangki);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnql();
        btndk();
    }

    public void btnql() {
        Button quaylai = findViewById(R.id.btnql);
        quaylai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dangki.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void btndk() {
        Button dangki = findViewById(R.id.btndk);
        dangki.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText username = findViewById(R.id.txttk);
                EditText password = findViewById(R.id.txtpw);
                EditText password2 = findViewById(R.id.txtpwnl);
                String us = username.getText().toString().trim();
                String pw = password.getText().toString().trim();
                String pw2 = password2.getText().toString().trim();


                if (us.isEmpty() || pw.isEmpty() || pw2.isEmpty()) {
                    Toast.makeText(Dangki.this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!pw.equals(pw2)) {
                    Toast.makeText(Dangki.this, "Mật khẩu không trùng khớp", Toast.LENGTH_SHORT).show();
                    return;
                }


                ExecutorService executorService = Executors.newSingleThreadExecutor();
                executorService.execute(() -> {
                    connect db = new connect();
                    boolean isValid = db.addUser(us, pw);


                    runOnUiThread(() -> {
                        if (isValid) {
                            Toast.makeText(Dangki.this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                            // Quay lại màn hình đăng nhập
                            Intent intent = new Intent(Dangki.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Dangki.this, "Đăng ký thất bại", Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            }
        });
    }
}