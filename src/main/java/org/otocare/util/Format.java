package org.otocare.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/** Pemformat tampilan: Rupiah, angka, tanggal, dan nama bulan berbahasa Indonesia. */
public final class Format {
    private static final Locale ID = Locale.forLanguageTag("id-ID");
    private static final DateTimeFormatter TANGGAL = DateTimeFormatter.ofPattern("dd MMM yyyy", ID);

    private Format() { }

    public static String rupiah(double nilai) {
        return "Rp " + String.format(ID, "%,.0f", nilai);
    }

    public static String angka(long nilai) {
        return String.format(ID, "%,d", nilai);
    }

    public static String tanggal(LocalDate tgl) {
        return tgl == null ? "-" : TANGGAL.format(tgl);
    }

    /** Teks relatif untuk batas tanggal: "Lewat 3 hari", "5 hari lagi", "2 bulan lagi". */
    public static String sisaHariRelatif(long hari) {
        if (hari < 0) {
            return "Lewat " + (-hari) + " hari";
        }
        if (hari == 0) {
            return "Hari ini";
        }
        if (hari <= 30) {
            return hari + " hari lagi";
        }
        return (hari / 30) + " bulan lagi";
    }

    /** Inisial untuk avatar: "Muhammad Dzulqornain" -> "MD". */
    public static String inisial(String nama) {
        String[] kata = nama.trim().split("\\s+");
        String hasil = kata[0].substring(0, 1);
        if (kata.length > 1) {
            hasil += kata[1].substring(0, 1);
        }
        return hasil.toUpperCase();
    }

    /** "2026-11" -> "November 2026" */
    public static String bulan(String yyyyMm) {
        YearMonth ym = YearMonth.parse(yyyyMm);
        return ym.getMonth().getDisplayName(TextStyle.FULL, ID) + " " + ym.getYear();
    }
}
