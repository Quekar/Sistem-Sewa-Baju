package com.mycompany.sewabaju.services;

import com.mycompany.sewabaju.dao.BajuDAO;
import com.mycompany.sewabaju.dao.FavoritDAO;
import com.mycompany.sewabaju.dao.PelangganDAO;
import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.exceptions.ValidationException;
import com.mycompany.sewabaju.models.Baju;
import com.mycompany.sewabaju.models.Favorit;
import com.mycompany.sewabaju.models.Pelanggan;
import java.util.ArrayList;
import java.util.List;

public class FavoritService {
    
    private final FavoritDAO favoritDAO;
    private final BajuDAO bajuDAO;
    private final PelangganDAO pelangganDAO;
    
    public FavoritService() {
        this.favoritDAO = new FavoritDAO();
        this.bajuDAO = new BajuDAO();
        this.pelangganDAO = new PelangganDAO();
    }
    
    public boolean toggleFavorit(int pelangganId, int bajuId) throws DatabaseException, ValidationException {
        validatePelangganId(pelangganId);
        validateBajuId(bajuId);
        
        if (!pelangganExists(pelangganId)) {
            throw new ValidationException("Pelanggan tidak ditemukan");
        }
        
        if (!bajuExists(bajuId)) {
            throw new ValidationException("Baju tidak ditemukan");
        }
        
        try {
            return favoritDAO.toggle(pelangganId, bajuId);
        } catch (DatabaseException e) {
            throw new DatabaseException("Gagal toggle favorit: " + e.getMessage(), e);
        }
    }
    
    public void addFavorit(int pelangganId, int bajuId) throws DatabaseException, ValidationException {
        validatePelangganId(pelangganId);
        validateBajuId(bajuId);
        
        if (isFavorit(pelangganId, bajuId)) {
            throw new ValidationException("Baju sudah ada di favorit");
        }
        
        if (!pelangganExists(pelangganId)) {
            throw new ValidationException("Pelanggan tidak ditemukan");
        }
        
        if (!bajuExists(bajuId)) {
            throw new ValidationException("Baju tidak ditemukan");
        }
        
        try {
            Favorit favorit = new Favorit(pelangganId, bajuId);
            favoritDAO.save(favorit);
        } catch (DatabaseException e) {
            throw new DatabaseException("Gagal menambahkan favorit: " + e.getMessage(), e);
        }
    }
    
    public void removeFavorit(int pelangganId, int bajuId) throws DatabaseException, ValidationException {
        validatePelangganId(pelangganId);
        validateBajuId(bajuId);
        
        if (!isFavorit(pelangganId, bajuId)) {
            throw new ValidationException("Baju tidak ada di favorit");
        }
        
        try {
            Favorit favorit = favoritDAO.findByPelangganIdAndBajuId(pelangganId, bajuId);
            if (favorit != null) {
                favoritDAO.delete(favorit.getFavoritId());
            }
        } catch (DatabaseException e) {
            throw new DatabaseException("Gagal menghapus favorit: " + e.getMessage(), e);
        }
    }
    
    public void removeFavoritById(int favoritId) throws DatabaseException {
        try {
            favoritDAO.delete(favoritId);
        } catch (DatabaseException e) {
            throw new DatabaseException("Gagal menghapus favorit: " + e.getMessage(), e);
        }
    }
    
    public boolean isFavorit(int pelangganId, int bajuId) throws DatabaseException {
        try {
            return favoritDAO.isFavorit(pelangganId, bajuId);
        } catch (DatabaseException e) {
            throw new DatabaseException("Error checking favorit: " + e.getMessage(), e);
        }
    }
    
    public List<Baju> getFavoritBajuList(int pelangganId) throws DatabaseException {
        validatePelangganId(pelangganId);
        
        try {
            return favoritDAO.getFavoritBajuByPelangganId(pelangganId);
        } catch (DatabaseException e) {
            throw new DatabaseException("Error loading favorit: " + e.getMessage(), e);
        }
    }
    
    public List<Favorit> getFavoritList(int pelangganId) throws DatabaseException {
        validatePelangganId(pelangganId);
        
        try {
            return favoritDAO.findByPelangganId(pelangganId);
        } catch (DatabaseException e) {
            throw new DatabaseException("Error loading favorit: " + e.getMessage(), e);
        }
    }
    
    public int getTotalFavorit(int pelangganId) throws DatabaseException {
        validatePelangganId(pelangganId);
        
        try {
            return favoritDAO.countByPelangganId(pelangganId);
        } catch (DatabaseException e) {
            throw new DatabaseException("Error counting favorit: " + e.getMessage(), e);
        }
    }
    
    public List<Baju> getTopFavoritBaju(int limit) throws DatabaseException {
        if (limit <= 0) {
            throw new ValidationException("Limit harus lebih dari 0");
        }
        
        try {
            return favoritDAO.getTopFavoritBaju(limit);
        } catch (DatabaseException e) {
            throw new DatabaseException("Error loading top favorit: " + e.getMessage(), e);
        }
    }
    
    public int countFavoritByBaju(int bajuId) throws DatabaseException {
        validateBajuId(bajuId);
        
        try {
            return favoritDAO.countByBajuId(bajuId);
        } catch (DatabaseException e) {
            throw new DatabaseException("Error counting favorit: " + e.getMessage(), e);
        }
    }
    
    public List<Favorit> getFavoritByBaju(int bajuId) throws DatabaseException {
        validateBajuId(bajuId);
        
        try {
            return favoritDAO.findByBajuId(bajuId);
        } catch (DatabaseException e) {
            throw new DatabaseException("Error loading favorit: " + e.getMessage(), e);
        }
    }
    
    public void deleteAllFavoritByPelanggan(int pelangganId) throws DatabaseException {
        validatePelangganId(pelangganId);
        
        try {
            favoritDAO.deleteByPelangganId(pelangganId);
        } catch (DatabaseException e) {
            throw new DatabaseException("Error deleting favorit: " + e.getMessage(), e);
        }
    }
    
    public void deleteAllFavoritByBaju(int bajuId) throws DatabaseException {
        validateBajuId(bajuId);
        
        try {
            favoritDAO.deleteByBajuId(bajuId);
        } catch (DatabaseException e) {
            throw new DatabaseException("Error deleting favorit: " + e.getMessage(), e);
        }
    }
    
    public List<Baju> getFavoritBajuWithFullDetails(int pelangganId) throws DatabaseException {
        validatePelangganId(pelangganId);
        
        try {
            List<Baju> favoritList = favoritDAO.getFavoritBajuByPelangganId(pelangganId);
            for (Baju baju : favoritList) {
                // Sudah ada kategori dari JOIN query
                // Tinggal load detail baju jika diperlukan
            }
            
            return favoritList;
        } catch (DatabaseException e) {
            throw new DatabaseException("Error loading favorit with details: " + e.getMessage(), e);
        }
    }
    
    public List<Baju> searchFavoritBaju(int pelangganId, String keyword) throws DatabaseException {
        validatePelangganId(pelangganId);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            return getFavoritBajuList(pelangganId);
        }
        
        try {
            List<Baju> allFavorit = getFavoritBajuList(pelangganId);
            List<Baju> filtered = new ArrayList<>();
            
            String lowerKeyword = keyword.toLowerCase().trim();
            
            for (Baju baju : allFavorit) {
                if (baju.getNamaBaju().toLowerCase().contains(lowerKeyword) ||
                    baju.getNamaKategori().toLowerCase().contains(lowerKeyword)) {
                    filtered.add(baju);
                }
            }
            
            return filtered;
        } catch (DatabaseException e) {
            throw new DatabaseException("Error searching favorit: " + e.getMessage(), e);
        }
    }
    
    public List<Baju> filterFavoritByKategori(int pelangganId, int kategoriId) throws DatabaseException {
        validatePelangganId(pelangganId);
        
        try {
            List<Baju> allFavorit = getFavoritBajuList(pelangganId);
            
            if (kategoriId == 0) {
                return allFavorit;
            }
            
            List<Baju> filtered = new ArrayList<>();
            for (Baju baju : allFavorit) {
                if (baju.getKategoriId() == kategoriId) {
                    filtered.add(baju);
                }
            }
            
            return filtered;
        } catch (DatabaseException e) {
            throw new DatabaseException("Error filtering favorit: " + e.getMessage(), e);
        }
    }
    
    private void validatePelangganId(int pelangganId) throws ValidationException {
        if (pelangganId <= 0) {
            throw new ValidationException("ID pelanggan tidak valid");
        }
    }
    
    private void validateBajuId(int bajuId) throws ValidationException {
        if (bajuId <= 0) {
            throw new ValidationException("ID baju tidak valid");
        }
    }
    
    private boolean pelangganExists(int pelangganId) throws DatabaseException {
        try {
            Pelanggan pelanggan = pelangganDAO.findById(pelangganId);
            return pelanggan != null;
        } catch (DatabaseException e) {
            throw new DatabaseException("Error checking pelanggan: " + e.getMessage(), e);
        }
    }
    
    private boolean bajuExists(int bajuId) throws DatabaseException {
        try {
            Baju baju = bajuDAO.findById(bajuId);
            return baju != null;
        } catch (DatabaseException e) {
            throw new DatabaseException("Error checking baju: " + e.getMessage(), e);
        }
    }
}