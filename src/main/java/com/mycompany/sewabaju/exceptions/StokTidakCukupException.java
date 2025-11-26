package com.mycompany.sewabaju.exceptions;

public class StokTidakCukupException extends RuntimeException {
    
    private int bajuId;
    private String namaBaju;
    private int stokTersedia;
    private int stokDiminta;
    
    public StokTidakCukupException(String message) {
        super(message);
    }
    
    public StokTidakCukupException(String message, int bajuId, String namaBaju, 
                                   int stokTersedia, int stokDiminta) {
        super(message);
        this.bajuId = bajuId;
        this.namaBaju = namaBaju;
        this.stokTersedia = stokTersedia;
        this.stokDiminta = stokDiminta;
    }
    
    public StokTidakCukupException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public int getBajuId() {
        return bajuId;
    }
    
    public String getNamaBaju() {
        return namaBaju;
    }
    
    public int getStokTersedia() {
        return stokTersedia;
    }
    
    public int getStokDiminta() {
        return stokDiminta;
    }
    
    public String getDetailedMessage() {
        if (namaBaju != null) {
            return String.format("%s - Baju: %s, Stok tersedia: %d, Diminta: %d",
                    getMessage(), namaBaju, stokTersedia, stokDiminta);
        }
        return getMessage();
    }
}