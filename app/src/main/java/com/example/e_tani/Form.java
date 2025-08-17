package com.example.e_tani;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
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
import java.util.HashMap;
import java.util.Map;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
 

public class Form extends AppCompatActivity {

    private EditText jenisTanamanEditText, jumlahPanenEditText, satuanEditText, musimEditText, kualitasEditText, luasLahanEditText, hargaJualEditText, lokasiLahanEditText, catatanEditText, tanggalPanenEditText;
    private Button submitButton;
    private ImageView backButton;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form);

        // Inisialisasi komponen
        jenisTanamanEditText = findViewById(R.id.jenisTanamanEditText);
        jumlahPanenEditText = findViewById(R.id.jumlahPanenEditText);
        satuanEditText = findViewById(R.id.satuanEditText);
        musimEditText = findViewById(R.id.musimEditText);
        kualitasEditText = findViewById(R.id.kualitasEditText);
        luasLahanEditText = findViewById(R.id.luasLahanEditText);
        hargaJualEditText = findViewById(R.id.hargaJualEditText);
        lokasiLahanEditText = findViewById(R.id.lokasiLahanEditText);
        catatanEditText = findViewById(R.id.catatanEditText);
        tanggalPanenEditText = findViewById(R.id.tanggalPanenEditText);
        submitButton = findViewById(R.id.submitButton);
        backButton = findViewById(R.id.backButton);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        
        // Check apakah ini mode edit
        checkEditMode();
 

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

 

        // Submit button handler
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    updateForm();
                } else {
                    submitForm();
                }
            }
        });
    }
    
    // Variables untuk mode edit
    private boolean isEditMode = false;
    private String editHarvestId = "";
    

    
    // Method untuk check apakah ini mode edit
    private void checkEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("edit_mode", false)) {
            isEditMode = true;
            editHarvestId = intent.getStringExtra("harvest_id");
            
            // Update title dan button text
            submitButton.setText("Update Data Panen");
            
            // Load data yang akan diedit
            loadEditData(intent);
        }
    }
    
    // Method untuk load data yang akan diedit
    private void loadEditData(Intent intent) {
        // Set data ke form fields
        String jenisTanaman = intent.getStringExtra("jenis_tanaman");
        String jumlahPanen = intent.getStringExtra("jumlah_panen");
        String satuan = intent.getStringExtra("satuan");
        String musim = intent.getStringExtra("musim");
        String kualitas = intent.getStringExtra("kualitas");
        String tanggalPanen = intent.getStringExtra("tanggal_panen");
        
        if (jenisTanaman != null) jenisTanamanEditText.setText(jenisTanaman);
        if (jumlahPanen != null) jumlahPanenEditText.setText(jumlahPanen);
        if (tanggalPanen != null) {
            tanggalPanenEditText.setText(tanggalPanen);
        }
        
        // Set EditText values
        if (satuan != null) satuanEditText.setText(satuan);
        if (musim != null) musimEditText.setText(musim);
        if (kualitas != null) kualitasEditText.setText(kualitas);
    }
    

    
    // Method untuk update data yang sudah ada
    private void updateForm() {
        String jenis = jenisTanamanEditText.getText().toString().trim();
        String jumlah = jumlahPanenEditText.getText().toString().trim();
        String satuan = satuanEditText.getText().toString();
        String musim = musimEditText.getText().toString();
        String kualitas = kualitasEditText.getText().toString();
        String tanggal = tanggalPanenEditText.getText().toString().trim();

        // Validasi input
        if (jenis.isEmpty() || jumlah.isEmpty() || tanggal.isEmpty()) {
            Toast.makeText(this, "Harap lengkapi data wajib", Toast.LENGTH_LONG).show();
            return;
        }

        submitButton.setEnabled(false);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Anda harus login terlebih dahulu", Toast.LENGTH_SHORT).show();
            submitButton.setEnabled(true);
            return;
        }

        // Update document yang sudah ada
        updateDocument(jenis, jumlah, satuan, musim, kualitas, tanggal, currentUser.getUid());
    }
    
    // Method untuk update document di Firestore
    private void updateDocument(String jenis, String jumlah, String satuan, String musim, 
                              String kualitas, String tanggal, String uid) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("jenisTanaman", jenis);
        updates.put("jumlahPanen", jumlah);
        updates.put("satuan", satuan);
        updates.put("musim", musim);
        updates.put("kualitas", kualitas);
        updates.put("tanggalPanen", tanggal);
        updates.put("updatedAt", Timestamp.now());

        // Debug: print data yang akan diupdate
        System.out.println("Updating document with ID: " + editHarvestId);
        System.out.println("jenisTanaman: " + jenis);
        System.out.println("jumlahPanen: " + jumlah);
        System.out.println("satuan: " + satuan);
        System.out.println("musim: " + musim);
        System.out.println("kualitas: " + kualitas);
        System.out.println("tanggalPanen: " + tanggal);

        db.collection("harvests").document(editHarvestId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    System.out.println("Document updated successfully");
                    Toast.makeText(Form.this, "Data panen berhasil diupdate!", Toast.LENGTH_LONG).show();
                    
                    // Kembali ke Dashboard
                    Intent intent = new Intent(Form.this, Dashboard.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    System.out.println("Failed to update document: " + e.getMessage());
                    Toast.makeText(Form.this, "Gagal mengupdate data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    submitButton.setEnabled(true);
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

 

    private void submitForm() {
        String jenis = jenisTanamanEditText.getText().toString();
        String jumlah = jumlahPanenEditText.getText().toString();
        String satuan = satuanEditText.getText().toString();
        String musim = musimEditText.getText().toString();
        String kualitas = kualitasEditText.getText().toString();
        String luasLahan = luasLahanEditText.getText().toString();
        String hargaJual = hargaJualEditText.getText().toString();
        String lokasiLahan = lokasiLahanEditText.getText().toString();
        String catatan = catatanEditText.getText().toString();
        String tanggal = tanggalPanenEditText.getText().toString();

        if (jenis.isEmpty() || jumlah.isEmpty() || satuan.isEmpty() || musim.isEmpty() || kualitas.isEmpty() || luasLahan.isEmpty() || tanggal.isEmpty()) {
            Toast.makeText(this, "Harap lengkapi data wajib", Toast.LENGTH_SHORT).show();
        } else {
            submitButton.setEnabled(false);

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Anda harus login terlebih dahulu", Toast.LENGTH_SHORT).show();
                submitButton.setEnabled(true);
                return;
            }

            // Save document dengan data lengkap
            saveDocument(jenis, jumlah, satuan, musim, kualitas, luasLahan, hargaJual, lokasiLahan, catatan, tanggal, currentUser.getUid());
        }
    }
 
    private void saveDocument(String jenis, String jumlah, String satuan, String musim, String kualitas, String luasLahan, String hargaJual, String lokasiLahan, String catatan, String tanggal, String uid) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("jenisTanaman", jenis);
        doc.put("jumlahPanen", jumlah);
        doc.put("satuan", satuan);
        doc.put("musim", musim);
        doc.put("kualitas", kualitas);
        doc.put("luasLahan", luasLahan);
        doc.put("hargaJual", hargaJual.isEmpty() ? "0" : hargaJual);
        doc.put("lokasiLahan", lokasiLahan.isEmpty() ? "-" : lokasiLahan);
        doc.put("catatan", catatan.isEmpty() ? "-" : catatan);
        doc.put("tanggalPanen", tanggal);
        doc.put("userId", uid);
        doc.put("userEmail", mAuth.getCurrentUser().getEmail());
        doc.put("createdAt", Timestamp.now());
        doc.put("status", "waiting"); // Initial status: waiting for admin approval

        // Debug: print data yang akan disimpan
        System.out.println("Saving document with data:");
        System.out.println("jenisTanaman: " + jenis);
        System.out.println("jumlahPanen: " + jumlah);
        System.out.println("satuan: " + satuan);
        System.out.println("musim: " + musim);
        System.out.println("kualitas: " + kualitas);
        System.out.println("luasLahan: " + luasLahan);
        System.out.println("hargaJual: " + hargaJual);
        System.out.println("lokasiLahan: " + lokasiLahan);
        System.out.println("catatan: " + catatan);
        System.out.println("tanggalPanen: " + tanggal);
        System.out.println("userId: " + uid);

        db.collection("harvests").add(doc)
                .addOnSuccessListener(documentReference -> {
                    System.out.println("Document saved successfully with ID: " + documentReference.getId());
                    Toast.makeText(Form.this, "Data berhasil disimpan", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Form.this, Dashboard.class);
                    intent.putExtra("nama_tanaman", jenis);
                    intent.putExtra("jumlah", jumlah);
                    // Tambahkan flag untuk memaksa refresh
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    System.out.println("Failed to save document: " + e.getMessage());
                    Toast.makeText(Form.this, "Gagal menyimpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    submitButton.setEnabled(true);
                });
    }
}