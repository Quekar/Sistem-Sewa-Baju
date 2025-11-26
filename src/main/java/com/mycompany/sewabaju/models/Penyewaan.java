package com.mycompany.sewabaju.models;

import com.mycompany.sewabaju.models.enums.JenisDenda;
import com.mycompany.sewabaju.models.enums.StatusPenyewaan;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Penyewaan {
    private int sewaId;
    private int userId;
    private LocalDate tglSewa;
    private LocalDate tglKembali;
    private LocalDate tglKembaliAktual;
    private double totalHarga;
    private StatusPenyewaan status;
    private LocalDateTime createdAt;
    
    private User user;
    private List<DetailPenyewaan> detailPenyewaanList;
    private Pembayaran pembayaran;
    private List<Denda> dendaList;
    
    public Penyewaan() {
        this.status = StatusPenyewaan.MENUNGGU_PEMBAYARAN;
        this.createdAt = LocalDateTime.now();
        this.detailPenyewaanList = new ArrayList<>();
        this.dendaList = new ArrayList<>();
    }
    
    public Penyewaan(int userId, LocalDate tglSewa, LocalDate tglKembali, double totalHarga) {
        this();
        this.userId = userId;
        this.tglSewa = tglSewa;
        this.tglKembali = tglKembali;
        this.totalHarga = totalHarga;
    }
    
    public Penyewaan(int sewaId, int userId, LocalDate tglSewa, LocalDate tglKembali, 
                     double totalHarga, StatusPenyewaan status) {
        this(userId, tglSewa, tglKembali, totalHarga);
        this.sewaId = sewaId;
        this.status = status;
    }
    
    public int getSewaId() {
        return sewaId;
    }
    
    public void setSewaId(int sewaId) {
        this.sewaId = sewaId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public LocalDate getTglSewa() {
        return tglSewa;
    }
    
    public void setTglSewa(LocalDate tglSewa) {
        this.tglSewa = tglSewa;
    }
    
    public LocalDate getTglKembali() {
        return tglKembali;
    }
    
    public void setTglKembali(LocalDate tglKembali) {
        this.tglKembali = tglKembali;
    }
    
    public LocalDate getTglKembaliAktual() {
        return tglKembaliAktual;
    }
    
    public void setTglKembaliAktual(LocalDate tglKembaliAktual) {
        this.tglKembaliAktual = tglKembaliAktual;
    }
    
    public double getTotalHarga() {
        return totalHarga;
    }
    
    public void setTotalHarga(double totalHarga) {
        this.totalHarga = totalHarga;
    }
    
    public StatusPenyewaan getStatus() {
        return status;
    }
    
    public void setStatus(StatusPenyewaan status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.userId = user.getUserId();
        }
    }
    
    public List<DetailPenyewaan> getDetailPenyewaanList() {
        return detailPenyewaanList;
    }
    
    public void setDetailPenyewaanList(List<DetailPenyewaan> detailPenyewaanList) {
        this.detailPenyewaanList = detailPenyewaanList;
    }
    
    public Pembayaran getPembayaran() {
        return pembayaran;
    }
    
    public void setPembayaran(Pembayaran pembayaran) {
        this.pembayaran = pembayaran;
    }
    
    public List<Denda> getDendaList() {
        return dendaList;
    }
    
    public void setDendaList(List<Denda> dendaList) {
        this.dendaList = dendaList;
    }
    
    public long hitungLamaHari() {
        if (tglSewa == null || tglKembali == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(tglSewa, tglKembali);
    }
    
    public long hitungKeterlambatan() {
        if (tglKembaliAktual == null || tglKembali == null) {
            return 0;
        }
        
        long days = ChronoUnit.DAYS.between(tglKembali, tglKembaliAktual);
        return Math.max(0, days);  // tidak boleh negatif
    }
    
    public boolean isOverdue() {
        if (tglKembaliAktual != null) {
            return tglKembaliAktual.isAfter(tglKembali);
        }
        // Jika belum dikembalikan, cek dengan hari ini
        return LocalDate.now().isAfter(tglKembali) && 
               status == StatusPenyewaan.SEDANG_DISEWA;
    }
    
    public double hitungDendaKeterlambatan() {
        long hariTerlambat = hitungKeterlambatan();
        if (hariTerlambat > 0) {
            return JenisDenda.KETERLAMBATAN.hitungDenda((int) hariTerlambat);
        }
        return 0;
    }
    
    public double getTotalDenda() {
        return dendaList.stream()
                .mapToDouble(Denda::getJumlah)
                .sum();
    }
    
    public double getTotalDendaBelumBayar() {
        return dendaList.stream()
                .filter(Denda::isUnpaid)
                .mapToDouble(Denda::getJumlah)
                .sum();
    }
    
    public double getGrandTotal() {
        return totalHarga + getTotalDenda();
    }
    
    public int getJumlahItem() {
        return detailPenyewaanList.stream()
                .mapToInt(DetailPenyewaan::getJumlah)
                .sum();
    }
    
    public String getNamaPelanggan() {
        return user != null ? user.getNama() : "";
    }
    
    public boolean isPaid() {
        return pembayaran != null && pembayaran.isVerified();
    }
    
    public boolean isCompleted() {
        return status == StatusPenyewaan.DIKEMBALIKAN;
    }
    
    public boolean isValid() {
        return userId > 0 &&
               tglSewa != null &&
               tglKembali != null &&
               tglKembali.isAfter(tglSewa) &&
               totalHarga >= 0 &&
               status != null;
    }
    
    @Override
    public String toString() {
        return "Penyewaan #" + sewaId + " - " + getNamaPelanggan() + 
               " (" + status.getDisplayName() + ")";
    }
    
    public String toDetailString() {
        return "Penyewaan{" +
                "sewaId=" + sewaId +
                ", user='" + getNamaPelanggan() + '\'' +
                ", tglSewa=" + tglSewa +
                ", tglKembali=" + tglKembali +
                ", totalHarga=" + totalHarga +
                ", status=" + status +
                ", items=" + getJumlahItem() +
                ", denda=" + getTotalDenda() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Penyewaan penyewaan = (Penyewaan) o;
        return sewaId == penyewaan.sewaId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sewaId);
    }
}