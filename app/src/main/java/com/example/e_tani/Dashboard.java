package com.example.e_tani;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class Dashboard extends AppCompatActivity {

    private EditText searchEditText;
    private LinearLayout jagungCard, padiCard, jatiCard;
    private ImageView menuIcon, profileIcon;
    private SharedPreferences sharedPreferences;
    
    // TextView untuk menampilkan data di card
    private TextView jagungTitle, jagungProgress;
    private TextView padiTitle, padiProgress;
    private TextView jatiTitle, jatiProgress;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private java.util.Map<String, DocumentSnapshot> latestByJenis = new java.util.HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inisialisasi komponen
        searchEditText = findViewById(R.id.searchEditText);
        jagungCard = findViewById(R.id.jagungCard);
        padiCard = findViewById(R.id.padiCard);
        jatiCard = findViewById(R.id.jatiCard);
        menuIcon = findViewById(R.id.menuIcon);
        profileIcon = findViewById(R.id.profileIcon);
        
        // Inisialisasi TextView untuk card
        jagungTitle = findViewById(R.id.jagungTitle);
        jagungProgress = findViewById(R.id.jagungProgress);
        padiTitle = findViewById(R.id.padiTitle);
        padiProgress = findViewById(R.id.padiProgress);
        jatiTitle = findViewById(R.id.jatiTitle);
        jatiProgress = findViewById(R.id.jatiProgress);

        // Set click listeners untuk cards
        jagungCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, DetailDataActivity.class);
                intent.putExtra("jenis_tanaman", "Jagung");
                startActivity(intent);
            }
        });

        padiCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, DetailDataActivity.class);
                intent.putExtra("jenis_tanaman", "Padi");
                startActivity(intent);
            }
        });

        jatiCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Dashboard.this, DetailDataActivity.class);
                intent.putExtra("jenis_tanaman", "Jati");
                startActivity(intent);
            }
        });

        // Menu icon click listener
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Dashboard.this, "Menu", Toast.LENGTH_SHORT).show();
                // TODO: Buka menu atau drawer
            }
        });

        // Profile icon click listener
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tampilkan dialog logout
                showLogoutDialog();
            }
        });

        // Set click listeners untuk bottom navigation
        setupBottomNavigation();

        // Setup filter buttons
        setupFilterButtons();

        // Search functionality
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String keyword = searchEditText.getText().toString().trim();
                    applySearch(keyword);
                    return true;
                }
                return false;
            }
        });

        // Muat data dari Firestore
        loadDashboardData();
    }

    private void setupBottomNavigation() {
        // Home tab (sudah aktif)
        LinearLayout homeTab = findViewById(R.id.homeTab);
        if (homeTab != null) {
            homeTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Dashboard.this, "Home", Toast.LENGTH_SHORT).show();
                    // TODO: Refresh dashboard atau kembali ke home
                }
            });
        }

        // Add tab
        LinearLayout addTab = findViewById(R.id.addTab);
        if (addTab != null) {
            addTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Dashboard.this, "Add Data", Toast.LENGTH_SHORT).show();
                    // TODO: Buka form untuk menambah data
                    Intent intent = new Intent(Dashboard.this, Form.class);
                    startActivity(intent);
                }
            });
        }

        // Statistik tab
        LinearLayout statsTab = findViewById(R.id.statsTab);
        if (statsTab != null) {
            statsTab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(Dashboard.this, "Statistik", Toast.LENGTH_SHORT).show();
                    // TODO: Buka halaman statistik dengan MPAndroidChart
                    Intent intent = new Intent(Dashboard.this, Statistic.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void setupFilterButtons() {
        // Done filter button
        LinearLayout doneFilter = findViewById(R.id.doneFilter);
        if (doneFilter != null) {
            doneFilter.setOnClickListener(v -> {
                Intent intent = new Intent(Dashboard.this, HarvestListActivity.class);
                intent.putExtra("status", "done");
                startActivity(intent);
            });
        }

        // History filter button
        LinearLayout historyFilter = findViewById(R.id.historyFilter);
        if (historyFilter != null) {
            historyFilter.setOnClickListener(v -> {
                Intent intent = new Intent(Dashboard.this, HarvestListActivity.class);
                intent.putExtra("status", "history");
                startActivity(intent);
            });
        }

        // Reject filter button
        LinearLayout rejectFilter = findViewById(R.id.rejectFilter);
        if (rejectFilter != null) {
            rejectFilter.setOnClickListener(v -> {
                Intent intent = new Intent(Dashboard.this, HarvestListActivity.class);
                intent.putExtra("status", "reject");
                startActivity(intent);
            });
        }

        // Waiting filter button
        LinearLayout waitingFilter = findViewById(R.id.waitingFilter);
        if (waitingFilter != null) {
            waitingFilter.setOnClickListener(v -> {
                Intent intent = new Intent(Dashboard.this, HarvestListActivity.class);
                intent.putExtra("status", "waiting");
                startActivity(intent);
            });
        }
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("Apakah Anda yakin ingin keluar?");
        builder.setPositiveButton("Ya", new android.content.DialogInterface.OnClickListener() {
            @Override
            public void onClick(android.content.DialogInterface dialog, int which) {
                // Hapus status login
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", false);
                editor.apply();

                Toast.makeText(Dashboard.this, "Logout berhasil", Toast.LENGTH_SHORT).show();
                
                // Kembali ke Home
                Intent intent = new Intent(Dashboard.this, Home.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton("Tidak", null);
        builder.show();
    }
    
    private void loadDashboardData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("harvests")
                .whereEqualTo("userId", user.getUid())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    latestByJenis.clear();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String jenis = safeLower(doc.getString("jenis"));
                        if (jenis == null || jenis.isEmpty()) continue;
                        // Simpan entri terbaru per jenis
                        if (!latestByJenis.containsKey(jenis)) {
                            latestByJenis.put(jenis, doc);
                        }
                    }
                    // Render cards default
                    renderCard("jagung", jagungTitle, jagungProgress, "Data Jagung");
                    renderCard("padi", padiTitle, padiProgress, "Data Padi");
                    renderCard("jati", jatiTitle, jatiProgress, "Data Jati");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Dashboard.this, "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void applySearch(String keywordRaw) {
        String keyword = keywordRaw == null ? "" : keywordRaw.trim().toLowerCase();
        if (keyword.isEmpty()) {
            // Reset to default rendering
            renderCard("jagung", jagungTitle, jagungProgress, "Data Jagung");
            renderCard("padi", padiTitle, padiProgress, "Data Padi");
            renderCard("jati", jatiTitle, jatiProgress, "Data Jati");
            return;
        }

        boolean any = false;
        if ("jagung".contains(keyword)) { renderCard("jagung", jagungTitle, jagungProgress, "Data Jagung"); any = true; } else { setEmpty(jagungTitle, jagungProgress, "Data Jagung"); }
        if ("padi".contains(keyword)) { renderCard("padi", padiTitle, padiProgress, "Data Padi"); any = true; } else { setEmpty(padiTitle, padiProgress, "Data Padi"); }
        if ("jati".contains(keyword)) { renderCard("jati", jatiTitle, jatiProgress, "Data Jati"); any = true; } else { setEmpty(jatiTitle, jatiProgress, "Data Jati"); }

        if (!any) {
            Toast.makeText(this, "Tidak ada hasil untuk: " + keywordRaw, Toast.LENGTH_SHORT).show();
        }
    }

    private void renderCard(String jenisKey, TextView title, TextView progress, String defaultTitle) {
        title.setText(defaultTitle);
        DocumentSnapshot doc = latestByJenis.get(jenisKey);
        if (doc == null) {
            progress.setText("Belum ada data");
            return;
        }
        String jumlah = valueOrEmpty(doc.getString("jumlah"));
        String satuan = valueOrEmpty(doc.getString("satuan"));
        String tanggal = valueOrEmpty(doc.getString("tanggal"));
        if (tanggal.isEmpty()) {
            Timestamp ts = doc.getTimestamp("createdAt");
            if (ts != null) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                tanggal = sdf.format(ts.toDate());
            }
        }
        String info = jumlah;
        if (!satuan.isEmpty()) info += " " + satuan;
        if (!tanggal.isEmpty()) info += " - " + tanggal;
        if (info.isEmpty()) info = "Belum ada data";
        progress.setText(info);
    }

    private void setEmpty(TextView title, TextView progress, String defaultTitle) {
        title.setText(defaultTitle);
        progress.setText("Belum ada data");
    }

    private String valueOrEmpty(String s) { return s == null ? "" : s; }
    private String safeLower(String s) { return s == null ? null : s.toLowerCase(); }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload data setiap kali kembali ke Dashboard dari Firestore
        loadDashboardData();
    }
}
