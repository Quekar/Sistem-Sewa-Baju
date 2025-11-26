package com.mycompany.sewabaju.exceptions;

public class PembayaranException extends RuntimeException {
    
    private int pembayaranId;
    private String errorCode;
    
    public PembayaranException(String message) {
        super(message);
    }
    
    public PembayaranException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public PembayaranException(String message, int pembayaranId) {
        super(message);
        this.pembayaranId = pembayaranId;
    }
    
    public PembayaranException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public PembayaranException(Throwable cause) {
        super(cause);
    }
    
    public int getPembayaranId() {
        return pembayaranId;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}