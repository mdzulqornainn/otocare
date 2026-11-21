package org.otocare.model;

import java.time.LocalDate;

/** Catatan servis yang sudah dilakukan. Semua atribut private; setter memvalidasi logika bisnis. */
public class RiwayatServis {
    private int id;
    private int idKendaraan;
    private String platNomor;
    private String namaKendaraan;
    private int idKategori;
    private String namaKategori;
    private LocalDate tanggal;
    private int odometer;
    private double biaya;
    private String namaBengkel;
    private String catatan;

    public RiwayatServis(int id, int idKendaraan, String platNomor, String namaKendaraan, int idKategori, String namaKategori,
                         LocalDate tanggal, int odometer, double biaya, String namaBengkel, String catatan) {
        this.id = id;
        this.idKendaraan = idKendaraan;
        this.platNomor = platNomor;
        this.namaKendaraan = namaKendaraan;
        this.idKategori = idKategori;
        this.namaKategori = namaKategori;
        setTanggal(tanggal);
        setOdometer(odometer);
        setBiaya(biaya);
        this.namaBengkel = namaBengkel;
        this.catatan = catatan;
    }

    public int getId() { return id; }
    public int getIdKendaraan() { return idKendaraan; }
    public String getPlatNomor() { return platNomor; }
    public String getNamaKendaraan() { return namaKendaraan; }
    public int getIdKategori() { return idKategori; }
    public String getNamaKategori() { return namaKategori; }
    public LocalDate getTanggal() { return tanggal; }
    public int getOdometer() { return odometer; }
    public double getBiaya() { return biaya; }
    public String getNamaBengkel() { return namaBengkel; }
    public String getCatatan() { return catatan; }

    public void setTanggal(LocalDate tanggal) {
        if (tanggal == null) {
            throw new IllegalArgumentException("Tanggal servis wajib diisi!");
        }
        if (tanggal.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Tanggal servis tidak boleh di masa depan!");
        }
        this.tanggal = tanggal;
    }

    public void setOdometer(int odometer) {
        if (odometer < 0) {
            throw new IllegalArgumentException("Odometer tidak boleh negatif!");
        }
        this.odometer = odometer;
    }

    public void setBiaya(double biaya) {
        if (biaya < 0) {
            throw new IllegalArgumentException("Biaya tidak boleh negatif!");
        }
        this.biaya = biaya;
    }

    public void setNamaBengkel(String namaBengkel) { this.namaBengkel = namaBengkel; }
    public void setCatatan(String catatan) { this.catatan = catatan; }
}
