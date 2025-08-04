package com.example.e_tani;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // Pastikan file XML bernama activity_login.xml

        // Inisialisasi komponen
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        // Aksi tombol login
        loginButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            // Validasi sederhana
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            // Autentikasi sederhana (contoh)
            if (email.equals("admin@etani.com") && password.equals("admin123")) {
                Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show();

                // Contoh pindah ke halaman dashboard
                Intent intent = new Intent(Login.this, Dashboard.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Email atau password salah", Toast.LENGTH_SHORT).show();
            }

            // TODO: Ganti dengan proses login ke server menggunakan API
        });
    }
}
