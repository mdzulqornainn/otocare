package org.otocare.model;

public class Motor extends Kendaraan {
    private static final long serialVersionUID = 1L;
    private String jenisTransmisi;

    public Motor(String platNomor, String merk, String pemilik, String jenisTransmisi) {
        super(platNomor, merk, pemilik);
        setJenisTransmisi(jenisTransmisi);
    }

    public Motor(int id, String platNomor, String merk, String pemilik, int odometer, String jenisTransmisi) {
        super(id, platNomor, merk, pemilik, odometer);
        setJenisTransmisi(jenisTransmisi);
    }

    public String getJenisTransmisi() { return jenisTransmisi; }
    public void setJenisTransmisi(String jenisTransmisi) {
        if (jenisTransmisi == null || jenisTransmisi.isBlank()) {
            throw new IllegalArgumentException("Jenis transmisi tidak boleh kosong!");
        }
        this.jenisTransmisi = jenisTransmisi.trim();
    }

    @Override
    public String getTipe() { return "Motor"; }

    @Override
    public String getDetailKhusus() { return "Transmisi " + jenisTransmisi; }

    @Override
    public String getNilaiKhusus() { return jenisTransmisi; }

    @Override
    public String getInfo() {
        return "[MOTOR] " + super.getInfo() + " | Transmisi: " + jenisTransmisi;
    }
}
