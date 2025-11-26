package com.mycompany.sewabaju.utils;

import com.mycompany.sewabaju.models.Admin;
import com.mycompany.sewabaju.models.Pelanggan;
import com.mycompany.sewabaju.models.User;
import com.mycompany.sewabaju.models.enums.Role;

public class Session {
    
    private static Session instance;
    
    private User currentUser;
    private Admin currentAdmin;
    private Pelanggan currentPelanggan;
    
    private Session() {
    }
    
    public static Session getInstance() {
        if (instance == null) {
            synchronized (Session.class) {
                if (instance == null) {
                    instance = new Session();
                }
            }
        }
        return instance;
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        
        if (user instanceof Admin) {
            this.currentAdmin = (Admin) user;
            this.currentPelanggan = null;
        } else if (user instanceof Pelanggan) {
            this.currentPelanggan = (Pelanggan) user;
            this.currentAdmin = null;
        }
    }
    
    public User getCurrentUser() {
        return currentUser;
    }
    
    public Admin getCurrentAdmin() {
        return currentAdmin;
    }
    
    public Pelanggan getCurrentPelanggan() {
        return currentPelanggan;
    }
    
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == Role.ADMIN;
    }
    
    public boolean isPelanggan() {
        return currentUser != null && currentUser.getRole() == Role.PELANGGAN;
    }
    
    public int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : -1;
    }
    
    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getNama() : "";
    }
    
    public String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : "";
    }
    
    public Role getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }
    
    public void clear() {
        this.currentUser = null;
        this.currentAdmin = null;
        this.currentPelanggan = null;
    }
    
    public void logout() {
        clear();
    }
    
    @Override
    public String toString() {
        if (currentUser != null) {
            return "Session{" +
                    "user=" + currentUser.getNama() +
                    ", role=" + currentUser.getRole() +
                    ", userId=" + currentUser.getUserId() +
                    '}';
        }
        return "Session{not logged in}";
    }
}