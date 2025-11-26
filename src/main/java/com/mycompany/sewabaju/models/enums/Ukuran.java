package com.mycompany.sewabaju.models.enums;

public enum Ukuran {
    S("S", "Small"),
    M("M", "Medium"),
    L("L", "Large"),
    XL("XL", "Extra Large"),
    XXL("XXL", "Double Extra Large");
    
    private final String code;
    private final String displayName;
    
    Ukuran(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return code;
    }
    
    public String getFullDisplay() {
        return code + " (" + displayName + ")";
    }
    
    public static Ukuran fromString(String text) {
        if (text == null) return null;
        
        for (Ukuran ukuran : Ukuran.values()) {
            if (ukuran.name().equalsIgnoreCase(text) || 
                ukuran.code.equalsIgnoreCase(text)) {
                return ukuran;
            }
        }
        return null;
    }
    
    public static String[] getAllCodes() {
        Ukuran[] sizes = values();
        String[] codes = new String[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            codes[i] = sizes[i].code;
        }
        return codes;
    }
}