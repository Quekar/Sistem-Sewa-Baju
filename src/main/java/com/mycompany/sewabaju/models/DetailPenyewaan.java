package com.mycompany.sewabaju.models;

import com.mycompany.sewabaju.models.enums.Kondisi;
import java.util.Objects;

public class DetailPenyewaan {
    private int detailSewaId;
    private int sewaId;
    private int detailBajuId;
    private int jumlah;
    private double hargaPerItem;
    private double subtotal;
    private Kondisi kondisiSaatKembali;
    private String keteranganKerusakan;
    private Penyewaan penyewaan;
    private DetailBaju detailBaju;
    
    public DetailPenyewaan() {
    }
    
    public DetailPenyewaan(int sewaId, int detailBajuId, int jumlah, double hargaPerItem) {
        this.sewaId = sewaId;
        this.detailBajuId = detailBajuId;
        this.jumlah = jumlah;
        this.hargaPerItem = hargaPerItem;
        this.subtotal = hitungSubtotal();
    }
    
    public DetailPenyewaan(int detailSewaId, int sewaId, int detailBajuId, int jumlah, 
                          double hargaPerItem, double subtotal) {
        this(sewaId, detailBajuId, jumlah, hargaPerItem);
        this.detailSewaId = detailSewaId;
        this.subtotal = subtotal;
    }
    
    public int getDetailSewaId() {
        return detailSewaId;
    }
    
    public void setDetailSewaId(int detailSewaId) {
        this.detailSewaId = detailSewaId;
    }
    
    public int getSewaId() {
        return sewaId;
    }
    
    public void setSewaId(int sewaId) {
        this.sewaId = sewaId;
    }
    
    public int getDetailBajuId() {
        return detailBajuId;
    }
    
    public void setDetailBajuId(int detailBajuId) {
        this.detailBajuId = detailBajuId;
    }
    
    public int getJumlah() {
        return jumlah;
    }
    
    public void setJumlah(int jumlah) {
        this.jumlah = Math.max(1, jumlah);
        this.subtotal = hitungSubtotal();
    }
    
    public double getHargaPerItem() {
        return hargaPerItem;
    }
    
    public void setHargaPerItem(double hargaPerItem) {
        this.hargaPerItem = hargaPerItem;
        this.subtotal = hitungSubtotal();
    }
    
    public double getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }
    
    public Kondisi getKondisiSaatKembali() {
        return kondisiSaatKembali;
    }
    
    public void setKondisiSaatKembali(Kondisi kondisiSaatKembali) {
        this.kondisiSaatKembali = kondisiSaatKembali;
    }
    
    public String getKeteranganKerusakan() {
        return keteranganKerusakan;
    }
    
    public void setKeteranganKerusakan(String keteranganKerusakan) {
        this.keteranganKerusakan = keteranganKerusakan;
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
    
    public DetailBaju getDetailBaju() {
        return detailBaju;
    }
    
    public void setDetailBaju(DetailBaju detailBaju) {
        this.detailBaju = detailBaju;
        if (detailBaju != null) {
            this.detailBajuId = detailBaju.getDetailBajuId();
        }
    }
    
    public double hitungSubtotal() {
        return jumlah * hargaPerItem;
    }
    
    public boolean isReturned() {
        return kondisiSaatKembali != null;
    }
    
    public boolean isDamaged() {
        return kondisiSaatKembali != null && kondisiSaatKembali.requiresDenda();
    }
    
    public boolean requiresDenda() {
        return isDamaged();
    }
    
    public double getEstimasiDenda() {
        if (kondisiSaatKembali != null) {
            return kondisiSaatKembali.getDendaEstimasi() * jumlah;
        }
        return 0;
    }
    
    public String getNamaBaju() {
        return detailBaju != null ? detailBaju.getNamaBaju() : "";
    }
    
    public String getUkuran() {
        return detailBaju != null ? detailBaju.getUkuranDisplay() : "";
    }
    
    public String getItemDisplay() {
        return getNamaBaju() + " - " + getUkuran() + " × " + jumlah;
    }
    
    public String getKondisiDisplay() {
        return kondisiSaatKembali != null ? kondisiSaatKembali.getDisplayName() : "-";
    }
    
    public boolean isValid() {
        return sewaId > 0 &&
               detailBajuId > 0 &&
               jumlah > 0 &&
               hargaPerItem >= 0 &&
               subtotal >= 0;
    }
    
    @Override
    public String toString() {
        return getItemDisplay() + " - Rp " + subtotal;
    }
    
    public String toDetailString() {
        return "DetailPenyewaan{" +
                "detailSewaId=" + detailSewaId +
                ", item='" + getItemDisplay() + '\'' +
                ", hargaPerItem=" + hargaPerItem +
                ", subtotal=" + subtotal +
                ", kondisi=" + getKondisiDisplay() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetailPenyewaan that = (DetailPenyewaan) o;
        return detailSewaId == that.detailSewaId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(detailSewaId);
    }
}