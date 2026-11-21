package org.otocare.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.otocare.dao.KendaraanDAO;
import org.otocare.model.Bengkel;
import org.otocare.model.Kendaraan;
import org.otocare.model.Mobil;
import org.otocare.model.Montir;
import org.otocare.model.Motor;
import org.otocare.model.Pengguna;
import org.otocare.util.Format;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

/** Garasi Kendaraan Saya: filter, kartu kendaraan, serta modal Tambah/Edit dan Update Odometer. */
public class HalamanKendaraan implements Halaman {
    private static final String FILE_DATA = "data_kendaraan.dat";
    private static final String FILE_LAPORAN = "laporan_kendaraan.txt";

    private final Pengguna pengguna;
    private final Runnable onBerubah;
    private final KendaraanDAO kendaraanDAO = new KendaraanDAO();
    private final Bengkel bengkel = new Bengkel();          // AGREGASI
    private final Montir montir = new Montir("Pak Budi");   // ASOSIASI

    private String filter = "Semua";

    private final VBox root = new VBox(24);
    private final HBox barisTab = new HBox(8);
    private final GridPane gridKartu = new GridPane();

    public HalamanKendaraan(Pengguna pengguna, Runnable onBerubah) {
        this.pengguna = pengguna;
        this.onBerubah = onBerubah;
        bangunTampilan();
    }

    @Override
    public Node getTampilan() { return root; }

    @Override
    public void segarkan() {
        muatDariDatabase();
        tampilkanDaftar();
    }

    // =====================================================
    // TAMPILAN HALAMAN
    // =====================================================
    private void bangunTampilan() {
        Button btnTambah = new Button("+  TAMBAH KENDARAAN");
        btnTambah.getStyleClass().add("btn-primary");
        btnTambah.setOnAction(e -> bukaForm(null));

        Button btnMuatDb = new Button("Muat dari DB");
        Button btnSimpanFile = new Button("Simpan ke File");
        Button btnMuatFile = new Button("Muat dari File");
        Button btnEkspor = new Button("Ekspor TXT");
        for (Button b : new Button[]{btnMuatDb, btnSimpanFile, btnMuatFile, btnEkspor}) {
            b.getStyleClass().addAll("btn-outline", "btn-small");
        }
        barisTab.setAlignment(Pos.CENTER_LEFT);
        HBox toolbar = new HBox(8, barisTab, Tampilan.spacer(), btnMuatDb, btnSimpanFile, btnMuatFile, btnEkspor);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        gridKartu.getStyleClass().add("grid-kartu");
        gridKartu.setHgap(18);
        gridKartu.setVgap(16);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints kolom = new ColumnConstraints();
            kolom.setPercentWidth(33.33);
            gridKartu.getColumnConstraints().add(kolom);
        }
        ScrollPane scroll = new ScrollPane(gridKartu);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        root.getChildren().addAll(
                Tampilan.headerHalaman("Garasi Kendaraan Saya",
                        "Kelola data kendaraan, spesifikasi teknis, dan pembaruan odometer terkini", btnTambah),
                toolbar, scroll);

        btnMuatDb.setOnAction(e -> segarkan());

        btnSimpanFile.setOnAction(e -> {
            try {
                bengkel.simpanKeFile(FILE_DATA);
                Tampilan.alert(AlertType.INFORMATION, "Berhasil",
                        "Daftar kendaraan disimpan ke file:\n" + new File(FILE_DATA).getAbsolutePath());
            } catch (IOException ex) {
                Tampilan.alert(AlertType.ERROR, "Kesalahan File", "Gagal menyimpan file: " + ex.getMessage());
            }
        });

        btnMuatFile.setOnAction(e -> {
            try {
                bengkel.muatDariFile(FILE_DATA);
                tampilkanDaftar();
                Tampilan.alert(AlertType.INFORMATION, "Berhasil",
                        bengkel.getDaftarKendaraan().size() + " kendaraan dimuat dari file.\n"
                                + "Klik \"Muat dari DB\" untuk kembali ke data database.");
            } catch (FileNotFoundException ex) {
                Tampilan.alert(AlertType.WARNING, "File Tidak Ditemukan",
                        "Belum ada file " + FILE_DATA + ".\nKlik \"Simpan ke File\" terlebih dahulu.");
            } catch (IOException | ClassNotFoundException ex) {
                Tampilan.alert(AlertType.ERROR, "Kesalahan File", "Gagal memuat file: " + ex.getMessage());
            }
        });

        btnEkspor.setOnAction(e -> {
            try {
                bengkel.eksporKeTeks(FILE_LAPORAN);
                Tampilan.alert(AlertType.INFORMATION, "Berhasil",
                        "Laporan diekspor ke file:\n" + new File(FILE_LAPORAN).getAbsolutePath());
            } catch (IOException ex) {
                Tampilan.alert(AlertType.ERROR, "Kesalahan File", "Gagal mengekspor: " + ex.getMessage());
            }
        });
    }

    // =====================================================
    // DATA, FILTER & KARTU
    // =====================================================
    private void muatDariDatabase() {
        try {
            List<Kendaraan> hasil = kendaraanDAO.semua(pengguna.getId());
            bengkel.kosongkan();
            for (Kendaraan k : hasil) {
                bengkel.tambahKendaraan(k);   // objek dibuat di luar Bengkel -> agregasi
            }
        } catch (SQLException ex) {
            Tampilan.alert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat data kendaraan:\n" + ex.getMessage());
        }
    }

    private int hitung(String tipe) {
        int n = 0;
        for (Kendaraan k : bengkel.getDaftarKendaraan()) {
            if (tipe.equals("Semua") || k.getTipe().equals(tipe)) {
                n++;
            }
        }
        return n;
    }

    private void tampilkanDaftar() {
        // Tab filter: Semua / Mobil / Motor
        barisTab.getChildren().clear();
        for (String nama : new String[]{"Semua", "Mobil", "Motor"}) {
            Button tab = new Button(nama.toUpperCase() + " (" + hitung(nama) + ")");
            tab.getStyleClass().add("tab");
            if (nama.equals(filter)) {
                tab.getStyleClass().add("tab-aktif");
            }
            tab.setOnAction(e -> {
                filter = nama;
                tampilkanDaftar();
            });
            barisTab.getChildren().add(tab);
        }

        // Kartu kendaraan (3 kolom)
        gridKartu.getChildren().clear();
        List<Kendaraan> tampil = new ArrayList<>();
        for (Kendaraan k : bengkel.getDaftarKendaraan()) {
            if (filter.equals("Semua") || k.getTipe().equals(filter)) {
                tampil.add(k);
            }
        }
        if (tampil.isEmpty()) {
            gridKartu.add(Tampilan.stateKosong("Belum ada kendaraan.\nKlik \"+ Tambah Kendaraan\" untuk memulai."), 0, 0, 3, 1);
            return;
        }
        int indeks = 0;
        for (Kendaraan k : tampil) {
            gridKartu.add(buatKartu(k), indeks % 3, indeks / 3);
            indeks++;
        }
    }

    private VBox buatKartu(Kendaraan k) {
        // POLYMORPHISM: getTipe() / getDetailKhusus() menyesuaikan objek aslinya (Mobil / Motor)
        Label lblNama = new Label(k.getMerk());
        lblNama.getStyleClass().add("card-title");
        Label badge = new Label(k.getTipe().toUpperCase());
        badge.getStyleClass().addAll("badge", k.getTipe().equals("Mobil") ? "badge-mobil" : "badge-motor");
        HBox atas = new HBox(8, lblNama, Tampilan.spacer(), badge);
        atas.setAlignment(Pos.CENTER_LEFT);

        Label plat = Tampilan.mono(k.getPlatNomor());
        plat.getStyleClass().add("plat-pill");
        HBox barisPlat = new HBox(plat);

        Label lblOdoJudul = new Label("ODOMETER SAAT INI");
        lblOdoJudul.getStyleClass().add("kpi-label");
        Label lblOdo = Tampilan.mono(Format.angka(k.getOdometer()) + " KM");
        lblOdo.getStyleClass().add("odometer");
        VBox blokOdo = new VBox(2, lblOdoJudul, lblOdo);
        blokOdo.getStyleClass().add("blok-odometer");

        VBox spesifikasi = new VBox(6, barisSpek("Spesifikasi", k.getDetailKhusus()),
                barisSpek("No. STNK", k.getNomorStnk()));

        Button btnOdo = new Button("UPDATE ODOMETER");
        btnOdo.getStyleClass().addAll("btn-primary", "btn-small");
        btnOdo.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(btnOdo, Priority.ALWAYS);
        Button btnEdit = new Button("EDIT");
        btnEdit.getStyleClass().addAll("btn-outline", "btn-small");

        Button btnServis = new Button("Servis oleh Montir");
        btnServis.getStyleClass().addAll("btn-link", "btn-small");
        Button btnHapus = new Button("Hapus");
        btnHapus.getStyleClass().addAll("btn-link-danger", "btn-small");

        // ASOSIASI: Montir menservis Kendaraan lewat parameter method
        btnServis.setOnAction(e -> Tampilan.alert(AlertType.INFORMATION, "Servis Kendaraan", montir.servis(k)));
        btnOdo.setOnAction(e -> bukaUpdateOdometer(k));
        btnEdit.setOnAction(e -> bukaForm(k));
        btnHapus.setOnAction(e -> {
            if (!Tampilan.konfirmasi("Konfirmasi Hapus", "Hapus kendaraan " + k.getPlatNomor()
                    + " ?\nRiwayat servis dan pengingatnya ikut terhapus.")) {
                return;
            }
            try {
                kendaraanDAO.hapus(pengguna.getId(), k.getId());
                segarkan();
                onBerubah.run();
            } catch (SQLException ex) {
                Tampilan.alert(AlertType.ERROR, "Kesalahan Database", "Gagal menghapus: " + ex.getMessage());
            }
        });

        VBox kartu = new VBox(12, atas, barisPlat, blokOdo, spesifikasi,
                new HBox(8, btnOdo, btnEdit),
                new HBox(4, btnServis, Tampilan.spacer(), btnHapus));
        kartu.getStyleClass().add("garasi-card");
        return kartu;
    }

    private HBox barisSpek(String judul, String nilai) {
        Label lblJudul = new Label(judul);
        lblJudul.getStyleClass().add("muted-sm");
        Label lblNilai = new Label(nilai);
        lblNilai.getStyleClass().add("cell-title");
        HBox baris = new HBox(8, lblJudul, Tampilan.spacer(), lblNilai);
        baris.setAlignment(Pos.CENTER_LEFT);
        return baris;
    }

    // =====================================================
    // MODAL: TAMBAH / EDIT KENDARAAN
    // =====================================================
    private void bukaForm(Kendaraan edit) {
        RadioButton rbMobil = new RadioButton("Mobil");
        RadioButton rbMotor = new RadioButton("Motor");
        ToggleGroup grupTipe = new ToggleGroup();
        rbMobil.setToggleGroup(grupTipe);
        rbMotor.setToggleGroup(grupTipe);
        rbMobil.setSelected(true);
        HBox boxTipe = new HBox(18, rbMobil, rbMotor);

        TextField txtPlat = new TextField();
        txtPlat.setPromptText("Contoh: BE 1234 AB");
        TextField txtMerk = new TextField();
        txtMerk.setPromptText("Contoh: Toyota Avanza");
        TextField txtPemilik = new TextField(pengguna.getNamaLengkap());
        TextField txtOdometer = new TextField();
        txtOdometer.setPromptText("Odometer saat ini (km)");
        TextField txtPintu = new TextField();
        txtPintu.setPromptText("2 - 6");
        ComboBox<String> cbTransmisi = new ComboBox<>();
        cbTransmisi.getItems().addAll("Manual", "Matic", "Semi-Otomatis");
        cbTransmisi.setValue("Manual");
        cbTransmisi.setMaxWidth(Double.MAX_VALUE);

        Label lblPintu = Tampilan.labelField("Jumlah Pintu");
        Label lblTransmisi = Tampilan.labelField("Jenis Transmisi");

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(6);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints kolom = new ColumnConstraints();
            kolom.setPercentWidth(50);
            grid.getColumnConstraints().add(kolom);
        }
        grid.add(Tampilan.labelField("Tipe Kendaraan"), 0, 0, 2, 1);
        grid.add(boxTipe, 0, 1, 2, 1);
        grid.add(Tampilan.labelField("Plat Nomor"), 0, 2);
        grid.add(txtPlat, 0, 3);
        grid.add(Tampilan.labelField("Merk & Model"), 1, 2);
        grid.add(txtMerk, 1, 3);
        grid.add(Tampilan.labelField("Pemilik"), 0, 4);
        grid.add(txtPemilik, 0, 5);
        grid.add(Tampilan.labelField("Odometer (km)"), 1, 4);
        grid.add(txtOdometer, 1, 5);
        grid.add(lblPintu, 0, 6);
        grid.add(txtPintu, 0, 7);
        grid.add(lblTransmisi, 0, 6);
        grid.add(cbTransmisi, 0, 7);
        GridPane.setHgrow(txtPlat, Priority.ALWAYS);
        GridPane.setHgrow(txtMerk, Priority.ALWAYS);

        // Field khusus menyesuaikan tipe: Mobil -> jumlah pintu, Motor -> jenis transmisi
        Runnable aturFieldKhusus = () -> {
            boolean mobil = rbMobil.isSelected();
            lblPintu.setVisible(mobil);      lblPintu.setManaged(mobil);
            txtPintu.setVisible(mobil);      txtPintu.setManaged(mobil);
            lblTransmisi.setVisible(!mobil); lblTransmisi.setManaged(!mobil);
            cbTransmisi.setVisible(!mobil);  cbTransmisi.setManaged(!mobil);
        };
        aturFieldKhusus.run();
        grupTipe.selectedToggleProperty().addListener((obs, lama, baru) -> aturFieldKhusus.run());

        // Mode edit: isi form dari data kendaraan, tipe tidak boleh diubah
        if (edit != null) {
            rbMobil.setSelected(edit.getTipe().equals("Mobil"));
            rbMotor.setSelected(edit.getTipe().equals("Motor"));
            rbMobil.setDisable(true);
            rbMotor.setDisable(true);
            txtPlat.setText(edit.getPlatNomor());
            txtMerk.setText(edit.getMerk());
            txtPemilik.setText(edit.getPemilik());
            txtOdometer.setText(String.valueOf(edit.getOdometer()));
            if (edit.getTipe().equals("Mobil")) {
                txtPintu.setText(edit.getNilaiKhusus());     // POLYMORPHISM
            } else {
                cbTransmisi.setValue(edit.getNilaiKhusus());
            }
        }

        Button btnBatal = new Button("BATAL");
        btnBatal.getStyleClass().add("btn-outline");
        Button btnSimpan = new Button(edit == null ? "SIMPAN KENDARAAN" : "SIMPAN PERUBAHAN");
        btnSimpan.getStyleClass().add("btn-primary");

        VBox isi = Tampilan.kerangkaModal(
                edit == null ? "Tambah Kendaraan" : "Edit Kendaraan",
                "Lengkapi data kendaraan. Field khusus menyesuaikan tipe yang dipilih.",
                grid, btnBatal, btnSimpan);
        isi.setPrefWidth(560);

        Stage modal = Tampilan.modal(root.getScene().getWindow(), edit == null ? "Tambah Kendaraan" : "Edit Kendaraan", isi);
        btnBatal.setOnAction(e -> modal.close());
        btnSimpan.setOnAction(e -> {
            if (simpan(edit, rbMobil.isSelected(), txtPlat.getText().trim(), txtMerk.getText().trim(),
                    txtPemilik.getText().trim(), txtOdometer.getText().trim(), txtPintu.getText().trim(),
                    cbTransmisi.getValue())) {
                modal.close();
                segarkan();
                onBerubah.run();
            }
        });
        modal.showAndWait();
    }

    /** Validasi + simpan. Mengembalikan true jika berhasil (modal boleh ditutup). */
    private boolean simpan(Kendaraan edit, boolean mobil, String plat, String merk, String pemilik,
                           String strOdometer, String strPintu, String transmisi) {
        // A. Validasi kolom kosong
        if (plat.isEmpty() || merk.isEmpty() || pemilik.isEmpty() || strOdometer.isEmpty()
                || (mobil && strPintu.isEmpty())) {
            Tampilan.alert(AlertType.WARNING, "Peringatan", "Seluruh kolom form wajib diisi!");
            return false;
        }

        // B. Konversi angka + error handling
        int odometer;
        int pintu = 0;
        try {
            odometer = Integer.parseInt(strOdometer);
            if (mobil) {
                pintu = Integer.parseInt(strPintu);
            }
        } catch (NumberFormatException ex) {
            Tampilan.alert(AlertType.ERROR, "Kesalahan Format", "Odometer dan jumlah pintu harus berupa angka bulat!");
            return false;
        }

        try {
            // C. Objek dibuat dari SUBCLASS yang sesuai; validasi bisnis (plat, pintu, odometer) ada di model
            int idLama = edit == null ? 0 : edit.getId();
            int odometerAwal = edit == null ? odometer : edit.getOdometer();
            Kendaraan data = mobil
                    ? new Mobil(idLama, plat, merk, pemilik, odometerAwal, pintu)
                    : new Motor(idLama, plat, merk, pemilik, odometerAwal, transmisi);
            data.setOdometer(odometer);   // menolak odometer yang lebih kecil dari sebelumnya

            // D. Simpan ke database
            if (edit == null) {
                kendaraanDAO.tambah(pengguna.getId(), data);
                Tampilan.alert(AlertType.INFORMATION, "Berhasil", "Data kendaraan berhasil disimpan!\n\n" + data.getInfo());
            } else {
                kendaraanDAO.perbarui(pengguna.getId(), data);
                Tampilan.alert(AlertType.INFORMATION, "Berhasil", "Data kendaraan berhasil diperbarui!\n\n" + data.getInfo());
            }
            return true;
        } catch (IllegalArgumentException ex) {
            Tampilan.alert(AlertType.WARNING, "Peringatan", ex.getMessage());
        } catch (SQLIntegrityConstraintViolationException ex) {
            Tampilan.alert(AlertType.WARNING, "Data Duplikat", "Plat nomor " + plat.toUpperCase() + " sudah terdaftar!");
        } catch (SQLException ex) {
            Tampilan.alert(AlertType.ERROR, "Kesalahan Database", "Gagal menyimpan: " + ex.getMessage());
        }
        return false;
    }

    // =====================================================
    // MODAL: UPDATE ODOMETER
    // =====================================================
    private void bukaUpdateOdometer(Kendaraan k) {
        Label info = new Label(k.getMerk() + "  •  " + k.getPlatNomor() + "\nOdometer saat ini: "
                + Format.angka(k.getOdometer()) + " km");
        info.getStyleClass().add("muted");
        TextField txtOdometer = new TextField(String.valueOf(k.getOdometer()));
        txtOdometer.setPromptText("Odometer baru (km)");

        Button btnBatal = new Button("BATAL");
        btnBatal.getStyleClass().add("btn-outline");
        Button btnSimpan = new Button("SIMPAN ODOMETER");
        btnSimpan.getStyleClass().add("btn-primary");

        VBox form = new VBox(8, info, Tampilan.labelField("Odometer Baru (km)"), txtOdometer);
        VBox isi = Tampilan.kerangkaModal("Update Odometer", null, form, btnBatal, btnSimpan);
        isi.setPrefWidth(400);

        Stage modal = Tampilan.modal(root.getScene().getWindow(), "Update Odometer", isi);
        btnBatal.setOnAction(e -> modal.close());
        btnSimpan.setOnAction(e -> {
            int baru;
            try {
                baru = Integer.parseInt(txtOdometer.getText().trim());
            } catch (NumberFormatException ex) {
                Tampilan.alert(AlertType.ERROR, "Kesalahan Format", "Odometer harus berupa angka bulat!");
                return;
            }
            try {
                // Validasi bisnis di model: odometer tidak boleh lebih kecil dari sebelumnya
                Kendaraan cek = new Kendaraan(k.getId(), k.getPlatNomor(), k.getMerk(), k.getPemilik(), k.getOdometer());
                cek.setOdometer(baru);
                kendaraanDAO.perbaruiOdometer(pengguna.getId(), k.getId(), baru);
                modal.close();
                segarkan();
                onBerubah.run();
            } catch (IllegalArgumentException ex) {
                Tampilan.alert(AlertType.WARNING, "Peringatan", ex.getMessage());
            } catch (SQLException ex) {
                Tampilan.alert(AlertType.ERROR, "Kesalahan Database", "Gagal menyimpan: " + ex.getMessage());
            }
        });
        modal.showAndWait();
    }
}
