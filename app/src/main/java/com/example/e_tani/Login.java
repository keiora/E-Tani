package com.example.e_tani;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.SetOptions;

public class Login extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    
    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Inisialisasi Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Inisialisasi komponen
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        

        // Aksi tombol login
        loginButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            // Validasi input
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(Login.this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show();
                return;
            }

            // Login ke Firebase
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            if (mAuth.getCurrentUser() != null) {
                                fetchRoleAndNavigate(mAuth.getCurrentUser().getUid());
                            } else {
                                Toast.makeText(Login.this, "Login berhasil, namun user tidak tersedia", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Login.this, "Email atau password salah", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        
    }

    

    private void fetchRoleAndNavigate(String uid) {
        System.out.println("Fetching role for user: " + uid);
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        System.out.println("User document does not exist, creating default user role");
                        // Buat document user dengan role default
                        java.util.Map<String, Object> userDoc = new java.util.HashMap<>();
                        userDoc.put("role", "petani"); // Default role
                        userDoc.put("email", mAuth.getCurrentUser().getEmail());
                        userDoc.put("name", mAuth.getCurrentUser().getDisplayName() != null ? mAuth.getCurrentUser().getDisplayName() : "");
                        
                        db.collection("users").document(uid).set(userDoc)
                                .addOnSuccessListener(unused -> {
                                    System.out.println("Created user document with petani role");
                                    navigateToDashboard();
                                })
                                .addOnFailureListener(e -> {
                                    System.out.println("Failed to create user document: " + e.getMessage());
                                    Toast.makeText(Login.this, "Gagal membuat data user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                        return;
                    }
                    
                    String role = documentSnapshot.getString("role");
                    System.out.println("User role found: " + role);
                    
                    if (role == null) {
                        System.out.println("Role is null, setting default role to petani");
                        // Update role jika null
                        java.util.Map<String, Object> updateData = new java.util.HashMap<>();
                        updateData.put("role", "petani");
                        db.collection("users").document(uid).update(updateData)
                                .addOnSuccessListener(unused -> {
                                    System.out.println("Updated user role to petani");
                                    navigateToDashboard();
                                })
                                .addOnFailureListener(e -> {
                                    System.out.println("Failed to update role: " + e.getMessage());
                                    Toast.makeText(Login.this, "Gagal update role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                        return;
                    }

                    if (role.equalsIgnoreCase("admin")) {
                        System.out.println("Navigating to Statistic (admin)");
                        startActivity(new Intent(Login.this, Statistic.class));
                        finish();
                    } else if (role.equalsIgnoreCase("petani") || role.equalsIgnoreCase("user")) {
                        System.out.println("Navigating to Dashboard (petani/user)");
                        navigateToDashboard();
                    } else {
                        System.out.println("Unknown role: " + role + ", defaulting to petani");
                        // Jika role tidak dikenali, default ke petani
                        java.util.Map<String, Object> updateData = new java.util.HashMap<>();
                        updateData.put("role", "petani");
                        db.collection("users").document(uid).update(updateData)
                                .addOnSuccessListener(unused -> {
                                    System.out.println("Updated unknown role to petani");
                                    navigateToDashboard();
                                })
                                .addOnFailureListener(e -> {
                                    System.out.println("Failed to update unknown role: " + e.getMessage());
                                    Toast.makeText(Login.this, "Gagal update role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error fetching user role: " + e.getMessage());
                    Toast.makeText(Login.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    private void navigateToDashboard() {
        // Simpan status login
        android.content.SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.apply();
        
        System.out.println("Login successful, navigating to Dashboard");
        Intent intent = new Intent(Login.this, Dashboard.class);
        startActivity(intent);
        finish();
    }
}
