package org.otocare;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.otocare.dao.PenggunaDAO;
import org.otocare.dao.PengingatDAO;
import org.otocare.model.JadwalPengingat;
import org.otocare.model.Pengguna;
import org.otocare.model.StatusPengingat;
import org.otocare.ui.Halaman;
import org.otocare.ui.HalamanDashboard;
import org.otocare.ui.HalamanKendaraan;
import org.otocare.ui.HalamanLaporan;
import org.otocare.ui.HalamanPengingat;
import org.otocare.ui.HalamanRiwayat;
import org.otocare.ui.Tampilan;
import org.otocare.util.Format;
import org.otocare.util.Keamanan;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/**
 * OtoCare - aplikasi pengingat servis kendaraan (JavaFX).
 * Pola Pertemuan 12: Stage -> Scene -> Layout -> Controls -> Event Handling, Validasi, try-catch, Alert.
 * Navigasi: satu Stage berganti Scene (Login <-> Registrasi <-> Aplikasi), tampilan diatur css/style.css.
 */
public class Main extends Application {
    private final PenggunaDAO penggunaDAO = new PenggunaDAO();
    private final PengingatDAO pengingatDAO = new PengingatDAO();

    private Stage window;
    private Pengguna pengguna;                       // akun yang sedang login

    // Elemen kerangka aplikasi
    private final VBox konten = new VBox();
    private final List<Button> tombolNav = new ArrayList<>();
    private Button btnNotif;

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("OtoCare - Pengingat Servis Kendaraan");
        window.setResizable(false);
        ganti(buatSceneLogin(null));
        window.show();
    }

    // Stage hanya menampilkan satu Scene dalam satu waktu
    private void ganti(Scene scene) {
        window.setScene(scene);
        window.sizeToScene();
        window.centerOnScreen();
    }

    // =====================================================
    // SCENE: LOGIN (panel kiri gelap + panel form kanan, sesuai desain)
    // =====================================================
    private HBox panelSplit(VBox formKanan) {
        Label brand = new Label("OTOCARE");
        brand.getStyleClass().add("split-brand");
        Label judul = new Label("RAWAT KENDARAAN TEPAT WAKTU");
        judul.getStyleClass().add("split-title");
        judul.setWrapText(true);
        Label sub = new Label("Kelola jadwal ganti oli, cek komponen rutin, dan catatan biaya servis mobil maupun motor dalam satu dasbor tegas.");
        sub.getStyleClass().add("split-sub");
        sub.setWrapText(true);
        VBox kiri = new VBox(18, brand, judul, sub, Tampilan_ilustrasi(), chip("PANTAU ODOMETER"), chip("PENGINGAT OTOMATIS"), chip("RIWAYAT TERCATAT"));
        kiri.getStyleClass().add("split-kiri");
        kiri.setPrefWidth(500);
        kiri.setMinWidth(500);
        kiri.setMaxWidth(500);

        formKanan.getStyleClass().add("split-kanan");
        HBox.setHgrow(formKanan, Priority.ALWAYS);
        return new HBox(kiri, formKanan);
    }

    private Label chip(String teks) {
        Label chip = new Label(teks);
        chip.getStyleClass().add("chip");
        return chip;
    }

    private javafx.scene.Node Tampilan_ilustrasi() {
        VBox kotak = new VBox(org.otocare.ui.Ilustrasi.mobilDanMotor());
        kotak.setAlignment(Pos.CENTER);
        VBox.setMargin(kotak, new Insets(18, 0, 0, 0));
        return kotak;
    }

    private Scene buatSceneLogin(String usernameAwal) {
        Label lblJudul = new Label("MASUK KE OTOCARE");
        lblJudul.getStyleClass().add("page-title");
        Label lblSub = new Label("Masuk untuk memantau jadwal servis kendaraan Anda");
        lblSub.getStyleClass().add("muted");

        TextField txtUser = new TextField();
        txtUser.setPromptText("Masukkan username");
        if (usernameAwal != null) {
            txtUser.setText(usernameAwal);
        }
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Masukkan password");

        Button btnLogin = new Button("MASUK SEKARANG");
        btnLogin.getStyleClass().add("btn-primary");
        btnLogin.setMaxWidth(Double.MAX_VALUE);
        btnLogin.setDefaultButton(true);

        Button btnDaftar = new Button("Belum punya akun? Daftar");
        btnDaftar.getStyleClass().add("btn-link");
        btnDaftar.setMaxWidth(Double.MAX_VALUE);

        VBox form = new VBox(8, lblJudul, lblSub, Tampilan.labelField("Username"), txtUser,
                Tampilan.labelField("Password"), txtPass, btnLogin, btnDaftar);
        form.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(lblSub, new Insets(0, 0, 14, 0));
        VBox.setMargin(btnLogin, new Insets(14, 0, 0, 0));

        btnLogin.setOnAction(e -> {
            String username = txtUser.getText().trim();
            String password = txtPass.getText();

            // Validasi kolom kosong
            if (username.isEmpty() || password.isEmpty()) {
                Tampilan.alert(AlertType.WARNING, "Peringatan", "Username dan password wajib diisi!");
                return;
            }

            // Error handling koneksi database
            try {
                Pengguna hasil = penggunaDAO.login(username, Keamanan.hash(password));
                if (hasil == null) {
                    Tampilan.alert(AlertType.ERROR, "Login Gagal", "Username / password salah!");
                    txtPass.clear();
                    return;
                }
                pengguna = hasil;
                ganti(buatSceneAplikasi());
                // Pop-up otomatis saat login jika ada kendaraan Segera Servis / Jatuh Tempo
                Platform.runLater(() -> tampilkanPopupPengingat(false));
            } catch (SQLException ex) {
                Tampilan.alert(AlertType.ERROR, "Kesalahan Database",
                        "Tidak dapat terhubung ke database:\n" + ex.getMessage());
            }
        });
        btnDaftar.setOnAction(e -> ganti(buatSceneRegistrasi()));

        return buatScene(panelSplit(form), 1020, 700);
    }

    // =====================================================
    // SCENE: REGISTRASI AKUN
    // =====================================================
    private Scene buatSceneRegistrasi() {
        Label lblJudul = new Label("BUAT AKUN");
        lblJudul.getStyleClass().add("page-title");
        Label lblSub = new Label("Daftar sebagai pemilik kendaraan");
        lblSub.getStyleClass().add("muted");

        TextField txtNama = new TextField();
        txtNama.setPromptText("Nama lengkap");
        TextField txtUser = new TextField();
        txtUser.setPromptText("Username (huruf/angka/_, 3-20 karakter)");
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Minimal 6 karakter");
        PasswordField txtUlang = new PasswordField();
        txtUlang.setPromptText("Ulangi password");

        Button btnDaftar = new Button("DAFTAR SEKARANG");
        btnDaftar.getStyleClass().add("btn-primary");
        btnDaftar.setMaxWidth(Double.MAX_VALUE);
        btnDaftar.setDefaultButton(true);
        Button btnKembali = new Button("Sudah punya akun? Masuk");
        btnKembali.getStyleClass().add("btn-link");
        btnKembali.setMaxWidth(Double.MAX_VALUE);

        VBox form = new VBox(6, lblJudul, lblSub,
                Tampilan.labelField("Nama Lengkap"), txtNama,
                Tampilan.labelField("Username"), txtUser,
                Tampilan.labelField("Password"), txtPass,
                Tampilan.labelField("Ulangi Password"), txtUlang,
                btnDaftar, btnKembali);
        form.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(lblSub, new Insets(0, 0, 8, 0));
        VBox.setMargin(btnDaftar, new Insets(12, 0, 0, 0));

        btnDaftar.setOnAction(e -> {
            String nama = txtNama.getText().trim();
            String username = txtUser.getText().trim();
            String password = txtPass.getText();
            String ulang = txtUlang.getText();

            // A. Kolom kosong
            if (nama.isEmpty() || username.isEmpty() || password.isEmpty() || ulang.isEmpty()) {
                Tampilan.alert(AlertType.WARNING, "Peringatan", "Seluruh kolom wajib diisi!");
                return;
            }
            // B. Pola username (regex)
            if (!username.matches("\\w{3,20}")) {
                Tampilan.alert(AlertType.WARNING, "Peringatan",
                        "Username hanya boleh huruf, angka, dan underscore (3-20 karakter)!");
                return;
            }
            // C. Panjang & kecocokan password
            if (password.length() < 6) {
                Tampilan.alert(AlertType.WARNING, "Peringatan", "Password minimal 6 karakter!");
                return;
            }
            if (!password.equals(ulang)) {
                Tampilan.alert(AlertType.WARNING, "Peringatan", "Konfirmasi password tidak sama!");
                return;
            }

            try {
                penggunaDAO.daftar(nama, username, Keamanan.hash(password));
                Tampilan.alert(AlertType.INFORMATION, "Berhasil", "Akun berhasil dibuat! Silakan masuk.");
                ganti(buatSceneLogin(username));
            } catch (SQLIntegrityConstraintViolationException ex) {
                Tampilan.alert(AlertType.WARNING, "Username Dipakai", "Username \"" + username + "\" sudah terdaftar!");
            } catch (SQLException ex) {
                Tampilan.alert(AlertType.ERROR, "Kesalahan Database", "Gagal mendaftar: " + ex.getMessage());
            }
        });
        btnKembali.setOnAction(e -> ganti(buatSceneLogin(null)));

        return buatScene(panelSplit(form), 1020, 700);
    }

    // =====================================================
    // SCENE: APLIKASI UTAMA (sidebar gelap 240px + halaman)
    // =====================================================
    private Scene buatSceneAplikasi() {
        tombolNav.clear();
        konten.getChildren().clear();

        // Callback: dipanggil halaman saat data berubah -> badge notifikasi diperbarui
        Runnable onBerubah = this::perbaruiNotifikasi;

        HalamanRiwayat riwayat = new HalamanRiwayat(pengguna, onBerubah);
        HalamanPengingat pengingat = new HalamanPengingat(pengguna, onBerubah);
        Halaman kendaraan = new HalamanKendaraan(pengguna, onBerubah);
        Halaman laporan = new HalamanLaporan(pengguna);

        // Tombol navigasi (dibuat dulu agar dapat dirujuk callback dashboard)
        Button navDashboard = new Button("Dashboard");
        Button navKendaraan = new Button("Kendaraan Saya");
        Button navRiwayat = new Button("Riwayat Servis");
        Button navPengingat = new Button("Pengingat");
        Button navLaporan = new Button("Laporan Bulanan");

        Halaman dashboard = new HalamanDashboard(pengguna,
                () -> {                                   // "+ Catat Servis Baru"
                    tampilkanHalaman(riwayat, navRiwayat);
                    riwayat.catatDariPengingat(null);
                },
                j -> {                                    // "Servis Sekarang"
                    tampilkanHalaman(riwayat, navRiwayat);
                    riwayat.catatDariPengingat(j);
                },
                () -> tampilkanHalaman(pengingat, navPengingat));

        pasangNav(navDashboard, dashboard);
        pasangNav(navKendaraan, kendaraan);
        pasangNav(navRiwayat, riwayat);
        pasangNav(navPengingat, pengingat);
        pasangNav(navLaporan, laporan);

        // ---------- Sidebar ----------
        Label lblBrand = new Label("OTOCARE");
        lblBrand.getStyleClass().add("side-brand");
        Label lblBrandSub = new Label("PENGINGAT SERVIS");
        lblBrandSub.getStyleClass().add("side-brand-sub");
        VBox blokBrand = new VBox(2, lblBrand, lblBrandSub);
        blokBrand.getStyleClass().add("side-brand-box");

        btnNotif = new Button("PENGINGAT (0)");
        btnNotif.getStyleClass().add("notif-btn");
        btnNotif.setMaxWidth(Double.MAX_VALUE);
        btnNotif.setOnAction(e -> tampilkanPopupPengingat(true));

        Label avatar = new Label(Format.inisial(pengguna.getNamaLengkap()));
        avatar.getStyleClass().add("avatar");
        avatar.setAlignment(Pos.CENTER);
        Label lblUser = new Label(pengguna.getNamaLengkap());
        lblUser.getStyleClass().add("side-user");
        Label lblRole = new Label("Pemilik");
        lblRole.getStyleClass().add("side-role");
        HBox blokUser = new HBox(10, avatar, new VBox(1, lblUser, lblRole));
        blokUser.setAlignment(Pos.CENTER_LEFT);

        Button btnKeluar = new Button("KELUAR");
        btnKeluar.getStyleClass().add("keluar-btn");
        btnKeluar.setMaxWidth(Double.MAX_VALUE);
        btnKeluar.setOnAction(e -> {
            if (Tampilan.konfirmasi("Keluar", "Yakin ingin keluar ke halaman login?")) {
                pengguna = null;
                ganti(buatSceneLogin(null));
            }
        });

        VBox menu = new VBox(4, navDashboard, navKendaraan, navRiwayat, navPengingat, navLaporan);
        VBox.setVgrow(menu, Priority.ALWAYS);
        VBox bawah = new VBox(10, btnNotif, blokUser, btnKeluar);
        bawah.getStyleClass().add("side-bawah");

        VBox sidebar = new VBox(blokBrand, menu, bawah);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(240);
        sidebar.setMaxWidth(240);

        konten.getStyleClass().add("konten");
        HBox.setHgrow(konten, Priority.ALWAYS);

        HBox badan = new HBox(sidebar, konten);
        tampilkanHalaman(dashboard, navDashboard);
        return buatScene(badan, 1280, 780);
    }

    private void pasangNav(Button tombol, Halaman halaman) {
        tombol.getStyleClass().add("nav-btn");
        tombol.setMaxWidth(Double.MAX_VALUE);
        tombol.setOnAction(e -> tampilkanHalaman(halaman, tombol));
        tombolNav.add(tombol);
    }

    private void tampilkanHalaman(Halaman halaman, Button aktif) {
        for (Button b : tombolNav) {
            b.getStyleClass().remove("nav-aktif");
        }
        aktif.getStyleClass().add("nav-aktif");

        halaman.segarkan();
        konten.getChildren().setAll(halaman.getTampilan());
        VBox.setVgrow(halaman.getTampilan(), Priority.ALWAYS);
        perbaruiNotifikasi();
    }

    // =====================================================
    // NOTIFIKASI PENGINGAT SERVIS
    // =====================================================
    /** Memperbarui jumlah pengingat yang perlu perhatian pada tombol di header. */
    private void perbaruiNotifikasi() {
        if (btnNotif == null || pengguna == null) {
            return;
        }
        try {
            int jumlah = pengingatDAO.perluPerhatian(pengguna.getId()).size();
            btnNotif.setText("PENGINGAT (" + jumlah + ")");
            btnNotif.getStyleClass().remove("notif-aktif");
            if (jumlah > 0) {
                btnNotif.getStyleClass().add("notif-aktif");
            }
        } catch (SQLException ex) {
            btnNotif.setText("PENGINGAT (?)");
        }
    }

    /**
     * Pop-up daftar kendaraan berstatus Segera Servis / Jatuh Tempo.
     * Otomatis saat login (tampilkanJikaAman = false) atau manual lewat tombol header (true).
     */
    private void tampilkanPopupPengingat(boolean tampilkanJikaAman) {
        if (pengguna == null) {
            return;
        }
        try {
            List<JadwalPengingat> daftar = pengingatDAO.perluPerhatian(pengguna.getId());
            if (daftar.isEmpty()) {
                if (tampilkanJikaAman) {
                    Tampilan.alert(AlertType.INFORMATION, "Pengingat Servis",
                            "Semua jadwal servis masih aman. Tidak ada kendaraan yang perlu diservis.");
                }
                return;
            }

            final int batas = 10;
            StringBuilder pesan = new StringBuilder();
            pesan.append("Ada ").append(daftar.size()).append(" pengingat servis yang perlu perhatian:\n\n");
            for (int i = 0; i < daftar.size() && i < batas; i++) {
                JadwalPengingat j = daftar.get(i);
                StatusPengingat status = j.cekStatus();
                pesan.append("[").append(status.getLabel().toUpperCase()).append("] ")
                        .append(j.getPlatNomor()).append(" - ").append(j.getPesanPengingat()).append("\n");
            }
            if (daftar.size() > batas) {
                pesan.append("... dan ").append(daftar.size() - batas).append(" lainnya (lihat menu Pengingat).");
            }
            Tampilan.alert(AlertType.WARNING, "Pengingat Servis Kendaraan", pesan.toString());
        } catch (SQLException ex) {
            Tampilan.alert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat pengingat:\n" + ex.getMessage());
        }
    }

    // =====================================================
    // HELPER
    // =====================================================
    private Scene buatScene(Region root, double lebar, double tinggi) {
        Scene scene = new Scene(root, lebar, tinggi);
        scene.getStylesheets().add(Tampilan.css());
        return scene;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
