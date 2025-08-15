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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
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
    private SignInButton googleSignInButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient googleSignInClient;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (ActivityResult result) -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account != null) {
                            firebaseAuthWithGoogle(account);
                        } else {
                            toast("Gagal mendapatkan akun Google");
                        }
                    } catch (ApiException e) {
                        toast("Google sign-in gagal: " + e.getMessage());
                    }
                } else {
                    toast("Google sign-in dibatalkan");
                }
            });

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
        googleSignInButton = findViewById(R.id.googleSignInButton);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        signupButton.setOnClickListener(v -> attemptRegister());
        if (googleSignInButton != null) {
            googleSignInButton.setSize(SignInButton.SIZE_WIDE);
            googleSignInButton.setOnClickListener(v -> beginGoogleSignIn());
        }
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

    private void beginGoogleSignIn() {
        if (!isNetworkAvailable()) {
            toast("Tidak ada koneksi internet");
            return;
        }
        setLoading(true);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(@NonNull GoogleSignInAccount account) {
        String idToken = account.getIdToken();
        if (idToken == null) {
            setLoading(false);
            toast("Token Google tidak ditemukan");
            return;
        }

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (isFinishing()) return;

                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user == null) {
                            setLoading(false);
                            toast("Autentikasi gagal");
                            return;
                        }
                        // Save/merge user data to Firestore
                        String name = account.getDisplayName() != null ? account.getDisplayName() : "";
                        String email = account.getEmail() != null ? account.getEmail() : "";
                        saveUserToFirestoreGoogle(user.getUid(), name, email, user.isEmailVerified())
                                .addOnCompleteListener(t -> {
                                    setLoading(false);
                                    if (t.isSuccessful()) {
                                        toast("Berhasil masuk dengan Google");
                                        startActivity(new Intent(Register.this, Dashboard.class));
                                        finish();
                                    } else {
                                        toast("Gagal menyimpan data: " + (t.getException() != null ? t.getException().getMessage() : ""));
                                    }
                                });
                    } else {
                        setLoading(false);
                        toast("Firebase auth gagal: " + (task.getException() != null ? task.getException().getMessage() : ""));
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

    private Task<Void> saveUserToFirestoreGoogle(@NonNull String uid, @NonNull String name, @NonNull String email, boolean emailVerified) {
        Map<String, Object> userDoc = new HashMap<>();
        userDoc.put("name", name);
        userDoc.put("email", email);
        userDoc.put("provider", "google");
        userDoc.put("emailVerified", emailVerified);
        userDoc.put("createdAt", Timestamp.now());
        userDoc.put("role", "petani");

        return db.collection("users").document(uid).set(userDoc, SetOptions.merge());
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