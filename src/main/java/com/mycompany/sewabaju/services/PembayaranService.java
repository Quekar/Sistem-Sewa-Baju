package com.mycompany.sewabaju.services;

import com.mycompany.sewabaju.dao.PembayaranDAO;
import com.mycompany.sewabaju.dao.PenyewaanDAO;
import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.exceptions.PembayaranException;
import com.mycompany.sewabaju.exceptions.ValidationException;
import com.mycompany.sewabaju.models.Pembayaran;
import com.mycompany.sewabaju.models.Penyewaan;
import com.mycompany.sewabaju.models.enums.MetodePembayaran;
import com.mycompany.sewabaju.models.enums.StatusPembayaran;
import com.mycompany.sewabaju.models.enums.StatusPenyewaan;
import com.mycompany.sewabaju.utils.FileUtil;
import com.mycompany.sewabaju.utils.Session;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class PembayaranService {
    
    private static final Logger LOGGER = Logger.getLogger(PembayaranService.class.getName());
    
    private final PembayaranDAO pembayaranDAO;
    private final PenyewaanDAO penyewaanDAO;
    private final PenyewaanService penyewaanService;
    private static PembayaranService instance;
    
    private PembayaranService() {
        this.pembayaranDAO = new PembayaranDAO();
        this.penyewaanDAO = new PenyewaanDAO();
        this.penyewaanService = PenyewaanService.getInstance();
    }
    
    public static PembayaranService getInstance() {
        if (instance == null) {
            synchronized (PembayaranService.class) {
                if (instance == null) {
                    instance = new PembayaranService();
                }
            }
        }
        return instance;
    }
    
    public Pembayaran createPembayaran(int sewaId, MetodePembayaran metode, 
                                       double jumlah, File buktiFile) 
            throws ValidationException, PembayaranException, DatabaseException {
        
        try {
            validatePembayaranInput(sewaId, metode, jumlah, buktiFile);
            
            Pembayaran existing = pembayaranDAO.findBySewaId(sewaId);
            if (existing != null) {
                throw new PembayaranException("Pembayaran sudah ada untuk penyewaan ini");
            }
            
            String buktiFilename = null;
            if (buktiFile != null) {
                buktiFilename = FileUtil.uploadBuktiPembayaran(buktiFile);
                LOGGER.info("Bukti pembayaran uploaded: " + buktiFilename);
            }
            
            Pembayaran pembayaran = new Pembayaran();
            pembayaran.setSewaId(sewaId);
            pembayaran.setMetodePembayaran(metode);
            pembayaran.setJumlah(jumlah);
            pembayaran.setBuktiPembayaran(buktiFilename);
            
            if (metode == MetodePembayaran.CASH) {
                pembayaran.setStatus(StatusPembayaran.BERHASIL);
            } else {
                pembayaran.setStatus(StatusPembayaran.MENUNGGU_VERIFIKASI);
            }
            
            int pembayaranId = pembayaranDAO.save(pembayaran);
            
            if (pembayaranId <= 0) {
                if (buktiFilename != null) {
                    FileUtil.deleteBuktiPembayaran(buktiFilename);
                }
                throw new DatabaseException("Gagal menyimpan pembayaran");
            }
            
            pembayaran.setPembayaranId(pembayaranId);
            
            if (metode == MetodePembayaran.CASH) {
                penyewaanService.confirmPenyewaan(sewaId);
            }
            
            LOGGER.info("Pembayaran created: #" + pembayaranId + 
                       " | Metode: " + metode + 
                       " | Status: " + pembayaran.getStatus());
            
            return pembayaran;
            
        } catch (ValidationException | PembayaranException | DatabaseException e) {
            throw e;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error uploading bukti pembayaran", e);
            throw new PembayaranException("Gagal upload bukti pembayaran: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating pembayaran", e);
            throw new DatabaseException("Gagal create pembayaran: " + e.getMessage(), e);
        }
    }
    
    public boolean reUploadBukti(int pembayaranId, File newBuktiFile) 
            throws ValidationException, PembayaranException, DatabaseException {
        
        try {
            if (newBuktiFile == null || !newBuktiFile.exists()) {
                throw new ValidationException("File bukti pembayaran tidak valid");
            }
            
            Pembayaran pembayaran = pembayaranDAO.findById(pembayaranId);
            
            if (pembayaran == null) {
                throw new DatabaseException("Pembayaran tidak ditemukan");
            }
            
            Penyewaan penyewaan = penyewaanDAO.findById(pembayaran.getSewaId());
            
            if (penyewaan == null) {
                throw new DatabaseException("Penyewaan tidak ditemukan");
            }
            
            int currentUserId = Session.getInstance().getCurrentUserId();
            
            if (penyewaan.getUserId() != currentUserId && !Session.getInstance().isAdmin()) {
                LOGGER.warning("Unauthorized re-upload attempt by user " + currentUserId + 
                             " for pembayaran #" + pembayaranId);
                throw new ValidationException("Anda tidak memiliki akses ke pembayaran ini");
            }
            
            if (pembayaran.getStatus() != StatusPembayaran.DITOLAK) {
                throw new PembayaranException("Hanya bisa re-upload jika pembayaran ditolak");
            }
            
            String oldBukti = pembayaran.getBuktiPembayaran();
            if (oldBukti != null) {
                FileUtil.deleteBuktiPembayaran(oldBukti);
            }
            
            String newFilename = FileUtil.uploadBuktiPembayaran(newBuktiFile);
            
            boolean updated = pembayaranDAO.updateBuktiPembayaran(pembayaranId, newFilename);
            
            if (!updated) {
                FileUtil.deleteBuktiPembayaran(newFilename);
                throw new DatabaseException("Gagal update bukti pembayaran");
            }
            
            LOGGER.info("Bukti re-uploaded: Pembayaran #" + pembayaranId);
            notifyAdminNewBukti(pembayaranId);
            
            return true;
            
        } catch (ValidationException | PembayaranException | DatabaseException e) {
            throw e;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error uploading bukti", e);
            throw new PembayaranException("Gagal upload bukti: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error re-uploading bukti", e);
            throw new DatabaseException("Gagal re-upload bukti: " + e.getMessage(), e);
        }
    }
    
    public Pembayaran getPembayaranById(int pembayaranId) throws DatabaseException {
        try {
            return pembayaranDAO.findById(pembayaranId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting pembayaran by ID", e);
            throw new DatabaseException("Gagal get pembayaran: " + e.getMessage(), e);
        }
    }
    
    public Pembayaran getPembayaranBySewaId(int sewaId) throws DatabaseException {
        try {
            return pembayaranDAO.findBySewaId(sewaId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting pembayaran by sewa ID", e);
            throw new DatabaseException("Gagal get pembayaran: " + e.getMessage(), e);
        }
    }
    
    public List<Pembayaran> getPembayaranByStatus(StatusPembayaran status) throws DatabaseException {
        try {
            return pembayaranDAO.findByStatus(status);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting pembayaran by status", e);
            throw new DatabaseException("Gagal get pembayaran by status: " + e.getMessage(), e);
        }
    }
    
    public List<Pembayaran> getPembayaranPending() throws DatabaseException {
        try {
            return pembayaranDAO.findPending();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting pembayaran pending", e);
            throw new DatabaseException("Gagal get pembayaran pending: " + e.getMessage(), e);
        }
    }
    
    public int countPembayaranPending() throws DatabaseException {
        try {
            return pembayaranDAO.countByStatus(StatusPembayaran.MENUNGGU_VERIFIKASI);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting pembayaran pending", e);
            throw new DatabaseException("Gagal count pembayaran pending: " + e.getMessage(), e);
        }
    }
    
    public boolean approvePembayaran(int pembayaranId, int adminId) 
            throws ValidationException, DatabaseException {
        
        try {
            if (!Session.getInstance().isAdmin()) {
                throw new ValidationException("Hanya admin yang dapat verifikasi pembayaran");
            }
            
            Pembayaran pembayaran = pembayaranDAO.findById(pembayaranId);
            
            if (pembayaran == null) {
                throw new DatabaseException("Pembayaran tidak ditemukan");
            }
            
            if (pembayaran.getStatus() != StatusPembayaran.MENUNGGU_VERIFIKASI) {
                throw new ValidationException("Hanya bisa approve pembayaran yang pending");
            }
            
            boolean approved = pembayaranDAO.approve(pembayaranId, adminId);
            
            if (!approved) {
                throw new DatabaseException("Gagal approve pembayaran");
            }
            
            penyewaanService.confirmPenyewaan(pembayaran.getSewaId());
            
            LOGGER.info("Pembayaran approved: #" + pembayaranId + " by admin " + adminId);
            notifyPelangganApproved(pembayaran.getSewaId());
            return true;
            
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error approving pembayaran", e);
            throw new DatabaseException("Gagal approve pembayaran: " + e.getMessage(), e);
        }
    }
    
    public boolean rejectPembayaran(int pembayaranId, int adminId) 
            throws ValidationException, DatabaseException {
        
        try {
            if (!Session.getInstance().isAdmin()) {
                throw new ValidationException("Hanya admin yang dapat verifikasi pembayaran");
            }
            
            Pembayaran pembayaran = pembayaranDAO.findById(pembayaranId);
            
            if (pembayaran == null) {
                throw new DatabaseException("Pembayaran tidak ditemukan");
            }
            
            if (pembayaran.getStatus() != StatusPembayaran.MENUNGGU_VERIFIKASI) {
                throw new ValidationException("Hanya bisa reject pembayaran yang pending");
            }
            
            boolean rejected = pembayaranDAO.reject(pembayaranId, adminId);
            
            if (!rejected) {
                throw new DatabaseException("Gagal reject pembayaran");
            }
            
            LOGGER.info("Pembayaran rejected: #" + pembayaranId + " by admin " + adminId);
            notifyPelangganRejected(pembayaran.getSewaId());
            return true;
            
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error rejecting pembayaran", e);
            throw new DatabaseException("Gagal reject pembayaran: " + e.getMessage(), e);
        }
    }
    
    public String getBuktiFilePath(String filename) {
        return FileUtil.getBuktiPembayaranPath(filename);
    }
    
    public boolean buktiFileExists(String filename) {
        if (filename == null) return false;
        String path = FileUtil.getBuktiPembayaranPath(filename);
        return FileUtil.fileExists(path);
    }
    
    public String getPaymentInstructions(MetodePembayaran metode) {
        return metode != null ? metode.getInstructions() : "";
    }
    
    private void notifyPelangganApproved(int sewaId) {
        try {
            Penyewaan penyewaan = penyewaanDAO.findById(sewaId);
            if (penyewaan != null && penyewaan.getUser() != null) {
                String message = "Pembayaran Anda telah diverifikasi untuk Penyewaan #" + sewaId + 
                               ". Baju siap diambil!";
                
                LOGGER.info("NOTIFICATION: To User #" + penyewaan.getUserId() + " - " + message);
                
                // TODO: Implement actual notification
                // - Send email using JavaMail
                // - Send SMS using Twilio/Nexmo
                // - Create in-app notification in database
                // - Push notification to mobile app
            }
        } catch (Exception e) {
            // Don't throw exception, just log
            LOGGER.log(Level.WARNING, "Failed to send notification", e);
        }
    }
    
    private void notifyPelangganRejected(int sewaId) {
        try {
            Penyewaan penyewaan = penyewaanDAO.findById(sewaId);
            if (penyewaan != null && penyewaan.getUser() != null) {
                String message = "Pembayaran Anda ditolak untuk Penyewaan #" + sewaId + 
                               ". Silakan upload ulang bukti pembayaran yang valid.";
                
                LOGGER.info("NOTIFICATION: To User #" + penyewaan.getUserId() + " - " + message);
                
                // TODO: Implement actual notification
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send notification", e);
        }
    }
    
    private void notifyAdminNewBukti(int pembayaranId) {
        try {
            String message = "Bukti pembayaran baru telah diupload untuk Pembayaran #" + 
                           pembayaranId + ". Silakan verifikasi.";
            
            LOGGER.info("NOTIFICATION: To Admin - " + message);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to send notification", e);
        }
    }
    
    private void validatePembayaranInput(int sewaId, MetodePembayaran metode, 
                                         double jumlah, File buktiFile) 
            throws ValidationException, DatabaseException {
        if (sewaId <= 0) {
            throw new ValidationException("Penyewaan ID tidak valid");
        }
        
        Penyewaan penyewaan = penyewaanDAO.findById(sewaId);
        if (penyewaan == null) {
            throw new DatabaseException("Penyewaan tidak ditemukan");
        }
        
        if (penyewaan.getStatus() != StatusPenyewaan.MENUNGGU_PEMBAYARAN) {
            throw new ValidationException("Penyewaan tidak dalam status menunggu pembayaran");
        }
        
        if (metode == null) {
            throw new ValidationException("Metode pembayaran harus dipilih");
        }
        
        if (jumlah <= 0) {
            throw new ValidationException("Jumlah pembayaran harus lebih dari 0");
        }
        
        if (Math.abs(jumlah - penyewaan.getTotalHarga()) > 0.01) {
            throw new ValidationException(
                "Jumlah pembayaran tidak sesuai dengan total harga penyewaan. " +
                "Expected: Rp " + penyewaan.getTotalHarga() + ", Got: Rp " + jumlah
            );
        }
        
        if (metode.requiresProof()) {
            if (buktiFile == null || !buktiFile.exists()) {
                throw new ValidationException(
                    "Bukti pembayaran harus diupload untuk metode " + metode.getDisplayName()
                );
            }
            
            if (buktiFile.length() > 5 * 1024 * 1024) {
                throw new ValidationException("Ukuran file maksimal 5MB");
            }
            
            String filename = buktiFile.getName().toLowerCase();
            if (!filename.endsWith(".jpg") && !filename.endsWith(".jpeg") && 
                !filename.endsWith(".png") && !filename.endsWith(".pdf")) {
                throw new ValidationException("Format file harus JPG, JPEG, PNG, atau PDF");
            }
        }
    }
}