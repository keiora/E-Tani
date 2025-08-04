package com.example.e_tani;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private TextView loginLink, registerLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home); // pastikan file XML kamu bernama activity_welcome.xml

        // Inisialisasi komponen
        loginLink = findViewById(R.id.loginLink);
        registerLink = findViewById(R.id.registerLink);

        // Aksi ketika klik "Masuk Sekarang"
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(WelcomeActivity.this, Login.class);
                startActivity(loginIntent);
            }
        });

        // Aksi ketika klik "Daftar Sekarang"
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent registerIntent = new Intent(WelcomeActivity.this, Register.class);
                startActivity(registerIntent);
            }
        });
    }
}
