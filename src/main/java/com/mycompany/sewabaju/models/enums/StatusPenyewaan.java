package com.mycompany.sewabaju.models.enums;

public enum StatusPenyewaan {
    MENUNGGU_PEMBAYARAN("Menunggu Pembayaran", "warning"),
    DIKONFIRMASI("Dikonfirmasi", "info"),
    SEDANG_DISEWA("Sedang Disewa", "primary"),
    DIKEMBALIKAN("Dikembalikan", "success"),
    DIBATALKAN("Dibatalkan", "danger");
    
    private final String displayName;
    private final String cssClass;
    
    StatusPenyewaan(String displayName, String cssClass) {
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
    
    public boolean canTransitionTo(StatusPenyewaan nextStatus) {
        return switch (this) {
            case MENUNGGU_PEMBAYARAN -> nextStatus == DIKONFIRMASI || nextStatus == DIBATALKAN;
            case DIKONFIRMASI -> nextStatus == SEDANG_DISEWA || nextStatus == DIBATALKAN;
            case SEDANG_DISEWA -> nextStatus == DIKEMBALIKAN;
            case DIKEMBALIKAN, DIBATALKAN -> false;
        };
    }
    
    public static StatusPenyewaan fromString(String text) {
        if (text == null) return null;
        
        for (StatusPenyewaan status : StatusPenyewaan.values()) {
            if (status.name().equalsIgnoreCase(text) || 
                status.displayName.equalsIgnoreCase(text)) {
                return status;
            }
        }
        return null;
    }
}