package com.example.e_tani;

public class DataModel {
    private String jenisTanaman;
    private String jumlah;
    private String satuan;
    private String tanggal;
    private String fotoBase64;
    private String status;

    public DataModel() {
        // kosong buat Firestore
    }

    public DataModel(String jenisTanaman, String jumlah, String satuan, String tanggal, String status) {
        this.jenisTanaman = jenisTanaman;
        this.jumlah = jumlah;
        this.satuan = satuan;
        this.tanggal = tanggal;
        this.status = status;
        this.fotoBase64 = null; // biar default kosong
    }

    // Getter & Setter
    public String getJenisTanaman() { return jenisTanaman; }
    public String getJumlah() { return jumlah; }
    public String getSatuan() { return satuan; }
    public String getTanggal() { return tanggal; }
    public String getFotoBase64() { return fotoBase64; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }
}
