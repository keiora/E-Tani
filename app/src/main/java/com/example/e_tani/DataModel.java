package com.example.e_tani;

public class DataModel {
    private String jenisTanaman;
    private String jumlah;
    private String satuan;
    private String tanggal;
    private String fotoBase64;

    public DataModel(String jenisTanaman, String jumlah, String satuan, String tanggal) {
        this.jenisTanaman = jenisTanaman;
        this.jumlah = jumlah;
        this.satuan = satuan;
        this.tanggal = tanggal;
        this.fotoBase64 = "";
    }

    public DataModel(String jenisTanaman, String jumlah, String satuan, String tanggal, String fotoBase64) {
        this.jenisTanaman = jenisTanaman;
        this.jumlah = jumlah;
        this.satuan = satuan;
        this.tanggal = tanggal;
        this.fotoBase64 = fotoBase64;
    }

    public String getJenisTanaman() {
        return jenisTanaman;
    }

    public void setJenisTanaman(String jenisTanaman) {
        this.jenisTanaman = jenisTanaman;
    }

    public String getJumlah() {
        return jumlah;
    }

    public void setJumlah(String jumlah) {
        this.jumlah = jumlah;
    }

    public String getSatuan() {
        return satuan;
    }

    public void setSatuan(String satuan) {
        this.satuan = satuan;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getFotoBase64() {
        return fotoBase64;
    }

    public void setFotoBase64(String fotoBase64) {
        this.fotoBase64 = fotoBase64;
    }
}
