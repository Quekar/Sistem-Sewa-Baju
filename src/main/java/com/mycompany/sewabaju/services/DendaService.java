package com.mycompany.sewabaju.services;

import com.mycompany.sewabaju.dao.DendaDAO;
import com.mycompany.sewabaju.dao.DetailPenyewaanDAO;
import com.mycompany.sewabaju.dao.PenyewaanDAO;
import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.exceptions.ValidationException;
import com.mycompany.sewabaju.models.Denda;
import com.mycompany.sewabaju.models.DetailPenyewaan;
import com.mycompany.sewabaju.models.Penyewaan;
import com.mycompany.sewabaju.models.enums.JenisDenda;
import com.mycompany.sewabaju.models.enums.StatusBayarDenda;
import com.mycompany.sewabaju.models.enums.StatusPenyewaan;
import com.mycompany.sewabaju.utils.Session;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class DendaService {
    
    private static final Logger LOGGER = Logger.getLogger(DendaService.class.getName());
    
    private final DendaDAO dendaDAO;
    private final PenyewaanDAO penyewaanDAO;
    private final DetailPenyewaanDAO detailPenyewaanDAO;
    private static DendaService instance;
    
    private DendaService() {
        this.dendaDAO = new DendaDAO();
        this.penyewaanDAO = new PenyewaanDAO();
        this.detailPenyewaanDAO = new DetailPenyewaanDAO();
    }
    
    public static DendaService getInstance() {
        if (instance == null) {
            synchronized (DendaService.class) {
                if (instance == null) {
                    instance = new DendaService();
                }
            }
        }
        return instance;
    }
    
    public Denda createDendaKeterlambatan(int sewaId) 
            throws ValidationException, DatabaseException {
        
        try {
            Penyewaan penyewaan = penyewaanDAO.findById(sewaId);
            
            if (penyewaan == null) {
                throw new DatabaseException("Penyewaan tidak ditemukan");
            }
            
            if (penyewaan.getTglKembaliAktual() == null) {
                throw new ValidationException("Penyewaan belum dikembalikan");
            }
            
            long hariTerlambat = ChronoUnit.DAYS.between(
                penyewaan.getTglKembali(), 
                penyewaan.getTglKembaliAktual()
            );
            
            if (hariTerlambat <= 0) {
                LOGGER.info("No keterlambatan for sewa #" + sewaId);
                return null;
            }
            
            List<Denda> existingDenda = dendaDAO.findBySewaId(sewaId);
            boolean hasKeterlambatanDenda = existingDenda.stream()
                .anyMatch(d -> d.getJenisDenda() == JenisDenda.KETERLAMBATAN);
            
            if (hasKeterlambatanDenda) {
                LOGGER.warning("Denda keterlambatan already exists for sewa #" + sewaId);
                throw new ValidationException("Denda keterlambatan sudah ada");
            }
            
            Denda denda = Denda.createDendaKeterlambatan(sewaId, (int) hariTerlambat);
            int dendaId = dendaDAO.save(denda);
            
            if (dendaId <= 0) {
                throw new DatabaseException("Gagal menyimpan denda");
            }
            
            denda.setDendaId(dendaId);
            
            LOGGER.info("Denda keterlambatan created: #" + dendaId + 
                       " | Hari: " + hariTerlambat + 
                       " | Jumlah: Rp " + denda.getJumlah());
            
            return denda;
            
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating denda keterlambatan", e);
            throw new DatabaseException("Gagal create denda keterlambatan: " + e.getMessage(), e);
        }
    }
    
    public Denda createDendaKerusakan(int sewaId, double jumlah, String keterangan) 
            throws ValidationException, DatabaseException {
        
        try {
            if (!Session.getInstance().isAdmin()) {
                throw new ValidationException("Hanya admin yang dapat menambah denda kerusakan");
            }
            
            validateDendaInput(sewaId, jumlah, keterangan);
            Denda denda = Denda.createDendaKerusakan(sewaId, jumlah, keterangan);
            int dendaId = dendaDAO.save(denda);
            
            if (dendaId <= 0) {
                throw new DatabaseException("Gagal menyimpan denda");
            }
            
            denda.setDendaId(dendaId);
            
            LOGGER.info("Denda kerusakan created: #" + dendaId + 
                       " | Jumlah: Rp " + jumlah);
            
            return denda;
            
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating denda kerusakan", e);
            throw new DatabaseException("Gagal create denda kerusakan: " + e.getMessage(), e);
        }
    }
    
    public Denda createDendaKehilangan(int sewaId, double jumlah, String keterangan) 
            throws ValidationException, DatabaseException {
        
        try {
            if (!Session.getInstance().isAdmin()) {
                throw new ValidationException("Hanya admin yang dapat menambah denda kehilangan");
            }
            
            validateDendaInput(sewaId, jumlah, keterangan);
            Denda denda = Denda.createDendaKehilangan(sewaId, jumlah, keterangan);
            int dendaId = dendaDAO.save(denda);
            
            if (dendaId <= 0) {
                throw new DatabaseException("Gagal menyimpan denda");
            }
            
            denda.setDendaId(dendaId);
            
            LOGGER.info("Denda kehilangan created: #" + dendaId + 
                       " | Jumlah: Rp " + jumlah);
            
            return denda;
            
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating denda kehilangan", e);
            throw new DatabaseException("Gagal create denda kehilangan: " + e.getMessage(), e);
        }
    }
    
    public List<Denda> autoCreateDendaFromReturn(int sewaId) throws DatabaseException {
        List<Denda> createdDenda = new ArrayList<>();
        
        try {
            LOGGER.info("Auto creating denda for sewa #" + sewaId);
            try {
                Denda dendaKeterlambatan = createDendaKeterlambatan(sewaId);
                if (dendaKeterlambatan != null) {
                    createdDenda.add(dendaKeterlambatan);
                    LOGGER.info("Denda keterlambatan created successfully");
                }
            } catch (ValidationException e) {
                LOGGER.info("Denda keterlambatan already exists for sewa #" + sewaId);
            }
            
            List<DetailPenyewaan> details = detailPenyewaanDAO.findBySewaId(sewaId);
            List<Denda> existingDenda = dendaDAO.findBySewaId(sewaId);
            
            for (DetailPenyewaan detail : details) {
                if (detail.isDamaged()) {
                    // ✅ Create unique identifier for this detail's denda
                    String uniqueId = "DETAIL#" + detail.getDetailSewaId();
                    
                    // ✅ Check if denda for this detail already exists
                    boolean alreadyExists = existingDenda.stream()
                        .anyMatch(d -> d.getJenisDenda() == JenisDenda.KERUSAKAN &&
                                       d.getKeterangan() != null &&
                                       d.getKeterangan().contains(uniqueId));
                    
                    if (alreadyExists) {
                        LOGGER.info("Denda kerusakan already exists for detail #" + 
                                  detail.getDetailSewaId());
                        continue;
                    }
                    
                    try {
                        double estimasiDenda = detail.getEstimasiDenda();
                        String keterangan = uniqueId + " - Kerusakan: " + 
                                          detail.getKondisiDisplay();
                        
                        if (detail.getKeteranganKerusakan() != null && 
                            !detail.getKeteranganKerusakan().trim().isEmpty()) {
                            keterangan += " | " + detail.getKeteranganKerusakan();
                        }
                        
                        Denda dendaKerusakan = createDendaKerusakan(
                            sewaId, 
                            estimasiDenda, 
                            keterangan
                        );
                        
                        createdDenda.add(dendaKerusakan);
                        LOGGER.info("Denda kerusakan created for detail #" + 
                                  detail.getDetailSewaId());
                        
                    } catch (ValidationException e) {
                        LOGGER.warning("Failed to create denda kerusakan for detail #" + 
                                     detail.getDetailSewaId() + ": " + e.getMessage());
                        // Continue with next detail
                    }
                }
            }
            
            LOGGER.info("Auto create denda completed. Total created: " + createdDenda.size());
            
            return createdDenda;
            
        } catch (DatabaseException e) {
            LOGGER.log(Level.SEVERE, "Error in autoCreateDendaFromReturn", e);
            throw new DatabaseException("Gagal auto create denda: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error in autoCreateDendaFromReturn", e);
            throw new DatabaseException("Error tidak terduga saat create denda: " + e.getMessage(), e);
        }
    }
    
    public Denda getDendaById(int dendaId) throws DatabaseException {
        try {
            return dendaDAO.findById(dendaId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting denda by ID", e);
            throw new DatabaseException("Gagal get denda: " + e.getMessage(), e);
        }
    }
    
    public List<Denda> getDendaBySewaId(int sewaId) throws DatabaseException {
        try {
            return dendaDAO.findBySewaId(sewaId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting denda by sewa ID", e);
            throw new DatabaseException("Gagal get denda: " + e.getMessage(), e);
        }
    }
    
    public List<Denda> getUnpaidDenda() throws DatabaseException {
        try {
            return dendaDAO.findUnpaid();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting unpaid denda", e);
            throw new DatabaseException("Gagal get unpaid denda: " + e.getMessage(), e);
        }
    }
    
    public List<Denda> getDendaByJenis(JenisDenda jenis) throws DatabaseException {
        try {
            return dendaDAO.findByJenis(jenis);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting denda by jenis", e);
            throw new DatabaseException("Gagal get denda by jenis: " + e.getMessage(), e);
        }
    }
    
    public double getTotalDenda(int sewaId) throws DatabaseException {
        try {
            return dendaDAO.getTotalBySewaId(sewaId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting total denda", e);
            throw new DatabaseException("Gagal get total denda: " + e.getMessage(), e);
        }
    }
    
    public double getTotalUnpaidDenda(int sewaId) throws DatabaseException {
        try {
            return dendaDAO.getTotalUnpaidBySewaId(sewaId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting total unpaid denda", e);
            throw new DatabaseException("Gagal get total unpaid denda: " + e.getMessage(), e);
        }
    }
    
    public boolean markDendaPaid(int dendaId) 
            throws ValidationException, DatabaseException {
        
        try {
            if (!Session.getInstance().isAdmin()) {
                throw new ValidationException("Hanya admin yang dapat update status denda");
            }
            
            Denda denda = dendaDAO.findById(dendaId);
            
            if (denda == null) {
                throw new DatabaseException("Denda tidak ditemukan");
            }
            
            if (denda.isPaid()) {
                throw new ValidationException("Denda sudah dibayar");
            }
            
            boolean updated = dendaDAO.markAsPaid(dendaId);
            
            if (!updated) {
                throw new DatabaseException("Gagal update status denda");
            }
            
            LOGGER.info("Denda marked as paid: #" + dendaId);
            
            return true;
            
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error marking denda as paid", e);
            throw new DatabaseException("Gagal mark denda paid: " + e.getMessage(), e);
        }
    }
    
    public boolean updateDenda(Denda denda) 
            throws ValidationException, DatabaseException {
        
        try {
            if (!Session.getInstance().isAdmin()) {
                throw new ValidationException("Hanya admin yang dapat update denda");
            }
            
            if (denda.getJumlah() <= 0) {
                throw new ValidationException("Jumlah denda harus lebih dari 0");
            }
            
            boolean updated = dendaDAO.update(denda);
            
            if (!updated) {
                throw new DatabaseException("Gagal update denda");
            }
            
            LOGGER.info("Denda updated: #" + denda.getDendaId());
            
            return true;
            
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating denda", e);
            throw new DatabaseException("Gagal update denda: " + e.getMessage(), e);
        }
    }
    
    public boolean deleteDenda(int dendaId) 
            throws ValidationException, DatabaseException {
        
        try {
            if (!Session.getInstance().isAdmin()) {
                throw new ValidationException("Hanya admin yang dapat hapus denda");
            }
            
            boolean deleted = dendaDAO.delete(dendaId);
            
            if (!deleted) {
                throw new DatabaseException("Gagal hapus denda");
            }
            
            LOGGER.info("Denda deleted: #" + dendaId);
            
            return true;
            
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting denda", e);
            throw new DatabaseException("Gagal delete denda: " + e.getMessage(), e);
        }
    }
    
    public double hitungDendaKeterlambatan(LocalDate tglKembali, LocalDate tglKembaliAktual) {
        if (tglKembali == null || tglKembaliAktual == null) {
            return 0;
        }
        
        long hariTerlambat = ChronoUnit.DAYS.between(tglKembali, tglKembaliAktual);
        
        if (hariTerlambat <= 0) {
            return 0;
        }
        
        return JenisDenda.KETERLAMBATAN.hitungDenda((int) hariTerlambat);
    }
    
    public double hitungTotalDendaPenyewaan(int sewaId) throws DatabaseException {
        try {
            Penyewaan penyewaan = penyewaanDAO.findById(sewaId);
            
            if (penyewaan == null) {
                return 0;
            }
            
            double totalDenda = 0;
            totalDenda += dendaDAO.getTotalBySewaId(sewaId);
            
            if (penyewaan.getStatus() == StatusPenyewaan.SEDANG_DISEWA) {
                if (LocalDate.now().isAfter(penyewaan.getTglKembali())) {
                    double dendaKeterlambatan = hitungDendaKeterlambatan(
                        penyewaan.getTglKembali(), 
                        LocalDate.now()
                    );
                    totalDenda += dendaKeterlambatan;
                }
            }
            
            return totalDenda;
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error calculating total denda", e);
            throw new DatabaseException("Gagal hitung total denda: " + e.getMessage(), e);
        }
    }
    
    public int countUnpaidDenda() throws DatabaseException {
        try {
            return dendaDAO.countByStatus(StatusBayarDenda.BELUM_DIBAYAR);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error counting unpaid denda", e);
            throw new DatabaseException("Gagal count unpaid denda: " + e.getMessage(), e);
        }
    }
    
    public double getTotalAmountUnpaidDenda() throws DatabaseException {
        try {
            List<Denda> unpaidDenda = dendaDAO.findUnpaid();
            return unpaidDenda.stream()
                .mapToDouble(Denda::getJumlah)
                .sum();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting total amount unpaid denda", e);
            throw new DatabaseException("Gagal get total amount unpaid denda: " + e.getMessage(), e);
        }
    }
    
    private void validateDendaInput(int sewaId, double jumlah, String keterangan) 
            throws ValidationException, DatabaseException {
        
        if (sewaId <= 0) {
            throw new ValidationException("Penyewaan ID tidak valid");
        }
        
        Penyewaan penyewaan = penyewaanDAO.findById(sewaId);
        if (penyewaan == null) {
            throw new DatabaseException("Penyewaan tidak ditemukan");
        }
        
        if (penyewaan.getStatus() != StatusPenyewaan.DIKEMBALIKAN) {
            throw new ValidationException("Penyewaan belum dikembalikan");
        }
        
        if (jumlah <= 0) {
            throw new ValidationException("Jumlah denda harus lebih dari 0");
        }
        
        if (keterangan == null || keterangan.trim().isEmpty()) {
            throw new ValidationException("Keterangan tidak boleh kosong");
        }
    }
}