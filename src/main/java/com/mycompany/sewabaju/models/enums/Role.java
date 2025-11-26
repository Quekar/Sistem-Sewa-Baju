package com.mycompany.sewabaju.models.enums;

public enum Role {
    ADMIN("Admin"),
    PELANGGAN("Pelanggan");
    
    private final String displayName;
    
    Role(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
    
    public static Role fromString(String text) {
        if (text == null) return null;
        
        for (Role role : Role.values()) {
            if (role.name().equalsIgnoreCase(text) || 
                role.displayName.equalsIgnoreCase(text)) {
                return role;
            }
        }
        return null;
    }
}