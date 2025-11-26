package com.mycompany.sewabaju.dao;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.models.Admin;
import com.mycompany.sewabaju.models.enums.Role;
import java.sql.*;

public class AdminDAO extends BaseDAO<Admin> {
    
    @Override
    protected String getTableName() {
        return "admin";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "admin_id";
    }
    
    @Override
    protected Admin mapResultSetToEntity(ResultSet rs) throws SQLException {
        Admin admin = new Admin();
        admin.setAdminId(rs.getInt("admin_id"));
        admin.setUserId(rs.getInt("user_id"));
        admin.setJabatan(rs.getString("jabatan"));
        
        return admin;
    }
    
    public int save(Admin admin) throws DatabaseException {
        String sql = "INSERT INTO admin (user_id, jabatan) VALUES (?, ?)";
        
        return executeInsertWithGeneratedKey(sql,
                admin.getUserId(),
                admin.getJabatan()
        );
    }
    
    public boolean update(Admin admin) throws DatabaseException {
        String sql = "UPDATE admin SET jabatan = ? WHERE admin_id = ?";
        
        int rowsAffected = executeUpdate(sql,
                admin.getJabatan(),
                admin.getAdminId()
        );
        
        return rowsAffected > 0;
    }
    
    public Admin findByUserId(int userId) throws DatabaseException {
        String sql = "SELECT * FROM admin WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding admin by user_id: " + userId, e);
        }
        
        return null;
    }
    
    public Admin findByIdWithUserDetails(int adminId) throws DatabaseException {
        String sql = "SELECT a.*, u.* FROM admin a " +
                     "JOIN user u ON a.user_id = u.user_id " +
                     "WHERE a.admin_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, adminId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Admin admin = mapResultSetToEntity(rs);
                    admin.setUserId(rs.getInt("user_id"));
                    admin.setNama(rs.getString("nama"));
                    admin.setEmail(rs.getString("email"));
                    admin.setPassword(rs.getString("password"));
                    admin.setRole(Role.ADMIN);
                    admin.setNoHp(rs.getString("no_hp"));
                    
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (createdAt != null) admin.setCreatedAt(createdAt.toLocalDateTime());
                    if (updatedAt != null) admin.setUpdatedAt(updatedAt.toLocalDateTime());
                    
                    return admin;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding admin with user details", e);
        }
        
        return null;
    }
    
    public java.util.List<Admin> findAllWithUserDetails() throws DatabaseException {
        String sql = "SELECT a.*, u.* FROM admin a " +
                     "JOIN user u ON a.user_id = u.user_id " +
                     "ORDER BY a.admin_id";
        
        java.util.List<Admin> admins = new java.util.ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Admin admin = mapResultSetToEntity(rs);
                admin.setUserId(rs.getInt("user_id"));
                admin.setNama(rs.getString("nama"));
                admin.setEmail(rs.getString("email"));
                admin.setPassword(rs.getString("password"));
                admin.setRole(Role.ADMIN);
                admin.setNoHp(rs.getString("no_hp"));
                
                Timestamp createdAt = rs.getTimestamp("created_at");
                Timestamp updatedAt = rs.getTimestamp("updated_at");
                if (createdAt != null) admin.setCreatedAt(createdAt.toLocalDateTime());
                if (updatedAt != null) admin.setUpdatedAt(updatedAt.toLocalDateTime());
                
                admins.add(admin);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding all admins with user details", e);
        }
        
        return admins;
    }

    public boolean deleteByUserId(int userId) throws DatabaseException {
        String sql = "DELETE FROM admin WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting admin by user_id: " + userId, e);
        }
    }
}