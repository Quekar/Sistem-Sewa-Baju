package com.mycompany.sewabaju.models;

import com.mycompany.sewabaju.models.enums.Role;

public class Admin extends User {
    private int adminId;
    private String jabatan;
    
    public Admin() {
        super();
        setRole(Role.ADMIN);
    }
    
    public Admin(String nama, String email, String password, String noHp, String jabatan) {
        super(nama, email, password, Role.ADMIN, noHp);
        this.jabatan = jabatan;
    }
    
    public int getAdminId() {
        return adminId;
    }
    
    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }
    
    public String getJabatan() {
        return jabatan;
    }
    
    public void setJabatan(String jabatan) {
        this.jabatan = jabatan;
        setUpdatedAt(java.time.LocalDateTime.now());
    }
    
    @Override
    public String getSpecificInfo() {
        return "Admin - Jabatan: " + (jabatan != null ? jabatan : "Tidak ada");
    }
    
    @Override
    public boolean isValid() {
        return super.isValid() && getRole() == Role.ADMIN;
    }
    
    @Override
    public String toString() {
        return "Admin{" +
                "adminId=" + adminId +
                ", userId=" + getUserId() +
                ", nama='" + getNama() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", jabatan='" + jabatan + '\'' +
                ", noHp='" + getNoHp() + '\'' +
                '}';
    }
}