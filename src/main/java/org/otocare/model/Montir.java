package org.otocare.model;

public class Montir {
    private String nama;

    public Montir(String nama) {
        this.nama = nama;
    }

    public String getNama() { return nama; }

    public String servis(Kendaraan kendaraan) {
        return "Montir " + nama + " sedang menservis:\n\n" + kendaraan.getInfo()
                + "\nNo. STNK: " + kendaraan.getNomorStnk();
    }
}
