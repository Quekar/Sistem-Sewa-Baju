package com.mycompany.sewabaju.dao;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.models.DetailBaju;
import com.mycompany.sewabaju.models.enums.Kondisi;
import com.mycompany.sewabaju.models.enums.Ukuran;
import java.sql.*;
import java.util.List;

public class DetailBajuDAO extends BaseDAO<DetailBaju> {
    
    @Override
    protected String getTableName() {
        return "detail_baju";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "detail_baju_id";
    }
    
    @Override
    protected DetailBaju mapResultSetToEntity(ResultSet rs) throws SQLException {
        DetailBaju detail = new DetailBaju();
        detail.setDetailBajuId(rs.getInt("detail_baju_id"));
        detail.setBajuId(rs.getInt("baju_id"));
        
        String ukuranStr = rs.getString("ukuran");
        detail.setUkuran(Ukuran.fromString(ukuranStr));
        
        detail.setHargaSewa(rs.getDouble("harga_sewa"));
        detail.setStok(rs.getInt("stok"));
        
        String kondisiStr = rs.getString("kondisi");
        detail.setKondisi(Kondisi.fromString(kondisiStr));
        
        return detail;
    }
    
    public int save(DetailBaju detail) throws DatabaseException {
        String sql = "INSERT INTO detail_baju (baju_id, ukuran, harga_sewa, stok, kondisi) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        return executeInsertWithGeneratedKey(sql,
                detail.getBajuId(),
                detail.getUkuran().name(),
                detail.getHargaSewa(),
                detail.getStok(),
                detail.getKondisi().name()
        );
    }
    
    public boolean update(DetailBaju detail) throws DatabaseException {
        String sql = "UPDATE detail_baju SET baju_id = ?, ukuran = ?, harga_sewa = ?, " +
                     "stok = ?, kondisi = ? WHERE detail_baju_id = ?";
        
        int rowsAffected = executeUpdate(sql,
                detail.getBajuId(),
                detail.getUkuran().name(),
                detail.getHargaSewa(),
                detail.getStok(),
                detail.getKondisi().name(),
                detail.getDetailBajuId()
        );
        
        return rowsAffected > 0;
    }
    
    public List<DetailBaju> findByBajuId(int bajuId) throws DatabaseException {
        String sql = "SELECT * FROM detail_baju WHERE baju_id = ? ORDER BY ukuran";
        return executeQuery(sql, bajuId);
    }
    
    public DetailBaju findByBajuIdAndUkuran(int bajuId, Ukuran ukuran) throws DatabaseException {
        String sql = "SELECT * FROM detail_baju WHERE baju_id = ? AND ukuran = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bajuId);
            stmt.setString(2, ukuran.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding detail baju by baju_id and ukuran", e);
        }
        
        return null;
    }
    
    public List<DetailBaju> findAvailable() throws DatabaseException {
        String sql = "SELECT * FROM detail_baju WHERE stok > 0 ORDER BY baju_id, ukuran";
        return executeQuery(sql);
    }
    
    public List<DetailBaju> findAvailableByBajuId(int bajuId) throws DatabaseException {
        String sql = "SELECT * FROM detail_baju WHERE baju_id = ? AND stok > 0 ORDER BY ukuran";
        return executeQuery(sql, bajuId);
    }
    
    public boolean updateStok(int detailBajuId, int newStok) throws DatabaseException {
        String sql = "UPDATE detail_baju SET stok = ? WHERE detail_baju_id = ?";
        
        int rowsAffected = executeUpdate(sql, newStok, detailBajuId);
        return rowsAffected > 0;
    }
    
    public boolean kurangiStok(int detailBajuId, int jumlah) throws DatabaseException {
        String sql = "UPDATE detail_baju SET stok = stok - ? " +
                     "WHERE detail_baju_id = ? AND stok >= ?";
        
        int rowsAffected = executeUpdate(sql, jumlah, detailBajuId, jumlah);
        return rowsAffected > 0;
    }
    
    public boolean tambahStok(int detailBajuId, int jumlah) throws DatabaseException {
        String sql = "UPDATE detail_baju SET stok = stok + ? WHERE detail_baju_id = ?";
        
        int rowsAffected = executeUpdate(sql, jumlah, detailBajuId);
        return rowsAffected > 0;
    }
    
    public boolean isStokCukup(int detailBajuId, int jumlah) throws DatabaseException {
        String sql = "SELECT stok FROM detail_baju WHERE detail_baju_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, detailBajuId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int stok = rs.getInt("stok");
                    return stok >= jumlah;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking stok", e);
        }
        
        return false;
    }
    
    public int getTotalStokByBajuId(int bajuId) throws DatabaseException {
        String sql = "SELECT SUM(stok) FROM detail_baju WHERE baju_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bajuId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting total stok", e);
        }
        
        return 0;
    }
    
    public boolean updateKondisi(int detailBajuId, Kondisi kondisi) throws DatabaseException {
        String sql = "UPDATE detail_baju SET kondisi = ? WHERE detail_baju_id = ?";
        
        int rowsAffected = executeUpdate(sql, kondisi.name(), detailBajuId);
        return rowsAffected > 0;
    }
    
    public boolean deleteByBajuId(int bajuId) throws DatabaseException {
        String sql = "DELETE FROM detail_baju WHERE baju_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bajuId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting detail baju by baju_id", e);
        }
    }
}