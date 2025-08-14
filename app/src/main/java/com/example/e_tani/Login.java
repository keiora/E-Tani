package com.example.e_tani;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        // Inisialisasi komponen
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);

        // Aksi tombol login
        loginButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View view) {
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString();

                // Validasi input
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    Toast.makeText(Login.this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Cek login manual
                if (checkLogin(email, password)) {
                    // Simpan status login
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putString("userEmail", email);
                    editor.apply();

                    Toast.makeText(Login.this, "Login berhasil!", Toast.LENGTH_SHORT).show();
                    
                    // Pindah ke Dashboard
                    Intent intent = new Intent(Login.this, Dashboard.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Login.this, "Email atau password salah", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkLogin(String email, String password) {
        // Sistem login manual - bisa ditambah user lain di sini
        if (email.equals("admin@etani.com") && password.equals("admin123")) {
            return true;
        }
        if (email.equals("user@etani.com") && password.equals("user123")) {
            return true;
        }
        if (email.equals("test@etani.com") && password.equals("test123")) {
            return true;
        }
        
        // Cek dari data register yang tersimpan
        String registeredEmail = sharedPreferences.getString("registeredEmail", "");
        String registeredPassword = sharedPreferences.getString("registeredPassword", "");
        
        if (email.equals(registeredEmail) && password.equals(registeredPassword)) {
            return true;
        }
        
        return false;
    }
}
