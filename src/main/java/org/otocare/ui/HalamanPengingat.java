package org.otocare.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.otocare.dao.KendaraanDAO;
import org.otocare.dao.PengingatDAO;
import org.otocare.dao.RiwayatDAO;
import org.otocare.model.JadwalPengingat;
import org.otocare.model.KategoriServis;
import org.otocare.model.Kendaraan;
import org.otocare.model.Pengguna;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Mengatur dan memantau pengingat servis (tabel + modal "Atur Pengingat"). */
public class HalamanPengingat implements Halaman {
    private final Pengguna pengguna;
    private final Runnable onBerubah;
    private final KendaraanDAO kendaraanDAO = new KendaraanDAO();
    private final RiwayatDAO riwayatDAO = new RiwayatDAO();
    private final PengingatDAO pengingatDAO = new PengingatDAO();

    private final VBox root = new VBox(24);
    private final Label lblJumlah = new Label("0 jadwal");
    private final VBox tabelBox = new VBox();

    public HalamanPengingat(Pengguna pengguna, Runnable onBerubah) {
        this.pengguna = pengguna;
        this.onBerubah = onBerubah;

        Button btnAtur = new Button("+  ATUR PENGINGAT");
        btnAtur.getStyleClass().add("btn-primary");
        btnAtur.setOnAction(e -> bukaForm());

        Label judul = new Label("JADWAL PENGINGAT SERVIS");
        judul.getStyleClass().add("panel-title");
        Label sub = new Label("Aman: hijau  |  Segera Servis: sisa < 500 km atau H-7  |  Jatuh Tempo: melewati target");
        sub.getStyleClass().add("muted-sm");
        lblJumlah.getStyleClass().addAll("badge", "badge-count");
        HBox kepalaPanel = new HBox(12, new VBox(2, judul, sub), Tampilan.spacer(), lblJumlah);
        kepalaPanel.setAlignment(Pos.CENTER_LEFT);
        kepalaPanel.getStyleClass().add("panel-head");

        ScrollPane scroll = new ScrollPane(tabelBox);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox panel = new VBox(kepalaPanel, scroll);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(panel, Priority.ALWAYS);

        root.getChildren().addAll(
                Tampilan.headerHalaman("Pengingat Servis",
                        "Atur target kilometer dan tanggal servis berikutnya", btnAtur),
                panel);
    }

    @Override
    public Node getTampilan() { return root; }

    @Override
    public void segarkan() {
        try {
            tampilkanTabel(pengingatDAO.semua(pengguna.getId()));
        } catch (SQLException ex) {
            Tampilan.alert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat pengingat:\n" + ex.getMessage());
        }
    }

    private void tampilkanTabel(List<JadwalPengingat> daftar) {
        tabelBox.getChildren().clear();
        lblJumlah.setText(daftar.size() + " jadwal");
        if (daftar.isEmpty()) {
            tabelBox.getChildren().add(Tampilan.stateKosong("Belum ada jadwal pengingat."));
            return;
        }
        tabelBox.getChildren().add(Tampilan.tabelPengingat(daftar, j -> {
            Button btnHapus = new Button("Hapus");
            btnHapus.getStyleClass().addAll("btn-link-danger", "btn-small");
            btnHapus.setOnAction(e -> {
                if (!Tampilan.konfirmasi("Konfirmasi Hapus", "Hapus pengingat \"" + j.getNamaKategori()
                        + "\" untuk " + j.getPlatNomor() + " ?")) {
                    return;
                }
                try {
                    pengingatDAO.hapus(pengguna.getId(), j.getIdJadwal());
                    segarkan();
                    onBerubah.run();
                } catch (SQLException ex) {
                    Tampilan.alert(AlertType.ERROR, "Kesalahan Database", "Gagal menghapus: " + ex.getMessage());
                }
            });
            return btnHapus;
        }));
    }

    // =====================================================
    // MODAL: ATUR PENGINGAT
    // =====================================================
    private void bukaForm() {
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
        cbKategori.setPromptText("Pilih kategori servis");
        cbKategori.setMaxWidth(Double.MAX_VALUE);
        TextField txtTargetKm = new TextField();
        txtTargetKm.setPromptText("Target odometer (km)");
        DatePicker dpTarget = new DatePicker();
        dpTarget.setPromptText("Target tanggal");
        dpTarget.setMaxWidth(Double.MAX_VALUE);

        VBox form = new VBox(6,
                Tampilan.labelField("Kendaraan"), cbKendaraan,
                Tampilan.labelField("Kategori Servis"), cbKategori,
                Tampilan.labelField("Target KM"), txtTargetKm,
                Tampilan.labelField("Target Tanggal"), dpTarget);

        Button btnBatal = new Button("BATAL");
        btnBatal.getStyleClass().add("btn-outline");
        Button btnSimpan = new Button("SIMPAN PENGINGAT");
        btnSimpan.getStyleClass().add("btn-primary");

        VBox isi = Tampilan.kerangkaModal("Atur Pengingat Servis",
                "Isi target KM, target tanggal, atau keduanya.", form, btnBatal, btnSimpan);
        isi.setPrefWidth(420);

        Stage modal = Tampilan.modal(root.getScene().getWindow(), "Atur Pengingat", isi);
        btnBatal.setOnAction(e -> modal.close());
        btnSimpan.setOnAction(e -> {
            if (simpan(cbKendaraan.getValue(), cbKategori.getValue(), txtTargetKm.getText().trim(), dpTarget.getValue())) {
                modal.close();
                segarkan();
                onBerubah.run();
            }
        });
        modal.showAndWait();
    }

    private boolean simpan(Kendaraan k, KategoriServis kategori, String strTarget, LocalDate targetTanggal) {
        if (k == null || kategori == null) {
            Tampilan.alert(AlertType.WARNING, "Peringatan", "Pilih kendaraan dan kategori servis!");
            return false;
        }
        Integer targetKm = null;
        try {
            if (!strTarget.isEmpty()) {
                targetKm = Integer.parseInt(strTarget);
            }
        } catch (NumberFormatException ex) {
            Tampilan.alert(AlertType.ERROR, "Kesalahan Format", "Target KM harus berupa angka bulat!");
            return false;
        }
        if (targetKm == null && targetTanggal == null) {
            Tampilan.alert(AlertType.WARNING, "Peringatan", "Isi target KM atau target tanggal!");
            return false;
        }
        if (targetKm != null && targetKm <= k.getOdometer()) {
            Tampilan.alert(AlertType.WARNING, "Peringatan",
                    "Target KM harus lebih besar dari odometer saat ini (" + k.getOdometer() + " km)!");
            return false;
        }
        if (targetTanggal != null && !targetTanggal.isAfter(LocalDate.now())) {
            Tampilan.alert(AlertType.WARNING, "Peringatan", "Target tanggal harus setelah hari ini!");
            return false;
        }
        try {
            pengingatDAO.simpan(k.getId(), kategori.getId(), targetKm, targetTanggal);
            Tampilan.alert(AlertType.INFORMATION, "Berhasil", "Pengingat untuk " + k.getPlatNomor()
                    + " (" + kategori.getNama() + ") berhasil disimpan!");
            return true;
        } catch (SQLException ex) {
            Tampilan.alert(AlertType.ERROR, "Kesalahan Database", "Gagal menyimpan: " + ex.getMessage());
            return false;
        }
    }
}
