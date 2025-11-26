package com.mycompany.sewabaju.models;

import com.mycompany.sewabaju.models.enums.JenisDenda;
import com.mycompany.sewabaju.models.enums.StatusBayarDenda;
import java.time.LocalDateTime;
import java.util.Objects;

public class Denda {
    private int dendaId;
    private int sewaId;
    private JenisDenda jenisDenda;
    private double jumlah;
    private String keterangan;
    private StatusBayarDenda statusBayar;
    private LocalDateTime createdAt;
    private Penyewaan penyewaan;
    
    public Denda() {
        this.statusBayar = StatusBayarDenda.BELUM_DIBAYAR;
        this.createdAt = LocalDateTime.now();
    }
    
    public Denda(int sewaId, JenisDenda jenisDenda, double jumlah, String keterangan) {
        this();
        this.sewaId = sewaId;
        this.jenisDenda = jenisDenda;
        this.jumlah = jumlah;
        this.keterangan = keterangan;
    }
    
    public Denda(int dendaId, int sewaId, JenisDenda jenisDenda, double jumlah, 
                 String keterangan, StatusBayarDenda statusBayar) {
        this(sewaId, jenisDenda, jumlah, keterangan);
        this.dendaId = dendaId;
        this.statusBayar = statusBayar;
    }
    
    public int getDendaId() {
        return dendaId;
    }
    
    public void setDendaId(int dendaId) {
        this.dendaId = dendaId;
    }
    
    public int getSewaId() {
        return sewaId;
    }
    
    public void setSewaId(int sewaId) {
        this.sewaId = sewaId;
    }
    
    public JenisDenda getJenisDenda() {
        return jenisDenda;
    }
    
    public void setJenisDenda(JenisDenda jenisDenda) {
        this.jenisDenda = jenisDenda;
    }
    
    public double getJumlah() {
        return jumlah;
    }
    
    public void setJumlah(double jumlah) {
        this.jumlah = Math.max(0, jumlah);
    }
    
    public String getKeterangan() {
        return keterangan;
    }
    
    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }
    
    public StatusBayarDenda getStatusBayar() {
        return statusBayar;
    }
    
    public void setStatusBayar(StatusBayarDenda statusBayar) {
        this.statusBayar = statusBayar;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
    
    public boolean isPaid() {
        return statusBayar == StatusBayarDenda.SUDAH_DIBAYAR;
    }
    
    public boolean isUnpaid() {
        return statusBayar == StatusBayarDenda.BELUM_DIBAYAR;
    }
    
    public void markAsPaid() {
        this.statusBayar = StatusBayarDenda.SUDAH_DIBAYAR;
    }
    
    public String getJenisDendaDisplay() {
        return jenisDenda != null ? jenisDenda.getDisplayName() : "";
    }
    
    public String getStatusBayarDisplay() {
        return statusBayar != null ? statusBayar.getDisplayName() : "";
    }
    
    public String getStatusCssClass() {
        return statusBayar != null ? statusBayar.getCssClass() : "";
    }
    
    public String getJumlahFormatted() {
        return String.format("Rp %.0f", jumlah);
    }
    
    public static Denda createDendaKeterlambatan(int sewaId, int hariTerlambat) {
        double jumlah = JenisDenda.KETERLAMBATAN.hitungDenda(hariTerlambat);
        String keterangan = "Keterlambatan " + hariTerlambat + " hari";
        return new Denda(sewaId, JenisDenda.KETERLAMBATAN, jumlah, keterangan);
    }
    
    public static Denda createDendaKerusakan(int sewaId, double jumlah, String keterangan) {
        return new Denda(sewaId, JenisDenda.KERUSAKAN, jumlah, keterangan);
    }
    
    public static Denda createDendaKehilangan(int sewaId, double jumlah, String keterangan) {
        return new Denda(sewaId, JenisDenda.KEHILANGAN, jumlah, keterangan);
    }
    
    public boolean isValid() {
        return sewaId > 0 &&
               jenisDenda != null &&
               jumlah >= 0 &&
               statusBayar != null;
    }
    
    @Override
    public String toString() {
        return getJenisDendaDisplay() + " - " + getJumlahFormatted() + 
               " (" + getStatusBayarDisplay() + ")";
    }
    
    public String toDetailString() {
        return "Denda{" +
                "dendaId=" + dendaId +
                ", sewaId=" + sewaId +
                ", jenis=" + jenisDenda +
                ", jumlah=" + jumlah +
                ", keterangan='" + keterangan + '\'' +
                ", statusBayar=" + statusBayar +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Denda denda = (Denda) o;
        return dendaId == denda.dendaId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(dendaId);
    }
}