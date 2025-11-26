package com.mycompany.sewabaju.services;

import com.mycompany.sewabaju.dao.BajuDAO;
import com.mycompany.sewabaju.dao.DetailBajuDAO;
import com.mycompany.sewabaju.dao.DetailPenyewaanDAO;
import com.mycompany.sewabaju.dao.KategoriDAO;
import com.mycompany.sewabaju.database.DatabaseConnection;
import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.exceptions.ValidationException;
import com.mycompany.sewabaju.models.Baju;
import com.mycompany.sewabaju.models.DetailBaju;
import com.mycompany.sewabaju.models.Kategori;
import com.mycompany.sewabaju.models.enums.Kondisi;
import com.mycompany.sewabaju.models.enums.StatusPenyewaan;
import com.mycompany.sewabaju.models.enums.Ukuran;
import com.mycompany.sewabaju.utils.FileUtil;
import com.mycompany.sewabaju.utils.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BajuService {
    
    private final BajuDAO bajuDAO;
    private final DetailBajuDAO detailBajuDAO;
    private final DetailPenyewaanDAO detailPenyewaanDAO;
    private final KategoriDAO kategoriDAO;
    
    private static BajuService instance;
    
    private BajuService() {
        this.bajuDAO = new BajuDAO();
        this.detailBajuDAO = new DetailBajuDAO();
        this.detailPenyewaanDAO = new DetailPenyewaanDAO();
        this.kategoriDAO = new KategoriDAO();
    }
    
    public static BajuService getInstance() {
        if (instance == null) {
            synchronized (BajuService.class) {
                if (instance == null) {
                    instance = new BajuService();
                }
            }
        }
        return instance;
    }
    
    public List<Baju> getAllBaju() throws DatabaseException {
        try {
            List<Baju> bajuList = bajuDAO.findAllWithKategori();
            
            for (Baju baju : bajuList) {
                List<DetailBaju> details = detailBajuDAO.findByBajuId(baju.getBajuId());
                baju.setDetailBajuList(details);
            }
            
            return bajuList;
            
        } catch (Exception e) {
            throw new DatabaseException("Gagal mengambil data baju: " + e.getMessage(), e);
        }
    }
    
    public Baju getBajuById(int bajuId) throws DatabaseException {
        try {
            Baju baju = bajuDAO.findByIdWithKategori(bajuId);
            
            if (baju == null) {
                return null;
            }
            
            List<DetailBaju> details = detailBajuDAO.findByBajuId(bajuId);
            baju.setDetailBajuList(details);
            return baju;
            
        } catch (Exception e) {
            throw new DatabaseException("Gagal mengambil data baju: " + e.getMessage(), e);
        }
    }
    
    public List<Baju> searchBaju(String keyword) throws DatabaseException {
        try {
            if (ValidationUtil.isEmpty(keyword)) {
                return getAllBaju();
            }
            
            List<Baju> bajuList = bajuDAO.search(keyword);
            
            for (Baju baju : bajuList) {
                List<DetailBaju> details = detailBajuDAO.findByBajuId(baju.getBajuId());
                baju.setDetailBajuList(details);
            }
            
            return bajuList;
            
        } catch (Exception e) {
            throw new DatabaseException("Gagal search baju: " + e.getMessage(), e);
        }
    }
    
    public List<Baju> filterBaju(Integer kategoriId, Ukuran ukuran, 
                                  Double minHarga, Double maxHarga) throws DatabaseException {
        try {
            List<Baju> allBaju = getAllBaju();
            
            return allBaju.stream()
                .filter(baju -> {
                    if (kategoriId != null && baju.getKategoriId() != kategoriId) {
                        return false;
                    }
                    
                    if (ukuran != null) {
                        boolean hasUkuran = baju.getDetailBajuList().stream()
                            .anyMatch(d -> d.getUkuran() == ukuran && d.getStok() > 0);
                        if (!hasUkuran) return false;
                    }
                    
                    if (minHarga != null || maxHarga != null) {
                        List<DetailBaju> details = baju.getDetailBajuList();
                        if (details.isEmpty()) return false;
                        
                        double minBajuHarga = details.stream()
                            .mapToDouble(DetailBaju::getHargaSewa)
                            .min().orElse(0);
                        
                        double maxBajuHarga = details.stream()
                            .mapToDouble(DetailBaju::getHargaSewa)
                            .max().orElse(Double.MAX_VALUE);
                        
                        if (minHarga != null && maxBajuHarga < minHarga) return false;
                        if (maxHarga != null && minBajuHarga > maxHarga) return false;
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new DatabaseException("Gagal filter baju: " + e.getMessage(), e);
        }
    }
    
    public List<Baju> getAvailableBaju() throws DatabaseException {
        try {
            List<Baju> allBaju = getAllBaju();
            
            return allBaju.stream()
                .filter(Baju::isAvailable)
                .collect(Collectors.toList());
            
        } catch (Exception e) {
            throw new DatabaseException("Gagal mengambil baju available: " + e.getMessage(), e);
        }
    }
    
    public List<Baju> getMostPopularBaju(int limit) throws DatabaseException {
        try {
            List<Baju> popularBaju = bajuDAO.findMostPopular(limit);
            
            for (Baju baju : popularBaju) {
                List<DetailBaju> details = detailBajuDAO.findByBajuId(baju.getBajuId());
                baju.setDetailBajuList(details);
            }
            
            return popularBaju;
            
        } catch (Exception e) {
            throw new DatabaseException("Gagal mengambil baju populer: " + e.getMessage(), e);
        }
    }
    
    public Baju createBaju(Baju baju, List<DetailBaju> detailBajuList, File fotoFile) 
            throws ValidationException, DatabaseException {
        
        Connection conn = null;
        String uploadedFilename = null;
        
        try {
            validateBaju(baju, detailBajuList);
            
            if (fotoFile != null) {
                uploadedFilename = FileUtil.uploadBajuPhoto(fotoFile);
                baju.setFoto(uploadedFilename);
            }
            
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);
            int bajuId = bajuDAO.save(baju);
            
            if (bajuId <= 0) {
                throw new DatabaseException("Gagal menyimpan baju");
            }
            
            baju.setBajuId(bajuId);
            List<DetailBaju> savedDetails = new ArrayList<>();
            for (DetailBaju detail : detailBajuList) {
                detail.setBajuId(bajuId);
                int detailId = detailBajuDAO.save(detail);
                
                if (detailId <= 0) {
                    throw new DatabaseException("Gagal menyimpan detail baju");
                }
                
                detail.setDetailBajuId(detailId);
                savedDetails.add(detail);
            }
            
            baju.setDetailBajuList(savedDetails);
            conn.commit();
            System.out.println("Baju created: " + baju.getNamaBaju() + 
                             " with " + savedDetails.size() + " sizes");
            
            return baju;
            
        } catch (ValidationException | DatabaseException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Transaction rolled back");
                } catch (SQLException ex) {
                    System.err.println("Error rolling back: " + ex.getMessage());
                }
            }
            
            if (uploadedFilename != null) {
                FileUtil.deleteBajuPhoto(uploadedFilename);
            }
            
            throw e;
        } catch (IOException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error rolling back: " + ex.getMessage());
                }
            }
            throw new DatabaseException("Gagal upload foto: " + e.getMessage(), e);
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Error rolling back: " + ex.getMessage());
                }
            }
            
            if (uploadedFilename != null) {
                FileUtil.deleteBajuPhoto(uploadedFilename);
            }
            
            throw new DatabaseException("Gagal create baju: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error restoring auto-commit: " + e.getMessage());
                }
            }
        }
    }
    
    public boolean updateBaju(Baju baju, File newFotoFile) 
            throws ValidationException, DatabaseException {
        
        String oldFoto = baju.getFoto();
        String newFoto = null;
        
        try {
            if (ValidationUtil.isEmpty(baju.getNamaBaju())) {
                throw new ValidationException("Nama baju tidak boleh kosong");
            }
            
            if (newFotoFile != null) {
                newFoto = FileUtil.uploadBajuPhoto(newFotoFile);
                baju.setFoto(newFoto);
            }
            
            boolean updated = bajuDAO.update(baju);
            
            if (!updated) {
                if (newFoto != null) {
                    FileUtil.deleteBajuPhoto(newFoto);
                }
                throw new DatabaseException("Gagal update baju");
            }
            
            if (newFotoFile != null && oldFoto != null) {
                FileUtil.deleteBajuPhoto(oldFoto);
            }
            
            System.out.println("Baju updated: " + baju.getNamaBaju());
            
            return true;
            
        } catch (ValidationException | DatabaseException e) {
            if (newFoto != null) {
                FileUtil.deleteBajuPhoto(newFoto);
                baju.setFoto(oldFoto);
            }
            throw e;
        } catch (IOException e) {
            throw new DatabaseException("Gagal upload foto: " + e.getMessage(), e);
        } catch (Exception e) {
            if (newFoto != null) {
                FileUtil.deleteBajuPhoto(newFoto);
                baju.setFoto(oldFoto);
            }
            throw new DatabaseException("Gagal update baju: " + e.getMessage(), e);
        }
    }
    
    public boolean deleteBaju(int bajuId) throws DatabaseException {
        try {
            Baju baju = bajuDAO.findById(bajuId);
            
            if (baju == null) {
                throw new DatabaseException("Baju tidak ditemukan");
            }
            
            boolean hasActiveRentals = hasActiveRentals(bajuId);
            
            if (hasActiveRentals) {
                throw new ValidationException(
                    "Tidak bisa hapus baju yang sedang atau pernah disewa. " +
                    "Silakan ubah status menjadi tidak aktif atau tandai sebagai discontinued."
                );
            }
            
            if (baju.getFoto() != null) {
                FileUtil.deleteBajuPhoto(baju.getFoto());
            }
            
            detailBajuDAO.deleteByBajuId(bajuId);
            boolean deleted = bajuDAO.delete(bajuId);
            
            if (!deleted) {
                throw new DatabaseException("Gagal delete baju");
            }
            
            System.out.println("Baju deleted: " + bajuId);
            
            return true;
            
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Gagal delete baju: " + e.getMessage(), e);
        }
    }
    
    private boolean hasActiveRentals(int bajuId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM detail_penyewaan dp " +
                     "JOIN detail_baju db ON dp.detail_baju_id = db.detail_baju_id " +
                     "JOIN penyewaan p ON dp.sewa_id = p.sewa_id " +
                     "WHERE db.baju_id = ? AND p.status IN ('DIKONFIRMASI', 'SEDANG_DISEWA')";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bajuId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            throw new DatabaseException("Error checking active rentals: " + e.getMessage(), e);
        }
    }
    
    public DetailBaju addSize(int bajuId, Ukuran ukuran, double hargaSewa, int stok) 
            throws ValidationException, DatabaseException {
        
        try {
            if (hargaSewa <= 0) {
                throw new ValidationException("Harga sewa harus lebih dari 0");
            }
            
            if (stok < 0) {
                throw new ValidationException("Stok tidak boleh negatif");
            }
            
            DetailBaju existing = detailBajuDAO.findByBajuIdAndUkuran(bajuId, ukuran);
            if (existing != null) {
                throw new ValidationException("Ukuran " + ukuran + " sudah ada");
            }
            
            DetailBaju detail = new DetailBaju(bajuId, ukuran, hargaSewa, stok, Kondisi.BAIK);
            
            int detailId = detailBajuDAO.save(detail);
            
            if (detailId <= 0) {
                throw new DatabaseException("Gagal menyimpan ukuran");
            }
            
            detail.setDetailBajuId(detailId);
            
            System.out.println("Size added: " + ukuran + " for baju " + bajuId);
            
            return detail;
            
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Gagal add size: " + e.getMessage(), e);
        }
    }
    
    public boolean updateDetailBaju(DetailBaju detail) 
            throws ValidationException, DatabaseException {
        
        try {
            if (detail.getHargaSewa() <= 0) {
                throw new ValidationException("Harga sewa harus lebih dari 0");
            }
            
            if (detail.getStok() < 0) {
                throw new ValidationException("Stok tidak boleh negatif");
            }
            
            boolean updated = detailBajuDAO.update(detail);
            
            if (!updated) {
                throw new DatabaseException("Gagal update detail baju");
            }
            
            return true;
            
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Gagal update detail: " + e.getMessage(), e);
        }
    }
    
    public boolean updateStok(int detailBajuId, int newStok) 
            throws ValidationException, DatabaseException {
        
        try {
            if (newStok < 0) {
                throw new ValidationException("Stok tidak boleh negatif");
            }
            
            boolean updated = detailBajuDAO.updateStok(detailBajuId, newStok);
            
            if (!updated) {
                throw new DatabaseException("Gagal update stok");
            }
            
            System.out.println("Stok updated: detail " + detailBajuId + " = " + newStok);
            
            return true;
            
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Gagal update stok: " + e.getMessage(), e);
        }
    }
    
    public boolean deleteDetailBaju(int detailBajuId) throws DatabaseException {
        try {
            String sql = "SELECT COUNT(*) FROM detail_penyewaan dp " +
                         "JOIN penyewaan p ON dp.sewa_id = p.sewa_id " +
                         "WHERE dp.detail_baju_id = ? AND p.status IN ('DIKONFIRMASI', 'SEDANG_DISEWA')";
            
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, detailBajuId);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new ValidationException("Tidak bisa hapus ukuran yang sedang disewa");
                    }
                }
            }
            
            boolean deleted = detailBajuDAO.delete(detailBajuId);
            
            if (!deleted) {
                throw new DatabaseException("Gagal delete detail baju");
            }
            
            return true;
            
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Gagal delete detail: " + e.getMessage(), e);
        }
    }
    
    public boolean isStokAvailable(int detailBajuId, int jumlah) throws DatabaseException {
        try {
            return detailBajuDAO.isStokCukup(detailBajuId, jumlah);
        } catch (Exception e) {
            throw new DatabaseException("Gagal cek stok: " + e.getMessage(), e);
        }
    }
    
    public DetailBaju getDetailBaju(int bajuId, Ukuran ukuran) throws DatabaseException {
        try {
            return detailBajuDAO.findByBajuIdAndUkuran(bajuId, ukuran);
        } catch (Exception e) {
            throw new DatabaseException("Gagal get detail baju: " + e.getMessage(), e);
        }
    }
    
    public List<Kategori> getAllKategori() throws DatabaseException {
        try {
            return kategoriDAO.findAll();
        } catch (Exception e) {
            throw new DatabaseException("Gagal get kategori: " + e.getMessage(), e);
        }
    }
    
    public Kategori getKategoriById(int kategoriId) throws DatabaseException {
        try {
            return kategoriDAO.findById(kategoriId);
        } catch (Exception e) {
            throw new DatabaseException("Gagal get kategori: " + e.getMessage(), e);
        }
    }
    
    private void validateBaju(Baju baju, List<DetailBaju> detailBajuList) 
            throws ValidationException, DatabaseException {
        
        if (ValidationUtil.isEmpty(baju.getNamaBaju())) {
            throw new ValidationException("Nama baju tidak boleh kosong");
        }
        
        if (baju.getKategoriId() <= 0) {
            throw new ValidationException("Kategori harus dipilih");
        }
        
        Kategori kategori = kategoriDAO.findById(baju.getKategoriId());
        if (kategori == null) {
            throw new ValidationException("Kategori tidak valid");
        }
        
        if (detailBajuList == null || detailBajuList.isEmpty()) {
            throw new ValidationException("Minimal 1 ukuran harus diinput");
        }
        
        for (DetailBaju detail : detailBajuList) {
            if (detail.getUkuran() == null) {
                throw new ValidationException("Ukuran tidak boleh kosong");
            }
            
            if (detail.getHargaSewa() <= 0) {
                throw new ValidationException("Harga sewa harus lebih dari 0");
            }
            
            if (detail.getStok() < 0) {
                throw new ValidationException("Stok tidak boleh negatif");
            }
        }
        
        Map<Ukuran, Integer> ukuranMap = new HashMap<>();
        for (DetailBaju detail : detailBajuList) {
            if (ukuranMap.containsKey(detail.getUkuran())) {
                throw new ValidationException("Ukuran " + detail.getUkuran() + " duplikat");
            }
            ukuranMap.put(detail.getUkuran(), 1);
        }
    }
}