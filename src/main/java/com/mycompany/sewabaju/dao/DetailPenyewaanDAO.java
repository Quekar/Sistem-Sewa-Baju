package com.mycompany.sewabaju.dao;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.models.DetailPenyewaan;
import com.mycompany.sewabaju.models.enums.Kondisi;
import java.sql.*;
import java.util.List;

public class DetailPenyewaanDAO extends BaseDAO<DetailPenyewaan> {
    
    @Override
    protected String getTableName() {
        return "detail_penyewaan";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "detail_sewa_id";
    }
    
    @Override
    protected DetailPenyewaan mapResultSetToEntity(ResultSet rs) throws SQLException {
        DetailPenyewaan detail = new DetailPenyewaan();
        detail.setDetailSewaId(rs.getInt("detail_sewa_id"));
        detail.setSewaId(rs.getInt("sewa_id"));
        detail.setDetailBajuId(rs.getInt("detail_baju_id"));
        detail.setJumlah(rs.getInt("jumlah"));
        detail.setHargaPerItem(rs.getDouble("harga_per_item"));
        detail.setSubtotal(rs.getDouble("subtotal"));
        
        String kondisiStr = rs.getString("kondisi_saat_kembali");
        if (kondisiStr != null) {
            detail.setKondisiSaatKembali(Kondisi.fromString(kondisiStr));
        }
        
        detail.setKeteranganKerusakan(rs.getString("keterangan_kerusakan"));
        
        return detail;
    }
    
    public int save(DetailPenyewaan detail) throws DatabaseException {
        String sql = "INSERT INTO detail_penyewaan (sewa_id, detail_baju_id, jumlah, " +
                     "harga_per_item, subtotal, kondisi_saat_kembali, keterangan_kerusakan) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        return executeInsertWithGeneratedKey(sql,
                detail.getSewaId(),
                detail.getDetailBajuId(),
                detail.getJumlah(),
                detail.getHargaPerItem(),
                detail.getSubtotal(),
                detail.getKondisiSaatKembali() != null ? detail.getKondisiSaatKembali().name() : null,
                detail.getKeteranganKerusakan()
        );
    }
    
    public boolean update(DetailPenyewaan detail) throws DatabaseException {
        String sql = "UPDATE detail_penyewaan SET sewa_id = ?, detail_baju_id = ?, jumlah = ?, " +
                     "harga_per_item = ?, subtotal = ?, kondisi_saat_kembali = ?, " +
                     "keterangan_kerusakan = ? WHERE detail_sewa_id = ?";
        
        int rowsAffected = executeUpdate(sql,
                detail.getSewaId(),
                detail.getDetailBajuId(),
                detail.getJumlah(),
                detail.getHargaPerItem(),
                detail.getSubtotal(),
                detail.getKondisiSaatKembali() != null ? detail.getKondisiSaatKembali().name() : null,
                detail.getKeteranganKerusakan(),
                detail.getDetailSewaId()
        );
        
        return rowsAffected > 0;
    }
    
    public List<DetailPenyewaan> findBySewaId(int sewaId) throws DatabaseException {
        String sql = "SELECT * FROM detail_penyewaan WHERE sewa_id = ?";
        return executeQuery(sql, sewaId);
    }
    
    public boolean updateKondisiKembali(int detailSewaId, Kondisi kondisi, String keterangan) 
            throws DatabaseException {
        String sql = "UPDATE detail_penyewaan SET kondisi_saat_kembali = ?, " +
                     "keterangan_kerusakan = ? WHERE detail_sewa_id = ?";
        
        int rowsAffected = executeUpdate(sql,
                kondisi != null ? kondisi.name() : null,
                keterangan,
                detailSewaId
        );
        
        return rowsAffected > 0;
    }
    
    public double getTotalBySewaId(int sewaId) throws DatabaseException {
        String sql = "SELECT SUM(subtotal) FROM detail_penyewaan WHERE sewa_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sewaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting total by sewa_id", e);
        }
        
        return 0;
    }
    
    public int getJumlahItemBySewaId(int sewaId) throws DatabaseException {
        String sql = "SELECT SUM(jumlah) FROM detail_penyewaan WHERE sewa_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sewaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting jumlah item", e);
        }
        
        return 0;
    }
    
    public List<DetailPenyewaan> findWithDamage() throws DatabaseException {
        String sql = "SELECT * FROM detail_penyewaan WHERE kondisi_saat_kembali IN (?, ?)";
        return executeQuery(sql, Kondisi.RUSAK_RINGAN.name(), Kondisi.RUSAK_BERAT.name());
    }
    
    public boolean deleteBySewaId(int sewaId) throws DatabaseException {
        String sql = "DELETE FROM detail_penyewaan WHERE sewa_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, sewaId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting detail penyewaan by sewa_id", e);
        }
    }
}