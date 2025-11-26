package com.mycompany.sewabaju.models.enums;

public enum Kondisi {
    BARU("Baru", "success", 0),
    BAIK("Baik", "info", 0),
    RUSAK_RINGAN("Rusak Ringan", "warning", 25000),
    RUSAK_BERAT("Rusak Berat", "danger", 100000);
    
    private final String displayName;
    private final String cssClass;
    private final double dendaEstimasi;
    
    Kondisi(String displayName, String cssClass, double dendaEstimasi) {
        this.displayName = displayName;
        this.cssClass = cssClass;
        this.dendaEstimasi = dendaEstimasi;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getCssClass() {
        return cssClass;
    }
    
    public double getDendaEstimasi() {
        return dendaEstimasi;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public boolean requiresDenda() {
        return dendaEstimasi > 0;
    }
    
    public boolean isRentable() {
        return this == BARU || this == BAIK || this == RUSAK_RINGAN;
    }
    
    public static Kondisi fromString(String text) {
        if (text == null) return null;
        
        for (Kondisi kondisi : Kondisi.values()) {
            if (kondisi.name().equalsIgnoreCase(text) || 
                kondisi.displayName.equalsIgnoreCase(text)) {
                return kondisi;
            }
        }
        return null;
    }
}