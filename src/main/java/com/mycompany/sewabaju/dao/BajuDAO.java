package com.mycompany.sewabaju.dao;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.models.Baju;
import com.mycompany.sewabaju.models.Kategori;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BajuDAO extends BaseDAO<Baju> {
    
    @Override
    protected String getTableName() {
        return "baju";
    }
    
    @Override
    protected String getPrimaryKeyColumn() {
        return "baju_id";
    }
    
    @Override
    protected Baju mapResultSetToEntity(ResultSet rs) throws SQLException {
        Baju baju = new Baju();
        baju.setBajuId(rs.getInt("baju_id"));
        baju.setKategoriId(rs.getInt("kategori_id"));
        baju.setNamaBaju(rs.getString("nama_baju"));
        baju.setDeskripsi(rs.getString("deskripsi"));
        baju.setFoto(rs.getString("foto"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            baju.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        return baju;
    }
    
    public int save(Baju baju) throws DatabaseException {
        String sql = "INSERT INTO baju (kategori_id, nama_baju, deskripsi, foto, created_at) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        return executeInsertWithGeneratedKey(sql,
                baju.getKategoriId(),
                baju.getNamaBaju(),
                baju.getDeskripsi(),
                baju.getFoto(),
                baju.getCreatedAt()
        );
    }
    
    public boolean update(Baju baju) throws DatabaseException {
        String sql = "UPDATE baju SET kategori_id = ?, nama_baju = ?, deskripsi = ?, foto = ? " +
                     "WHERE baju_id = ?";
        
        int rowsAffected = executeUpdate(sql,
                baju.getKategoriId(),
                baju.getNamaBaju(),
                baju.getDeskripsi(),
                baju.getFoto(),
                baju.getBajuId()
        );
        
        return rowsAffected > 0;
    }
    
    public List<Baju> findByKategori(int kategoriId) throws DatabaseException {
        String sql = "SELECT * FROM baju WHERE kategori_id = ? ORDER BY nama_baju";
        return executeQuery(sql, kategoriId);
    }
    
    public List<Baju> findAllWithKategori() throws DatabaseException {
        String sql = "SELECT b.*, k.nama_kategori, k.deskripsi as kategori_deskripsi " +
                     "FROM baju b " +
                     "JOIN kategori k ON b.kategori_id = k.kategori_id " +
                     "ORDER BY b.nama_baju";
        
        List<Baju> bajuList = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Baju baju = mapResultSetToEntity(rs);
                
                Kategori kategori = new Kategori();
                kategori.setKategoriId(rs.getInt("kategori_id"));
                kategori.setNamaKategori(rs.getString("nama_kategori"));
                kategori.setDeskripsi(rs.getString("kategori_deskripsi"));
                
                baju.setKategori(kategori);
                bajuList.add(baju);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding all baju with kategori", e);
        }
        
        return bajuList;
    }
    
    public Baju findByIdWithKategori(int bajuId) throws DatabaseException {
        String sql = "SELECT b.*, k.nama_kategori, k.deskripsi as kategori_deskripsi " +
                     "FROM baju b " +
                     "JOIN kategori k ON b.kategori_id = k.kategori_id " +
                     "WHERE b.baju_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bajuId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Baju baju = mapResultSetToEntity(rs);
                    
                    Kategori kategori = new Kategori();
                    kategori.setKategoriId(rs.getInt("kategori_id"));
                    kategori.setNamaKategori(rs.getString("nama_kategori"));
                    kategori.setDeskripsi(rs.getString("kategori_deskripsi"));
                    
                    baju.setKategori(kategori);
                    return baju;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding baju by ID with kategori", e);
        }
        
        return null;
    }
    
    public List<Baju> search(String keyword) throws DatabaseException {
        String sql = "SELECT b.*, k.nama_kategori " +
                     "FROM baju b " +
                     "JOIN kategori k ON b.kategori_id = k.kategori_id " +
                     "WHERE b.nama_baju LIKE ? OR b.deskripsi LIKE ? OR k.nama_kategori LIKE ? " +
                     "ORDER BY b.nama_baju";
        
        String searchPattern = "%" + keyword + "%";
        List<Baju> bajuList = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Baju baju = mapResultSetToEntity(rs);
                    
                    Kategori kategori = new Kategori();
                    kategori.setKategoriId(rs.getInt("kategori_id"));
                    kategori.setNamaKategori(rs.getString("nama_kategori"));
                    baju.setKategori(kategori);
                    
                    bajuList.add(baju);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error searching baju", e);
        }
        
        return bajuList;
    }
    
    public List<Baju> findAllWithTotalStok() throws DatabaseException {
        String sql = "SELECT b.*, k.nama_kategori, COALESCE(SUM(db.stok), 0) as total_stok " +
                     "FROM baju b " +
                     "JOIN kategori k ON b.kategori_id = k.kategori_id " +
                     "LEFT JOIN detail_baju db ON b.baju_id = db.baju_id " +
                     "GROUP BY b.baju_id " +
                     "ORDER BY b.nama_baju";
        
        List<Baju> bajuList = new ArrayList<>();
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Baju baju = mapResultSetToEntity(rs);
                
                Kategori kategori = new Kategori();
                kategori.setKategoriId(rs.getInt("kategori_id"));
                kategori.setNamaKategori(rs.getString("nama_kategori"));
                baju.setKategori(kategori);
                
                // Note: total_stok bisa disimpan di variable transient jika perlu
                // Atau return sebagai Map<Baju, Integer> di Service layer
                
                bajuList.add(baju);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding baju with total stok", e);
        }
        
        return bajuList;
    }
    
    public List<Baju> findMostPopular(int limit) throws DatabaseException {
        String sql = "SELECT b.*, k.nama_kategori, COUNT(dp.detail_sewa_id) as total_sewa " +
                     "FROM baju b " +
                     "JOIN kategori k ON b.kategori_id = k.kategori_id " +
                     "LEFT JOIN detail_baju db ON b.baju_id = db.baju_id " +
                     "LEFT JOIN detail_penyewaan dp ON db.detail_baju_id = dp.detail_baju_id " +
                     "GROUP BY b.baju_id " +
                     "ORDER BY total_sewa DESC " +
                     "LIMIT ?";
        
        List<Baju> bajuList = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Baju baju = mapResultSetToEntity(rs);
                    
                    Kategori kategori = new Kategori();
                    kategori.setKategoriId(rs.getInt("kategori_id"));
                    kategori.setNamaKategori(rs.getString("nama_kategori"));
                    baju.setKategori(kategori);
                    
                    bajuList.add(baju);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error finding most popular baju", e);
        }
        
        return bajuList;
    }
}