package com.example.e_tani;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signupButton;
    private ProgressBar progressBar;
    

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    

    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        signupButton = findViewById(R.id.signupButton);
        progressBar = findViewById(R.id.progressBar);
        

        

        signupButton.setOnClickListener(v -> attemptRegister());
        
    }

    private void attemptRegister() {
        // Check network connectivity
        if (!isNetworkAvailable()) {
            toast("Tidak ada koneksi internet");
            return;
        }

        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String confirmPassword = confirmPasswordEditText.getText().toString();

        // Input validation
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            toast("Harap isi semua data");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toast("Format email tidak valid");
            return;
        }
        if (!password.equals(confirmPassword)) {
            toast("Password tidak cocok");
            return;
        }
        if (password.length() < 6) {
            toast("Password minimal 6 karakter");
            return;
        }

        setLoading(true);

        // Create Firebase Auth account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (isFinishing()) return; // Prevent crash if activity is finishing
                    
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            setLoading(false);
                            toast("Terjadi kendala. Coba lagi.");
                            return;
                        }

                        // Set displayName to Firebase profile
                        UserProfileChangeRequest updates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();

                        user.updateProfile(updates)
                                .addOnCompleteListener(t1 -> {
                                    if (isFinishing()) return; // Prevent crash if activity is finishing
                                    
                                    if (t1.isSuccessful()) {
                                        // Save user data to Firestore
                                        saveUserToFirestore(user.getUid(), name, email)
                                                .addOnCompleteListener(t2 -> {
                                                    if (isFinishing()) return; // Prevent crash if activity is finishing
                                                    
                                                    if (t2.isSuccessful()) {
                                                        // Send verification email
                                                        user.sendEmailVerification()
                                                                .addOnCompleteListener(t3 -> {
                                                                    if (isFinishing()) return; // Prevent crash if activity is finishing
                                                                    
                                                                    setLoading(false);
                                                                    toast("Registrasi berhasil! Cek email untuk verifikasi.");
                                                                    startActivity(new Intent(Register.this, Login.class));
                                                                    finish();
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    if (isFinishing()) return; // Prevent crash if activity is finishing
                                                                    
                                                                    setLoading(false);
                                                                    toast("Gagal mengirim email verifikasi: " + e.getMessage());
                                                                });
                                                    } else {
                                                        setLoading(false);
                                                        toast("Gagal menyimpan data user: " + t2.getException().getMessage());
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    if (isFinishing()) return; // Prevent crash if activity is finishing
                                                    
                                                    setLoading(false);
                                                    toast("Gagal menyimpan data user: " + e.getMessage());
                                                });
                                    } else {
                                        setLoading(false);
                                        toast("Gagal mengupdate profil: " + t1.getException().getMessage());
                                    }
                                });

                    } else {
                        setLoading(false);
                        handleSignupError(task);
                    }
                });
    }

    

    private Task<Void> saveUserToFirestore(@NonNull String uid, @NonNull String name, @NonNull String email) {
        Map<String, Object> userDoc = new HashMap<>();
        userDoc.put("name", name);
        userDoc.put("email", email);
        userDoc.put("provider", "password");
        userDoc.put("createdAt", Timestamp.now());
        userDoc.put("emailVerified", false);
        userDoc.put("role", "petani");

        return db.collection("users").document(uid).set(userDoc);
    }

    

    private void handleSignupError(@NonNull Task<?> task) {
        Exception e = task.getException();
        if (e == null) {
            toast("Registrasi gagal. Coba lagi.");
            return;
        }
        if (e instanceof FirebaseAuthWeakPasswordException) {
            toast("Password terlalu lemah.");
        } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
            toast("Email tidak valid.");
        } else if (e instanceof FirebaseAuthUserCollisionException) {
            toast("Email sudah terdaftar. Coba login.");
        } else {
            toast("Error: " + e.getMessage());
        }
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (signupButton != null) {
            signupButton.setEnabled(!loading);
        }
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                Network network = connectivityManager.getActiveNetwork();
                if (network != null) {
                    NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                    return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
                }
            } else {
                // For older Android versions
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
        return false;
    }
}   