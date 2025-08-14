package com.example.e_tani;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DetailDataActivity extends AppCompatActivity {

    private RecyclerView dataRecyclerView;
    private DataAdapter dataAdapter;
    private LinearLayout emptyState;
    private TextView headerTitle;
    private ImageView backButton, addButton;
    private String jenisTanaman;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_data);

        // Ambil jenis tanaman dari intent
        jenisTanaman = getIntent().getStringExtra("jenis_tanaman");
        if (jenisTanaman == null) {
            jenisTanaman = "Tanaman";
        }

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("PanenPrefs", MODE_PRIVATE);

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
        List<DataModel> dataList = new ArrayList<>();

        // Ambil data dari SharedPreferences
        String jenis = sharedPreferences.getString("jenis", "");
        String jumlah = sharedPreferences.getString("jumlah", "");
        String satuan = sharedPreferences.getString("satuan", "");
        String tanggal = sharedPreferences.getString("tanggal", "");

        // Jika ada data dan jenis tanaman sesuai
        if (!jenis.isEmpty() && jenis.toLowerCase().contains(jenisTanaman.toLowerCase())) {
            String foto = sharedPreferences.getString("foto", "");
            dataList.add(new DataModel(jenis, jumlah, satuan, tanggal, foto));
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data setiap kali kembali ke activity ini
        loadData();
    }
}
