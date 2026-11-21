package org.otocare.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.otocare.dao.KendaraanDAO;
import org.otocare.dao.PengingatDAO;
import org.otocare.dao.RiwayatDAO;
import org.otocare.model.JadwalPengingat;
import org.otocare.model.Kendaraan;
import org.otocare.model.Pengguna;
import org.otocare.model.StatusPengingat;
import org.otocare.util.Format;

import java.sql.SQLException;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/** Dashboard Pengingat Servis: 3 kartu KPI + tabel prioritas pengingat (sesuai desain). */
public class HalamanDashboard implements Halaman {
    private final Pengguna pengguna;
    private final Runnable catatBaru;
    private final Consumer<JadwalPengingat> servisSekarang;
    private final Runnable keHalamanPengingat;

    private final KendaraanDAO kendaraanDAO = new KendaraanDAO();
    private final RiwayatDAO riwayatDAO = new RiwayatDAO();
    private final PengingatDAO pengingatDAO = new PengingatDAO();

    private final VBox root = new VBox(24);
    private final Label nilaiKendaraan = new Label("0 Unit");
    private final Label ketKendaraan = new Label();
    private final Label nilaiPerhatian = new Label("0 Jadwal");
    private final Label ketPerhatian = new Label();
    private final Label nilaiBulanIni = new Label("Rp 0");
    private final Label ketBulanIni = new Label("Total biaya servis bulan berjalan");
    private final Label lblJumlahJadwal = new Label("0 jadwal aktif");
    private final VBox tabelBox = new VBox();

    public HalamanDashboard(Pengguna pengguna, Runnable catatBaru,
                            Consumer<JadwalPengingat> servisSekarang, Runnable keHalamanPengingat) {
        this.pengguna = pengguna;
        this.catatBaru = catatBaru;
        this.servisSekarang = servisSekarang;
        this.keHalamanPengingat = keHalamanPengingat;

        Button btnCatat = new Button("+  CATAT SERVIS BARU");
        btnCatat.getStyleClass().add("btn-primary");
        btnCatat.setOnAction(e -> this.catatBaru.run());

        // 3 kartu KPI dengan lebar sama
        GridPane kpi = new GridPane();
        kpi.setHgap(16);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints kolom = new ColumnConstraints();
            kolom.setPercentWidth(33.33);
            kpi.getColumnConstraints().add(kolom);
        }
        kpi.add(Tampilan.kartuKpi("Total Kendaraan", nilaiKendaraan, ketKendaraan, "kpi-biru"), 0, 0);
        kpi.add(Tampilan.kartuKpi("Perlu Perhatian", nilaiPerhatian, ketPerhatian, "kpi-koral"), 1, 0);
        kpi.add(Tampilan.kartuKpi("Pengeluaran Bulan Ini", nilaiBulanIni, ketBulanIni, "kpi-mint"), 2, 0);

        // Panel tabel prioritas
        Label judul = new Label("PRIORITAS PENGINGAT SERVIS");
        judul.getStyleClass().add("panel-title");
        Label sub = new Label("Diurutkan berdasarkan urgensi jarak tempuh dan batas tanggal servis");
        sub.getStyleClass().add("muted-sm");
        lblJumlahJadwal.getStyleClass().addAll("badge", "badge-count");
        HBox kepalaPanel = new HBox(12, new VBox(2, judul, sub), Tampilan.spacer(), lblJumlahJadwal);
        kepalaPanel.setAlignment(Pos.CENTER_LEFT);
        kepalaPanel.getStyleClass().add("panel-head");

        ScrollPane scroll = new ScrollPane(tabelBox);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox panel = new VBox(kepalaPanel, scroll);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(panel, Priority.ALWAYS);

        root.getChildren().addAll(
                Tampilan.headerHalaman("Dashboard Pengingat Servis",
                        "Pantau jadwal perawatan berkala mobil dan motor Anda", btnCatat),
                kpi, panel);
    }

    @Override
    public Node getTampilan() { return root; }

    @Override
    public void segarkan() {
        try {
            int id = pengguna.getId();

            List<Kendaraan> kendaraan = kendaraanDAO.semua(id);
            int mobil = 0;
            for (Kendaraan k : kendaraan) {
                if (k.getTipe().equals("Mobil")) {
                    mobil++;
                }
            }
            nilaiKendaraan.setText(kendaraan.size() + " UNIT");
            ketKendaraan.setText((kendaraan.size() - mobil) + " Motor, " + mobil + " Mobil");

            nilaiBulanIni.setText(Format.rupiah(riwayatDAO.pengeluaranBulan(id, YearMonth.now().toString())));

            List<JadwalPengingat> jadwal = pengingatDAO.semua(id);
            int jatuhTempo = 0;
            int segera = 0;
            for (JadwalPengingat j : jadwal) {
                StatusPengingat status = j.cekStatus();
                if (status == StatusPengingat.JATUH_TEMPO) {
                    jatuhTempo++;
                } else if (status == StatusPengingat.SEGERA_SERVIS) {
                    segera++;
                }
            }
            nilaiPerhatian.setText((jatuhTempo + segera) + " JADWAL");
            ketPerhatian.setText(jatuhTempo + " Jatuh Tempo, " + segera + " Segera Servis");
            lblJumlahJadwal.setText(jadwal.size() + " JADWAL AKTIF");

            tabelBox.getChildren().clear();
            if (jadwal.isEmpty()) {
                tabelBox.getChildren().add(Tampilan.stateKosong(
                        "Belum ada jadwal pengingat.\nAtur lewat menu Riwayat Servis atau Pengingat."));
            } else {
                Function<JadwalPengingat, Node> aksi = j -> {
                    if (j.cekStatus() != StatusPengingat.AMAN) {
                        Button b = new Button("SERVIS SEKARANG");
                        b.getStyleClass().addAll("btn-primary", "btn-small");
                        b.setOnAction(e -> servisSekarang.accept(j));
                        return b;
                    }
                    Button b = new Button("DETAIL");
                    b.getStyleClass().addAll("btn-outline", "btn-small");
                    b.setOnAction(e -> keHalamanPengingat.run());
                    return b;
                };
                tabelBox.getChildren().add(Tampilan.tabelPengingat(jadwal, aksi));
            }
        } catch (SQLException ex) {
            Tampilan.alert(AlertType.ERROR, "Kesalahan Database", "Gagal memuat dashboard:\n" + ex.getMessage());
        }
    }
}
