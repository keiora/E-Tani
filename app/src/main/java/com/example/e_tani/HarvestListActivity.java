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
    private boolean isAdminMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harvest_list);

        // Get status from intent
        currentStatus = getIntent().getStringExtra("status");
        if (currentStatus == null) {
            currentStatus = "waiting";
        }
        isAdminMode = getIntent().getBooleanExtra("admin_mode", false);

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
        adapter = new HarvestAdapter(harvestList, new HarvestAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(HarvestData harvest) { /* no-op */ }
            @Override
            public void onEditClick(HarvestData harvest) { /* disabled for admin */ }
            @Override
            public void onDeleteClick(HarvestData harvest) {
                confirmAndDelete(harvest);
            }
        });
        adapter.setAdminMode(isAdminMode); // Aktifkan mode admin hanya untuk admin
        adapter.setAdminActionListener(new HarvestAdapter.OnAdminActionListener() {
            @Override
            public void onDone(HarvestData harvest, String feedback) {
                updateHarvestStatus(harvest.id, "done", feedback);
            }
            @Override
            public void onReject(HarvestData harvest, String feedback) {
                updateHarvestStatus(harvest.id, "reject", feedback);
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

        com.google.firebase.firestore.Query query = db.collection("harvests");

        // Terapkan filter berdasarkan status/tab
        if ("history".equalsIgnoreCase(currentStatus)) {
            java.util.List<String> historyStatuses = new java.util.ArrayList<>();
            // tangkap berbagai variasi kapitalisasi
            historyStatuses.add("done");
            historyStatuses.add("Done");
            historyStatuses.add("DONE");
            historyStatuses.add("reject");
            historyStatuses.add("Reject");
            historyStatuses.add("REJECT");
            historyStatuses.add("rejected");
            historyStatuses.add("Rejected");
            historyStatuses.add("REJECTED");
            query = query.whereIn("status", historyStatuses);
        } else if ("all".equalsIgnoreCase(currentStatus)) {
            // tanpa filter status
        } else {
            // default waiting
            java.util.List<String> waiting = new java.util.ArrayList<>();
            waiting.add("waiting");
            waiting.add("Waiting");
            waiting.add("WAITING");
            query = query.whereIn("status", waiting);
        }

        // Jika bukan admin mode, batasi ke user yang sedang login
        if (!isAdminMode) {
            FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
            if (current != null) {
                query = query.whereEqualTo("userId", current.getUid());
            }
        }

        query.get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                harvestList.clear();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    // Map manual agar konsisten dengan field Firestore saat ini
                    HarvestData harvest = new HarvestData();
                    harvest.setId(doc.getId());
                    // Fallback ke nama field lama jika ada dokumen lama
                    String jenis = doc.getString("jenisTanaman");
                    if (jenis == null || jenis.isEmpty()) jenis = doc.getString("jenis");
                    harvest.setJenis(jenis);

                    String jumlah = doc.getString("jumlahPanen");
                    if (jumlah == null || jumlah.isEmpty()) jumlah = doc.getString("jumlah");
                    harvest.setJumlah(jumlah);

                    harvest.setSatuan(doc.getString("satuan"));

                    String tanggal = doc.getString("tanggalPanen");
                    if (tanggal == null || tanggal.isEmpty()) tanggal = doc.getString("tanggal");
                    harvest.setTanggal(tanggal);

                    // field tambahan
                    harvest.setLuasLahan(doc.getString("luasLahan"));
                    harvest.setMusim(doc.getString("musim"));
                    harvest.setKualitas(doc.getString("kualitas"));
                    harvest.setHargaJual(doc.getString("hargaJual"));
                    harvest.setLokasiLahan(doc.getString("lokasiLahan"));
                    harvest.setCatatan(doc.getString("catatan"));
                    harvest.setUserId(doc.getString("userId"));
                    harvest.setUserEmail(doc.getString("userEmail"));
                    String status = doc.getString("status");
                    if (status != null && status.equalsIgnoreCase("rejected")) status = "reject";
                    harvest.setStatus(status);

                    com.google.firebase.Timestamp ts = doc.getTimestamp("createdAt");
                    if (ts != null) {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
                        harvest.setCreatedAt(sdf.format(ts.toDate()));
                    }

                    harvestList.add(harvest);
                }
                adapter.notifyDataSetChanged();
            });
    }

    private void confirmAndDelete(HarvestData harvest) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Hapus Data")
                .setMessage("Yakin hapus data '" + (harvest.getJenis() != null ? harvest.getJenis() : "-") + "'?")
                .setPositiveButton("Hapus", (d, w) -> {
                    FirebaseFirestore.getInstance().collection("harvests").document(harvest.getId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Data berhasil dihapus", Toast.LENGTH_SHORT).show();
                                loadHarvestData();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Gagal menghapus: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void updateHarvestStatus(String harvestId, String status, String feedback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String normalized = status == null ? null : status.toLowerCase(java.util.Locale.getDefault());
        db.collection("harvests").document(harvestId)
            .update("status", normalized, "feedback", feedback)
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
        private String userId, userEmail, userName;

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

        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }

        public String getUserEmail() { return userEmail; }
        public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

        public String getUserName() { return userName; }
        public void setUserName(String userName) { this.userName = userName; }
    }
}
