package com.example.e_tani;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

public class Form extends AppCompatActivity {

    private EditText jenisTanamanEditText, jumlahPanenEditText, satuanEditText, tanggalPanenEditText;
    private Button submitButton, uploadButton;
    private ImageView fotoPreview, backButton;
    private TextView fotoText;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Bitmap selectedImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form);

        // Inisialisasi komponen
        jenisTanamanEditText = findViewById(R.id.jenisTanamanEditText);
        jumlahPanenEditText = findViewById(R.id.jumlahPanenEditText);
        satuanEditText = findViewById(R.id.satuanEditText);
        tanggalPanenEditText = findViewById(R.id.tanggalPanenEditText);
        submitButton = findViewById(R.id.submitButton);
        uploadButton = findViewById(R.id.uploadButton);
        fotoPreview = findViewById(R.id.fotoPreview);
        fotoText = findViewById(R.id.fotoText);
        backButton = findViewById(R.id.backButton);

        // Back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Tampilkan DatePicker saat klik tanggal
        tanggalPanenEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Upload button
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        // Submit button handler
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                        String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        tanggalPanenEditText.setText(selectedDate);
                    }
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                fotoPreview.setImageBitmap(selectedImageBitmap);
                fotoText.setText("Foto berhasil dipilih");
                fotoText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } catch (Exception e) {
                Toast.makeText(this, "Gagal memuat gambar", Toast.LENGTH_SHORT).show();
            }
        }
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

            // Simpan gambar jika ada
            if (selectedImageBitmap != null) {
                saveImageToSharedPreferences(selectedImageBitmap);
            }

            Toast.makeText(this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show();

            // Kembali ke Dashboard dengan data baru
            Intent intent = new Intent(Form.this, Dashboard.class);
            intent.putExtra("nama_tanaman", jenis);
            intent.putExtra("jumlah", jumlah);
            startActivity(intent);
            finish();
        }
    }

    private void saveImageToSharedPreferences(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] imageBytes = baos.toByteArray();
        
        getSharedPreferences("PanenPrefs", MODE_PRIVATE)
                .edit()
                .putString("foto", android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT))
                .apply();
    }
}