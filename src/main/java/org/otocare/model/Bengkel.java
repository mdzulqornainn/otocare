package org.otocare.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Bengkel {
    private ArrayList<Kendaraan> daftarKendaraan = new ArrayList<>();

    public void tambahKendaraan(Kendaraan k) { daftarKendaraan.add(k); }
    public void kosongkan() { daftarKendaraan.clear(); }
    public ArrayList<Kendaraan> getDaftarKendaraan() { return daftarKendaraan; }

    public void simpanKeFile(String namaFile) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(namaFile))) {
            out.writeObject(daftarKendaraan);
        }
    }

    @SuppressWarnings("unchecked")
    public void muatDariFile(String namaFile) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(namaFile))) {
            daftarKendaraan = (ArrayList<Kendaraan>) in.readObject();
        }
    }

    public void eksporKeTeks(String namaFile) throws IOException {
        try (FileWriter penulis = new FileWriter(namaFile)) {
            penulis.write("Laporan Kendaraan OtoCare\n");
            penulis.write("Jumlah kendaraan: " + daftarKendaraan.size() + "\n\n");
            int nomor = 1;
            for (Kendaraan k : daftarKendaraan) {
                penulis.write(nomor++ + ". " + k.getInfo() + "\n");
            }
        }
    }
}
