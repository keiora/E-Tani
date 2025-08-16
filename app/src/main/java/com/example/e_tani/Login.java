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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.SetOptions;

public class Login extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private SignInButton googleSignInButton;
    private FirebaseAuth mAuth;
    private GoogleSignInClient googleSignInClient;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

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
                            Toast.makeText(Login.this, "Gagal mendapatkan akun Google", Toast.LENGTH_SHORT).show();
                        }
                    } catch (ApiException e) {
                        Toast.makeText(Login.this, "Google sign-in gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Login.this, "Google sign-in dibatalkan", Toast.LENGTH_SHORT).show();
                }
            });

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
        googleSignInButton = findViewById(R.id.googleSignInButton);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

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

        if (googleSignInButton != null) {
            googleSignInButton.setSize(SignInButton.SIZE_WIDE);
            googleSignInButton.setOnClickListener(v -> beginGoogleSignIn());
        }
    }

    private void beginGoogleSignIn() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        String idToken = account.getIdToken();
        if (idToken == null) {
            Toast.makeText(this, "Token Google tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Cek role dan arahkan
                            ensureUserDocIfMissing(user, account);
                        }
                    } else {
                        Toast.makeText(Login.this, "Firebase auth gagal: " + (task.getException() != null ? task.getException().getMessage() : ""), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void ensureUserDocIfMissing(FirebaseUser user, GoogleSignInAccount account) {
        System.out.println("Ensuring user document exists for: " + user.getUid());
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        System.out.println("User document exists, fetching role");
                        fetchRoleAndNavigate(user.getUid());
                    } else {
                        System.out.println("User document does not exist, creating new one");
                        java.util.Map<String, Object> userDoc = new java.util.HashMap<>();
                        userDoc.put("name", account.getDisplayName() != null ? account.getDisplayName() : "");
                        userDoc.put("email", account.getEmail() != null ? account.getEmail() : "");
                        userDoc.put("provider", "google");
                        userDoc.put("emailVerified", user.isEmailVerified());
                        userDoc.put("role", "petani"); // Default role untuk user baru
                        userDoc.put("createdAt", com.google.firebase.Timestamp.now());
                        
                        System.out.println("Creating user document with role: petani");
                        db.collection("users").document(user.getUid()).set(userDoc, SetOptions.merge())
                                .addOnSuccessListener(unused -> {
                                    System.out.println("User document created successfully");
                                    fetchRoleAndNavigate(user.getUid());
                                })
                                .addOnFailureListener(e -> {
                                    System.out.println("Failed to create user document: " + e.getMessage());
                                    Toast.makeText(Login.this, "Gagal menyimpan user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error checking user document: " + e.getMessage());
                    Toast.makeText(Login.this, "Gagal mengambil data user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
