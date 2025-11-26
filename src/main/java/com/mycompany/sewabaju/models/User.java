package com.mycompany.sewabaju.models;

import com.mycompany.sewabaju.models.enums.Role;
import java.time.LocalDateTime;

public abstract class User {
    private int userId;
    private String nama;
    private String email;
    private String password;
    private Role role;
    private String noHp;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public User(String nama, String email, String password, Role role, String noHp) {
        this();
        this.nama = nama;
        this.email = email;
        this.password = password;
        this.role = role;
        this.noHp = noHp;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getNama() {
        return nama;
    }
    
    public void setNama(String nama) {
        this.nama = nama;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public String getNoHp() {
        return noHp;
    }
    
    public void setNoHp(String noHp) {
        this.noHp = noHp;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public abstract String getSpecificInfo();
    
    public boolean isValid() {
        return nama != null && !nama.trim().isEmpty() &&
               email != null && email.contains("@") &&
               password != null && !password.isEmpty() &&
               role != null;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", nama='" + nama + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", noHp='" + noHp + '\'' +
                '}';
    }
}