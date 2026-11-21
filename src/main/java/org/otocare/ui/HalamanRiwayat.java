package org.otocare.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.otocare.dao.KendaraanDAO;
import org.otocare.dao.RiwayatDAO;
import org.otocare.model.JadwalPengingat;
import org.otocare.model.KategoriServis;
import org.otocare.model.Kendaraan;
import org.otocare.model.Pengguna;
import org.otocare.model.RiwayatServis;
import org.otocare.util.Format;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Riwayat Servis: tabel catatan + modal "Catat Servis" (sekaligus atur pengingat berikutnya). */
public class HalamanRiwayat implements Halaman {
    private final Pengguna pengguna;
    private final Runnable onBerubah;
    private final KendaraanDAO kendaraanDAO = new KendaraanDAO();
    private final RiwayatDAO riwayatDAO = new RiwayatDAO();

    private final VBox root = new VBox(24);
    private final Label lblJumlah = new Label("0 catatan");
    private final VBox tabelBox = new VBox();

    public HalamanRiwayat(Pengguna pengguna, Runnable onBerubah) {
        this.pengguna = pengguna;
        this.onBerubah = onBerubah;

        Button btnCatat = new Button("+  CATAT SERVIS BARU");
        btnCatat.getStyleClass().add("btn-primary");
        btnCatat.setOnAction(e -> bukaForm(null, null));

        Label judul = new Label("CATATAN SERVIS TERBARU");
        judul.getStyleClass().add("panel-title");
        lblJumlah.getStyleClass().addAll("badge", "badge-count");
        HBox kepalaPanel = new HBox(12, judul, Tampilan.spacer(), lblJumlah);
        kepalaPanel.setAlignment(Pos.CENTER_LEFT);
        kepalaPanel.getStyleClass().add("panel-head");

        ScrollPane scroll = new ScrollPane(tabelBox);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox panel = new VBox(kepalaPanel, scroll);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(panel, Priority.ALWAYS);

        root.getChildren().addAll(
                Tampilan.headerHalaman("Riwayat Servis",
                        "Catatan perawatan kendaraan beserta biaya dan bengkel", btnCatat),
                panel);
    }

    @Override
    public Node getTampilan() { return root; }

    @Override
    public void segarkan() {
        try {
            tampilkanTabel(riwayatDAO.daftar(pengguna.getId()));
        } catch (SQLException ex) {
            Tampilan.alert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat riwayat servis:\n" + ex.getMessage());
        }
    }

    /** Dari dashboard: buka modal; jika j tidak null, kendaraan & kategori langsung terisi. */
    public void catatDariPengingat(JadwalPengingat j) {
        if (j == null) {
            bukaForm(null, null);
        } else {
            bukaForm(j.getIdKendaraan(), j.getIdKategori());
        }
    }

    private void tampilkanTabel(List<RiwayatServis> daftar) {
        tabelBox.getChildren().clear();
        double total = 0;
        for (RiwayatServis r : daftar) {
            total += r.getBiaya();
        }
        lblJumlah.setText("TOTAL: " + Format.rupiah(total));
        if (daftar.isEmpty()) {
            tabelBox.getChildren().add(Tampilan.stateKosong(
                    "Belum ada riwayat servis.\nKlik \"+ Catat Servis Baru\" untuk mencatat servis pertama."));
            return;
        }
        Tabel tabel = new Tabel(13, 23, 20, 12, 13, 12, 7);
        tabel.header("Tanggal", "Kendaraan", "Kategori Servis", "Odometer", "Bengkel", "Biaya", "Aksi");
        for (RiwayatServis r : daftar) {
            Label lblNama = new Label(r.getNamaKendaraan());
            lblNama.getStyleClass().add("cell-title");
            VBox selKendaraan = new VBox(2, lblNama, Tampilan.mono(r.getPlatNomor()));

            Label lblKategori = new Label(r.getNamaKategori());
            lblKategori.setWrapText(true);
            Label lblBengkel = new Label(r.getNamaBengkel() == null || r.getNamaBengkel().isBlank()
                    ? "-" : r.getNamaBengkel());
            lblBengkel.setWrapText(true);
            Label lblBiaya = new Label(Format.rupiah(r.getBiaya()));
            lblBiaya.getStyleClass().add("biaya");

            Button btnHapus = new Button("Hapus");
            btnHapus.getStyleClass().addAll("btn-link-danger", "btn-small");
            btnHapus.setOnAction(e -> {
                if (!Tampilan.konfirmasi("Konfirmasi Hapus",
                        "Hapus catatan servis \"" + r.getNamaKategori() + "\" ini?")) {
                    return;
                }
                try {
                    riwayatDAO.hapus(pengguna.getId(), r.getId());
                    segarkan();
                    onBerubah.run();
                } catch (SQLException ex) {
                    Tampilan.alert(AlertType.ERROR, "Kesalahan Database", "Gagal menghapus: " + ex.getMessage());
                }
            });

            tabel.baris(new Label(Format.tanggal(r.getTanggal())), selKendaraan, lblKategori,
                    Tampilan.mono(Format.angka(r.getOdometer()) + " km"), lblBengkel, lblBiaya, btnHapus);
        }
        tabelBox.getChildren().add(tabel.getNode());
    }

    // =====================================================
    // MODAL: CATAT SERVIS
    // =====================================================
    private void bukaForm(Integer idKendaraanAwal, Integer idKategoriAwal) {
        List<Kendaraan> kendaraan;
        List<KategoriServis> kategori;
        try {
            kendaraan = kendaraanDAO.semua(pengguna.getId());
            kategori = riwayatDAO.daftarKategori();
        } catch (SQLException ex) {
            Tampilan.alert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat data:\n" + ex.getMessage());
            return;
        }
        if (kendaraan.isEmpty()) {
            Tampilan.alert(AlertType.WARNING, "Peringatan", "Tambahkan kendaraan dulu di menu Kendaraan.");
            return;
        }

        ComboBox<Kendaraan> cbKendaraan = new ComboBox<>();
        cbKendaraan.getItems().setAll(kendaraan);
        cbKendaraan.setPromptText("Pilih kendaraan");
        cbKendaraan.setMaxWidth(Double.MAX_VALUE);
        ComboBox<KategoriServis> cbKategori = new ComboBox<>();
        cbKategori.getItems().setAll(kategori);
        cbKategori.setPromptText("Pilih kategori");
        cbKategori.setMaxWidth(Double.MAX_VALUE);
        DatePicker dpTanggal = new DatePicker(LocalDate.now());
        dpTanggal.setMaxWidth(Double.MAX_VALUE);
        TextField txtOdometer = new TextField();
        txtOdometer.setPromptText("Odometer saat servis (km)");
        TextField txtBengkel = new TextField();
        txtBengkel.setPromptText("Nama bengkel");
        TextField txtBiaya = new TextField();
        txtBiaya.setPromptText("Contoh: 150000");
        TextField txtCatatan = new TextField();
        txtCatatan.setPromptText("Catatan (opsional)");
        CheckBox chkPengingat = new CheckBox("Atur pengingat servis berikutnya");
        TextField txtTargetKm = new TextField();
        txtTargetKm.setPromptText("Target odometer (km)");
        DatePicker dpTarget = new DatePicker();
        dpTarget.setPromptText("Target tanggal");
        dpTarget.setMaxWidth(Double.MAX_VALUE);

        // Field target pengingat hanya aktif jika CheckBox dicentang
        txtTargetKm.disableProperty().bind(chkPengingat.selectedProperty().not());
        dpTarget.disableProperty().bind(chkPengingat.selectedProperty().not());

        // Prefill dari tombol "Servis Sekarang"
        if (idKendaraanAwal != null) {
            for (Kendaraan k : kendaraan) {
                if (k.getId() == idKendaraanAwal) {
                    cbKendaraan.setValue(k);
                    txtOdometer.setText(String.valueOf(k.getOdometer()));
                }
            }
        }
        if (idKategoriAwal != null) {
            for (KategoriServis ks : kategori) {
                if (ks.getId() == idKategoriAwal) {
                    cbKategori.setValue(ks);
                }
            }
        }

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(6);
        for (int i = 0; i < 2; i++) {
            ColumnConstraints kolom = new ColumnConstraints();
            kolom.setPercentWidth(50);
            grid.getColumnConstraints().add(kolom);
        }
        grid.add(Tampilan.labelField("Kendaraan"), 0, 0);
        grid.add(cbKendaraan, 0, 1);
        grid.add(Tampilan.labelField("Tanggal Servis"), 1, 0);
        grid.add(dpTanggal, 1, 1);
        grid.add(Tampilan.labelField("Kategori Servis"), 0, 2);
        grid.add(cbKategori, 0, 3);
        grid.add(Tampilan.labelField("Odometer (km)"), 1, 2);
        grid.add(txtOdometer, 1, 3);
        grid.add(Tampilan.labelField("Bengkel"), 0, 4);
        grid.add(txtBengkel, 0, 5);
        grid.add(Tampilan.labelField("Biaya (Rp)"), 1, 4);
        grid.add(txtBiaya, 1, 5);
        grid.add(Tampilan.labelField("Catatan"), 0, 6, 2, 1);
        grid.add(txtCatatan, 0, 7, 2, 1);
        grid.add(chkPengingat, 0, 8, 2, 1);
        grid.add(Tampilan.labelField("Target KM"), 0, 9);
        grid.add(txtTargetKm, 0, 10);
        grid.add(Tampilan.labelField("Target Tanggal"), 1, 9);
        grid.add(dpTarget, 1, 10);
        GridPane.setMargin(chkPengingat, new Insets(10, 0, 4, 0));

        Button btnBatal = new Button("BATAL");
        btnBatal.getStyleClass().add("btn-outline");
        Button btnSimpan = new Button("SIMPAN RIWAYAT & PENGINGAT");
        btnSimpan.getStyleClass().add("btn-primary");

        VBox isi = Tampilan.kerangkaModal("Catat Servis & Atur Pengingat",
                "Catat servis yang sudah dilakukan dan atur pengingat berikutnya.", grid, btnBatal, btnSimpan);
        isi.setPrefWidth(600);

        Stage modal = Tampilan.modal(root.getScene().getWindow(), "Catat Servis", isi);
        btnBatal.setOnAction(e -> modal.close());
        btnSimpan.setOnAction(e -> {
            if (simpan(cbKendaraan.getValue(), cbKategori.getValue(), dpTanggal.getValue(),
                    txtOdometer.getText().trim(), txtBiaya.getText().trim(), txtBengkel.getText(),
                    txtCatatan.getText(), chkPengingat.isSelected(), txtTargetKm.getText().trim(),
                    dpTarget.getValue())) {
                modal.close();
                segarkan();
                onBerubah.run();
            }
        });
        modal.showAndWait();
    }

    /** Validasi + simpan. true jika berhasil. */
    private boolean simpan(Kendaraan k, KategoriServis kategori, LocalDate tanggal, String strOdometer,
                           String strBiaya, String bengkel, String catatan, boolean aturPengingat,
                           String strTarget, LocalDate targetTanggal) {
        // A. Validasi kolom wajib
        if (k == null || kategori == null || tanggal == null || strOdometer.isEmpty() || strBiaya.isEmpty()) {
            Tampilan.alert(AlertType.WARNING, "Peringatan",
                    "Kendaraan, tanggal, kategori, odometer, dan biaya wajib diisi!");
            return false;
        }

        // B. Konversi angka + error handling
        int odometer;
        double biaya;
        try {
            odometer = Integer.parseInt(strOdometer);
            biaya = Double.parseDouble(strBiaya.replace(".", "").replace(",", "."));
        } catch (NumberFormatException ex) {
            Tampilan.alert(AlertType.ERROR, "Kesalahan Format", "Odometer dan biaya harus berupa angka!");
            return false;
        }

        // C. Validasi target pengingat (jika diaktifkan)
        Integer targetKm = null;
        LocalDate tglTarget = null;
        if (aturPengingat) {
            try {
                if (!strTarget.isEmpty()) {
                    targetKm = Integer.parseInt(strTarget);
                }
            } catch (NumberFormatException ex) {
                Tampilan.alert(AlertType.ERROR, "Kesalahan Format", "Target KM harus berupa angka bulat!");
                return false;
            }
            tglTarget = targetTanggal;
            if (targetKm == null && tglTarget == null) {
                Tampilan.alert(AlertType.WARNING, "Peringatan", "Isi target KM atau target tanggal untuk pengingat!");
                return false;
            }
            if (targetKm != null && targetKm <= odometer) {
                Tampilan.alert(AlertType.WARNING, "Peringatan", "Target KM harus lebih besar dari odometer servis!");
                return false;
            }
            if (tglTarget != null && !tglTarget.isAfter(tanggal)) {
                Tampilan.alert(AlertType.WARNING, "Peringatan", "Target tanggal harus setelah tanggal servis!");
                return false;
            }
        }

        try {
            // D. Validasi bisnis di model: odometer tidak boleh lebih kecil dari odometer kendaraan sekarang
            Kendaraan cek = new Kendaraan(k.getId(), k.getPlatNomor(), k.getMerk(), k.getPemilik(), k.getOdometer());
            cek.setOdometer(odometer);

            RiwayatServis riwayat = new RiwayatServis(0, k.getId(), k.getPlatNomor(), k.getMerk(), kategori.getId(),
                    kategori.getNama(), tanggal, odometer, biaya, bengkel, catatan);

            // E. Simpan (satu transaksi: riwayat + odometer kendaraan + pengingat)
            riwayatDAO.simpan(riwayat, targetKm, tglTarget);

            Tampilan.alert(AlertType.INFORMATION, "Berhasil", "Riwayat servis berhasil disimpan!"
                    + (aturPengingat ? "\nPengingat servis berikutnya sudah diatur." : ""));
            return true;
        } catch (IllegalArgumentException ex) {
            Tampilan.alert(AlertType.WARNING, "Peringatan", ex.getMessage());
        } catch (SQLException ex) {
            Tampilan.alert(AlertType.ERROR, "Kesalahan Database", "Gagal menyimpan: " + ex.getMessage());
        }
        return false;
    }
}
