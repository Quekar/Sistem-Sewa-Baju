package com.mycompany.sewabaju.models;

import com.mycompany.sewabaju.models.enums.MetodePembayaran;
import com.mycompany.sewabaju.models.enums.StatusPembayaran;
import java.time.LocalDateTime;
import java.util.Objects;

public class Pembayaran {
    private int pembayaranId;
    private int sewaId;
    private MetodePembayaran metodePembayaran;
    private double jumlah;
    private String buktiPembayaran;
    private StatusPembayaran status;
    private LocalDateTime tanggalBayar;
    private Integer verifiedBy;
    private LocalDateTime verifiedAt; 
    private Penyewaan penyewaan;
    private Admin verifier;
    
    public Pembayaran() {
        this.status = StatusPembayaran.MENUNGGU_VERIFIKASI;
        this.tanggalBayar = LocalDateTime.now();
    }
    
    public Pembayaran(int sewaId, MetodePembayaran metodePembayaran, double jumlah, String buktiPembayaran) {
        this();
        this.sewaId = sewaId;
        this.metodePembayaran = metodePembayaran;
        this.jumlah = jumlah;
        this.buktiPembayaran = buktiPembayaran;
    }
    
    public Pembayaran(int pembayaranId, int sewaId, MetodePembayaran metodePembayaran, 
                      double jumlah, String buktiPembayaran, StatusPembayaran status) {
        this(sewaId, metodePembayaran, jumlah, buktiPembayaran);
        this.pembayaranId = pembayaranId;
        this.status = status;
    }
    
    public int getPembayaranId() {
        return pembayaranId;
    }
    
    public void setPembayaranId(int pembayaranId) {
        this.pembayaranId = pembayaranId;
    }
    
    public int getSewaId() {
        return sewaId;
    }
    
    public void setSewaId(int sewaId) {
        this.sewaId = sewaId;
    }
    
    public MetodePembayaran getMetodePembayaran() {
        return metodePembayaran;
    }
    
    public void setMetodePembayaran(MetodePembayaran metodePembayaran) {
        this.metodePembayaran = metodePembayaran;
    }
    
    public double getJumlah() {
        return jumlah;
    }
    
    public void setJumlah(double jumlah) {
        this.jumlah = jumlah;
    }
    
    public String getBuktiPembayaran() {
        return buktiPembayaran;
    }
    
    public void setBuktiPembayaran(String buktiPembayaran) {
        this.buktiPembayaran = buktiPembayaran;
    }
    
    public StatusPembayaran getStatus() {
        return status;
    }
    
    public void setStatus(StatusPembayaran status) {
        this.status = status;
    }
    
    public LocalDateTime getTanggalBayar() {
        return tanggalBayar;
    }
    
    public void setTanggalBayar(LocalDateTime tanggalBayar) {
        this.tanggalBayar = tanggalBayar;
    }
    
    public Integer getVerifiedBy() {
        return verifiedBy;
    }
    
    public void setVerifiedBy(Integer verifiedBy) {
        this.verifiedBy = verifiedBy;
    }
    
    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }
    
    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
    
    public Penyewaan getPenyewaan() {
        return penyewaan;
    }
    
    public void setPenyewaan(Penyewaan penyewaan) {
        this.penyewaan = penyewaan;
        if (penyewaan != null) {
            this.sewaId = penyewaan.getSewaId();
        }
    }
    
    public Admin getVerifier() {
        return verifier;
    }
    
    public void setVerifier(Admin verifier) {
        this.verifier = verifier;
        if (verifier != null) {
            this.verifiedBy = verifier.getAdminId();
        }
    }
    
    public boolean isVerified() {
        return status == StatusPembayaran.BERHASIL;
    }
    
    public boolean isPending() {
        return status == StatusPembayaran.MENUNGGU_VERIFIKASI;
    }
    
    public boolean isRejected() {
        return status == StatusPembayaran.DITOLAK;
    }
    
    public boolean isProcessed() {
        return status.isProcessed();
    }
    
    public boolean requiresProof() {
        return metodePembayaran != null && metodePembayaran.requiresProof();
    }
    
    public void approve(int adminId) {
        this.status = StatusPembayaran.BERHASIL;
        this.verifiedBy = adminId;
        this.verifiedAt = LocalDateTime.now();
    }
    
    public void reject(int adminId) {
        this.status = StatusPembayaran.DITOLAK;
        this.verifiedBy = adminId;
        this.verifiedAt = LocalDateTime.now();
    }
    
    public String getNamaVerifier() {
        return verifier != null ? verifier.getNama() : "-";
    }
    
    public String getMetodePembayaranDisplay() {
        return metodePembayaran != null ? metodePembayaran.getDisplayName() : "";
    }
    
    public String getStatusDisplay() {
        return status != null ? status.getDisplayName() : "";
    }
    
    public String getStatusCssClass() {
        return status != null ? status.getCssClass() : "";
    }
    
    public String getJumlahFormatted() {
        return String.format("Rp %.0f", jumlah);
    }
    
    public boolean isValid() {
        return sewaId > 0 &&
               metodePembayaran != null &&
               jumlah > 0 &&
               status != null &&
               (!requiresProof() || buktiPembayaran != null);
    }
    
    @Override
    public String toString() {
        return "Pembayaran #" + pembayaranId + " - " + getMetodePembayaranDisplay() + 
               " (" + getStatusDisplay() + ")";
    }
    
    public String toDetailString() {
        return "Pembayaran{" +
                "pembayaranId=" + pembayaranId +
                ", sewaId=" + sewaId +
                ", metode=" + metodePembayaran +
                ", jumlah=" + jumlah +
                ", status=" + status +
                ", verifiedBy=" + getNamaVerifier() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pembayaran that = (Pembayaran) o;
        return pembayaranId == that.pembayaranId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(pembayaranId);
    }
}