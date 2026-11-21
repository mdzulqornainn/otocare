package org.otocare.ui;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;

/** Ilustrasi garis minimalis mobil dan motor untuk panel kiri halaman login (mengikuti desain). */
public final class Ilustrasi {
    private Ilustrasi() { }

    private static SVGPath garis(String path, String styleClass) {
        SVGPath p = new SVGPath();
        p.setContent(path);
        p.getStyleClass().add(styleClass);
        return p;
    }

    private static Circle lingkaran(double x, double y, double r, String styleClass) {
        Circle c = new Circle(x, y, r);
        c.getStyleClass().add(styleClass);
        return c;
    }

    public static Pane mobilDanMotor() {
        Pane pane = new Pane();
        pane.setPrefSize(420, 160);
        pane.setMinSize(420, 160);
        pane.setMaxSize(420, 160);

        // Jalan
        pane.getChildren().add(garis("M16 142 L404 142", "svg-jalan"));

        // Mobil
        pane.getChildren().add(garis("M38 118 L44 96 C46 90 52 86 58 86 L92 84 L116 64 C121 60 127 58 134 58 "
                + "L184 58 C192 58 199 61 204 67 L224 86 L240 90 C246 92 250 97 250 104 L250 118 "
                + "C250 122 247 125 243 125 L230 125", "svg-putih"));
        pane.getChildren().add(garis("M38 118 C38 122 41 125 45 125 L58 125", "svg-putih"));
        pane.getChildren().add(garis("M94 125 L194 125", "svg-putih"));
        pane.getChildren().add(garis("M102 85 L121 67 C124 64 128 63 133 63 L156 63 L156 85 Z", "svg-muted"));
        pane.getChildren().add(garis("M162 63 L182 63 C187 63 192 65 196 69 L212 85 L162 85 Z", "svg-muted"));
        pane.getChildren().add(lingkaran(76, 125, 16, "svg-roda"));
        pane.getChildren().add(lingkaran(76, 125, 6, "svg-hub"));
        pane.getChildren().add(lingkaran(212, 125, 16, "svg-roda"));
        pane.getChildren().add(lingkaran(212, 125, 6, "svg-hub"));
        pane.getChildren().add(garis("M242 98 L249 98", "svg-biru"));

        // Motor
        pane.getChildren().add(lingkaran(292, 127, 14, "svg-roda"));
        pane.getChildren().add(lingkaran(292, 127, 5, "svg-hub"));
        pane.getChildren().add(lingkaran(372, 127, 14, "svg-roda"));
        pane.getChildren().add(lingkaran(372, 127, 5, "svg-hub"));
        pane.getChildren().add(garis("M292 127 L314 102 L346 102 L358 84 L366 127", "svg-putih"));
        pane.getChildren().add(garis("M284 96 L312 96 C318 96 324 98 328 102", "svg-putih"));
        pane.getChildren().add(garis("M350 76 L364 76 L358 84", "svg-putih"));
        pane.getChildren().add(garis("M322 102 L330 120 L348 120", "svg-muted"));
        pane.getChildren().add(garis("M364 88 L372 88", "svg-biru"));
        return pane;
    }
}
