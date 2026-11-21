-- =====================================================================
--  OtoCare - Skema MySQL 8.0+ (Laragon / XAMPP)
--  PERHATIAN: script ini menghapus database otocare_db lalu membuatnya ulang.
--  Akun dummy: admin / admin123   (password disimpan sebagai hash SHA-256)
-- =====================================================================
DROP DATABASE IF EXISTS otocare_db;
CREATE DATABASE otocare_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE otocare_db;

CREATE TABLE users (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    password     VARCHAR(100) NOT NULL,
    nama_lengkap VARCHAR(100) NOT NULL
) ENGINE = InnoDB;

CREATE TABLE kendaraan (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    id_user    INT          NOT NULL,
    tipe       VARCHAR(10)  NOT NULL,              -- 'Mobil' / 'Motor'
    plat_nomor VARCHAR(15)  NOT NULL UNIQUE,
    merk       VARCHAR(50)  NOT NULL,
    pemilik    VARCHAR(100) NOT NULL,
    khusus     VARCHAR(20)  NOT NULL,              -- jumlah pintu (Mobil) / jenis transmisi (Motor)
    odometer   INT          NOT NULL DEFAULT 0,    -- km
    INDEX idx_kendaraan_user (id_user),
    CONSTRAINT fk_kendaraan_user FOREIGN KEY (id_user) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE kategori_servis (
    id   INT AUTO_INCREMENT PRIMARY KEY,
    nama VARCHAR(80) NOT NULL UNIQUE
) ENGINE = InnoDB;

CREATE TABLE riwayat_servis (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    id_kendaraan INT           NOT NULL,
    id_kategori  INT           NOT NULL,
    tanggal      DATE          NOT NULL,
    odometer     INT           NOT NULL,
    biaya        DECIMAL(12,2) NOT NULL DEFAULT 0,
    nama_bengkel VARCHAR(100)  NULL,
    catatan      VARCHAR(255)  NULL,
    INDEX idx_riwayat_tanggal (tanggal),
    INDEX idx_riwayat_kendaraan (id_kendaraan),
    CONSTRAINT fk_riwayat_kendaraan FOREIGN KEY (id_kendaraan) REFERENCES kendaraan (id) ON DELETE CASCADE,
    CONSTRAINT fk_riwayat_kategori  FOREIGN KEY (id_kategori)  REFERENCES kategori_servis (id) ON DELETE RESTRICT,
    CONSTRAINT chk_riwayat_biaya CHECK (biaya >= 0)
) ENGINE = InnoDB;

-- Satu kendaraan hanya punya satu jadwal per kategori; setelah servis, jadwal yang sama di-UPDATE
CREATE TABLE jadwal_pengingat (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    id_kendaraan    INT  NOT NULL,
    id_kategori     INT  NOT NULL,
    target_odometer INT  NULL,
    target_tanggal  DATE NULL,
    UNIQUE KEY uq_jadwal (id_kendaraan, id_kategori),
    CONSTRAINT fk_jadwal_kendaraan FOREIGN KEY (id_kendaraan) REFERENCES kendaraan (id) ON DELETE CASCADE,
    CONSTRAINT fk_jadwal_kategori  FOREIGN KEY (id_kategori)  REFERENCES kategori_servis (id) ON DELETE CASCADE,
    CONSTRAINT chk_jadwal_target CHECK (target_odometer IS NOT NULL OR target_tanggal IS NOT NULL)
) ENGINE = InnoDB;

-- ---------------------------------------------------------------------
-- DATA AWAL (tanggal relatif terhadap CURDATE() agar ketiga status pengingat muncul saat demo)
-- ---------------------------------------------------------------------
INSERT INTO users (username, password, nama_lengkap) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Administrator');

INSERT INTO kategori_servis (nama) VALUES
('Ganti Oli Mesin'), ('Servis Berkala'), ('Servis Rem'), ('Ganti Filter Udara'), ('Ganti Busi'),
('Servis CVT / V-Belt'), ('Servis Rantai & Gir'), ('Ganti Aki'), ('Ganti Ban'), ('Lainnya');

INSERT INTO kendaraan (id_user, tipe, plat_nomor, merk, pemilik, khusus, odometer) VALUES
(1, 'Mobil', 'BE 1234 AB', 'Toyota Avanza',   'Administrator', '4',     45200),
(1, 'Motor', 'BE 5678 CD', 'Honda Vario 125', 'Administrator', 'Matic', 22000);

INSERT INTO riwayat_servis (id_kendaraan, id_kategori, tanggal, odometer, biaya, nama_bengkel, catatan) VALUES
(1, 1, DATE_SUB(CURDATE(), INTERVAL 150 DAY), 40000, 450000, 'Auto2000 Teuku Umar', 'Oli 10W-40 + filter oli'),
(1, 2, DATE_SUB(CURDATE(), INTERVAL 62 DAY),  43000, 850000, 'Auto2000 Teuku Umar', 'Servis berkala 43.000 km'),
(1, 3, DATE_SUB(CURDATE(), INTERVAL 20 DAY),  45200, 600000, 'Bengkel Jaya Abadi',  'Kampas rem depan'),
(2, 5, DATE_SUB(CURDATE(), INTERVAL 75 DAY),  20500, 40000,  'AHASS Maju Motor',    NULL),
(2, 1, DATE_SUB(CURDATE(), INTERVAL 5 DAY),   22000, 65000,  'AHASS Maju Motor',    'Oli MPX2');

INSERT INTO jadwal_pengingat (id_kendaraan, id_kategori, target_odometer, target_tanggal) VALUES
(1, 1, 45000, DATE_ADD(CURDATE(), INTERVAL 30 DAY)),    -- Avanza: odometer 45.200 > 45.000   -> JATUH TEMPO
(1, 3, 60000, DATE_ADD(CURDATE(), INTERVAL 200 DAY)),   -- Avanza: servis rem                  -> AMAN
(2, 1, 22400, DATE_ADD(CURDATE(), INTERVAL 100 DAY)),   -- Vario : sisa 400 km (< 500)         -> SEGERA SERVIS
(2, 5, 40000, DATE_ADD(CURDATE(), INTERVAL 5 DAY));     -- Vario : H-5 (<= 7 hari)             -> SEGERA SERVIS
