package com.example.e_tani;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class Home extends AppCompatActivity {

    private TextView loginLink, registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Cek apakah user sudah login
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        
        if (isLoggedIn) {
            // Jika sudah login, langsung ke Dashboard
            Intent intent = new Intent(Home.this, Dashboard.class);
            startActivity(intent);
            finish();
            return;
        }

        // Inisialisasi komponen
        loginLink = findViewById(R.id.loginLink);
        registerLink = findViewById(R.id.registerLink);

        // Aksi ketika klik "Masuk Sekarang"
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(Home.this, Login.class);
                startActivity(loginIntent);
            }
        });

        // Aksi ketika klik "Daftar Sekarang"
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(Home.this, Register.class);
                startActivity(registerIntent);
            }
        });
    }
}
