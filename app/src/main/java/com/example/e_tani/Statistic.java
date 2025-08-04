package com.example.e_tani;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.e_tani.databinding.StatisticBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import android.content.Intent;

public class Statistic extends AppCompatActivity {

    private StatisticBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = StatisticBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = binding.navView;

        // Optional: Set default selected item
        navView.setSelectedItemId(R.id.navigation_dashboard);

        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.navigation_dashboard) {
                    // Sudah di Statistik (Dashboard), tidak perlu pindah
                    return true;
                } else if (id == R.id.navigation_form) {
                    startActivity(new Intent(Statistic.this, Form.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
    }
}
