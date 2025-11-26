package com.mycompany.sewabaju.models.enums;

public enum MetodePembayaran {
    TRANSFER_BANK("Transfer Bank", "Rekening: BCA 1234567890 a.n. Sewa Baju"),
    E_WALLET("E-Wallet", "GoPay / OVO / Dana: 081234567890"),
    CASH("Cash", "Bayar di tempat saat pengambilan");
    
    private final String displayName;
    private final String instructions;
    
    MetodePembayaran(String displayName, String instructions) {
        this.displayName = displayName;
        this.instructions = instructions;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getInstructions() {
        return instructions;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public boolean requiresProof() {
        return this == TRANSFER_BANK || this == E_WALLET;
    }
    
    public boolean isInstant() {
        return this == CASH;
    }
    
    public static MetodePembayaran fromString(String text) {
        if (text == null) return null;
        
        for (MetodePembayaran metode : MetodePembayaran.values()) {
            if (metode.name().equalsIgnoreCase(text) || 
                metode.displayName.equalsIgnoreCase(text)) {
                return metode;
            }
        }
        return null;
    }
}