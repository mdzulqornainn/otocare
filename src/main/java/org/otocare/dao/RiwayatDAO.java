package org.otocare.dao;

import org.otocare.model.KategoriServis;
import org.otocare.model.LaporanBulanan;
import org.otocare.model.RiwayatServis;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RiwayatDAO {

    public List<KategoriServis> daftarKategori() throws SQLException {
        List<KategoriServis> hasil = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, nama FROM kategori_servis ORDER BY nama");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hasil.add(new KategoriServis(rs.getInt("id"), rs.getString("nama")));
            }
        }
        return hasil;
    }

    /**
     * Menyimpan riwayat servis, menaikkan odometer kendaraan (jika lebih tinggi), dan
     * (opsional) mengatur jadwal pengingat berikutnya - semuanya dalam SATU transaksi.
     */
    public void simpan(RiwayatServis r, Integer targetKm, LocalDate targetTanggal) throws SQLException {
        String sqlRiwayat = "INSERT INTO riwayat_servis "
                + "(id_kendaraan, id_kategori, tanggal, odometer, biaya, nama_bengkel, catatan) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlOdometer = "UPDATE kendaraan SET odometer = GREATEST(odometer, ?) WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sqlRiwayat, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, r.getIdKendaraan());
                    ps.setInt(2, r.getIdKategori());
                    ps.setDate(3, Date.valueOf(r.getTanggal()));
                    ps.setInt(4, r.getOdometer());
                    ps.setDouble(5, r.getBiaya());
                    isiTeks(ps, 6, r.getNamaBengkel());
                    isiTeks(ps, 7, r.getCatatan());
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(sqlOdometer)) {
                    ps.setInt(1, r.getOdometer());
                    ps.setInt(2, r.getIdKendaraan());
                    ps.executeUpdate();
                }
                if (targetKm != null || targetTanggal != null) {
                    PengingatDAO.upsert(conn, r.getIdKendaraan(), r.getIdKategori(), targetKm, targetTanggal);
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            }
        }
    }

    private void isiTeks(PreparedStatement ps, int index, String nilai) throws SQLException {
        if (nilai == null || nilai.isBlank()) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, nilai.trim());
        }
    }

    public List<RiwayatServis> daftar(int idUser) throws SQLException {
        String sql = "SELECT r.id, r.id_kendaraan, k.plat_nomor, k.merk, r.id_kategori, ks.nama AS kategori, "
                + "       r.tanggal, r.odometer, r.biaya, r.nama_bengkel, r.catatan "
                + "FROM riwayat_servis r "
                + "JOIN kendaraan k ON k.id = r.id_kendaraan "
                + "JOIN kategori_servis ks ON ks.id = r.id_kategori "
                + "WHERE k.id_user = ? ORDER BY r.tanggal DESC, r.id DESC";
        List<RiwayatServis> hasil = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new RiwayatServis(
                            rs.getInt("id"), rs.getInt("id_kendaraan"), rs.getString("plat_nomor"), rs.getString("merk"),
                            rs.getInt("id_kategori"), rs.getString("kategori"),
                            rs.getDate("tanggal").toLocalDate(), rs.getInt("odometer"),
                            rs.getDouble("biaya"), rs.getString("nama_bengkel"), rs.getString("catatan")));
                }
            }
        }
        return hasil;
    }

    public int hapus(int idUser, int idRiwayat) throws SQLException {
        String sql = "DELETE r FROM riwayat_servis r JOIN kendaraan k ON k.id = r.id_kendaraan "
                + "WHERE r.id = ? AND k.id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRiwayat);
            ps.setInt(2, idUser);
            return ps.executeUpdate();
        }
    }

    public double totalPengeluaran(int idUser) throws SQLException {
        String sql = "SELECT COALESCE(SUM(r.biaya), 0) FROM riwayat_servis r "
                + "JOIN kendaraan k ON k.id = r.id_kendaraan WHERE k.id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    /** Pengeluaran satu bulan, format bulan "yyyy-MM". */
    public double pengeluaranBulan(int idUser, String yyyyMm) throws SQLException {
        String sql = "SELECT COALESCE(SUM(r.biaya), 0) FROM riwayat_servis r "
                + "JOIN kendaraan k ON k.id = r.id_kendaraan "
                + "WHERE k.id_user = ? AND DATE_FORMAT(r.tanggal, '%Y-%m') = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            ps.setString(2, yyyyMm);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    /** Laporan pengeluaran per bulan (bulan terbaru paling atas). */
    public List<LaporanBulanan> laporanBulanan(int idUser) throws SQLException {
        String sql = "SELECT DATE_FORMAT(r.tanggal, '%Y-%m') AS bulan, COUNT(*) AS jumlah, SUM(r.biaya) AS total "
                + "FROM riwayat_servis r JOIN kendaraan k ON k.id = r.id_kendaraan "
                + "WHERE k.id_user = ? GROUP BY bulan ORDER BY bulan DESC";
        List<LaporanBulanan> hasil = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    hasil.add(new LaporanBulanan(rs.getString("bulan"), rs.getInt("jumlah"), rs.getDouble("total")));
                }
            }
        }
        return hasil;
    }
}
