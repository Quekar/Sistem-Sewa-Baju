package com.mycompany.sewabaju.dao;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.models.Penyewaan;
import com.mycompany.sewabaju.models.enums.StatusPenyewaan;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PenyewaanDAO extends BaseDAO<Penyewaan> {
    
    @Override
    protected String getTableName() {
        return "penyewaan";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "sewa_id";
    }
    
    @Override
    protected Penyewaan mapResultSetToEntity(ResultSet rs) throws SQLException {
        Penyewaan penyewaan = new Penyewaan();
        penyewaan.setSewaId(rs.getInt("sewa_id"));
        penyewaan.setUserId(rs.getInt("user_id"));
        
        Date tglSewa = rs.getDate("tgl_sewa");
        Date tglKembali = rs.getDate("tgl_kembali");
        Date tglKembaliAktual = rs.getDate("tgl_kembali_aktual");
        
        if (tglSewa != null) penyewaan.setTglSewa(tglSewa.toLocalDate());
        if (tglKembali != null) penyewaan.setTglKembali(tglKembali.toLocalDate());
        if (tglKembaliAktual != null) penyewaan.setTglKembaliAktual(tglKembaliAktual.toLocalDate());
        
        penyewaan.setTotalHarga(rs.getDouble("total_harga"));
        
        String statusStr = rs.getString("status");
        penyewaan.setStatus(StatusPenyewaan.fromString(statusStr));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) penyewaan.setCreatedAt(createdAt.toLocalDateTime());
        
        return penyewaan;
    }
    
    public int save(Penyewaan penyewaan) throws DatabaseException {
        String sql = "INSERT INTO penyewaan (user_id, tgl_sewa, tgl_kembali, tgl_kembali_aktual, " +
                     "total_harga, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        return executeInsertWithGeneratedKey(sql,
                penyewaan.getUserId(),
                penyewaan.getTglSewa(),
                penyewaan.getTglKembali(),
                penyewaan.getTglKembaliAktual(),
                penyewaan.getTotalHarga(),
                penyewaan.getStatus().name(),
                penyewaan.getCreatedAt()
        );
    }
    
    public boolean update(Penyewaan penyewaan) throws DatabaseException {
        String sql = "UPDATE penyewaan SET user_id = ?, tgl_sewa = ?, tgl_kembali = ?, " +
                     "tgl_kembali_aktual = ?, total_harga = ?, status = ? WHERE sewa_id = ?";
        
        int rowsAffected = executeUpdate(sql,
                penyewaan.getUserId(),
                penyewaan.getTglSewa(),
                penyewaan.getTglKembali(),
                penyewaan.getTglKembaliAktual(),
                penyewaan.getTotalHarga(),
                penyewaan.getStatus().name(),
                penyewaan.getSewaId()
        );
        
        return rowsAffected > 0;
    }
    
    public List<Penyewaan> findByUserId(int userId) throws DatabaseException {
        String sql = "SELECT * FROM penyewaan WHERE user_id = ? ORDER BY created_at DESC";
        return executeQuery(sql, userId);
    }
    
    public List<Penyewaan> findByStatus(StatusPenyewaan status) throws DatabaseException {
        String sql = "SELECT * FROM penyewaan WHERE status = ? ORDER BY created_at DESC";
        return executeQuery(sql, status.name());
    }
    
    public List<Penyewaan> findOverdue() throws DatabaseException {
        String sql = "SELECT * FROM penyewaan WHERE status = ? AND tgl_kembali < ? " +
                     "ORDER BY tgl_kembali";
        
        return executeQuery(sql, StatusPenyewaan.SEDANG_DISEWA.name(), LocalDate.now());
    }
    
    public List<Penyewaan> findByDateRange(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        String sql = "SELECT * FROM penyewaan WHERE tgl_sewa BETWEEN ? AND ? ORDER BY tgl_sewa DESC";
        return executeQuery(sql, startDate, endDate);
    }
    
    public boolean updateStatus(int sewaId, StatusPenyewaan newStatus) throws DatabaseException {
        String sql = "UPDATE penyewaan SET status = ? WHERE sewa_id = ?";
        
        int rowsAffected = executeUpdate(sql, newStatus.name(), sewaId);
        return rowsAffected > 0;
    }
    
    public boolean updateTglKembaliAktual(int sewaId, LocalDate tglKembaliAktual) throws DatabaseException {
        String sql = "UPDATE penyewaan SET tgl_kembali_aktual = ?, status = ? WHERE sewa_id = ?";
        
        int rowsAffected = executeUpdate(sql, 
                tglKembaliAktual, 
                StatusPenyewaan.DIKEMBALIKAN.name(), 
                sewaId);
        return rowsAffected > 0;
    }
    
    public double getTotalPendapatan(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        String sql = "SELECT SUM(total_harga) FROM penyewaan " +
                     "WHERE status = ? AND tgl_sewa BETWEEN ? AND ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, StatusPenyewaan.DIKEMBALIKAN.name());
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting total pendapatan", e);
        }
        
        return 0;
    }
    
    public int countByStatus(StatusPenyewaan status) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM penyewaan WHERE status = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting penyewaan by status", e);
        }
        
        return 0;
    }
    
    public List<Penyewaan> findRecent(int limit) throws DatabaseException {
        String sql = "SELECT * FROM penyewaan ORDER BY created_at DESC LIMIT ?";
        return executeQuery(sql, limit);
    }
}