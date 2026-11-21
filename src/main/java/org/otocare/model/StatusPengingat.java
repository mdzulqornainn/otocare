package org.otocare.model;

/** Status pengingat servis beserta label dan style class (warna badge) untuk GUI. */
public enum StatusPengingat {
    AMAN("Aman", "badge-aman"),
    SEGERA_SERVIS("Segera Servis", "badge-segera"),
    JATUH_TEMPO("Jatuh Tempo", "badge-jatuh");

    private final String label;
    private final String styleClass;

    StatusPengingat(String label, String styleClass) {
        this.label = label;
        this.styleClass = styleClass;
    }

    public String getLabel() { return label; }
    public String getStyleClass() { return styleClass; }
}
