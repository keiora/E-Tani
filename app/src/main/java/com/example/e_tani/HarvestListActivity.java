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
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Debug: print status yang sedang dicari
        System.out.println("HarvestListActivity: Loading data for status: " + currentStatus);

        Query query = db.collection("harvests")
                .whereEqualTo("userId", user.getUid());

        // Apply status filter untuk semua status
        if ("all".equals(currentStatus)) {
            // Untuk all, ambil semua data tanpa filter status
            System.out.println("All mode: getting all data without status filter");
        } else if ("history".equals(currentStatus)) {
            // Untuk history, ambil data yang sudah selesai (done/reject) atau lama
            System.out.println("History mode: getting completed data (done/reject)");
        }

        query.orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    System.out.println("Found " + queryDocumentSnapshots.getDocuments().size() + " documents");
                    
                    harvestList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String docStatus = doc.getString("status");
                        System.out.println("Document status: " + docStatus + ", jenis: " + doc.getString("jenisTanaman"));
                        
                        // Untuk history, filter data yang sudah selesai
                        if ("history".equals(currentStatus)) {
                            if ("waiting".equals(docStatus)) {
                                continue; // Skip data yang masih waiting
                            }
                            // Hanya tampilkan data dengan status done atau reject
                            if (!"done".equals(docStatus) && !"reject".equals(docStatus)) {
                                continue;
                            }
                        }
                        
                        HarvestData harvest = new HarvestData();
                        harvest.setId(doc.getId());
                        // Gunakan field baru yang sudah diupdate di Form.java
                        harvest.setJenis(doc.getString("jenisTanaman"));
                        harvest.setJumlah(doc.getString("jumlahPanen"));
                        harvest.setSatuan(doc.getString("satuan"));
                        harvest.setTanggal(doc.getString("tanggalPanen"));
                        harvest.setStatus(doc.getString("status"));
                        
                        // Set field baru
                        harvest.setLuasLahan(doc.getString("luasLahan"));
                        harvest.setMusim(doc.getString("musim"));
                        harvest.setKualitas(doc.getString("kualitas"));
                        harvest.setHargaJual(doc.getString("hargaJual"));
                        harvest.setLokasiLahan(doc.getString("lokasiLahan"));
                        harvest.setCatatan(doc.getString("catatan"));
                        
                        Timestamp ts = doc.getTimestamp("createdAt");
                        if (ts != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                            harvest.setCreatedAt(sdf.format(ts.toDate()));
                        }
                        
                        harvestList.add(harvest);
                    }
                    
                    System.out.println("Added " + harvestList.size() + " items to list");
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    System.out.println("Error loading harvest data: " + e.getMessage());
                    Toast.makeText(this, "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
