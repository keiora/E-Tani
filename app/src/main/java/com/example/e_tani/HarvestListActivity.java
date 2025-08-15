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
            case "done":
                titleText.setText("Data Panen Disetujui");
                break;
            case "reject":
                titleText.setText("Data Panen Ditolak");
                break;
            case "waiting":
                titleText.setText("Data Panen Menunggu");
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

        Query query = db.collection("harvests")
                .whereEqualTo("userId", user.getUid());

        // Apply status filter
        if (!"history".equals(currentStatus)) {
            query = query.whereEqualTo("status", currentStatus);
        }

        query.orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    harvestList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        HarvestData harvest = new HarvestData();
                        harvest.setId(doc.getId());
                        harvest.setJenis(doc.getString("jenis"));
                        harvest.setJumlah(doc.getString("jumlah"));
                        harvest.setSatuan(doc.getString("satuan"));
                        harvest.setTanggal(doc.getString("tanggal"));
                        harvest.setStatus(doc.getString("status"));
                        
                        Timestamp ts = doc.getTimestamp("createdAt");
                        if (ts != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                            harvest.setCreatedAt(sdf.format(ts.toDate()));
                        }
                        
                        harvestList.add(harvest);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public static class HarvestData {
        private String id, jenis, jumlah, satuan, tanggal, status, createdAt;

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
    }
}
