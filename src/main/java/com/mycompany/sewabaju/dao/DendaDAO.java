package com.mycompany.sewabaju.dao;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.models.Denda;
import com.mycompany.sewabaju.models.enums.JenisDenda;
import com.mycompany.sewabaju.models.enums.StatusBayarDenda;
import java.sql.*;
import java.util.List;

public class DendaDAO extends BaseDAO<Denda> {
    
    @Override
    protected String getTableName() {
        return "denda";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "denda_id";
    }
    
    @Override
    protected Denda mapResultSetToEntity(ResultSet rs) throws SQLException {
        Denda denda = new Denda();
        denda.setDendaId(rs.getInt("denda_id"));
        denda.setSewaId(rs.getInt("sewa_id"));
        
        String jenisStr = rs.getString("jenis_denda");
        denda.setJenisDenda(JenisDenda.fromString(jenisStr));
        
        denda.setJumlah(rs.getDouble("jumlah"));
        denda.setKeterangan(rs.getString("keterangan"));
        
        String statusStr = rs.getString("status_bayar");
        denda.setStatusBayar(StatusBayarDenda.fromString(statusStr));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) denda.setCreatedAt(createdAt.toLocalDateTime());
        
        return denda;
    }
    
    public int save(Denda denda) throws DatabaseException {
        String sql = "INSERT INTO denda (sewa_id, jenis_denda, jumlah, keterangan, " +
                     "status_bayar, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        
        return executeInsertWithGeneratedKey(sql,
                denda.getSewaId(),
                denda.getJenisDenda().name(),
                denda.getJumlah(),
                denda.getKeterangan(),
                denda.getStatusBayar().name(),
                denda.getCreatedAt()
        );
    }
    
    public boolean update(Denda denda) throws DatabaseException {
        String sql = "UPDATE denda SET sewa_id = ?, jenis_denda = ?, jumlah = ?, " +
                     "keterangan = ?, status_bayar = ? WHERE denda_id = ?";
        
        int rowsAffected = executeUpdate(sql,
                denda.getSewaId(),
                denda.getJenisDenda().name(),
                denda.getJumlah(),
                denda.getKeterangan(),
                denda.getStatusBayar().name(),
                denda.getDendaId()
        );
        
        return rowsAffected > 0;
    }
    
    public List<Denda> findBySewaId(int sewaId) throws DatabaseException {
        String sql = "SELECT * FROM denda WHERE sewa_id = ? ORDER BY created_at";
        return executeQuery(sql, sewaId);
    }
    
    public List<Denda> findUnpaid() throws DatabaseException {
        String sql = "SELECT * FROM denda WHERE status_bayar = ? ORDER BY created_at";
        return executeQuery(sql, StatusBayarDenda.BELUM_DIBAYAR.name());
    }
    
    public List<Denda> findByJenis(JenisDenda jenis) throws DatabaseException {
        String sql = "SELECT * FROM denda WHERE jenis_denda = ? ORDER BY created_at DESC";
        return executeQuery(sql, jenis.name());
    }
    
    public boolean markAsPaid(int dendaId) throws DatabaseException {
        String sql = "UPDATE denda SET status_bayar = ? WHERE denda_id = ?";
        
        int rowsAffected = executeUpdate(sql, StatusBayarDenda.SUDAH_DIBAYAR.name(), dendaId);
        return rowsAffected > 0;
    }
    
    public boolean updateStatusBayar(int dendaId, StatusBayarDenda status) throws DatabaseException {
        String sql = "UPDATE denda SET status_bayar = ? WHERE denda_id = ?";
        
        int rowsAffected = executeUpdate(sql, status.name(), dendaId);
        return rowsAffected > 0;
    }
    
    public double getTotalBySewaId(int sewaId) throws DatabaseException {
        String sql = "SELECT SUM(jumlah) FROM denda WHERE sewa_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sewaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting total denda by sewa_id", e);
        }
        
        return 0;
    }
    
    public double getTotalUnpaidBySewaId(int sewaId) throws DatabaseException {
        String sql = "SELECT SUM(jumlah) FROM denda WHERE sewa_id = ? AND status_bayar = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sewaId);
            stmt.setString(2, StatusBayarDenda.BELUM_DIBAYAR.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting total unpaid denda", e);
        }
        
        return 0;
    }
    
    public int countByStatus(StatusBayarDenda status) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM denda WHERE status_bayar = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting denda by status", e);
        }
        
        return 0;
    }
    
    public boolean deleteBySewaId(int sewaId) throws DatabaseException {
        String sql = "DELETE FROM denda WHERE sewa_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sewaId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting denda by sewa_id", e);
        }
    }
}