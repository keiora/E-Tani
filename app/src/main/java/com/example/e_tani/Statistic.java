package com.example.e_tani;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.ArrayList;
public class Statistic extends AppCompatActivity {

    Spinner spinner;
    BarChart barChart;
    TextView textTotalPanen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.statistic);

        spinner = findViewById(R.id.spinnerTanaman);
        barChart = findViewById(R.id.barChart);
        textTotalPanen = findViewById(R.id.textTotalPanen);

        // Dummy tanaman
        String[] tanamanList = {"Pilih Tanaman", "Jagung", "Padi"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tanamanList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Event saat spinner dipilih
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (!selected.equals("Pilih Tanaman")) {
                    tampilkanChartDummy(selected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void tampilkanChartDummy(String tanaman) {
        // Dummy data
        ArrayList<BarEntry> entries = new ArrayList<>();
        float[] dataPanen;
        if (tanaman.equals("Jagung")) {
            dataPanen = new float[]{120, 150, 180, 200, 160, 190};
        } else { // Padi
            dataPanen = new float[]{100, 140, 130, 170, 180, 210};
        }

        float total = 0;
        for (int i = 0; i < dataPanen.length; i++) {
            entries.add(new BarEntry(i, dataPanen[i]));
            total += dataPanen[i];
        }

        BarDataSet dataSet = new BarDataSet(entries, "Panen per Bulan");
        dataSet.setColor(Color.parseColor("#4CAF50"));
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        // Label X (bulan)
        String[] bulan = {"Mei", "Jun", "Jul", "Agu", "Sep", "Okt"};
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(bulan));
        barChart.getXAxis().setGranularity(1f);
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        // Animasi dan refresh
        barChart.animateY(1000);
        barChart.invalidate();

        // Update total panen
        textTotalPanen.setText("Total Panen: " + total + " kg");
    }
}