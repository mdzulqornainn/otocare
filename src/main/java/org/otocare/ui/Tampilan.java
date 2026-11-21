package org.otocare.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.otocare.model.JadwalPengingat;
import org.otocare.model.StatusPengingat;
import org.otocare.util.Format;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/** Helper tampilan bersama (alert, modal, header halaman, KPI, badge, tabel pengingat). */
public final class Tampilan {
    private static String cssUrl;

    private Tampilan() { }

    public static String css() {
        if (cssUrl == null) {
            cssUrl = Objects.requireNonNull(Tampilan.class.getResource("/css/style.css")).toExternalForm();
        }
        return cssUrl;
    }

    // ---------- Dialog Alert (pola Pertemuan 12) ----------
    public static void alert(AlertType tipe, String judul, String pesan) {
        Alert alert = new Alert(tipe);
        alert.setTitle(judul);
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.getDialogPane().getStylesheets().add(css());
        alert.showAndWait();
    }

    public static boolean konfirmasi(String judul, String pesan) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(judul);
        alert.setHeaderText(null);
        alert.setContentText(pesan);
        alert.getDialogPane().getStylesheets().add(css());
        Optional<ButtonType> hasil = alert.showAndWait();
        return hasil.isPresent() && hasil.get() == ButtonType.OK;
    }

    // ---------- Modal (jendela form di atas halaman, seperti desain "Catat Servis") ----------
    /**
     * Membuat jendela modal berisi form. Tombol/aksi di dalam isi mengatur penutupan lewat stage.close().
     * Panggil showAndWait() setelah semua aksi tombol dipasang.
     */
    public static Stage modal(Window pemilik, String judul, Region isi) {
        Stage stage = new Stage();
        stage.initOwner(pemilik);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(judul);
        stage.setResizable(false);
        Scene scene = new Scene(isi);
        scene.getStylesheets().add(css());
        stage.setScene(scene);
        return stage;
    }

    /** Kerangka isi modal: judul, subjudul opsional, isi, dan baris tombol di bawah. */
    public static VBox kerangkaModal(String judul, String sub, Node isi, Button... tombol) {
        Label lblJudul = new Label(judul.toUpperCase());
        lblJudul.getStyleClass().add("modal-title");
        VBox kepala = new VBox(2, lblJudul);
        if (sub != null) {
            Label lblSub = new Label(sub);
            lblSub.getStyleClass().add("muted");
            kepala.getChildren().add(lblSub);
        }
        kepala.getStyleClass().add("modal-head");

        HBox kaki = new HBox(10);
        kaki.setAlignment(Pos.CENTER_RIGHT);
        kaki.getChildren().addAll(tombol);
        kaki.getStyleClass().add("modal-foot");

        VBox badan = new VBox(isi);
        badan.getStyleClass().add("modal-body");

        VBox kerangka = new VBox(kepala, badan, kaki);
        kerangka.getStyleClass().add("modal");
        return kerangka;
    }

    // ---------- Komponen umum ----------
    public static Label labelField(String teks) {
        Label label = new Label(teks.toUpperCase());
        label.getStyleClass().add("field-label");
        return label;
    }

    public static Label judulSeksi(String teks) {
        Label label = new Label(teks.toUpperCase());
        label.getStyleClass().add("section-title");
        return label;
    }

    public static Label mono(String teks) {
        Label label = new Label(teks);
        label.getStyleClass().add("mono");
        return label;
    }

    public static Region spacer() {
        Region r = new Region();
        HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    /** Header halaman: judul + subjudul di kiri, tombol aksi (boleh null) di kanan. */
    public static HBox headerHalaman(String judul, String sub, Node aksi) {
        Label lblJudul = new Label(judul.toUpperCase());
        lblJudul.getStyleClass().add("page-title");
        Label lblSub = new Label(sub);
        lblSub.getStyleClass().add("muted");

        HBox header = new HBox(12, new VBox(4, lblJudul, lblSub), spacer());
        if (aksi != null) {
            header.getChildren().add(aksi);
        }
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    /** Kartu KPI: judul kecil, nilai besar, keterangan. */
    public static VBox kartuKpi(String judul, Label nilai, Label keterangan, String warna) {
        Label lblJudul = new Label(judul.toUpperCase());
        lblJudul.getStyleClass().add("kpi-label");
        nilai.getStyleClass().add("kpi-value");
        keterangan.getStyleClass().add("muted-sm");
        VBox kartu = new VBox(6, lblJudul, nilai, keterangan);
        kartu.getStyleClass().addAll("kpi-card", warna);
        kartu.setMaxWidth(Double.MAX_VALUE);
        return kartu;
    }

    /** Badge status: Aman (hijau), Segera Servis (kuning), Jatuh Tempo (merah). */
    public static Label badge(StatusPengingat status) {
        Label badge = new Label(status.getLabel().toUpperCase());
        badge.getStyleClass().addAll("badge", status.getStyleClass());
        badge.setAlignment(Pos.CENTER);
        return badge;
    }

    public static Label stateKosong(String pesan) {
        Label kosong = new Label(pesan);
        kosong.getStyleClass().add("empty-state");
        kosong.setAlignment(Pos.CENTER);
        kosong.setMaxWidth(Double.MAX_VALUE);
        return kosong;
    }

    /**
     * Tabel pengingat servis (dipakai Dashboard & halaman Pengingat):
     * Kendaraan & Plat | Kategori | Odometer vs Target KM | Batas Tanggal | Status | Aksi
     */
    public static Node tabelPengingat(List<JadwalPengingat> daftar, Function<JadwalPengingat, Node> aksi) {
        Tabel tabel = new Tabel(22, 19, 20, 16, 12, 11);
        tabel.header("KENDARAAN & PLAT NOMOR", "KATEGORI SERVIS", "ODOMETER VS TARGET KM",
                "BATAS TANGGAL", "STATUS", "AKSI");

        for (JadwalPengingat j : daftar) {
            // Kendaraan & plat
            Label lblNama = new Label(j.getNamaKendaraan());
            lblNama.getStyleClass().add("cell-title");
            VBox selKendaraan = new VBox(2, lblNama, mono(j.getPlatNomor()));

            Label lblKategori = new Label(j.getNamaKategori());
            lblKategori.setWrapText(true);

            // Odometer vs target
            VBox selKm = new VBox(2);
            Integer sisaKm = j.getSisaKm();
            if (sisaKm == null) {
                selKm.getChildren().add(new Label("-"));
            } else {
                selKm.getChildren().add(mono(Format.angka(j.getOdometerSaatIni()) + " / "
                        + Format.angka(j.getTargetOdometer()) + " km"));
                Label ket = new Label(sisaKm < 0 ? "Melebihi " + Format.angka(-sisaKm) + " km"
                        : "Sisa " + Format.angka(sisaKm) + " km");
                ket.getStyleClass().add(sisaKm < 0 ? "teks-bahaya" : "muted-sm");
                selKm.getChildren().add(ket);
            }

            // Batas tanggal
            VBox selTanggal = new VBox(2);
            Long sisaHari = j.getSisaHari();
            if (sisaHari == null) {
                selTanggal.getChildren().add(new Label("-"));
            } else {
                Label rel = new Label(Format.sisaHariRelatif(sisaHari));
                rel.getStyleClass().add(sisaHari < 0 ? "teks-bahaya" : "cell-title");
                Label tgl = new Label(Format.tanggal(j.getTargetTanggal()));
                tgl.getStyleClass().add("muted-sm");
                selTanggal.getChildren().addAll(rel, tgl);
            }

            tabel.baris(selKendaraan, lblKategori, selKm, selTanggal, badge(j.cekStatus()), aksi.apply(j));
        }
        return tabel.getNode();
    }
}
