package com.mycompany.sewabaju.dao;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.models.Pembayaran;
import com.mycompany.sewabaju.models.enums.MetodePembayaran;
import com.mycompany.sewabaju.models.enums.StatusPembayaran;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class PembayaranDAO extends BaseDAO<Pembayaran> {
    
    @Override
    protected String getTableName() {
        return "pembayaran";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "pembayaran_id";
    }
    
    @Override
    protected Pembayaran mapResultSetToEntity(ResultSet rs) throws SQLException {
        Pembayaran pembayaran = new Pembayaran();
        pembayaran.setPembayaranId(rs.getInt("pembayaran_id"));
        pembayaran.setSewaId(rs.getInt("sewa_id"));
        
        String metodeStr = rs.getString("metode_pembayaran");
        pembayaran.setMetodePembayaran(MetodePembayaran.fromString(metodeStr));
        
        pembayaran.setJumlah(rs.getDouble("jumlah"));
        pembayaran.setBuktiPembayaran(rs.getString("bukti_pembayaran"));
        
        String statusStr = rs.getString("status");
        pembayaran.setStatus(StatusPembayaran.fromString(statusStr));
        
        Timestamp tanggalBayar = rs.getTimestamp("tanggal_bayar");
        if (tanggalBayar != null) pembayaran.setTanggalBayar(tanggalBayar.toLocalDateTime());
        
        int verifiedBy = rs.getInt("verified_by");
        if (!rs.wasNull()) pembayaran.setVerifiedBy(verifiedBy);
        
        Timestamp verifiedAt = rs.getTimestamp("verified_at");
        if (verifiedAt != null) pembayaran.setVerifiedAt(verifiedAt.toLocalDateTime());
        
        return pembayaran;
    }
    
    public int save(Pembayaran pembayaran) throws DatabaseException {
        String sql = "INSERT INTO pembayaran (sewa_id, metode_pembayaran, jumlah, bukti_pembayaran, " +
                     "status, tanggal_bayar, verified_by, verified_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        return executeInsertWithGeneratedKey(sql,
                pembayaran.getSewaId(),
                pembayaran.getMetodePembayaran().name(),
                pembayaran.getJumlah(),
                pembayaran.getBuktiPembayaran(),
                pembayaran.getStatus().name(),
                pembayaran.getTanggalBayar(),
                pembayaran.getVerifiedBy(),
                pembayaran.getVerifiedAt()
        );
    }
    
    public boolean update(Pembayaran pembayaran) throws DatabaseException {
        String sql = "UPDATE pembayaran SET sewa_id = ?, metode_pembayaran = ?, jumlah = ?, " +
                     "bukti_pembayaran = ?, status = ?, verified_by = ?, verified_at = ? " +
                     "WHERE pembayaran_id = ?";
        
        int rowsAffected = executeUpdate(sql,
                pembayaran.getSewaId(),
                pembayaran.getMetodePembayaran().name(),
                pembayaran.getJumlah(),
                pembayaran.getBuktiPembayaran(),
                pembayaran.getStatus().name(),
                pembayaran.getVerifiedBy(),
                pembayaran.getVerifiedAt(),
                pembayaran.getPembayaranId()
        );
        
        return rowsAffected > 0;
    }
    
    public Pembayaran findBySewaId(int sewaId) throws DatabaseException {
        String sql = "SELECT * FROM pembayaran WHERE sewa_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sewaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding pembayaran by sewa_id", e);
        }
        
        return null;
    }
    
    public List<Pembayaran> findByStatus(StatusPembayaran status) throws DatabaseException {
        String sql = "SELECT * FROM pembayaran WHERE status = ? ORDER BY tanggal_bayar DESC";
        return executeQuery(sql, status.name());
    }
    
    public List<Pembayaran> findPending() throws DatabaseException {
        return findByStatus(StatusPembayaran.MENUNGGU_VERIFIKASI);
    }
    
    public boolean approve(int pembayaranId, int adminId) throws DatabaseException {
        String sql = "UPDATE pembayaran SET status = ?, verified_by = ?, verified_at = ? " +
                     "WHERE pembayaran_id = ?";
        
        int rowsAffected = executeUpdate(sql,
                StatusPembayaran.BERHASIL.name(),
                adminId,
                LocalDateTime.now(),
                pembayaranId
        );
        
        return rowsAffected > 0;
    }
    
    public boolean reject(int pembayaranId, int adminId) throws DatabaseException {
        String sql = "UPDATE pembayaran SET status = ?, verified_by = ?, verified_at = ? " +
                     "WHERE pembayaran_id = ?";
        
        int rowsAffected = executeUpdate(sql,
                StatusPembayaran.DITOLAK.name(),
                adminId,
                LocalDateTime.now(),
                pembayaranId
        );
        
        return rowsAffected > 0;
    }
    
    public boolean updateStatus(int pembayaranId, StatusPembayaran status) throws DatabaseException {
        String sql = "UPDATE pembayaran SET status = ? WHERE pembayaran_id = ?";
        
        int rowsAffected = executeUpdate(sql, status.name(), pembayaranId);
        return rowsAffected > 0;
    }
    
    public boolean updateBuktiPembayaran(int pembayaranId, String buktiBaru) throws DatabaseException {
        String sql = "UPDATE pembayaran SET bukti_pembayaran = ?, status = ? WHERE pembayaran_id = ?";
        
        int rowsAffected = executeUpdate(sql,
                buktiBaru,
                StatusPembayaran.MENUNGGU_VERIFIKASI.name(),
                pembayaranId
        );
        
        return rowsAffected > 0;
    }
    
    public int countByStatus(StatusPembayaran status) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM pembayaran WHERE status = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting pembayaran by status", e);
        }
        
        return 0;
    }
    
    public boolean deleteBySewaId(int sewaId) throws DatabaseException {
        String sql = "DELETE FROM pembayaran WHERE sewa_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sewaId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting pembayaran by sewa_id", e);
        }
    }
}