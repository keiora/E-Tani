package com.example.e_tani;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
public class Statistic extends AppCompatActivity {

    Spinner spinner;
    BarChart barChart;
    TextView textTotalPanen;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ArrayAdapter<String> adapter;
    private final java.util.List<String> tanamanList = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistic);

        spinner = findViewById(R.id.spinnerTanaman);
        barChart = findViewById(R.id.barChart);
        textTotalPanen = findViewById(R.id.textTotalPanen);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup spinner adapter
        tanamanList.add("Pilih Tanaman");
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tanamanList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Load tanaman for current user
        loadTanamanForCurrentUser();

        // Event saat spinner dipilih
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (!selected.equals("Pilih Tanaman")) {
                    tampilkanChartDariFirestore(selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Setup back button
        ImageView backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish(); // Kembali ke halaman sebelumnya
                }
            });
        }
    }

    private void loadTanamanForCurrentUser() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            textTotalPanen.setText("Silakan login untuk melihat statistik");
            return;
        }

        db.collection("harvests")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Set<String> uniqueTanaman = new HashSet<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String jenis = doc.getString("jenis");
                        if (jenis != null && !jenis.trim().isEmpty()) {
                            uniqueTanaman.add(jenis);
                        }
                    }
                    tanamanList.clear();
                    tanamanList.add("Pilih Tanaman");
                    tanamanList.addAll(uniqueTanaman);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    textTotalPanen.setText("Gagal memuat tanaman: " + e.getMessage());
                });
    }

    private void tampilkanChartDariFirestore(String tanaman) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            textTotalPanen.setText("Silakan login untuk melihat statistik");
            return;
        }

        db.collection("harvests")
                .whereEqualTo("userId", user.getUid())
                .whereEqualTo("jenis", tanaman)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    float[] totalPerBulan = new float[12];
                    float totalKeseluruhan = 0f;
                    String satuan = "";

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String jumlahStr = doc.getString("jumlah");
                        String satuanDoc = doc.getString("satuan");
                        if (satuan.isEmpty() && satuanDoc != null) {
                            satuan = satuanDoc;
                        }
                        float jumlah = 0f;
                        try {
                            if (jumlahStr != null) jumlah = Float.parseFloat(jumlahStr);
                        } catch (Exception ignored) {}

                        com.google.firebase.Timestamp ts = doc.getTimestamp("createdAt");
                        int monthIndex = 0;
                        if (ts != null) {
                            java.util.Calendar cal = java.util.Calendar.getInstance();
                            cal.setTime(ts.toDate());
                            monthIndex = cal.get(java.util.Calendar.MONTH);
                        }
                        totalPerBulan[monthIndex] += jumlah;
                        totalKeseluruhan += jumlah;
                    }

                    ArrayList<BarEntry> entries = new ArrayList<>();
                    for (int i = 0; i < 12; i++) {
                        entries.add(new BarEntry(i, totalPerBulan[i]));
                    }

                    BarDataSet dataSet = new BarDataSet(entries, "Panen per Bulan");
                    dataSet.setColor(Color.parseColor("#4CAF50"));
                    dataSet.setValueTextSize(12f);

                    BarData barData = new BarData(dataSet);
                    barChart.setData(barData);

                    String[] bulan = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};
                    barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(bulan));
                    barChart.getXAxis().setGranularity(1f);
                    barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                    barChart.getAxisLeft().setAxisMinimum(0f);
                    barChart.getAxisRight().setAxisMinimum(0f);

                    barChart.animateY(1000);
                    barChart.invalidate();

                    String suffix = satuan != null && !satuan.isEmpty() ? (" " + satuan) : "";
                    textTotalPanen.setText("Total Panen: " + formatNumber(totalKeseluruhan) + suffix);
                })
                .addOnFailureListener(e -> {
                    textTotalPanen.setText("Gagal memuat data: " + e.getMessage());
                });
    }

    private String formatNumber(float value) {
        if (value == (long) value) {
            return String.format(java.util.Locale.getDefault(), "%d", (long) value);
        }
        return String.format(java.util.Locale.getDefault(), "%.2f", value);
    }
}