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

public class Dashboard extends AppCompatActivity {

    private EditText searchEditText;
    private LinearLayout jagungCard, padiCard, jatiCard;
    private ImageView menuIcon, profileIcon;
    private SharedPreferences sharedPreferences;
    
    // TextView untuk menampilkan data di card
    private TextView jagungTitle, jagungProgress;
    private TextView padiTitle, padiProgress;
    private TextView jatiTitle, jatiProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

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

        // Search functionality
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, android.view.KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String keyword = searchEditText.getText().toString().trim();
                    if (!keyword.isEmpty()) {
                        Toast.makeText(Dashboard.this, "Mencari: " + keyword, Toast.LENGTH_SHORT).show();
                        // TODO: Implementasi search functionality
                    }
                    return true;
                }
                return false;
            }
        });

        // Tampilkan data dari Intent jika ada
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("nama_tanaman") && intent.hasExtra("jumlah")) {
            String nama = intent.getStringExtra("nama_tanaman");
            String jumlah = intent.getStringExtra("jumlah");
            Toast.makeText(this, "Data baru: " + nama + " - " + jumlah, Toast.LENGTH_LONG).show();
        }
        
        // Load dan tampilkan data dari SharedPreferences
        loadDataFromSharedPreferences();
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
    
    private void loadDataFromSharedPreferences() {
        // Ambil data dari SharedPreferences
        String jenis = sharedPreferences.getString("jenis", "");
        String jumlah = sharedPreferences.getString("jumlah", "");
        String satuan = sharedPreferences.getString("satuan", "");
        String tanggal = sharedPreferences.getString("tanggal", "");
        
        // Update card berdasarkan jenis tanaman
        if (!jenis.isEmpty()) {
            if (jenis.toLowerCase().contains("jagung")) {
                jagungTitle.setText("Data Jagung");
                jagungProgress.setText(jumlah + " " + satuan + " - " + tanggal);
            } else if (jenis.toLowerCase().contains("padi")) {
                padiTitle.setText("Data Padi");
                padiProgress.setText(jumlah + " " + satuan + " - " + tanggal);
            } else if (jenis.toLowerCase().contains("jati")) {
                jatiTitle.setText("Data Jati");
                jatiProgress.setText(jumlah + " " + satuan + " - " + tanggal);
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reload data setiap kali kembali ke Dashboard
        loadDataFromSharedPreferences();
    }
}
