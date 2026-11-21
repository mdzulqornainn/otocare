package org.otocare.model;

/** Akun pemilik kendaraan yang sedang login. */
public class Pengguna {
    private int id;
    private String username;
    private String namaLengkap;

    public Pengguna(int id, String username, String namaLengkap) {
        this.id = id;
        setUsername(username);
        setNamaLengkap(namaLengkap);
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getNamaLengkap() { return namaLengkap; }

    public void setUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username tidak boleh kosong!");
        }
        this.username = username.trim();
    }

    public void setNamaLengkap(String namaLengkap) {
        if (namaLengkap == null || namaLengkap.isBlank()) {
            throw new IllegalArgumentException("Nama lengkap tidak boleh kosong!");
        }
        this.namaLengkap = namaLengkap.trim();
    }
}
