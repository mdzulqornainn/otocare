package org.otocare.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.otocare.dao.RiwayatDAO;
import org.otocare.model.LaporanBulanan;
import org.otocare.model.Pengguna;
import org.otocare.model.RiwayatServis;
import org.otocare.util.Format;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Laporan pengeluaran servis per bulan, detail per bulan, dan ekspor ke file teks. */
public class HalamanLaporan implements Halaman {
    private static final String FILE_LAPORAN = "laporan_pengeluaran_bulanan.txt";
    private static final double LEBAR_BAR = 330;

    private final Pengguna pengguna;
    private final RiwayatDAO riwayatDAO = new RiwayatDAO();

    private List<LaporanBulanan> daftarLaporan = new ArrayList<>();
    private List<RiwayatServis> semuaRiwayat = new ArrayList<>();

    private final VBox root = new VBox(24);
    private final Label lblTotal = new Label("Rp 0");
    private final Label lblRataRata = new Label("Rp 0");
    private final VBox ringkasanBox = new VBox(14);
    private final ComboBox<LaporanBulanan> cbBulan = new ComboBox<>();
    private final Label lblTotalBulan = new Label();
    private final VBox detailBox = new VBox(8);

    public HalamanLaporan(Pengguna pengguna) {
        this.pengguna = pengguna;
        bangunTampilan();
    }

    @Override
    public Node getTampilan() { return root; }

    @Override
    public void segarkan() {
        try {
            semuaRiwayat = riwayatDAO.daftar(pengguna.getId());
            daftarLaporan = riwayatDAO.laporanBulanan(pengguna.getId());
        } catch (SQLException ex) {
            Tampilan.alert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat laporan:\n" + ex.getMessage());
            return;
        }

        double total = 0;
        for (LaporanBulanan l : daftarLaporan) {
            total += l.getTotalBiaya();
        }
        lblTotal.setText(Format.rupiah(total));
        lblRataRata.setText(Format.rupiah(daftarLaporan.isEmpty() ? 0 : total / daftarLaporan.size()));

        tampilkanRingkasan();
        cbBulan.getItems().setAll(daftarLaporan);
        if (daftarLaporan.isEmpty()) {
            tampilkanDetail(null);
        } else {
            cbBulan.getSelectionModel().selectFirst();
        }
    }

    private void bangunTampilan() {
        Button btnEkspor = new Button("EKSPOR KE TXT");
        btnEkspor.getStyleClass().add("btn-primary");
        btnEkspor.setOnAction(e -> eksporKeTeks());

        HBox barisAtas = Tampilan.headerHalaman("Laporan Pengeluaran Bulanan",
                "Total biaya servis kendaraan Anda dikelompokkan per bulan", btnEkspor);

        Label ketTotal = new Label("Akumulasi seluruh servis tercatat");
        Label ketRata = new Label("Dihitung dari bulan yang memiliki servis");
        GridPane kartuStat = new GridPane();
        kartuStat.setHgap(16);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints kolom = new ColumnConstraints();
            kolom.setPercentWidth(33.33);
            kartuStat.getColumnConstraints().add(kolom);
        }
        kartuStat.add(Tampilan.kartuKpi("Total Seluruh Pengeluaran", lblTotal, ketTotal, "kpi-biru"), 0, 0);
        kartuStat.add(Tampilan.kartuKpi("Rata-rata per Bulan", lblRataRata, ketRata, "kpi-mint"), 1, 0);

        // Kiri: ringkasan per bulan
        ScrollPane scrollRingkasan = new ScrollPane(ringkasanBox);
        scrollRingkasan.setFitToWidth(true);
        VBox.setVgrow(scrollRingkasan, Priority.ALWAYS);
        VBox kartuRingkasan = new VBox(12, Tampilan.judulSeksi("Ringkasan per Bulan"), scrollRingkasan);
        kartuRingkasan.getStyleClass().add("card");
        ringkasanBox.setPadding(new javafx.geometry.Insets(0, 8, 0, 0));
        kartuRingkasan.setPrefWidth(430);
        kartuRingkasan.setMinWidth(430);

        // Kanan: detail bulan terpilih
        cbBulan.setPromptText("Pilih bulan");
        lblTotalBulan.getStyleClass().add("biaya");
        HBox barisPilih = new HBox(12, cbBulan, Tampilan.spacer(), lblTotalBulan);
        barisPilih.setAlignment(Pos.CENTER_LEFT);
        ScrollPane scrollDetail = new ScrollPane(detailBox);
        scrollDetail.setFitToWidth(true);
        VBox.setVgrow(scrollDetail, Priority.ALWAYS);
        VBox kartuDetail = new VBox(12, Tampilan.judulSeksi("Detail Bulan"), barisPilih, scrollDetail);
        kartuDetail.getStyleClass().add("card");
        HBox.setHgrow(kartuDetail, Priority.ALWAYS);

        HBox isi = new HBox(20, kartuRingkasan, kartuDetail);
        VBox.setVgrow(isi, Priority.ALWAYS);

        root.getChildren().addAll(barisAtas, kartuStat, isi);

        cbBulan.valueProperty().addListener((obs, lama, baru) -> tampilkanDetail(baru));
    }

    private void tampilkanRingkasan() {
        ringkasanBox.getChildren().clear();
        if (daftarLaporan.isEmpty()) {
            ringkasanBox.getChildren().add(Tampilan.stateKosong("Belum ada data pengeluaran.\nCatat servis di menu Riwayat Servis."));
            return;
        }
        double maks = 0;
        for (LaporanBulanan l : daftarLaporan) {
            maks = Math.max(maks, l.getTotalBiaya());
        }
        for (LaporanBulanan l : daftarLaporan) {
            Label lblBulan = new Label(Format.bulan(l.getBulan()));
            lblBulan.getStyleClass().add("cell-title");
            Label lblNilai = new Label(Format.rupiah(l.getTotalBiaya()));
            lblNilai.getStyleClass().add("biaya");
            HBox atas = new HBox(10, lblBulan, Tampilan.spacer(), lblNilai);
            atas.setAlignment(Pos.CENTER_LEFT);

            // Bar proporsional terhadap bulan dengan pengeluaran terbesar
            Region isiBar = new Region();
            isiBar.getStyleClass().add("bar-fill");
            double lebar = maks <= 0 ? 4 : Math.max(4, LEBAR_BAR * l.getTotalBiaya() / maks);
            isiBar.setMinWidth(lebar);
            isiBar.setPrefWidth(lebar);
            isiBar.setMaxWidth(lebar);
            isiBar.setMinHeight(10);
            HBox jalur = new HBox(isiBar);
            jalur.getStyleClass().add("bar-track");

            Label lblJumlah = new Label(l.getJumlahServis() + " kali servis");
            lblJumlah.getStyleClass().add("muted");
            ringkasanBox.getChildren().add(new VBox(4, atas, jalur, lblJumlah));
        }
    }

    private List<RiwayatServis> riwayatBulan(String bulan) {
        List<RiwayatServis> hasil = new ArrayList<>();
        for (RiwayatServis r : semuaRiwayat) {
            if (r.getTanggal().toString().startsWith(bulan)) {
                hasil.add(r);
            }
        }
        return hasil;
    }

    private void tampilkanDetail(LaporanBulanan laporan) {
        detailBox.getChildren().clear();
        if (laporan == null) {
            lblTotalBulan.setText("");
            detailBox.getChildren().add(Tampilan.stateKosong("Pilih bulan untuk melihat detail."));
            return;
        }
        lblTotalBulan.setText(Format.rupiah(laporan.getTotalBiaya()));

        List<RiwayatServis> daftar = riwayatBulan(laporan.getBulan());
        Map<String, Double> perKategori = new LinkedHashMap<>();
        for (RiwayatServis r : daftar) {
            perKategori.merge(r.getNamaKategori(), r.getBiaya(), Double::sum);
        }

        detailBox.getChildren().add(Tampilan.labelField("PER KATEGORI"));
        for (Map.Entry<String, Double> e : perKategori.entrySet()) {
            Label nama = new Label(e.getKey());
            Label nilai = new Label(Format.rupiah(e.getValue()));
            nilai.getStyleClass().add("muted");
            detailBox.getChildren().add(new HBox(10, nama, Tampilan.spacer(), nilai));
        }

        Label rincian = Tampilan.labelField("RINCIAN SERVIS");
        VBox.setMargin(rincian, new javafx.geometry.Insets(10, 0, 0, 0));
        detailBox.getChildren().add(rincian);
        for (RiwayatServis r : daftar) {
            Label lblNama = new Label(r.getNamaKategori() + "  •  " + r.getPlatNomor());
            lblNama.getStyleClass().add("cell-title");
            Label lblInfo = new Label(Format.tanggal(r.getTanggal())
                    + (r.getNamaBengkel() == null || r.getNamaBengkel().isBlank() ? "" : "  |  " + r.getNamaBengkel()));
            lblInfo.getStyleClass().add("muted");
            Label lblBiaya = new Label(Format.rupiah(r.getBiaya()));
            lblBiaya.getStyleClass().add("biaya");
            HBox baris = new HBox(10, new VBox(2, lblNama, lblInfo), Tampilan.spacer(), lblBiaya);
            baris.getStyleClass().add("baris-detail");
            detailBox.getChildren().add(baris);
        }
    }

    // File teks dengan FileWriter (Pertemuan 11-2)
    private void eksporKeTeks() {
        if (daftarLaporan.isEmpty()) {
            Tampilan.alert(AlertType.WARNING, "Peringatan", "Belum ada data pengeluaran untuk diekspor.");
            return;
        }
        try (FileWriter penulis = new FileWriter(FILE_LAPORAN)) {
            penulis.write("LAPORAN PENGELUARAN SERVIS BULANAN - OtoCare\n");
            penulis.write("Pemilik: " + pengguna.getNamaLengkap() + "\n");
            penulis.write("==============================================\n\n");

            double total = 0;
            for (LaporanBulanan l : daftarLaporan) {
                penulis.write(Format.bulan(l.getBulan()) + "  (" + l.getJumlahServis() + " servis)  Total: "
                        + Format.rupiah(l.getTotalBiaya()) + "\n");
                for (RiwayatServis r : riwayatBulan(l.getBulan())) {
                    penulis.write("  - " + Format.tanggal(r.getTanggal()) + " | " + r.getPlatNomor() + " | "
                            + r.getNamaKategori() + " | " + Format.rupiah(r.getBiaya()) + "\n");
                }
                penulis.write("\n");
                total += l.getTotalBiaya();
            }
            penulis.write("TOTAL KESELURUHAN: " + Format.rupiah(total) + "\n");
            Tampilan.alert(AlertType.INFORMATION, "Berhasil",
                    "Laporan diekspor ke file:\n" + new File(FILE_LAPORAN).getAbsolutePath());
        } catch (IOException ex) {
            Tampilan.alert(AlertType.ERROR, "Kesalahan File", "Gagal mengekspor: " + ex.getMessage());
        }
    }
}
