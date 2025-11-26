package com.mycompany.sewabaju.dao;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.models.Pelanggan;
import com.mycompany.sewabaju.models.enums.Role;
import java.sql.*;

public class PelangganDAO extends BaseDAO<Pelanggan> {
    
    @Override
    protected String getTableName() {
        return "pelanggan";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "pelanggan_id";
    }
    
    @Override
    protected Pelanggan mapResultSetToEntity(ResultSet rs) throws SQLException {
        Pelanggan pelanggan = new Pelanggan();
        pelanggan.setPelangganId(rs.getInt("pelanggan_id"));
        pelanggan.setUserId(rs.getInt("user_id"));
        pelanggan.setAlamat(rs.getString("alamat"));
        pelanggan.setPoinLoyalitas(rs.getInt("poin_loyalitas"));
        
        return pelanggan;
    }
    
    public int save(Pelanggan pelanggan) throws DatabaseException {
        String sql = "INSERT INTO pelanggan (user_id, alamat, poin_loyalitas) VALUES (?, ?, ?)";
        
        return executeInsertWithGeneratedKey(sql,
                pelanggan.getUserId(),
                pelanggan.getAlamat(),
                pelanggan.getPoinLoyalitas()
        );
    }
    
    public boolean update(Pelanggan pelanggan) throws DatabaseException {
        String sql = "UPDATE pelanggan SET alamat = ?, poin_loyalitas = ? WHERE pelanggan_id = ?";
        
        int rowsAffected = executeUpdate(sql,
                pelanggan.getAlamat(),
                pelanggan.getPoinLoyalitas(),
                pelanggan.getPelangganId()
        );
        
        return rowsAffected > 0;
    }
    
    public Pelanggan findByUserId(int userId) throws DatabaseException {
        String sql = "SELECT * FROM pelanggan WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding pelanggan by user_id: " + userId, e);
        }
        
        return null;
    }
    
    public Pelanggan findByIdWithUserDetails(int pelangganId) throws DatabaseException {
        String sql = "SELECT p.*, u.* FROM pelanggan p " +
                     "JOIN user u ON p.user_id = u.user_id " +
                     "WHERE p.pelanggan_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, pelangganId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Pelanggan pelanggan = mapResultSetToEntity(rs);
                    pelanggan.setUserId(rs.getInt("user_id"));
                    pelanggan.setNama(rs.getString("nama"));
                    pelanggan.setEmail(rs.getString("email"));
                    pelanggan.setPassword(rs.getString("password"));
                    pelanggan.setRole(Role.PELANGGAN);
                    pelanggan.setNoHp(rs.getString("no_hp"));
                    
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (createdAt != null) pelanggan.setCreatedAt(createdAt.toLocalDateTime());
                    if (updatedAt != null) pelanggan.setUpdatedAt(updatedAt.toLocalDateTime());
                    
                    return pelanggan;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding pelanggan with user details", e);
        }
        
        return null;
    }
    
    public java.util.List<Pelanggan> findAllWithUserDetails() throws DatabaseException {
        String sql = "SELECT p.*, u.* FROM pelanggan p " +
                     "JOIN user u ON p.user_id = u.user_id " +
                     "ORDER BY p.pelanggan_id";
        
        java.util.List<Pelanggan> pelangganList = new java.util.ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Pelanggan pelanggan = mapResultSetToEntity(rs);
                pelanggan.setUserId(rs.getInt("user_id"));
                pelanggan.setNama(rs.getString("nama"));
                pelanggan.setEmail(rs.getString("email"));
                pelanggan.setPassword(rs.getString("password"));
                pelanggan.setRole(Role.PELANGGAN);
                pelanggan.setNoHp(rs.getString("no_hp"));
                
                Timestamp createdAt = rs.getTimestamp("created_at");
                Timestamp updatedAt = rs.getTimestamp("updated_at");
                if (createdAt != null) pelanggan.setCreatedAt(createdAt.toLocalDateTime());
                if (updatedAt != null) pelanggan.setUpdatedAt(updatedAt.toLocalDateTime());
                
                pelangganList.add(pelanggan);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding all pelanggan with user details", e);
        }
        
        return pelangganList;
    }
    
    public boolean updatePoinLoyalitas(int pelangganId, int newPoin) throws DatabaseException {
        String sql = "UPDATE pelanggan SET poin_loyalitas = ? WHERE pelanggan_id = ?";
        
        int rowsAffected = executeUpdate(sql, newPoin, pelangganId);
        return rowsAffected > 0;
    }
    
    public boolean tambahPoin(int pelangganId, int poin) throws DatabaseException {
        String sql = "UPDATE pelanggan SET poin_loyalitas = poin_loyalitas + ? WHERE pelanggan_id = ?";
        
        int rowsAffected = executeUpdate(sql, poin, pelangganId);
        return rowsAffected > 0;
    }
    
    public boolean kurangiPoin(int pelangganId, int poin) throws DatabaseException {
        String sql = "UPDATE pelanggan SET poin_loyalitas = poin_loyalitas - ? " +
                     "WHERE pelanggan_id = ? AND poin_loyalitas >= ?";
        
        int rowsAffected = executeUpdate(sql, poin, pelangganId, poin);
        return rowsAffected > 0;
    }
    
    public java.util.List<Pelanggan> getTopByPoinLoyalitas(int limit) throws DatabaseException {
        String sql = "SELECT p.*, u.* FROM pelanggan p " +
                     "JOIN user u ON p.user_id = u.user_id " +
                     "ORDER BY p.poin_loyalitas DESC LIMIT ?";
        
        java.util.List<Pelanggan> pelangganList = new java.util.ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Pelanggan pelanggan = mapResultSetToEntity(rs);
                    pelanggan.setNama(rs.getString("nama"));
                    pelanggan.setEmail(rs.getString("email"));
                    
                    pelangganList.add(pelanggan);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting top pelanggan by poin", e);
        }
        
        return pelangganList;
    }
    
    public boolean deleteByUserId(int userId) throws DatabaseException {
        String sql = "DELETE FROM pelanggan WHERE user_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting pelanggan by user_id: " + userId, e);
        }
    }
}