package com.mycompany.sewabaju.dao;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.models.Baju;
import com.mycompany.sewabaju.models.Favorit;
import com.mycompany.sewabaju.models.Kategori;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoritDAO extends BaseDAO<Favorit> {
    
    @Override
    protected String getTableName() {
        return "favorit";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "favorit_id";
    }
    
    @Override
    protected Favorit mapResultSetToEntity(ResultSet rs) throws SQLException {
        Favorit favorit = new Favorit();
        favorit.setFavoritId(rs.getInt("favorit_id"));
        favorit.setPelangganId(rs.getInt("pelanggan_id"));
        favorit.setBajuId(rs.getInt("baju_id"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) favorit.setCreatedAt(createdAt.toLocalDateTime());
        
        return favorit;
    }
    
    public int save(Favorit favorit) throws DatabaseException {
        String sql = "INSERT INTO favorit (pelanggan_id, baju_id, created_at) VALUES (?, ?, ?)";
        
        return executeInsertWithGeneratedKey(sql,
                favorit.getPelangganId(),
                favorit.getBajuId(),
                favorit.getCreatedAt()
        );
    }
    
    public boolean delete(int favoritId) throws DatabaseException {
        return super.delete(favoritId);
    }
    
    public List<Favorit> findByPelangganId(int pelangganId) throws DatabaseException {
        String sql = "SELECT * FROM favorit WHERE pelanggan_id = ? ORDER BY created_at DESC";
        return executeQuery(sql, pelangganId);
    }
    
    public List<Favorit> findByBajuId(int bajuId) throws DatabaseException {
        String sql = "SELECT * FROM favorit WHERE baju_id = ? ORDER BY created_at DESC";
        return executeQuery(sql, bajuId);
    }
    
    public boolean isFavorit(int pelangganId, int bajuId) throws DatabaseException {
        String sql = "SELECT 1 FROM favorit WHERE pelanggan_id = ? AND baju_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, pelangganId);
            stmt.setInt(2, bajuId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking favorit", e);
        }
    }
    
    public Favorit findByPelangganIdAndBajuId(int pelangganId, int bajuId) throws DatabaseException {
        String sql = "SELECT * FROM favorit WHERE pelanggan_id = ? AND baju_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, pelangganId);
            stmt.setInt(2, bajuId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding favorit", e);
        }
        
        return null;
    }
    
    /**
     * Toggle favorit (add if not exist, remove if exist)
     * @return true if added, false if removed
     */
    public boolean toggle(int pelangganId, int bajuId) throws DatabaseException {
        if (isFavorit(pelangganId, bajuId)) {
            String sql = "DELETE FROM favorit WHERE pelanggan_id = ? AND baju_id = ?";
            executeUpdate(sql, pelangganId, bajuId);
            return false;
        } else {
            Favorit favorit = new Favorit(pelangganId, bajuId);
            save(favorit);
            return true;
        }
    }
    
    public int countByBajuId(int bajuId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM favorit WHERE baju_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bajuId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting favorit by baju_id", e);
        }
        
        return 0;
    }
    
    public int countByPelangganId(int pelangganId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM favorit WHERE pelanggan_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, pelangganId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting favorit by pelanggan_id", e);
        }
        
        return 0;
    }
    
    public List<Baju> getTopFavoritBaju(int limit) throws DatabaseException {
        String sql = "SELECT b.*, k.nama_kategori, COUNT(f.favorit_id) as total_favorit " +
                     "FROM baju b " +
                     "JOIN kategori k ON b.kategori_id = k.kategori_id " +
                     "LEFT JOIN favorit f ON b.baju_id = f.baju_id " +
                     "GROUP BY b.baju_id " +
                     "ORDER BY total_favorit DESC " +
                     "LIMIT ?";
        
        List<Baju> bajuList = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Baju baju = new Baju();
                    baju.setBajuId(rs.getInt("baju_id"));
                    baju.setKategoriId(rs.getInt("kategori_id"));
                    baju.setNamaBaju(rs.getString("nama_baju"));
                    baju.setDeskripsi(rs.getString("deskripsi"));
                    baju.setFoto(rs.getString("foto"));
                    
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) baju.setCreatedAt(createdAt.toLocalDateTime());
                    
                    Kategori kategori = new Kategori();
                    kategori.setKategoriId(rs.getInt("kategori_id"));
                    kategori.setNamaKategori(rs.getString("nama_kategori"));
                    baju.setKategori(kategori);
                    
                    bajuList.add(baju);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting top favorit baju", e);
        }
        
        return bajuList;
    }
    
    public List<Baju> getFavoritBajuByPelangganId(int pelangganId) throws DatabaseException {
        String sql = "SELECT b.*, k.nama_kategori " +
                     "FROM favorit f " +
                     "JOIN baju b ON f.baju_id = b.baju_id " +
                     "JOIN kategori k ON b.kategori_id = k.kategori_id " +
                     "WHERE f.pelanggan_id = ? " +
                     "ORDER BY f.created_at DESC";
        
        List<Baju> bajuList = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, pelangganId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Baju baju = new Baju();
                    baju.setBajuId(rs.getInt("baju_id"));
                    baju.setKategoriId(rs.getInt("kategori_id"));
                    baju.setNamaBaju(rs.getString("nama_baju"));
                    baju.setDeskripsi(rs.getString("deskripsi"));
                    baju.setFoto(rs.getString("foto"));
                    
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) baju.setCreatedAt(createdAt.toLocalDateTime());
                    
                    Kategori kategori = new Kategori();
                    kategori.setKategoriId(rs.getInt("kategori_id"));
                    kategori.setNamaKategori(rs.getString("nama_kategori"));
                    baju.setKategori(kategori);
                    
                    bajuList.add(baju);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error getting favorit baju by pelanggan_id", e);
        }
        
        return bajuList;
    }
    
    public boolean deleteByPelangganId(int pelangganId) throws DatabaseException {
        String sql = "DELETE FROM favorit WHERE pelanggan_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, pelangganId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting favorit by pelanggan_id", e);
        }
    }
    
    public boolean deleteByBajuId(int bajuId) throws DatabaseException {
        String sql = "DELETE FROM favorit WHERE baju_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bajuId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting favorit by baju_id", e);
        }
    }
}