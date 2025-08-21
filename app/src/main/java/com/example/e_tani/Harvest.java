package com.example.e_tani;

public class Harvest {
    private String id;
    private String jenis;
    private String jumlah;
    private String satuan;
    private String tanggal;
    private String status;
    private String userId;

    // tambahan untuk display
    private String namaPetani;
    private String userEmail;
    private String kualitas;
    private String lokasiLahan;
    private String luasLahan;
    private String musim;
    private String hargaJual;
    private String catatan;
    private String createdAt; // formatted display

    public Harvest() {}

    public Harvest(String id, String jenis, String jumlah, String satuan, String tanggal, String status, String userId) {
        this.id = id;
        this.jenis = jenis;
        this.jumlah = jumlah;
        this.satuan = satuan;
        this.tanggal = tanggal;
        this.status = status;
        this.userId = userId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getJenis() { return jenis; }
    public void setJenis(String jenis) { this.jenis = jenis; }

    public String getJumlah() { return jumlah; }
    public void setJumlah(String jumlah) { this.jumlah = jumlah; }

    public String getSatuan() { return satuan; }
    public void setSatuan(String satuan) { this.satuan = satuan; }

    public String getTanggal() { return tanggal; }
    public void setTanggal(String tanggal) { this.tanggal = tanggal; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNamaPetani() { return namaPetani; }
    public void setNamaPetani(String namaPetani) { this.namaPetani = namaPetani; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getKualitas() { return kualitas; }
    public void setKualitas(String kualitas) { this.kualitas = kualitas; }

    public String getLokasiLahan() { return lokasiLahan; }
    public void setLokasiLahan(String lokasiLahan) { this.lokasiLahan = lokasiLahan; }

    public String getLuasLahan() { return luasLahan; }
    public void setLuasLahan(String luasLahan) { this.luasLahan = luasLahan; }

    public String getMusim() { return musim; }
    public void setMusim(String musim) { this.musim = musim; }

    public String getHargaJual() { return hargaJual; }
    public void setHargaJual(String hargaJual) { this.hargaJual = hargaJual; }

    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
