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

    private EditText jenisTanamanEditText, jumlahPanenEditText, satuanEditText, tanggalPanenEditText;
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
        tanggalPanenEditText = findViewById(R.id.tanggalPanenEditText);
        submitButton = findViewById(R.id.submitButton);
        backButton = findViewById(R.id.backButton);

        // Init Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
 

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

 

    private void submitForm() {
        String jenis = jenisTanamanEditText.getText().toString();
        String jumlah = jumlahPanenEditText.getText().toString();
        String satuan = satuanEditText.getText().toString();
        String tanggal = tanggalPanenEditText.getText().toString();

        if (jenis.isEmpty() || jumlah.isEmpty() || satuan.isEmpty() || tanggal.isEmpty()) {
            Toast.makeText(this, "Harap lengkapi semua data", Toast.LENGTH_SHORT).show();
        } else {
            submitButton.setEnabled(false);

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Anda harus login terlebih dahulu", Toast.LENGTH_SHORT).show();
                submitButton.setEnabled(true);
                return;
            }

            // No photo upload: save document directly
            saveDocument(jenis, jumlah, satuan, tanggal, currentUser.getUid());
        }
    }
 
    private void saveDocument(String jenis, String jumlah, String satuan, String tanggal, String uid) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("jenis", jenis);
        doc.put("jumlah", jumlah);
        doc.put("satuan", satuan);
        doc.put("tanggal", tanggal);
        doc.put("userId", uid);
        doc.put("createdAt", Timestamp.now());
        doc.put("status", "waiting"); // Initial status: waiting for admin approval

        // Debug: print data yang akan disimpan
        System.out.println("Saving document with data:");
        System.out.println("jenis: " + jenis);
        System.out.println("jumlah: " + jumlah);
        System.out.println("satuan: " + satuan);
        System.out.println("tanggal: " + tanggal);
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