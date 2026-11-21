package org.otocare.model;

import java.io.Serializable;


public class Kendaraan implements Serializable {
    private static final long serialVersionUID = 2L;
    private static final String POLA_PLAT = "[A-Z]{1,2}\\s?\\d{1,4}\\s?[A-Z]{0,3}";

    private int id;
    private String platNomor;
    private String merk;
    private String pemilik;
    private int odometer;
    private final Stnk stnk;

    // Constructor 1 (overloading): kendaraan baru, odometer 0
    public Kendaraan(String platNomor, String merk, String pemilik) {
        this(0, platNomor, merk, pemilik, 0);
    }

    // Constructor 2 (overloading): data lengkap, dipakai saat membaca dari database
    public Kendaraan(int id, String platNomor, String merk, String pemilik, int odometer) {
        this.stnk = new Stnk();      // KOMPOSISI: dibuat di dalam constructor, bukan dari luar
        this.id = id;
        setPlatNomor(platNomor);
        setMerk(merk);
        setPemilik(pemilik);
        if (odometer < 0) {
            throw new IllegalArgumentException("Odometer tidak boleh negatif!");
        }
        this.odometer = odometer;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPlatNomor() { return platNomor; }
    public void setPlatNomor(String platNomor) {
        String plat = platNomor == null ? "" : platNomor.trim().toUpperCase();
        if (!plat.matches(POLA_PLAT)) {
            throw new IllegalArgumentException("Format plat nomor tidak valid!\nContoh: BE 1234 AB");
        }
        this.platNomor = plat;
        this.stnk.setNomorRegistrasi(plat);
    }

    public String getMerk() { return merk; }
    public void setMerk(String merk) {
        if (merk == null || merk.isBlank()) {
            throw new IllegalArgumentException("Merk tidak boleh kosong!");
        }
        this.merk = merk.trim();
    }

    public String getPemilik() { return pemilik; }
    public void setPemilik(String pemilik) {
        if (pemilik == null || pemilik.isBlank()) {
            throw new IllegalArgumentException("Pemilik tidak boleh kosong!");
        }
        this.pemilik = pemilik.trim();
    }

    public int getOdometer() { return odometer; }

    /** Odometer hanya boleh naik: menolak nilai yang lebih kecil dari odometer sebelumnya. */
    public void setOdometer(int odometerBaru) {
        if (odometerBaru < 0) {
            throw new IllegalArgumentException("Odometer tidak boleh negatif!");
        }
        if (odometerBaru < this.odometer) {
            throw new IllegalArgumentException("Odometer baru (" + odometerBaru
                    + " km) tidak boleh lebih kecil dari odometer sebelumnya (" + this.odometer + " km)!");
        }
        this.odometer = odometerBaru;
    }

    public String getNomorStnk() { return stnk.getNomorRegistrasi(); }

    // Di-override oleh Mobil dan Motor (Inheritance + Polymorphism)
    public String getTipe() { return "Kendaraan"; }
    public String getDetailKhusus() { return "-"; }
    public String getNilaiKhusus() { return ""; }

    public String getInfo() {
        return "Pemilik: " + pemilik + " | Plat: " + platNomor + " | Merk: " + merk + " | Odometer: " + odometer + " km";
    }

    @Override
    public String toString() { return platNomor + " - " + merk; }   // teks di ComboBox
}

// Class pendukung dalam file yang sama (pola yang sama dengan class KTM di modul Pertemuan 11)
class Stnk implements Serializable {
    private static final long serialVersionUID = 1L;
    private String nomorRegistrasi = "";

    String getNomorRegistrasi() { return nomorRegistrasi; }

    void setNomorRegistrasi(String platNomor) {
        this.nomorRegistrasi = "STNK-" + platNomor.replace(" ", "");
    }
}
