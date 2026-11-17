package org.autocare;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Bisa diganti lewat environment variable, default-nya untuk XAMPP/MySQL lokal
    private static final String URL  = System.getenv().getOrDefault("AUTOCARE_DB_URL", "jdbc:mysql://localhost:3306/autocare_db");
    private static final String USER = System.getenv().getOrDefault("AUTOCARE_DB_USER", "root");
    private static final String PASS = System.getenv().getOrDefault("AUTOCARE_DB_PASS", "");

    // Melempar SQLException (bukan return null) supaya error bisa ditangkap & ditampilkan lewat Alert
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
