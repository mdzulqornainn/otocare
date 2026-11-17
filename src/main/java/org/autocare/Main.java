package org.autocare;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Optional;

public class Main extends Application {

    private final Bengkel bengkel = new Bengkel();
    private Stage window;

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("Sistem Manajemen AutoCare");
        window.setResizable(false);
        window.setScene(buatSceneLogin());   // Stage hanya menampilkan satu Scene dalam satu waktu
        window.show();
    }

    // GUI LOGIN
    private Scene buatSceneLogin() {
        Label lblJudul = new Label("AutoCare - Login");
        lblJudul.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label lblUser = new Label("Username:");
        TextField txtUser = new TextField();
        txtUser.setPromptText("Masukkan username");

        Label lblPass = new Label("Password:");
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Masukkan password");

        Button btnLogin = new Button("Login");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(25));

        grid.add(lblUser, 0, 0);
        grid.add(txtUser, 1, 0);
        grid.add(lblPass, 0, 1);
        grid.add(txtPass, 1, 1);
        grid.add(btnLogin, 1, 2);

        VBox root = new VBox(10, lblJudul, grid);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(15));

        // Login
        Runnable prosesLogin = () -> {
            String username = txtUser.getText().trim();
            String password = txtPass.getText();

            if (username.isEmpty() || password.isEmpty()) {
                tampilkanAlert(AlertType.WARNING, "Peringatan", "Username dan password wajib diisi!");
                return;
            }

            // Konek DB
            try {
                if (cekLogin(username, password)) {
                    window.setScene(buatSceneUtama());   // pindah Scene: Login -> Menu Utama
                    window.centerOnScreen();
                } else {
                    tampilkanAlert(AlertType.ERROR, "Login Gagal", "Username / password salah!");
                    txtPass.clear();
                }
            } catch (SQLException ex) {
                tampilkanAlert(AlertType.ERROR, "Kesalahan Database",
                        "Tidak dapat terhubung ke database:\n" + ex.getMessage());
            }
        };

        btnLogin.setOnAction(e -> prosesLogin.run());
        txtPass.setOnAction(e -> prosesLogin.run());   // tekan Enter di kolom password = klik Login

        return new Scene(root, 360, 280);
    }

    // FORM KENDARAAN
    private Scene buatSceneUtama() {
        Label lblTipe = new Label("Tipe Kendaraan:");
        RadioButton rbMobil = new RadioButton("Mobil");
        RadioButton rbMotor = new RadioButton("Motor");
        ToggleGroup grupTipe = new ToggleGroup();
        rbMobil.setToggleGroup(grupTipe);
        rbMotor.setToggleGroup(grupTipe);
        rbMobil.setSelected(true);
        HBox boxTipe = new HBox(15, rbMobil, rbMotor);

        Label lblPlat = new Label("Plat Nomor:");
        TextField txtPlat = new TextField();
        txtPlat.setPromptText("Contoh: BE 1234 AB");

        Label lblMerk = new Label("Merk:");
        TextField txtMerk = new TextField();
        txtMerk.setPromptText("Contoh: Toyota Avanza");

        Label lblPemilik = new Label("Pemilik:");
        TextField txtPemilik = new TextField();
        txtPemilik.setPromptText("Nama pemilik");

        Label lblPintu = new Label("Jumlah Pintu:");
        TextField txtPintu = new TextField();
        txtPintu.setPromptText("2 - 6");

        Label lblTransmisi = new Label("Jenis Transmisi:");
        ComboBox<String> cbTransmisi = new ComboBox<>();
        cbTransmisi.getItems().addAll("Manual", "Matic", "Semi-Otomatis");
        cbTransmisi.setValue("Manual");

        Button btnSimpan = new Button("Simpan");
        Button btnReset = new Button("Reset Form");
        Button btnHapus = new Button("Hapus (sesuai Plat)");
        Button btnMuat = new Button("Muat dari DB");
        Button btnLogout = new Button("Logout");

        TextArea txtAreaOutput = new TextArea();
        txtAreaOutput.setEditable(false);
        txtAreaOutput.setPrefRowCount(8);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(5));

        grid.add(lblTipe, 0, 0);
        grid.add(boxTipe, 1, 0);
        grid.add(lblPlat, 0, 1);
        grid.add(txtPlat, 1, 1);
        grid.add(lblMerk, 0, 2);
        grid.add(txtMerk, 1, 2);
        grid.add(lblPemilik, 0, 3);
        grid.add(txtPemilik, 1, 3);
        grid.add(lblPintu, 0, 4);
        grid.add(txtPintu, 1, 4);
        grid.add(lblTransmisi, 0, 5);
        grid.add(cbTransmisi, 1, 5);

        HBox barisAksi = new HBox(10, btnSimpan, btnReset, btnHapus);
        HBox barisData = new HBox(10, btnMuat, btnLogout);

        VBox root = new VBox(12);
        root.setPadding(new Insets(20));
        root.getChildren().addAll(grid, barisAksi, new Label("Daftar Kendaraan:"), txtAreaOutput, barisData);

        Runnable aturFieldKhusus = () -> {
            boolean mobil = rbMobil.isSelected();
            lblPintu.setVisible(mobil);      lblPintu.setManaged(mobil);
            txtPintu.setVisible(mobil);      txtPintu.setManaged(mobil);
            lblTransmisi.setVisible(!mobil); lblTransmisi.setManaged(!mobil);
            cbTransmisi.setVisible(!mobil);  cbTransmisi.setManaged(!mobil);
        };
        aturFieldKhusus.run();
        grupTipe.selectedToggleProperty().addListener((obs, lama, baru) -> aturFieldKhusus.run());

        btnReset.setOnAction(e -> {
            bersihkanForm(txtPlat, txtMerk, txtPemilik, txtPintu);
            cbTransmisi.setValue("Manual");
            txtPlat.requestFocus();
        });

        btnSimpan.setOnAction(e -> {
            String plat = txtPlat.getText().trim().toUpperCase();
            String merk = txtMerk.getText().trim();
            String pemilik = txtPemilik.getText().trim();
            boolean mobil = rbMobil.isSelected();
            String strPintu = txtPintu.getText().trim();

            if (plat.isEmpty() || merk.isEmpty() || pemilik.isEmpty() || (mobil && strPintu.isEmpty())) {
                tampilkanAlert(AlertType.WARNING, "Peringatan", "Seluruh kolom form wajib diisi!");
                return;
            }

            // plat nomor (regex)
            if (!plat.matches("[A-Z]{1,2}\\s?\\d{1,4}\\s?[A-Z]{0,3}")) {
                tampilkanAlert(AlertType.WARNING, "Peringatan",
                        "Format plat nomor tidak valid!\nContoh: BE 1234 AB");
                return;
            }

            int jumlahPintu = 0;
            String transmisi = cbTransmisi.getValue();
            if (mobil) {
                try {
                    jumlahPintu = Integer.parseInt(strPintu);
                } catch (NumberFormatException ex) {
                    tampilkanAlert(AlertType.ERROR, "Kesalahan Format", "Jumlah pintu harus berupa angka bulat!");
                    return;
                }
                if (jumlahPintu < 2 || jumlahPintu > 6) {
                    tampilkanAlert(AlertType.WARNING, "Peringatan", "Jumlah pintu harus berada pada rentang 2 sampai 6!");
                    return;
                }
            }

            String tipe = mobil ? "Mobil" : "Motor";
            String khusus = mobil ? String.valueOf(jumlahPintu) : transmisi;
            try {
                simpanKeDatabase(tipe, plat, merk, pemilik, khusus);
            } catch (SQLIntegrityConstraintViolationException ex) {
                tampilkanAlert(AlertType.WARNING, "Data Duplikat", "Plat nomor " + plat + " sudah terdaftar!");
                return;
            } catch (SQLException ex) {
                tampilkanAlert(AlertType.ERROR, "Kesalahan Database", "Gagal menyimpan: " + ex.getMessage());
                return;
            }

            Kendaraan k = mobil
                    ? new Mobil(plat, merk, pemilik, jumlahPintu)
                    : new Motor(plat, merk, pemilik, transmisi);
            bengkel.tambahKendaraan(k);
            txtAreaOutput.appendText("Tersimpan di DB & Memori: " + k.getInfo() + "\n");

            tampilkanAlert(AlertType.INFORMATION, "Berhasil", "Data kendaraan berhasil disimpan!\n\n" + k.getInfo());
            bersihkanForm(txtPlat, txtMerk, txtPemilik, txtPintu);
            cbTransmisi.setValue("Manual");
        });

        btnHapus.setOnAction(e -> {
            String plat = txtPlat.getText().trim().toUpperCase();
            if (plat.isEmpty()) {
                tampilkanAlert(AlertType.WARNING, "Peringatan", "Isi kolom Plat Nomor dengan plat yang ingin dihapus!");
                return;
            }
            if (!tampilkanKonfirmasi("Konfirmasi Hapus", "Hapus kendaraan dengan plat " + plat + " ?")) {
                return;
            }
            try {
                int terhapus = hapusDariDatabase(plat);
                if (terhapus == 0) {
                    tampilkanAlert(AlertType.WARNING, "Tidak Ditemukan", "Plat " + plat + " tidak ada di database.");
                    return;
                }
                bengkel.hapusByPlat(plat);
                tampilkanAlert(AlertType.INFORMATION, "Berhasil", "Kendaraan " + plat + " berhasil dihapus.");
                tampilkanDaftar(txtAreaOutput);
                bersihkanForm(txtPlat, txtMerk, txtPemilik, txtPintu);
            } catch (SQLException ex) {
                tampilkanAlert(AlertType.ERROR, "Kesalahan Database", "Gagal menghapus: " + ex.getMessage());
            }
        });

        btnMuat.setOnAction(e -> {
            try {
                muatDariDatabase();
                tampilkanDaftar(txtAreaOutput);
                tampilkanAlert(AlertType.INFORMATION, "Berhasil",
                        bengkel.getDaftarKendaraan().size() + " kendaraan dimuat dari database.");
            } catch (SQLException ex) {
                tampilkanAlert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat data: " + ex.getMessage());
            }
        });

        btnLogout.setOnAction(e -> {
            if (tampilkanKonfirmasi("Logout", "Yakin ingin keluar ke halaman login?")) {
                window.setScene(buatSceneLogin());
                window.centerOnScreen();
            }
        });

        return new Scene(root, 440, 640);
    }

    // Buat DB CRUD
    private boolean cekLogin(String username, String password) throws SQLException {
        String query = "SELECT 1 FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void simpanKeDatabase(String tipe, String plat, String merk, String pemilik, String khusus) throws SQLException {
        String sql = "INSERT INTO kendaraan (tipe, plat_nomor, merk, pemilik, khusus) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tipe);
            pstmt.setString(2, plat);
            pstmt.setString(3, merk);
            pstmt.setString(4, pemilik);
            pstmt.setString(5, khusus);
            pstmt.executeUpdate();
        }
    }

    private int hapusDariDatabase(String plat) throws SQLException {
        String sql = "DELETE FROM kendaraan WHERE plat_nomor = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plat);
            return pstmt.executeUpdate();
        }
    }

    private void muatDariDatabase() throws SQLException {
        String sql = "SELECT tipe, plat_nomor, merk, pemilik, khusus FROM kendaraan ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            bengkel.kosongkan();
            while (rs.next()) {
                String tipe = rs.getString("tipe");
                String plat = rs.getString("plat_nomor");
                String merk = rs.getString("merk");
                String pemilik = rs.getString("pemilik");
                String khusus = rs.getString("khusus");

                if (tipe.equals("Mobil")) {
                    try {
                        bengkel.tambahKendaraan(new Mobil(plat, merk, pemilik, Integer.parseInt(khusus)));
                    } catch (NumberFormatException ex) {
                        // baris dengan jumlah pintu rusak dilewati agar aplikasi tidak crash
                        System.out.println("Baris dilewati (pintu tidak valid): " + plat);
                    }
                } else {
                    bengkel.tambahKendaraan(new Motor(plat, merk, pemilik, khusus));
                }
            }
        }
    }

    private void tampilkanDaftar(TextArea area) {
        area.clear();
        for (Kendaraan k : bengkel.getDaftarKendaraan()) {
            area.appendText(k.getInfo() + "\n");
        }
    }

    private void bersihkanForm(TextField txtPlat, TextField txtMerk, TextField txtPemilik, TextField txtPintu) {
        txtPlat.clear();
        txtMerk.clear();
        txtPemilik.clear();
        txtPintu.clear();
    }

    private void tampilkanAlert(AlertType tipe, String judul, String pesan) {
        Alert alert = new Alert(tipe);
        alert.setTitle(judul);
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.showAndWait();
    }

    private boolean tampilkanKonfirmasi(String judul, String pesan) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(judul);
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        Optional<ButtonType> hasil = alert.showAndWait();
        return hasil.isPresent() && hasil.get() == ButtonType.OK;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
