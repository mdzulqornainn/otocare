package org.otocare.model;

import org.otocare.util.Format;

/** Ringkasan pengeluaran servis untuk satu bulan. */
public class LaporanBulanan {
    private final String bulan;        // format yyyy-MM
    private final int jumlahServis;
    private final double totalBiaya;

    public LaporanBulanan(String bulan, int jumlahServis, double totalBiaya) {
        this.bulan = bulan;
        this.jumlahServis = jumlahServis;
        this.totalBiaya = totalBiaya;
    }

    public String getBulan() { return bulan; }
    public int getJumlahServis() { return jumlahServis; }
    public double getTotalBiaya() { return totalBiaya; }

    @Override
    public String toString() { return Format.bulan(bulan); }   // teks di ComboBox
}
