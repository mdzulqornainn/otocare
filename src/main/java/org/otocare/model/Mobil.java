package org.otocare.model;

public class Mobil extends Kendaraan {
    private static final long serialVersionUID = 1L;
    private int jumlahPintu;

    public Mobil(String platNomor, String merk, String pemilik, int jumlahPintu) {
        super(platNomor, merk, pemilik);
        setJumlahPintu(jumlahPintu);
    }

    public Mobil(int id, String platNomor, String merk, String pemilik, int odometer, int jumlahPintu) {
        super(id, platNomor, merk, pemilik, odometer);
        setJumlahPintu(jumlahPintu);
    }

    public int getJumlahPintu() { return jumlahPintu; }
    public void setJumlahPintu(int jumlahPintu) {
        if (jumlahPintu < 2 || jumlahPintu > 6) {
            throw new IllegalArgumentException("Jumlah pintu harus berada pada rentang 2 sampai 6!");
        }
        this.jumlahPintu = jumlahPintu;
    }

    @Override
    public String getTipe() { return "Mobil"; }

    @Override
    public String getDetailKhusus() { return jumlahPintu + " pintu"; }

    @Override
    public String getNilaiKhusus() { return String.valueOf(jumlahPintu); }

    @Override
    public String getInfo() {
        return "[MOBIL] " + super.getInfo() + " | Jumlah Pintu: " + jumlahPintu;
    }
}
