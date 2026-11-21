package org.otocare.dao;

import org.otocare.model.JadwalPengingat;
import org.otocare.model.StatusPengingat;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PengingatDAO {

    /** Semua jadwal milik pengguna, diurutkan dari yang paling mendesak (Jatuh Tempo dulu). */
    public List<JadwalPengingat> semua(int idUser) throws SQLException {
        String sql = "SELECT j.id, k.id AS id_kendaraan, k.plat_nomor, k.merk, k.odometer, "
                + "       ks.id AS id_kategori, ks.nama AS kategori, j.target_odometer, j.target_tanggal "
                + "FROM jadwal_pengingat j "
                + "JOIN kendaraan k ON k.id = j.id_kendaraan "
                + "JOIN kategori_servis ks ON ks.id = j.id_kategori "
                + "WHERE k.id_user = ?";
        List<JadwalPengingat> hasil = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer targetKm = rs.getObject("target_odometer", Integer.class);
                    Date tgl = rs.getDate("target_tanggal");
                    hasil.add(new JadwalPengingat(
                            rs.getInt("id"), rs.getInt("id_kendaraan"), rs.getInt("id_kategori"), rs.getString("plat_nomor"),
                            rs.getString("merk"), rs.getString("kategori"), rs.getInt("odometer"),
                            targetKm, tgl == null ? null : tgl.toLocalDate()));
                }
            }
        }
        // POLYMORPHISM lewat interface: cekStatus() dipanggil pada objek yang sama, hasil tergantung datanya
        hasil.sort(Comparator
                .comparingInt((JadwalPengingat j) -> -j.cekStatus().ordinal())
                .thenComparing(JadwalPengingat::getPlatNomor));
        return hasil;
    }

    /** Hanya jadwal berstatus Segera Servis atau Jatuh Tempo (untuk pop-up & notifikasi). */
    public List<JadwalPengingat> perluPerhatian(int idUser) throws SQLException {
        List<JadwalPengingat> hasil = new ArrayList<>();
        for (JadwalPengingat j : semua(idUser)) {
            if (j.cekStatus() != StatusPengingat.AMAN) {
                hasil.add(j);
            }
        }
        return hasil;
    }

    public void simpan(int idKendaraan, int idKategori, Integer targetKm, LocalDate targetTanggal) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            upsert(conn, idKendaraan, idKategori, targetKm, targetTanggal);
        }
    }

    public int hapus(int idUser, int idJadwal) throws SQLException {
        String sql = "DELETE j FROM jadwal_pengingat j JOIN kendaraan k ON k.id = j.id_kendaraan "
                + "WHERE j.id = ? AND k.id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idJadwal);
            ps.setInt(2, idUser);
            return ps.executeUpdate();
        }
    }

    /** Insert, atau update jika kendaraan + kategori tersebut sudah punya jadwal. */
    static void upsert(Connection conn, int idKendaraan, int idKategori,
                       Integer targetKm, LocalDate targetTanggal) throws SQLException {
        String sql = "INSERT INTO jadwal_pengingat (id_kendaraan, id_kategori, target_odometer, target_tanggal) "
                + "VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE target_odometer = VALUES(target_odometer), "
                + "target_tanggal = VALUES(target_tanggal)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idKendaraan);
            ps.setInt(2, idKategori);
            if (targetKm == null) {
                ps.setNull(3, Types.INTEGER);
            } else {
                ps.setInt(3, targetKm);
            }
            if (targetTanggal == null) {
                ps.setNull(4, Types.DATE);
            } else {
                ps.setDate(4, Date.valueOf(targetTanggal));
            }
            ps.executeUpdate();
        }
    }
}
