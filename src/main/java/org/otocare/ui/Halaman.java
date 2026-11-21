package org.otocare.ui;

import javafx.scene.Node;

/** Kontrak untuk setiap halaman aplikasi (INTERFACE): punya tampilan dan bisa memuat ulang datanya. */
public interface Halaman {
    Node getTampilan();
    void segarkan();
}
