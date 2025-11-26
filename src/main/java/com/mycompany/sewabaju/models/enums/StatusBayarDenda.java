package com.mycompany.sewabaju.models.enums;

public enum StatusBayarDenda {
    BELUM_DIBAYAR("Belum Dibayar", "danger"),
    SUDAH_DIBAYAR("Sudah Dibayar", "success");
    
    private final String displayName;
    private final String cssClass;
    
    StatusBayarDenda(String displayName, String cssClass) {
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
    
    public boolean isPaid() {
        return this == SUDAH_DIBAYAR;
    }
    
    public static StatusBayarDenda fromString(String text) {
        if (text == null) return null;
        
        for (StatusBayarDenda status : StatusBayarDenda.values()) {
            if (status.name().equalsIgnoreCase(text) || 
                status.displayName.equalsIgnoreCase(text)) {
                return status;
            }
        }
        return null;
    }
}