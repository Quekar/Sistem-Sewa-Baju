package com.mycompany.sewabaju.services;

import com.mycompany.sewabaju.dao.DetailBajuDAO;
import com.mycompany.sewabaju.dao.DetailPenyewaanDAO;
import com.mycompany.sewabaju.dao.PelangganDAO;
import com.mycompany.sewabaju.dao.PenyewaanDAO;
import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.exceptions.StokTidakCukupException;
import com.mycompany.sewabaju.exceptions.ValidationException;
import com.mycompany.sewabaju.models.*;
import com.mycompany.sewabaju.models.enums.Kondisi;
import com.mycompany.sewabaju.models.enums.StatusPenyewaan;
import com.mycompany.sewabaju.utils.Session;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class PenyewaanService {
    
    private final PenyewaanDAO penyewaanDAO;
    private final DetailPenyewaanDAO detailPenyewaanDAO;
    private final DetailBajuDAO detailBajuDAO;
    private final PelangganDAO pelangganDAO;
    
    private static PenyewaanService instance;
    
    private PenyewaanService() {
        this.penyewaanDAO = new PenyewaanDAO();
        this.detailPenyewaanDAO = new DetailPenyewaanDAO();
        this.detailBajuDAO = new DetailBajuDAO();
        this.pelangganDAO = new PelangganDAO();
    }
    
    public static PenyewaanService getInstance() {
        if (instance == null) {
            synchronized (PenyewaanService.class) {
                if (instance == null) {
                    instance = new PenyewaanService();
                }
            }
        }
        return instance;
    }
    
    public Penyewaan createPenyewaan(int userId, List<CartItem> cartItems, 
                                     LocalDate tglSewa, LocalDate tglKembali) 
            throws ValidationException, StokTidakCukupException, DatabaseException {
        
        try {
            validatePenyewaanInput(cartItems, tglSewa, tglKembali);
            checkStokAvailability(cartItems);
            long jumlahHari = ChronoUnit.DAYS.between(tglSewa, tglKembali);
            double totalHarga = hitungTotalHarga(cartItems, (int) jumlahHari);
            Penyewaan penyewaan = new Penyewaan();
            penyewaan.setUserId(userId);
            penyewaan.setTglSewa(tglSewa);
            penyewaan.setTglKembali(tglKembali);
            penyewaan.setTotalHarga(totalHarga);
            penyewaan.setStatus(StatusPenyewaan.MENUNGGU_PEMBAYARAN);
            int sewaId = penyewaanDAO.save(penyewaan);
            
            if (sewaId <= 0) {
                throw new DatabaseException("Gagal menyimpan penyewaan");
            }
            
            penyewaan.setSewaId(sewaId);
            List<DetailPenyewaan> detailList = new ArrayList<>();
            
            for (CartItem item : cartItems) {
                double hargaPerItem = item.getDetailBaju().getHargaSewa() * jumlahHari;
                double subtotal = hargaPerItem * item.getJumlah();
                DetailPenyewaan detail = new DetailPenyewaan();
                detail.setSewaId(sewaId);
                detail.setDetailBajuId(item.getDetailBaju().getDetailBajuId());
                detail.setJumlah(item.getJumlah());
                detail.setHargaPerItem(hargaPerItem);
                detail.setSubtotal(subtotal);
                
                int detailId = detailPenyewaanDAO.save(detail);
                
                if (detailId <= 0) {
                    throw new DatabaseException("Gagal menyimpan detail penyewaan");
                }
                
                detail.setDetailSewaId(detailId);
                detail.setDetailBaju(item.getDetailBaju());
                detailList.add(detail);
                
                boolean stokReduced = detailBajuDAO.kurangiStok(
                    item.getDetailBaju().getDetailBajuId(), 
                    item.getJumlah()
                );
                
                if (!stokReduced) {
                    throw new StokTidakCukupException(
                        "Gagal mengurangi stok",
                        item.getDetailBaju().getBajuId(),
                        item.getDetailBaju().getNamaBaju(),
                        item.getDetailBaju().getStok(),
                        item.getJumlah()
                    );
                }
            }
            
            penyewaan.setDetailPenyewaanList(detailList);
            
            System.out.println("Penyewaan created: #" + sewaId + 
                             " | Items: " + detailList.size() + 
                             " | Total: Rp " + totalHarga);
            
            return penyewaan;
            
        } catch (ValidationException | StokTidakCukupException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Gagal create penyewaan: " + e.getMessage(), e);
        }
    }
    
    public double hitungTotalHarga(List<CartItem> cartItems, int jumlahHari) {
        return cartItems.stream()
            .mapToDouble(item -> {
                double hargaPerHari = item.getDetailBaju().getHargaSewa();
                return hargaPerHari * jumlahHari * item.getJumlah();
            })
            .sum();
    }
    
    public double hitungHargaItem(DetailBaju detailBaju, int jumlah, int jumlahHari) {
        return detailBaju.getHargaSewa() * jumlah * jumlahHari;
    }
    
    public Penyewaan getPenyewaanById(int sewaId) throws DatabaseException {
        try {
            Penyewaan penyewaan = penyewaanDAO.findById(sewaId);
            
            if (penyewaan == null) {
                return null;
            }
            
            List<DetailPenyewaan> details = detailPenyewaanDAO.findBySewaId(sewaId);
            
            for (DetailPenyewaan detail : details) {
                DetailBaju detailBaju = detailBajuDAO.findById(detail.getDetailBajuId());
                detail.setDetailBaju(detailBaju);
            }
            
            penyewaan.setDetailPenyewaanList(details);
            
            // Load pembayaran (if exists)
            // Will be loaded by PembayaranService
            
            // Load denda (if exists)
            // Will be loaded by DendaService
            
            return penyewaan;
            
        } catch (Exception e) {
            throw new DatabaseException("Gagal get penyewaan: " + e.getMessage(), e);
        }
    }
    
    public List<Penyewaan> getPenyewaanByUserId(int userId) throws DatabaseException {
        try {
            List<Penyewaan> penyewaanList = penyewaanDAO.findByUserId(userId);
            
            for (Penyewaan penyewaan : penyewaanList) {
                List<DetailPenyewaan> details = detailPenyewaanDAO.findBySewaId(penyewaan.getSewaId());
                
                for (DetailPenyewaan detail : details) {
                    DetailBaju detailBaju = detailBajuDAO.findById(detail.getDetailBajuId());
                    detail.setDetailBaju(detailBaju);
                }
                
                penyewaan.setDetailPenyewaanList(details);
            }
            
            return penyewaanList;
            
        } catch (Exception e) {
            throw new DatabaseException("Gagal get riwayat sewa: " + e.getMessage(), e);
        }
    }
    
    public List<Penyewaan> getAllPenyewaan() throws DatabaseException {
        try {
            List<Penyewaan> penyewaanList = penyewaanDAO.findAll();
            
            for (Penyewaan penyewaan : penyewaanList) {
                List<DetailPenyewaan> details = detailPenyewaanDAO.findBySewaId(penyewaan.getSewaId());
                penyewaan.setDetailPenyewaanList(details);
            }
            
            return penyewaanList;
            
        } catch (Exception e) {
            throw new DatabaseException("Gagal get all penyewaan: " + e.getMessage(), e);
        }
    }
    
    public List<Penyewaan> getPenyewaanByStatus(StatusPenyewaan status) throws DatabaseException {
        try {
            List<Penyewaan> penyewaanList = penyewaanDAO.findByStatus(status);
            
            for (Penyewaan penyewaan : penyewaanList) {
                List<DetailPenyewaan> details = detailPenyewaanDAO.findBySewaId(penyewaan.getSewaId());
                penyewaan.setDetailPenyewaanList(details);
            }
            
            return penyewaanList;
            
        } catch (Exception e) {
            throw new DatabaseException("Gagal get penyewaan by status: " + e.getMessage(), e);
        }
    }
    
    public List<Penyewaan> getOverduePenyewaan() throws DatabaseException {
        try {
            return penyewaanDAO.findOverdue();
        } catch (Exception e) {
            throw new DatabaseException("Gagal get overdue penyewaan: " + e.getMessage(), e);
        }
    }
    
    public List<Penyewaan> getRecentPenyewaan(int limit) throws DatabaseException {
        try {
            return penyewaanDAO.findRecent(limit);
        } catch (Exception e) {
            throw new DatabaseException("Gagal get recent penyewaan: " + e.getMessage(), e);
        }
    }
    
    public boolean updateStatus(int sewaId, StatusPenyewaan newStatus) 
            throws ValidationException, DatabaseException {
        
        try {
            Penyewaan penyewaan = penyewaanDAO.findById(sewaId);
            
            if (penyewaan == null) {
                throw new DatabaseException("Penyewaan tidak ditemukan");
            }
            
            if (!penyewaan.getStatus().canTransitionTo(newStatus)) {
                throw new ValidationException(
                    "Tidak bisa ubah status dari " + penyewaan.getStatus() + 
                    " ke " + newStatus
                );
            }
            
            boolean updated = penyewaanDAO.updateStatus(sewaId, newStatus);
            
            if (!updated) {
                throw new DatabaseException("Gagal update status");
            }
            
            System.out.println("Status updated: Sewa #" + sewaId + " -> " + newStatus);
            
            return true;
            
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Gagal update status: " + e.getMessage(), e);
        }
    }
    
    public boolean confirmPenyewaan(int sewaId) throws DatabaseException {
        try {
            return updateStatus(sewaId, StatusPenyewaan.DIKONFIRMASI);
        } catch (Exception e) {
            throw new DatabaseException("Gagal confirm penyewaan: " + e.getMessage(), e);
        }
    }
    
    public boolean startRental(int sewaId) throws DatabaseException {
        try {
            return updateStatus(sewaId, StatusPenyewaan.SEDANG_DISEWA);
        } catch (Exception e) {
            throw new DatabaseException("Gagal start rental: " + e.getMessage(), e);
        }
    }
    
    public boolean cancelPenyewaan(int sewaId) throws DatabaseException {
        try {
            Penyewaan penyewaan = getPenyewaanById(sewaId);
            
            if (penyewaan == null) {
                throw new DatabaseException("Penyewaan tidak ditemukan");
            }
            
            if (penyewaan.getStatus() != StatusPenyewaan.MENUNGGU_PEMBAYARAN) {
                throw new ValidationException("Hanya bisa cancel penyewaan yang belum dibayar");
            }
            
            for (DetailPenyewaan detail : penyewaan.getDetailPenyewaanList()) {
                detailBajuDAO.tambahStok(detail.getDetailBajuId(), detail.getJumlah());
            }
            
            boolean updated = updateStatus(sewaId, StatusPenyewaan.DIBATALKAN);
            
            System.out.println("Penyewaan cancelled: #" + sewaId);
            
            return updated;
            
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Gagal cancel penyewaan: " + e.getMessage(), e);
        }
    }
    
    public boolean processReturn(int sewaId, LocalDate tglKembaliAktual, 
                                 List<KondisiReturn> kondisiItems) 
            throws ValidationException, DatabaseException {
        
        try {
            Penyewaan penyewaan = getPenyewaanById(sewaId);
            
            if (penyewaan == null) {
                throw new DatabaseException("Penyewaan tidak ditemukan");
            }
            
            if (penyewaan.getStatus() != StatusPenyewaan.SEDANG_DISEWA) {
                throw new ValidationException("Hanya bisa return penyewaan yang sedang disewa");
            }
            
            if (kondisiItems.size() != penyewaan.getDetailPenyewaanList().size()) {
                throw new ValidationException("Jumlah kondisi tidak sesuai dengan jumlah item");
            }
            
            penyewaan.setTglKembaliAktual(tglKembaliAktual);
            penyewaanDAO.update(penyewaan);
            
            for (int i = 0; i < kondisiItems.size(); i++) {
                KondisiReturn kondisiReturn = kondisiItems.get(i);
                DetailPenyewaan detail = penyewaan.getDetailPenyewaanList().get(i);
                
                detailPenyewaanDAO.updateKondisiKembali(
                    detail.getDetailSewaId(),
                    kondisiReturn.getKondisi(),
                    kondisiReturn.getKeterangan()
                );
                
                detailBajuDAO.tambahStok(detail.getDetailBajuId(), detail.getJumlah());
                
                if (kondisiReturn.getKondisi() != null && 
                    kondisiReturn.getKondisi().requiresDenda()) {
                    detailBajuDAO.updateKondisi(
                        detail.getDetailBajuId(), 
                        kondisiReturn.getKondisi()
                    );
                }
            }
            
            updateStatus(sewaId, StatusPenyewaan.DIKEMBALIKAN);
            
            updatePoinLoyalitas(penyewaan.getUserId(), penyewaan.getTotalHarga());
            System.out.println("Return processed: Sewa #" + sewaId + 
                             " | Actual return: " + tglKembaliAktual);
            return true;
            
        } catch (ValidationException | DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Gagal process return: " + e.getMessage(), e);
        }
    }
    
    public int getTotalPenyewaan(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        try {
            List<Penyewaan> list = penyewaanDAO.findByDateRange(startDate, endDate);
            return list.size();
        } catch (Exception e) {
            throw new DatabaseException("Gagal get total penyewaan: " + e.getMessage(), e);
        }
    }
    
    public double getTotalPendapatan(LocalDate startDate, LocalDate endDate) throws DatabaseException {
        try {
            return penyewaanDAO.getTotalPendapatan(startDate, endDate);
        } catch (Exception e) {
            throw new DatabaseException("Gagal get total pendapatan: " + e.getMessage(), e);
        }
    }
    
    public int countByStatus(StatusPenyewaan status) throws DatabaseException {
        try {
            return penyewaanDAO.countByStatus(status);
        } catch (Exception e) {
            throw new DatabaseException("Gagal count penyewaan: " + e.getMessage(), e);
        }
    }
    
    private void validatePenyewaanInput(List<CartItem> cartItems, 
                                       LocalDate tglSewa, LocalDate tglKembali) 
            throws ValidationException {
        
        if (cartItems == null || cartItems.isEmpty()) {
            throw new ValidationException("Keranjang kosong");
        }
        
        if (tglSewa == null || tglKembali == null) {
            throw new ValidationException("Tanggal sewa dan kembali harus diisi");
        }
        
        if (tglKembali.isBefore(tglSewa)) {
            throw new ValidationException("Tanggal kembali harus setelah tanggal sewa");
        }
        
        if (tglSewa.isBefore(LocalDate.now())) {
            throw new ValidationException("Tanggal sewa tidak boleh di masa lalu");
        }
        
        long jumlahHari = ChronoUnit.DAYS.between(tglSewa, tglKembali);
        if (jumlahHari < 1) {
            throw new ValidationException("Minimal sewa 1 hari");
        }
        
        if (jumlahHari > 30) {
            throw new ValidationException("Maksimal sewa 30 hari");
        }
    }
    
    private void checkStokAvailability(List<CartItem> cartItems) 
            throws StokTidakCukupException, DatabaseException {
        
        for (CartItem item : cartItems) {
            DetailBaju detailBaju = detailBajuDAO.findById(item.getDetailBaju().getDetailBajuId());
            
            if (detailBaju == null) {
                throw new DatabaseException("Detail baju tidak ditemukan");
            }
            
            if (!detailBaju.isStokCukup(item.getJumlah())) {
                throw new StokTidakCukupException(
                    "Stok tidak cukup untuk " + detailBaju.getNamaBaju(),
                    detailBaju.getBajuId(),
                    detailBaju.getNamaBaju(),
                    detailBaju.getStok(),
                    item.getJumlah()
                );
            }
        }
    }
    
    private void updatePoinLoyalitas(int userId, double totalBelanja) throws DatabaseException {
        try {
            Pelanggan pelanggan = pelangganDAO.findByUserId(userId);
            
            if (pelanggan != null) {
                int poin = Pelanggan.hitungPoinFromBelanja(totalBelanja);
                pelangganDAO.tambahPoin(pelanggan.getPelangganId(), poin);
                
                System.out.println("Poin added: " + poin + " for user " + userId);
            }
        } catch (Exception e) {
            System.err.println("Failed to update poin: " + e.getMessage());
        }
    }
    
    public static class CartItem {
        private DetailBaju detailBaju;
        private int jumlah;
        
        public CartItem(DetailBaju detailBaju, int jumlah) {
            this.detailBaju = detailBaju;
            this.jumlah = jumlah;
        }
        
        public DetailBaju getDetailBaju() {
            return detailBaju;
        }
        
        public void setDetailBaju(DetailBaju detailBaju) {
            this.detailBaju = detailBaju;
        }
        
        public int getJumlah() {
            return jumlah;
        }
        
        public void setJumlah(int jumlah) {
            this.jumlah = jumlah;
        }
    }
    
    public static class KondisiReturn {
        private int detailSewaId;
        private Kondisi kondisi;
        private String keterangan;
        
        public KondisiReturn(int detailSewaId, Kondisi kondisi, String keterangan) {
            this.detailSewaId = detailSewaId;
            this.kondisi = kondisi;
            this.keterangan = keterangan;
        }
        
        public int getDetailSewaId() {
            return detailSewaId;
        }
        
        public Kondisi getKondisi() {
            return kondisi;
        }
        
        public String getKeterangan() {
            return keterangan;
        }
    }
}