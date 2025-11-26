package com.mycompany.sewabaju.models;

import com.mycompany.sewabaju.models.enums.Role;

public class Pelanggan extends User {
    private int pelangganId;
    private String alamat;
    private int poinLoyalitas;
    
    public Pelanggan() {
        super();
        setRole(Role.PELANGGAN);
        this.poinLoyalitas = 0;
    }
    
    public Pelanggan(String nama, String email, String password, String noHp, String alamat) {
        super(nama, email, password, Role.PELANGGAN, noHp);
        this.alamat = alamat;
        this.poinLoyalitas = 0;
    }
    
    public int getPelangganId() {
        return pelangganId;
    }
    
    public void setPelangganId(int pelangganId) {
        this.pelangganId = pelangganId;
    }
    
    public String getAlamat() {
        return alamat;
    }
    
    public void setAlamat(String alamat) {
        this.alamat = alamat;
        setUpdatedAt(java.time.LocalDateTime.now());
    }
    
    public int getPoinLoyalitas() {
        return poinLoyalitas;
    }
    
    public void setPoinLoyalitas(int poinLoyalitas) {
        this.poinLoyalitas = Math.max(0, poinLoyalitas);
        setUpdatedAt(java.time.LocalDateTime.now());
    }
    
    public void tambahPoin(int poin) {
        if (poin > 0) {
            this.poinLoyalitas += poin;
            setUpdatedAt(java.time.LocalDateTime.now());
        }
    }
    
    public boolean kurangiPoin(int poin) {
        if (poin > 0 && this.poinLoyalitas >= poin) {
            this.poinLoyalitas -= poin;
            setUpdatedAt(java.time.LocalDateTime.now());
            return true;
        }
        return false;
    }
    
    public static int hitungPoinFromBelanja(double totalBelanja) {
        return (int) (totalBelanja / 10000);
    }
    
    @Override
    public String getSpecificInfo() {
        return "Pelanggan - Poin: " + poinLoyalitas + 
               (alamat != null ? " | Alamat: " + alamat : "");
    }
    
    @Override
    public boolean isValid() {
        return super.isValid() && getRole() == Role.PELANGGAN;
    }
    
    @Override
    public String toString() {
        return "Pelanggan{" +
                "pelangganId=" + pelangganId +
                ", userId=" + getUserId() +
                ", nama='" + getNama() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", alamat='" + alamat + '\'' +
                ", poinLoyalitas=" + poinLoyalitas +
                ", noHp='" + getNoHp() + '\'' +
                '}';
    }
}