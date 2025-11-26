package com.mycompany.sewabaju.services;

import com.mycompany.sewabaju.dao.AdminDAO;
import com.mycompany.sewabaju.dao.PelangganDAO;
import com.mycompany.sewabaju.dao.UserDAO;
import com.mycompany.sewabaju.exceptions.AuthenticationException;
import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.exceptions.ValidationException;
import com.mycompany.sewabaju.models.Admin;
import com.mycompany.sewabaju.models.Pelanggan;
import com.mycompany.sewabaju.models.User;
import com.mycompany.sewabaju.models.enums.Role;
import com.mycompany.sewabaju.utils.PasswordUtil;
import com.mycompany.sewabaju.utils.Session;
import com.mycompany.sewabaju.utils.ValidationUtil;

import java.sql.Connection;
import java.sql.SQLException;
import com.mycompany.sewabaju.database.DatabaseConnection;

public class AuthService {
    
    private final UserDAO userDAO;
    private final AdminDAO adminDAO;
    private final PelangganDAO pelangganDAO;
    private static AuthService instance;
    
    private AuthService() {
        this.userDAO = new UserDAO();
        this.adminDAO = new AdminDAO();
        this.pelangganDAO = new PelangganDAO();
    }
    
    public static AuthService getInstance() {
        if (instance == null) {
            synchronized (AuthService.class) {
                if (instance == null) {
                    instance = new AuthService();
                }
            }
        }
        return instance;
    }
    
    public User login(String email, String password) throws AuthenticationException {
        try {
            if (!ValidationUtil.isValidEmail(email)) {
                throw new ValidationException("Format email tidak valid");
            }
            
            if (ValidationUtil.isEmpty(password)) {
                throw new ValidationException("Password tidak boleh kosong");
            }
            
            User user = userDAO.findByEmail(email);
            
            if (user == null) {
                throw new AuthenticationException("Email tidak terdaftar");
            }
            
            if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
                throw new AuthenticationException("Password salah");
            }
            
            User completeUser = loadCompleteUserData(user);
            
            if (completeUser == null) {
                throw new DatabaseException("Gagal memuat data user lengkap");
            }
            
            Session.getInstance().setCurrentUser(completeUser);
            
            System.out.println("Login successful: " + completeUser.getNama() + 
                             " (" + completeUser.getRole() + ")");
            
            return completeUser;
            
        } catch (ValidationException | AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException("Login gagal: " + e.getMessage(), e);
        }
    }
    
    public Pelanggan registerPelanggan(String nama, String email, String password, 
                                       String noHp, String alamat) 
            throws ValidationException, DatabaseException {
        
        Connection conn = null;
        try {
            validateRegisterInput(nama, email, password, noHp);
            
            if (userDAO.emailExists(email)) {
                throw new ValidationException("Email sudah terdaftar");
            }
            
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            String hashedPassword = PasswordUtil.hashPassword(password);
            
            Pelanggan pelanggan = new Pelanggan(nama, email, hashedPassword, noHp, alamat);
            
            int userId = userDAO.save(pelanggan);
            
            if (userId <= 0) {
                throw new DatabaseException("Gagal menyimpan user");
            }
            
            pelanggan.setUserId(userId);
            
            int pelangganId = pelangganDAO.save(pelanggan);
            
            if (pelangganId <= 0) {
                throw new DatabaseException("Gagal menyimpan pelanggan");
            }
            
            pelanggan.setPelangganId(pelangganId);
            
            conn.commit();
            
            System.out.println("Register successful: " + pelanggan.getNama());
            
            return pelanggan;
            
        } catch (ValidationException | DatabaseException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Transaction rolled back");
                } catch (SQLException ex) {
                    System.err.println("Error rolling back: " + ex.getMessage());
                }
            }
            throw e;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error rolling back: " + ex.getMessage());
                }
            }
            throw new DatabaseException("Register gagal: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error restoring auto-commit: " + e.getMessage());
                }
            }
        }
    }
    
    public Admin registerAdmin(String nama, String email, String password, 
                               String noHp, String jabatan) 
            throws ValidationException, DatabaseException {
        
        Connection conn = null;
        try {
            if (!Session.getInstance().isAdmin()) {
                throw new ValidationException("Hanya admin yang dapat menambah admin baru");
            }
            
            validateRegisterInput(nama, email, password, noHp);
            
            if (userDAO.emailExists(email)) {
                throw new ValidationException("Email sudah terdaftar");
            }
            
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            String hashedPassword = PasswordUtil.hashPassword(password);
            Admin admin = new Admin(nama, email, hashedPassword, noHp, jabatan);
            int userId = userDAO.save(admin);
            
            if (userId <= 0) {
                throw new DatabaseException("Gagal menyimpan user");
            }
            
            admin.setUserId(userId);
            int adminId = adminDAO.save(admin);
            
            if (adminId <= 0) {
                throw new DatabaseException("Gagal menyimpan admin");
            }
            
            admin.setAdminId(adminId);
            conn.commit();
            System.out.println("Admin registered: " + admin.getNama());
            return admin;
            
        } catch (ValidationException | DatabaseException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Transaction rolled back");
                } catch (SQLException ex) {
                    System.err.println("Error rolling back: " + ex.getMessage());
                }
            }
            throw e;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error rolling back: " + ex.getMessage());
                }
            }
            throw new DatabaseException("Register admin gagal: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error restoring auto-commit: " + e.getMessage());
                }
            }
        }
    }
    
    public void logout() {
        User currentUser = Session.getInstance().getCurrentUser();
        if (currentUser != null) {
            System.out.println("Logout: " + currentUser.getNama());
        }
        Session.getInstance().logout();
    }
    
    public void changePassword(int userId, String oldPassword, String newPassword) 
            throws ValidationException, AuthenticationException, DatabaseException {
        
        try {
            if (ValidationUtil.isEmpty(newPassword)) {
                throw new ValidationException("Password baru tidak boleh kosong");
            }
            
            if (!PasswordUtil.isStrongPassword(newPassword)) {
                throw new ValidationException(PasswordUtil.getPasswordStrengthMessage(newPassword));
            }
            
            if (oldPassword.equals(newPassword)) {
                throw new ValidationException("Password baru tidak boleh sama dengan password lama");
            }
            
            User user = userDAO.findById(userId);
            
            if (user == null) {
                throw new DatabaseException("User tidak ditemukan");
            }
            
            if (!PasswordUtil.verifyPassword(oldPassword, user.getPassword())) {
                throw new AuthenticationException("Password lama salah");
            }
            
            String hashedPassword = PasswordUtil.hashPassword(newPassword);
            boolean updated = userDAO.updatePassword(userId, hashedPassword);
            
            if (!updated) {
                throw new DatabaseException("Gagal mengubah password");
            }
            
            System.out.println("Password changed for user: " + userId);
            
        } catch (ValidationException | AuthenticationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Gagal mengubah password: " + e.getMessage(), e);
        }
    }
    
    public void updateProfile(int userId, String nama, String noHp, String alamat) 
            throws ValidationException, DatabaseException {
        
        Connection conn = null;
        try {
            if (ValidationUtil.isEmpty(nama)) {
                throw new ValidationException("Nama tidak boleh kosong");
            }
            
            if (!ValidationUtil.isEmpty(noHp) && !ValidationUtil.isValidPhone(noHp)) {
                throw new ValidationException("Format nomor HP tidak valid");
            }
            
            User user = userDAO.findById(userId);
            
            if (user == null) {
                throw new DatabaseException("User tidak ditemukan");
            }
            
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            
            user.setNama(nama);
            user.setNoHp(noHp);
            
            boolean updated = userDAO.update(user);
            
            if (!updated) {
                throw new DatabaseException("Gagal mengupdate user");
            }
            
            if (user.getRole() == Role.PELANGGAN && alamat != null) {
                Pelanggan pelanggan = pelangganDAO.findByUserId(userId);
                if (pelanggan != null) {
                    pelanggan.setAlamat(alamat);
                    pelangganDAO.update(pelanggan);
                }
            }
            
            conn.commit();
            
            if (Session.getInstance().getCurrentUserId() == userId) {
                User updatedUser = loadCompleteUserData(user);
                Session.getInstance().setCurrentUser(updatedUser);
            }
            
            System.out.println("Profile updated for user: " + userId);
            
        } catch (ValidationException | DatabaseException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error rolling back: " + ex.getMessage());
                }
            }
            throw e;
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error rolling back: " + ex.getMessage());
                }
            }
            throw new DatabaseException("Gagal mengupdate profile: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error restoring auto-commit: " + e.getMessage());
                }
            }
        }
    }
    
    public User getCurrentUser() {
        return Session.getInstance().getCurrentUser();
    }
    
    public boolean isLoggedIn() {
        return Session.getInstance().isLoggedIn();
    }
    
    public boolean isAdmin() {
        return Session.getInstance().isAdmin();
    }
    
    public boolean isPelanggan() {
        return Session.getInstance().isPelanggan();
    }
    
    private User loadCompleteUserData(User user) throws DatabaseException {
        try {
            if (user.getRole() == Role.ADMIN) {
                Admin admin = adminDAO.findByIdWithUserDetails(user.getUserId());
                
                if (admin == null) {
                    admin = adminDAO.findByUserId(user.getUserId());
                    if (admin != null) {
                        mergeUserDataToAdmin(user, admin);
                    }
                }
                
                return admin;
                
            } else {
                Pelanggan pelanggan = pelangganDAO.findByIdWithUserDetails(user.getUserId());
                
                if (pelanggan == null) {
                    pelanggan = pelangganDAO.findByUserId(user.getUserId());
                    if (pelanggan != null) {
                        mergeUserDataToPelanggan(user, pelanggan);
                    }
                }
                
                return pelanggan;
            }
        } catch (Exception e) {
            throw new DatabaseException("Gagal memuat data user lengkap: " + e.getMessage(), e);
        }
    }
    
    private void mergeUserDataToAdmin(User user, Admin admin) {
        admin.setUserId(user.getUserId());
        admin.setNama(user.getNama());
        admin.setEmail(user.getEmail());
        admin.setPassword(user.getPassword());
        admin.setRole(user.getRole());
        admin.setNoHp(user.getNoHp());
        admin.setCreatedAt(user.getCreatedAt());
        admin.setUpdatedAt(user.getUpdatedAt());
    }
    
    private void mergeUserDataToPelanggan(User user, Pelanggan pelanggan) {
        pelanggan.setUserId(user.getUserId());
        pelanggan.setNama(user.getNama());
        pelanggan.setEmail(user.getEmail());
        pelanggan.setPassword(user.getPassword());
        pelanggan.setRole(user.getRole());
        pelanggan.setNoHp(user.getNoHp());
        pelanggan.setCreatedAt(user.getCreatedAt());
        pelanggan.setUpdatedAt(user.getUpdatedAt());
    }
    
    private void validateRegisterInput(String nama, String email, String password, String noHp) 
            throws ValidationException {
        
        if (ValidationUtil.isEmpty(nama)) {
            throw new ValidationException("Nama tidak boleh kosong");
        }
        
        if (!ValidationUtil.hasMinLength(nama, 3)) {
            throw new ValidationException("Nama minimal 3 karakter");
        }
        
        if (!ValidationUtil.hasMaxLength(nama, 100)) {
            throw new ValidationException("Nama maksimal 100 karakter");
        }
        
        if (!ValidationUtil.isValidEmail(email)) {
            throw new ValidationException("Format email tidak valid");
        }
        
        if (ValidationUtil.isEmpty(password)) {
            throw new ValidationException("Password tidak boleh kosong");
        }
        
        if (!PasswordUtil.isStrongPassword(password)) {
            throw new ValidationException(PasswordUtil.getPasswordStrengthMessage(password));
        }
        
        if (!ValidationUtil.isEmpty(noHp) && !ValidationUtil.isValidPhone(noHp)) {
            throw new ValidationException("Format nomor HP tidak valid");
        }
    }
}