package com.example.e_tani;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Dashboard extends AppCompatActivity {

    private EditText searchBar;
    private BottomNavigationView navView;
    private TextView dataDariFormText;
    private TextView latestDataText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // Inisialisasi komponen
        searchBar = findViewById(R.id.searchBar);
        navView = findViewById(R.id.nav_view);
        dataDariFormText = findViewById(R.id.latestDataText);
        latestDataText = findViewById(R.id.latestDataText);

        // Tampilkan data dari Intent (misalnya data langsung setelah isi form)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("nama_tanaman") && intent.hasExtra("jumlah")) {
            String nama = intent.getStringExtra("nama_tanaman");
            String jumlah = intent.getStringExtra("jumlah");
            dataDariFormText.setText("Data dari Form:\nTanaman: " + nama + "\nJumlah: " + jumlah);
        } else {
            dataDariFormText.setText("Belum ada input form terkirim.");
        }

        // Ambil data dari SharedPreferences dan tampilkan
        SharedPreferences prefs = getSharedPreferences("PanenPrefs", MODE_PRIVATE);
        String jenis = prefs.getString("jenis", null);
        String jumlah = prefs.getString("jumlah", null);
        String satuan = prefs.getString("satuan", null);
        String tanggal = prefs.getString("tanggal", null);

        if (jenis != null && jumlah != null && satuan != null && tanggal != null) {
            String result = "Panen Terakhir:\n" +
                    "- Jenis: " + jenis + "\n" +
                    "- Jumlah: " + jumlah + " " + satuan + "\n" +
                    "- Tanggal: " + tanggal;
            latestDataText.setText(result);
        } else {
            latestDataText.setText("Belum ada data panen.");
        }

        // Listener untuk search bar
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            String keyword = searchBar.getText().toString().trim();
            if (!keyword.isEmpty()) {
                Toast.makeText(this, "Cari: " + keyword, Toast.LENGTH_SHORT).show();
            }
            return true;
        });

        // Listener BottomNavigation
        navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_dashboard) {
                Toast.makeText(this, "Dashboard aktif", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.navigation_statistic) {
                Intent intentStatistic = new Intent(Dashboard.this, Statistic.class);
                startActivity(intentStatistic);
                return true;
            } else if (itemId == R.id.navigation_form) {
                Intent intentForm = new Intent(Dashboard.this, Form.class);
                startActivity(intentForm);
                return true;
            }
            return false;
        });

        navView.setSelectedItemId(R.id.navigation_dashboard);
    }
}
