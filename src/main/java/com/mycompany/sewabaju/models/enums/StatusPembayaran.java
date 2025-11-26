package com.mycompany.sewabaju.models.enums;

public enum StatusPembayaran {
    MENUNGGU_VERIFIKASI("Menunggu Verifikasi", "warning"),
    BERHASIL("Berhasil", "success"),
    DITOLAK("Ditolak", "danger");
    
    private final String displayName;
    private final String cssClass;
    
    StatusPembayaran(String displayName, String cssClass) {
        this.displayName = displayName;
        this.cssClass = cssClass;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getCssClass() {
        return cssClass;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public boolean isProcessed() {
        return this == BERHASIL || this == DITOLAK;
    }
    
    public boolean isSuccess() {
        return this == BERHASIL;
    }
    
    public static StatusPembayaran fromString(String text) {
        if (text == null) return null;
        
        for (StatusPembayaran status : StatusPembayaran.values()) {
            if (status.name().equalsIgnoreCase(text) || 
                status.displayName.equalsIgnoreCase(text)) {
                return status;
            }
        }
        return null;
    }
}