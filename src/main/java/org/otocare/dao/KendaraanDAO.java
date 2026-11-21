package org.otocare.dao;

import org.otocare.model.Kendaraan;
import org.otocare.model.Mobil;
import org.otocare.model.Motor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class KendaraanDAO {

    public List<Kendaraan> semua(int idUser) throws SQLException {
        String sql = "SELECT id, tipe, plat_nomor, merk, pemilik, khusus, odometer "
                + "FROM kendaraan WHERE id_user = ? ORDER BY id";
        List<Kendaraan> hasil = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    try {
                        hasil.add(buatObjek(rs));
                    } catch (IllegalArgumentException ex) {
                        // baris dengan data rusak dilewati agar aplikasi tidak crash
                        System.out.println("Baris kendaraan dilewati: " + rs.getString("plat_nomor") + " (" + ex.getMessage() + ")");
                    }
                }
            }
        }
        return hasil;
    }

    // Membuat objek SUBCLASS yang sesuai (Mobil / Motor)
    private Kendaraan buatObjek(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String plat = rs.getString("plat_nomor");
        String merk = rs.getString("merk");
        String pemilik = rs.getString("pemilik");
        int odometer = rs.getInt("odometer");
        String khusus = rs.getString("khusus");

        if (rs.getString("tipe").equals("Mobil")) {
            return new Mobil(id, plat, merk, pemilik, odometer, Integer.parseInt(khusus));
        }
        return new Motor(id, plat, merk, pemilik, odometer, khusus);
    }

    /** Menyimpan kendaraan baru dan mengisi id hasil auto-increment ke objeknya. */
    public void tambah(int idUser, Kendaraan k) throws SQLException {
        String sql = "INSERT INTO kendaraan (id_user, tipe, plat_nomor, merk, pemilik, khusus, odometer) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idUser);
            ps.setString(2, k.getTipe());
            ps.setString(3, k.getPlatNomor());
            ps.setString(4, k.getMerk());
            ps.setString(5, k.getPemilik());
            ps.setString(6, k.getNilaiKhusus());
            ps.setInt(7, k.getOdometer());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    k.setId(keys.getInt(1));
                }
            }
        }
    }

    public void perbarui(int idUser, Kendaraan k) throws SQLException {
        String sql = "UPDATE kendaraan SET plat_nomor = ?, merk = ?, pemilik = ?, khusus = ?, odometer = ? "
                + "WHERE id = ? AND id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, k.getPlatNomor());
            ps.setString(2, k.getMerk());
            ps.setString(3, k.getPemilik());
            ps.setString(4, k.getNilaiKhusus());
            ps.setInt(5, k.getOdometer());
            ps.setInt(6, k.getId());
            ps.setInt(7, idUser);
            ps.executeUpdate();
        }
    }

    public void perbaruiOdometer(int idUser, int idKendaraan, int odometer) throws SQLException {
        String sql = "UPDATE kendaraan SET odometer = ? WHERE id = ? AND id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, odometer);
            ps.setInt(2, idKendaraan);
            ps.setInt(3, idUser);
            ps.executeUpdate();
        }
    }

    public int hapus(int idUser, int idKendaraan) throws SQLException {
        String sql = "DELETE FROM kendaraan WHERE id = ? AND id_user = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idKendaraan);
            ps.setInt(2, idUser);
            return ps.executeUpdate();
        }
    }
}
