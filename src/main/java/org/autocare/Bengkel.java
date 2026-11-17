package org.autocare;

import java.util.ArrayList;

/**
 * AGREGASI (Pertemuan 11): Bengkel menyimpan kumpulan objek Kendaraan.
 * Objek Kendaraan dibuat DI LUAR class ini (di Main), lalu dimasukkan lewat tambahKendaraan().
 * Jika Bengkel dihapus, objek Kendaraan tetap bisa hidup sendiri.
 */
public class Bengkel {
    private ArrayList<Kendaraan> daftarKendaraan = new ArrayList<>();

    public void tambahKendaraan(Kendaraan k) {
        daftarKendaraan.add(k);
    }

    public boolean hapusByPlat(String plat) {
        return daftarKendaraan.removeIf(k -> k.getPlatNomor().equalsIgnoreCase(plat));
    }

    public void kosongkan() {
        daftarKendaraan.clear();
    }

    public ArrayList<Kendaraan> getDaftarKendaraan() {
        return daftarKendaraan;
    }
}
