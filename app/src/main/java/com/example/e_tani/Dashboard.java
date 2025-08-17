package com.example.e_tani;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

public class Dashboard extends AppCompatActivity {

    private EditText searchEditText;
    private ImageView menuIcon, profileIcon;
    private SharedPreferences sharedPreferences;
    
    // RecyclerView untuk menampilkan semua data
    private RecyclerView dataRecyclerView;
    private HarvestAdapter dataAdapter;
    private List<HarvestListActivity.HarvestData> dataList;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Debug: tampilkan status login
        System.out.println("=== DASHBOARD STARTUP ===");
        System.out.println("SharedPreferences isLoggedIn: " + sharedPreferences.getBoolean("isLoggedIn", false));
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            System.out.println("Firebase current user: " + currentUser.getEmail());
            System.out.println("Firebase user ID: " + currentUser.getUid());
        } else {
            System.out.println("No Firebase current user");
        }

        // Inisialisasi komponen
        searchEditText = findViewById(R.id.searchEditText);
        menuIcon = findViewById(R.id.menuIcon);
        profileIcon = findViewById(R.id.profileIcon);
        
        // Inisialisasi RecyclerView
        dataRecyclerView = findViewById(R.id.dataRecyclerView);
        dataList = new ArrayList<>();
        dataAdapter = new HarvestAdapter(dataList, new HarvestAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(HarvestListActivity.HarvestData harvest) {
                showHarvestDetailDialog(harvest);
            }
            
            @Override
            public void onEditClick(HarvestListActivity.HarvestData harvest) {
                showEditHarvestDialog(harvest);
            }
            
            @Override
            public void onDeleteClick(HarvestListActivity.HarvestData harvest) {
                showDeleteConfirmationDialog(harvest);
            }
        });
        dataRecyclerView.setAdapter(dataAdapter);
        dataRecyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        // Set click listeners untuk cards
        // jagungCard.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {
        //         // Buka semua data
        //         Intent intent = new Intent(Dashboard.this, HarvestListActivity.class);
        //         intent.putExtra("status", "all");
        //         startActivity(intent);
        //     }
        // });

        // padiCard.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {
        //         // Buka semua data
        //         Intent intent = new Intent(Dashboard.this, HarvestListActivity.class);
        //         intent.putExtra("status", "all");
        //         startActivity(intent);
        //     }
        // });

        // jatiCard.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View v) {
        //         // Buka semua data
        //         Intent intent = new Intent(Dashboard.this, HarvestListActivity.class);
        //         intent.putExtra("status", "all");
        //         startActivity(intent);
        //     }
        // });

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

        // Muat data dari Firestore dengan delay kecil untuk memastikan UI sudah siap
        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadDashboardData();
            }
        }, 100);
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
        // Semua Data filter button
        LinearLayout allFilter = findViewById(R.id.doneFilter);
        if (allFilter != null) {
            allFilter.setOnClickListener(v -> {
                Intent intent = new Intent(Dashboard.this, HarvestListActivity.class);
                intent.putExtra("status", "all");
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

        // Debug: tampilkan semua data terlebih dahulu
        debugShowAllData();

        db.collection("harvests")
                .whereEqualTo("userId", user.getUid())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    
                    // Debug: print semua data yang ditemukan
                    System.out.println("Total data found: " + query.getDocuments().size());
                    
                    // Ambil semua data dan simpan untuk ditampilkan
                    List<HarvestListActivity.HarvestData> allData = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        HarvestListActivity.HarvestData harvestData = new HarvestListActivity.HarvestData();
                        harvestData.setId(doc.getId());
                        // Gunakan field baru yang sudah diupdate di Form.java
                        harvestData.setJenis(doc.getString("jenisTanaman"));
                        harvestData.setJumlah(doc.getString("jumlahPanen"));
                        harvestData.setSatuan(doc.getString("satuan"));
                        harvestData.setTanggal(doc.getString("tanggalPanen"));
                        harvestData.setStatus(doc.getString("status"));
                        
                        // Set field baru
                        harvestData.setLuasLahan(doc.getString("luasLahan"));
                        harvestData.setMusim(doc.getString("musim"));
                        harvestData.setKualitas(doc.getString("kualitas"));
                        harvestData.setHargaJual(doc.getString("hargaJual"));
                        harvestData.setLokasiLahan(doc.getString("lokasiLahan"));
                        harvestData.setCatatan(doc.getString("catatan"));
                        
                        Timestamp ts = doc.getTimestamp("createdAt");
                        if (ts != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                            harvestData.setCreatedAt(sdf.format(ts.toDate()));
                        }
                        
                        allData.add(harvestData);
                        System.out.println("=== PROCESSED DATA ===");
                        System.out.println("ID: " + harvestData.getId());
                        System.out.println("Jenis: " + harvestData.getJenis());
                        System.out.println("Jumlah: " + harvestData.getJumlah());
                        System.out.println("Satuan: " + harvestData.getSatuan());
                        System.out.println("Tanggal: " + harvestData.getTanggal());
                        System.out.println("Status: " + harvestData.getStatus());
                        System.out.println("CreatedAt: " + harvestData.getCreatedAt());
                        System.out.println("---");
                    }
                    
                    // Tampilkan data real yang masuk
                    if (!allData.isEmpty()) {
                        dataList.clear();
                        dataList.addAll(allData);
                        dataAdapter.notifyDataSetChanged();
                        
                        System.out.println("Data loaded successfully.");
                    } else {
                        // Jika tidak ada data
                        dataList.clear();
                        dataAdapter.notifyDataSetChanged();
                        Toast.makeText(Dashboard.this, "Belum ada data", Toast.LENGTH_SHORT).show();
                        System.out.println("No data found, showing empty state");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Dashboard.this, "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    System.out.println("Error loading data: " + e.getMessage());
                });
    }

    private void applySearch(String keywordRaw) {
        String keyword = keywordRaw == null ? "" : keywordRaw.trim().toLowerCase();
        System.out.println("Searching for keyword: " + keyword);
        
        if (keyword.isEmpty()) {
            // Reset to default - reload semua data
            loadDashboardData();
            return;
        }

        // Jika ada keyword, cari data yang sesuai
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
                    List<HarvestListActivity.HarvestData> filteredData = new ArrayList<>();
                    
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        HarvestListActivity.HarvestData harvestData = new HarvestListActivity.HarvestData();
                        harvestData.setId(doc.getId());
                        // Gunakan field baru yang sudah diupdate di Form.java
                        harvestData.setJenis(doc.getString("jenisTanaman"));
                        harvestData.setJumlah(doc.getString("jumlahPanen"));
                        harvestData.setSatuan(doc.getString("satuan"));
                        harvestData.setTanggal(doc.getString("tanggalPanen"));
                        harvestData.setStatus(doc.getString("status"));
                        
                        Timestamp ts = doc.getTimestamp("createdAt");
                        if (ts != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                            harvestData.setCreatedAt(sdf.format(ts.toDate()));
                        }
                        
                        if (harvestData.getJenis() != null && harvestData.getJenis().toLowerCase().contains(keyword) ||
                            harvestData.getJumlah() != null && harvestData.getJumlah().toLowerCase().contains(keyword) ||
                            harvestData.getSatuan() != null && harvestData.getSatuan().toLowerCase().contains(keyword)) {
                            filteredData.add(harvestData);
                        }
                    }
                    
                    if (!filteredData.isEmpty()) {
                        // Tampilkan data yang ditemukan
                        dataList.clear();
                        dataList.addAll(filteredData);
                        dataAdapter.notifyDataSetChanged();
                        
                        System.out.println("Search result displayed: " + filteredData.size() + " results");
                    } else {
                        // Tidak ada hasil
                        dataList.clear();
                        dataAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Tidak ada hasil untuk: " + keywordRaw, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal mencari data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String valueOrEmpty(String s) { return s == null ? "" : s; }
    
    // Method untuk menampilkan detail data dalam dialog
    private void showHarvestDetailDialog(HarvestListActivity.HarvestData harvest) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setTitle("Detail Data Panen");
        
        // Buat layout untuk dialog dengan background putih
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);
        layout.setBackgroundColor(getResources().getColor(android.R.color.white));
        
        // Tambahkan TextView untuk setiap field dengan styling yang lebih bagus
        TextView jenisText = new TextView(this);
        jenisText.setText("Jenis Tanaman: " + (harvest.getJenis() != null ? harvest.getJenis() : "Tidak ada data"));
        jenisText.setTextSize(16);
        jenisText.setTextColor(getResources().getColor(android.R.color.black));
        jenisText.setPadding(0, 15, 0, 15);
        jenisText.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(jenisText);
        
        TextView jumlahText = new TextView(this);
        jumlahText.setText("Jumlah: " + (harvest.getJumlah() != null ? harvest.getJumlah() : "Tidak ada data") + " " + (harvest.getSatuan() != null ? harvest.getSatuan() : ""));
        jumlahText.setTextSize(16);
        jumlahText.setTextColor(getResources().getColor(android.R.color.black));
        jumlahText.setPadding(0, 15, 0, 15);
        layout.addView(jumlahText);
        
        TextView luasLahanText = new TextView(this);
        luasLahanText.setText("Luas Lahan: " + (harvest.getLuasLahan() != null ? harvest.getLuasLahan() : "Tidak ada data") + " Hektar");
        luasLahanText.setTextSize(16);
        luasLahanText.setTextColor(getResources().getColor(android.R.color.black));
        luasLahanText.setPadding(0, 15, 0, 15);
        layout.addView(luasLahanText);
        
        TextView musimText = new TextView(this);
        musimText.setText("Musim: " + (harvest.getMusim() != null ? harvest.getMusim() : "Tidak ada data"));
        musimText.setTextSize(16);
        musimText.setTextColor(getResources().getColor(android.R.color.black));
        musimText.setPadding(0, 15, 0, 15);
        layout.addView(musimText);
        
        TextView kualitasText = new TextView(this);
        kualitasText.setText("Kualitas: " + (harvest.getKualitas() != null ? harvest.getKualitas() : "Tidak ada data"));
        kualitasText.setTextSize(16);
        kualitasText.setTextColor(getResources().getColor(android.R.color.black));
        kualitasText.setPadding(0, 15, 0, 15);
        layout.addView(kualitasText);
        
        TextView hargaJualText = new TextView(this);
        hargaJualText.setText("Harga Jual: Rp " + (harvest.getHargaJual() != null ? harvest.getHargaJual() : "0"));
        hargaJualText.setTextSize(16);
        hargaJualText.setTextColor(getResources().getColor(android.R.color.black));
        hargaJualText.setPadding(0, 15, 0, 15);
        layout.addView(hargaJualText);
        
        TextView lokasiLahanText = new TextView(this);
        lokasiLahanText.setText("Lokasi Lahan: " + (harvest.getLokasiLahan() != null ? harvest.getLokasiLahan() : "Tidak ada data"));
        lokasiLahanText.setTextSize(16);
        lokasiLahanText.setTextColor(getResources().getColor(android.R.color.black));
        lokasiLahanText.setPadding(0, 15, 0, 15);
        layout.addView(lokasiLahanText);
        
        TextView catatanText = new TextView(this);
        catatanText.setText("Catatan: " + (harvest.getCatatan() != null ? harvest.getCatatan() : "Tidak ada data"));
        catatanText.setTextSize(16);
        catatanText.setTextColor(getResources().getColor(android.R.color.black));
        catatanText.setPadding(0, 15, 0, 15);
        layout.addView(catatanText);
        
        TextView tanggalText = new TextView(this);
        tanggalText.setText("Tanggal Panen: " + (harvest.getTanggal() != null ? harvest.getTanggal() : "Tidak ada data"));
        tanggalText.setTextSize(16);
        tanggalText.setTextColor(getResources().getColor(android.R.color.black));
        tanggalText.setPadding(0, 15, 0, 15);
        layout.addView(tanggalText);
        
        TextView statusText = new TextView(this);
        statusText.setText("Status: " + getStatusText(harvest.getStatus()));
        statusText.setTextSize(16);
        statusText.setTextColor(getStatusColor(harvest.getStatus()));
        statusText.setPadding(0, 15, 0, 15);
        statusText.setTypeface(null, android.graphics.Typeface.BOLD);
        layout.addView(statusText);
        
        TextView createdAtText = new TextView(this);
        createdAtText.setText("Waktu Input: " + (harvest.getCreatedAt() != null ? harvest.getCreatedAt() : "Tidak ada data"));
        createdAtText.setTextSize(16);
        createdAtText.setTextColor(getResources().getColor(android.R.color.darker_gray));
        createdAtText.setPadding(0, 15, 0, 15);
        layout.addView(createdAtText);
        
        builder.setView(layout);
        builder.setPositiveButton("Tutup", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Set background dialog menjadi putih
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
    }
    
    // Method untuk menampilkan dialog edit data
    private void showEditHarvestDialog(HarvestListActivity.HarvestData harvest) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setTitle("Edit Data Panen");
        builder.setMessage("Apakah Anda yakin ingin mengedit data ini?");
        
        builder.setPositiveButton("Ya", (dialog, which) -> {
            // Buka Form dengan data yang sudah ada
            Intent intent = new Intent(Dashboard.this, Form.class);
            intent.putExtra("edit_mode", true);
            intent.putExtra("harvest_id", harvest.getId());
            intent.putExtra("jenis_tanaman", harvest.getJenis());
            intent.putExtra("jumlah_panen", harvest.getJumlah());
            intent.putExtra("satuan", harvest.getSatuan());
            intent.putExtra("tanggal_panen", harvest.getTanggal());
            intent.putExtra("luas_lahan", harvest.getLuasLahan());
            intent.putExtra("musim", harvest.getMusim());
            intent.putExtra("kualitas", harvest.getKualitas());
            intent.putExtra("harga_jual", harvest.getHargaJual());
            intent.putExtra("lokasi_lahan", harvest.getLokasiLahan());
            intent.putExtra("catatan", harvest.getCatatan());
            startActivity(intent);
        });
        
        builder.setNegativeButton("Tidak", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
    }
    
    // Method untuk menampilkan dialog konfirmasi hapus
    private void showDeleteConfirmationDialog(HarvestListActivity.HarvestData harvest) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialog);
        builder.setTitle("Hapus Data Panen");
        builder.setMessage("Apakah Anda yakin ingin menghapus data '" + harvest.getJenis() + "'? Data yang sudah dihapus tidak dapat dikembalikan.");
        
        builder.setPositiveButton("Ya, Hapus", (dialog, which) -> {
            deleteHarvestData(harvest.getId());
        });
        
        builder.setNegativeButton("Batal", null);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
    }
    
    // Method untuk menghapus data dari Firestore
    private void deleteHarvestData(String harvestId) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Tampilkan loading
        Toast.makeText(this, "Menghapus data...", Toast.LENGTH_SHORT).show();
        
        db.collection("harvests").document(harvestId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Dashboard.this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show();
                    // Refresh data
                    loadDashboardData();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Dashboard.this, "Gagal menghapus data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
    
    // Helper method untuk mendapatkan text status
    private String getStatusText(String status) {
        switch (status) {
            case "done":
                return "Disetujui";
            case "reject":
                return "Ditolak";
            case "waiting":
                return "Menunggu";
            default:
                return status;
        }
    }
    
    // Helper method untuk mendapatkan color status
    private int getStatusColor(String status) {
        switch (status) {
            case "done":
                return 0xFF4CAF50; // Green
            case "reject":
                return 0xFFF44336; // Red
            case "waiting":
                return 0xFFFF9800; // Orange
            default:
                return 0xFF666666; // Gray
        }
    }
    
    // Method untuk debugging - tampilkan semua data
    private void debugShowAllData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            System.out.println("No user logged in");
            return;
        }

        db.collection("harvests")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(query -> {
                    System.out.println("=== DEBUG: ALL DATA FOR USER ===");
                    System.out.println("Total documents: " + query.getDocuments().size());
                    
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        System.out.println("Document ID: " + doc.getId());
                        System.out.println("jenisTanaman: " + doc.getString("jenisTanaman"));
                        System.out.println("jumlahPanen: " + doc.getString("jumlahPanen"));
                        System.out.println("satuan: " + doc.getString("satuan"));
                        System.out.println("tanggalPanen: " + doc.getString("tanggalPanen"));
                        System.out.println("luasLahan: " + doc.getString("luasLahan"));
                        System.out.println("musim: " + doc.getString("musim"));
                        System.out.println("kualitas: " + doc.getString("kualitas"));
                        System.out.println("status: " + doc.getString("status"));
                        System.out.println("createdAt: " + doc.getTimestamp("createdAt"));
                        System.out.println("---");
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error getting all data: " + e.getMessage());
                });
    }
    
    // Method untuk memaksa refresh data
    public void refreshData() {
        System.out.println("Forcing data refresh...");
        loadDashboardData();
        // Juga tampilkan semua data untuk debugging
        debugShowAllData();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("Dashboard onResume called");
        // Reload data setiap kali kembali ke Dashboard dari Firestore
        loadDashboardData();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("Dashboard onStart called");
        // Pastikan data dimuat saat activity dimulai
        loadDashboardData();
    }
}


