package org.otocare.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 * Tabel sederhana berbasis GridPane (header abu-abu, garis pemisah 1px) sesuai desain.
 * Lebar kolom ditentukan dalam persen.
 */
public class Tabel {
    private final GridPane grid = new GridPane();
    private int baris = 0;

    public Tabel(double... persenKolom) {
        for (double persen : persenKolom) {
            ColumnConstraints kolom = new ColumnConstraints();
            kolom.setPercentWidth(persen);
            grid.getColumnConstraints().add(kolom);
        }
        grid.getStyleClass().add("tabel");
    }

    public void header(String... judul) {
        for (int i = 0; i < judul.length; i++) {
            Label teks = new Label(judul[i].toUpperCase());
            teks.getStyleClass().add("th-text");
            HBox sel = new HBox(teks);
            sel.getStyleClass().add("th");
            sel.setAlignment(Pos.CENTER_LEFT);
            grid.add(sel, i, baris);
        }
        baris++;
    }

    public void baris(Node... isi) {
        for (int i = 0; i < isi.length; i++) {
            HBox sel = new HBox(isi[i]);
            sel.getStyleClass().add("td");
            sel.setAlignment(Pos.CENTER_LEFT);
            grid.add(sel, i, baris);
        }
        baris++;
    }

    public Node getNode() { return grid; }
}
