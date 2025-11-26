package com.mycompany.sewabaju.dao;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.models.Kategori;
import java.sql.*;

public class KategoriDAO extends BaseDAO<Kategori> {
    
    @Override
    protected String getTableName() {
        return "kategori";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "kategori_id";
    }
    
    @Override
    protected Kategori mapResultSetToEntity(ResultSet rs) throws SQLException {
        Kategori kategori = new Kategori();
        kategori.setKategoriId(rs.getInt("kategori_id"));
        kategori.setNamaKategori(rs.getString("nama_kategori"));
        kategori.setDeskripsi(rs.getString("deskripsi"));
        
        return kategori;
    }
    
    public int save(Kategori kategori) throws DatabaseException {
        String sql = "INSERT INTO kategori (nama_kategori, deskripsi) VALUES (?, ?)";
        
        return executeInsertWithGeneratedKey(sql,
                kategori.getNamaKategori(),
                kategori.getDeskripsi()
        );
    }
    
    public boolean update(Kategori kategori) throws DatabaseException {
        String sql = "UPDATE kategori SET nama_kategori = ?, deskripsi = ? WHERE kategori_id = ?";
        
        int rowsAffected = executeUpdate(sql,
                kategori.getNamaKategori(),
                kategori.getDeskripsi(),
                kategori.getKategoriId()
        );
        
        return rowsAffected > 0;
    }
    
    public Kategori findByName(String namaKategori) throws DatabaseException {
        String sql = "SELECT * FROM kategori WHERE nama_kategori = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, namaKategori);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding kategori by name: " + namaKategori, e);
        }
        
        return null;
    }
    
    public boolean nameExists(String namaKategori) throws DatabaseException {
        String sql = "SELECT 1 FROM kategori WHERE nama_kategori = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, namaKategori);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking kategori name existence", e);
        }
    }
    
    public boolean nameExistsForOtherId(String namaKategori, int excludeId) throws DatabaseException {
        String sql = "SELECT 1 FROM kategori WHERE nama_kategori = ? AND kategori_id != ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, namaKategori);
            stmt.setInt(2, excludeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking kategori name for other ID", e);
        }
    }
    
    public int countBajuInKategori(int kategoriId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM baju WHERE kategori_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, kategoriId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting baju in kategori", e);
        }
        
        return 0;
    }
    
    public java.util.List<Kategori> findAllWithBajuCount() throws DatabaseException {
        String sql = "SELECT k.*, COUNT(b.baju_id) as total_baju " +
                     "FROM kategori k " +
                     "LEFT JOIN baju b ON k.kategori_id = b.kategori_id " +
                     "GROUP BY k.kategori_id " +
                     "ORDER BY k.nama_kategori";
        
        java.util.List<Kategori> kategoris = new java.util.ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Kategori kategori = mapResultSetToEntity(rs);
                // Note: total_baju bisa disimpan di variable tambahan jika perlu
                // Atau bisa pakai Map<Kategori, Integer> di Service layer
                kategoris.add(kategori);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding all kategori with baju count", e);
        }
        
        return kategoris;
    }
}