package com.example.e_tani;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;
import android.widget.Toast;

public class DetailDataActivity extends AppCompatActivity {

    private RecyclerView dataRecyclerView;
    private DataAdapter dataAdapter;
    private LinearLayout emptyState;
    private TextView headerTitle;
    private ImageView backButton, addButton;
    private String jenisTanaman;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_data);

        // Ambil jenis tanaman dari intent
        jenisTanaman = getIntent().getStringExtra("jenis_tanaman");
        if (jenisTanaman == null) {
            jenisTanaman = "Tanaman";
        }

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Inisialisasi komponen
        dataRecyclerView = findViewById(R.id.dataRecyclerView);
        emptyState = findViewById(R.id.emptyState);
        headerTitle = findViewById(R.id.headerTitle);
        backButton = findViewById(R.id.backButton);
        addButton = findViewById(R.id.add_circle);

        // Set judul header
        headerTitle.setText("Data " + jenisTanaman);

        // Setup RecyclerView
        dataRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dataAdapter = new DataAdapter(new ArrayList<>());
        dataRecyclerView.setAdapter(dataAdapter);

        // Back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Add button
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Buka form untuk menambah data baru
                Intent intent = new Intent(DetailDataActivity.this, Form.class);
                startActivity(intent);
            }
        });

        // Load data
        loadData();
    }

    private void loadData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Load data dari Firestore berdasarkan jenis tanaman
        db.collection("harvests")
                .whereEqualTo("userId", user.getUid())
                .whereEqualTo("jenis", jenisTanaman)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(query -> {
                    List<DataModel> dataList = new ArrayList<>();

                    for (DocumentSnapshot doc : query.getDocuments()) {
                        String jenis = doc.getString("jenis");
                        String jumlah = doc.getString("jumlah");
                        String satuan = doc.getString("satuan");
                        String tanggal = doc.getString("tanggal");
                        String status = doc.getString("status");

                        if (jenis != null && jenis.equalsIgnoreCase(jenisTanaman)) {
                            dataList.add(new DataModel(jenisTanaman, jumlah, satuan, tanggal, status));
                        }
                    }

                    // Update UI
                    if (dataList.isEmpty()) {
                        emptyState.setVisibility(View.VISIBLE);
                        dataRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyState.setVisibility(View.GONE);
                        dataRecyclerView.setVisibility(View.VISIBLE);
                        dataAdapter.updateData(dataList);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    emptyState.setVisibility(View.VISIBLE);
                    dataRecyclerView.setVisibility(View.GONE);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data setiap kali kembali ke activity ini
        loadData();
    }
}
