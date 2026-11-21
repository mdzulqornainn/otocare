package org.otocare.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Jadwal pengingat servis untuk satu kendaraan dan satu kategori servis.
 * Status dihitung otomatis dari odometer saat ini dan target (KM / tanggal):
 *  - JATUH_TEMPO   : melewati target KM atau melewati tanggal
 *  - SEGERA_SERVIS : sisa kurang dari 500 km atau H-7 (7 hari atau kurang)
 *  - AMAN          : selain itu
 */
public class JadwalPengingat implements Remindable {
    public static final int BATAS_KM_SEGERA = 500;
    public static final int BATAS_HARI_SEGERA = 7;

    private int idJadwal;
    private int idKendaraan;
    private int idKategori;
    private String platNomor;
    private String namaKendaraan;
    private String namaKategori;
    private int odometerSaatIni;
    private Integer targetOdometer;   // null = tidak ada target KM
    private LocalDate targetTanggal;  // null = tidak ada target tanggal

    public JadwalPengingat(int idJadwal, int idKendaraan, int idKategori, String platNomor, String namaKendaraan,
                           String namaKategori, int odometerSaatIni,
                           Integer targetOdometer, LocalDate targetTanggal) {
        if (targetOdometer == null && targetTanggal == null) {
            throw new IllegalArgumentException("Pengingat harus punya target KM atau target tanggal!");
        }
        this.idJadwal = idJadwal;
        this.idKendaraan = idKendaraan;
        this.idKategori = idKategori;
        this.platNomor = platNomor;
        this.namaKendaraan = namaKendaraan;
        this.namaKategori = namaKategori;
        this.odometerSaatIni = odometerSaatIni;
        this.targetOdometer = targetOdometer;
        this.targetTanggal = targetTanggal;
    }

    public int getIdJadwal() { return idJadwal; }
    public int getIdKendaraan() { return idKendaraan; }
    public int getIdKategori() { return idKategori; }
    public String getPlatNomor() { return platNomor; }
    public String getNamaKendaraan() { return namaKendaraan; }
    public String getNamaKategori() { return namaKategori; }
    public int getOdometerSaatIni() { return odometerSaatIni; }
    public Integer getTargetOdometer() { return targetOdometer; }
    public LocalDate getTargetTanggal() { return targetTanggal; }

    /** Sisa km menuju target (negatif = sudah lewat), null jika tidak ada target KM. */
    public Integer getSisaKm() {
        return targetOdometer == null ? null : targetOdometer - odometerSaatIni;
    }

    /** Sisa hari menuju target (negatif = sudah lewat), null jika tidak ada target tanggal. */
    public Long getSisaHari() {
        return targetTanggal == null ? null : ChronoUnit.DAYS.between(LocalDate.now(), targetTanggal);
    }

    @Override
    public StatusPengingat cekStatus() {
        Integer sisaKm = getSisaKm();
        Long sisaHari = getSisaHari();

        if ((sisaKm != null && sisaKm < 0) || (sisaHari != null && sisaHari < 0)) {
            return StatusPengingat.JATUH_TEMPO;
        }
        if ((sisaKm != null && sisaKm < BATAS_KM_SEGERA) || (sisaHari != null && sisaHari <= BATAS_HARI_SEGERA)) {
            return StatusPengingat.SEGERA_SERVIS;
        }
        return StatusPengingat.AMAN;
    }

    @Override
    public String getPesanPengingat() {
        List<String> bagian = new ArrayList<>();
        Integer sisaKm = getSisaKm();
        Long sisaHari = getSisaHari();

        if (sisaKm != null) {
            bagian.add(sisaKm < 0 ? "lewat " + (-sisaKm) + " km" : "sisa " + sisaKm + " km");
        }
        if (sisaHari != null) {
            if (sisaHari < 0) {
                bagian.add("lewat " + (-sisaHari) + " hari");
            } else if (sisaHari == 0) {
                bagian.add("jatuh tempo hari ini");
            } else {
                bagian.add("sisa " + sisaHari + " hari");
            }
        }
        return namaKategori + ": " + String.join(", ", bagian);
    }
}
