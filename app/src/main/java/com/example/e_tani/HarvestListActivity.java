package com.example.e_tani;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

public class HarvestListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView titleText;
    private ImageView backButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentStatus;
    private HarvestAdapter adapter;
    private List<HarvestData> harvestList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harvest_list);

        // Get status from intent
        currentStatus = getIntent().getStringExtra("status");
        if (currentStatus == null) {
            currentStatus = "waiting";
        }

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        titleText = findViewById(R.id.titleText);
        backButton = findViewById(R.id.backButton);

        // Set title based on status
        setTitleByStatus();

        // Setup RecyclerView
        harvestList = new ArrayList<>();
        adapter = new HarvestAdapter(harvestList);
        adapter.setAdminMode(true); // Aktifkan mode admin
        adapter.setAdminActionListener(new HarvestAdapter.OnAdminActionListener() {
            @Override
            public void onDone(HarvestData harvest, String feedback) {
                updateHarvestStatus(harvest.id, "done", feedback);
            }
            @Override
            public void onReject(HarvestData harvest, String feedback) {
                updateHarvestStatus(harvest.id, "rejected", feedback);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Back button
        backButton.setOnClickListener(v -> finish());

        // Load data
        loadHarvestData();
    }

    private void setTitleByStatus() {
        switch (currentStatus) {
            case "all":
                titleText.setText("Semua Data Panen");
                break;
            case "history":
                titleText.setText("Riwayat Panen");
                break;
            default:
                titleText.setText("Data Panen");
        }
    }

    private void loadHarvestData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("harvests")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                harvestList.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    HarvestData harvest = doc.toObject(HarvestData.class);
                    // Contoh filter: hanya tampilkan waiting untuk admin
                    if ("waiting".equals(harvest.status)) {
                        harvestList.add(harvest);
                    }
                    // Untuk riwayat petani: if (!"waiting".equals(harvest.status)) { ... }
                }
                adapter.notifyDataSetChanged();
            });
    }

    private void updateHarvestStatus(String harvestId, String status, String feedback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("harvests").document(harvestId)
            .update("status", status, "feedback", feedback)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Status berhasil diupdate", Toast.LENGTH_SHORT).show();
                loadHarvestData();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Gagal update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    public static class HarvestData {
        private String id, jenis, jumlah, satuan, tanggal, status, createdAt;
        private String luasLahan, musim, kualitas, hargaJual, lokasiLahan, catatan;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getJenis() { return jenis; }
        public void setJenis(String jenis) { this.jenis = jenis; }
        
        public String getJumlah() { return jumlah; }
        public void setJumlah(String jumlah) { this.jumlah = jumlah; }
        
        public String getSatuan() { return satuan; }
        public void setSatuan(String satuan) { this.satuan = satuan; }
        
        public String getTanggal() { return tanggal; }
        public void setTanggal(String tanggal) { this.tanggal = tanggal; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        
        // Getters and Setters untuk field baru
        public String getLuasLahan() { return luasLahan; }
        public void setLuasLahan(String luasLahan) { this.luasLahan = luasLahan; }
        
        public String getMusim() { return musim; }
        public void setMusim(String musim) { this.musim = musim; }
        
        public String getKualitas() { return kualitas; }
        public void setKualitas(String kualitas) { this.kualitas = kualitas; }
        
        public String getHargaJual() { return hargaJual; }
        public void setHargaJual(String hargaJual) { this.hargaJual = hargaJual; }
        
        public String getLokasiLahan() { return lokasiLahan; }
        public void setLokasiLahan(String lokasiLahan) { this.lokasiLahan = lokasiLahan; }
        
        public String getCatatan() { return catatan; }
        public void setCatatan(String catatan) { this.catatan = catatan; }
    }
}
