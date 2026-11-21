package org.otocare.dao;

import org.otocare.model.Pengguna;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PenggunaDAO {

    /** Mengembalikan Pengguna jika username & hash password cocok, null jika tidak. */
    public Pengguna login(String username, String passwordHash) throws SQLException {
        String sql = "SELECT id, username, nama_lengkap FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Pengguna(rs.getInt("id"), rs.getString("username"), rs.getString("nama_lengkap"));
                }
                return null;
            }
        }
    }

    /** Melempar SQLIntegrityConstraintViolationException jika username sudah dipakai. */
    public void daftar(String namaLengkap, String username, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (username, password, nama_lengkap) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, namaLengkap);
            ps.executeUpdate();
        }
    }
}
