package com.mycompany.sewabaju.models.enums;

public enum JenisDenda {
    KETERLAMBATAN("Keterlambatan", 10000, "per hari"),
    KERUSAKAN("Kerusakan", 0, "sesuai tingkat kerusakan"),
    KEHILANGAN("Kehilangan", 500000, "harga penggantian");
    
    private final String displayName;
    private final double defaultAmount;
    private final String description;
    
    JenisDenda(String displayName, double defaultAmount, String description) {
        this.displayName = displayName;
        this.defaultAmount = defaultAmount;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public double getDefaultAmount() {
        return defaultAmount;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public String getFullDisplay() {
        return displayName + " (" + description + ")";
    }
    
    public double hitungDenda(int hariTerlambat) {
        if (this == KETERLAMBATAN) {
            return defaultAmount * hariTerlambat;
        }
        return defaultAmount;
    }
    
    public static JenisDenda fromString(String text) {
        if (text == null) return null;
        
        for (JenisDenda jenis : JenisDenda.values()) {
            if (jenis.name().equalsIgnoreCase(text) || 
                jenis.displayName.equalsIgnoreCase(text)) {
                return jenis;
            }
        }
        return null;
    }
}