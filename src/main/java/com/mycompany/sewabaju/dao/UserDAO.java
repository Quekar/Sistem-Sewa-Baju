package com.mycompany.sewabaju.dao;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.models.Admin;
import com.mycompany.sewabaju.models.Pelanggan;
import com.mycompany.sewabaju.models.User;
import com.mycompany.sewabaju.models.enums.Role;
import java.sql.*;
import java.time.LocalDateTime;

public class UserDAO extends BaseDAO<User> {
    
    @Override
    protected String getTableName() {
        return "user";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "user_id";
    }
    
    @Override
    protected User mapResultSetToEntity(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        String nama = rs.getString("nama");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String roleStr = rs.getString("role");
        String noHp = rs.getString("no_hp");
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        Timestamp updatedAtTs = rs.getTimestamp("updated_at");
        
        Role role = Role.fromString(roleStr);
        
        User user;
        if (role == Role.ADMIN) {
            user = new Admin();
        } else {
            user = new Pelanggan();
        }
        
        user.setUserId(userId);
        user.setNama(nama);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setNoHp(noHp);
        if (createdAtTs != null) user.setCreatedAt(createdAtTs.toLocalDateTime());
        if (updatedAtTs != null) user.setUpdatedAt(updatedAtTs.toLocalDateTime());
        
        return user;
    }
    
    public int save(User user) throws DatabaseException {
        String sql = "INSERT INTO user (nama, email, password, role, no_hp, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        LocalDateTime now = LocalDateTime.now();
        
        return executeInsertWithGeneratedKey(sql,
                user.getNama(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().name(),
                user.getNoHp(),
                now,
                now
        );
    }
    
    public boolean update(User user) throws DatabaseException {
        String sql = "UPDATE user SET nama = ?, email = ?, password = ?, role = ?, " +
                     "no_hp = ?, updated_at = ? WHERE user_id = ?";
        
        int rowsAffected = executeUpdate(sql,
                user.getNama(),
                user.getEmail(),
                user.getPassword(),
                user.getRole().name(),
                user.getNoHp(),
                LocalDateTime.now(),
                user.getUserId()
        );
        
        return rowsAffected > 0;
    }
    
    public User findByEmail(String email) throws DatabaseException {
        String sql = "SELECT * FROM user WHERE email = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by email: " + email, e);
        }
        
        return null;
    }
    
    public User findByEmailAndPassword(String email, String password) throws DatabaseException {
        String sql = "SELECT * FROM user WHERE email = ? AND password = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding user by email and password", e);
        }
        
        return null;
    }
    
    public boolean emailExists(String email) throws DatabaseException {
        String sql = "SELECT 1 FROM user WHERE email = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking email existence", e);
        }
    }
    
    public java.util.List<User> findByRole(Role role) throws DatabaseException {
        String sql = "SELECT * FROM user WHERE role = ?";
        return executeQuery(sql, role.name());
    }
    
    public boolean updatePassword(int userId, String newPasswordHash) throws DatabaseException {
        String sql = "UPDATE user SET password = ?, updated_at = ? WHERE user_id = ?";
        
        int rowsAffected = executeUpdate(sql,
                newPasswordHash,
                LocalDateTime.now(),
                userId
        );
        
        return rowsAffected > 0;
    }
    
    public java.util.List<User> search(String keyword) throws DatabaseException {
        String sql = "SELECT * FROM user WHERE nama LIKE ? OR email LIKE ?";
        String searchPattern = "%" + keyword + "%";
        return executeQuery(sql, searchPattern, searchPattern);
    }
}