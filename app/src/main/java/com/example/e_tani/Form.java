package com.example.e_tani;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class Form extends AppCompatActivity {

    private EditText jenisTanamanEditText, jumlahPanenEditText, satuanEditText, tanggalPanenEditText;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.form);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inisialisasi komponen
        jenisTanamanEditText = findViewById(R.id.jenisTanamanEditText);
        jumlahPanenEditText = findViewById(R.id.jumlahPanenEditText);
        satuanEditText = findViewById(R.id.satuanEditText);
        tanggalPanenEditText = findViewById(R.id.tanggalPanenEditText);
        submitButton = findViewById(R.id.submitButton);

        // Tampilkan DatePicker saat klik tanggal
        tanggalPanenEditText.setOnClickListener(v -> showDatePicker());

        // Submit button handler
        submitButton.setOnClickListener(v -> submitForm());
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    tanggalPanenEditText.setText(selectedDate);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void submitForm() {
        String jenis = jenisTanamanEditText.getText().toString();
        String jumlah = jumlahPanenEditText.getText().toString();
        String satuan = satuanEditText.getText().toString();
        String tanggal = tanggalPanenEditText.getText().toString();

        if (jenis.isEmpty() || jumlah.isEmpty() || satuan.isEmpty() || tanggal.isEmpty()) {
            Toast.makeText(this, "Harap lengkapi semua data", Toast.LENGTH_SHORT).show();
        } else {
            // Simpan ke SharedPreferences
            getSharedPreferences("PanenPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("jenis", jenis)
                    .putString("jumlah", jumlah)
                    .putString("satuan", satuan)
                    .putString("tanggal", tanggal)
                    .apply();

            Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show();

            // Kembali ke Dashboard
            finish(); // atau pakai Intent jika ingin lebih eksplisit
        }
    }

}
