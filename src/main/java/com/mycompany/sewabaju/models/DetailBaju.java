package com.mycompany.sewabaju.models;

import com.mycompany.sewabaju.models.enums.Kondisi;
import com.mycompany.sewabaju.models.enums.Ukuran;
import java.util.Objects;

public class DetailBaju {
    private int detailBajuId;
    private int bajuId;
    private Ukuran ukuran;
    private double hargaSewa;  // harga per hari
    private int stok;
    private Kondisi kondisi;
    
    private Baju baju;
    
    public DetailBaju() {
        this.kondisi = Kondisi.BAIK;
    }
    
    public DetailBaju(int bajuId, Ukuran ukuran, double hargaSewa, int stok) {
        this();
        this.bajuId = bajuId;
        this.ukuran = ukuran;
        this.hargaSewa = hargaSewa;
        this.stok = stok;
    }
    
    public DetailBaju(int bajuId, Ukuran ukuran, double hargaSewa, int stok, Kondisi kondisi) {
        this(bajuId, ukuran, hargaSewa, stok);
        this.kondisi = kondisi;
    }
    
    public DetailBaju(int detailBajuId, int bajuId, Ukuran ukuran, double hargaSewa, int stok, Kondisi kondisi) {
        this(bajuId, ukuran, hargaSewa, stok, kondisi);
        this.detailBajuId = detailBajuId;
    }
    
    public int getDetailBajuId() {
        return detailBajuId;
    }
    
    public void setDetailBajuId(int detailBajuId) {
        this.detailBajuId = detailBajuId;
    }
    
    public int getBajuId() {
        return bajuId;
    }
    
    public void setBajuId(int bajuId) {
        this.bajuId = bajuId;
    }
    
    public Ukuran getUkuran() {
        return ukuran;
    }
    
    public void setUkuran(Ukuran ukuran) {
        this.ukuran = ukuran;
    }
    
    public double getHargaSewa() {
        return hargaSewa;
    }
    
    public void setHargaSewa(double hargaSewa) {
        this.hargaSewa = hargaSewa;
    }
    
    public int getStok() {
        return stok;
    }
    
    public void setStok(int stok) {
        this.stok = Math.max(0, stok);
    }
    
    public Kondisi getKondisi() {
        return kondisi;
    }
    
    public void setKondisi(Kondisi kondisi) {
        this.kondisi = kondisi;
    }
    
    public Baju getBaju() {
        return baju;
    }
    
    public void setBaju(Baju baju) {
        this.baju = baju;
        if (baju != null) {
            this.bajuId = baju.getBajuId();
        }
    }
    
    public boolean isAvailable() {
        return stok > 0 && (kondisi == null || kondisi.isRentable());
    }
    
    public boolean isStokCukup(int jumlah) {
        return stok >= jumlah && jumlah > 0;
    }
    
    public boolean kurangiStok(int jumlah) {
        if (isStokCukup(jumlah)) {
            this.stok -= jumlah;
            return true;
        }
        return false;
    }
    
    public void tambahStok(int jumlah) {
        if (jumlah > 0) {
            this.stok += jumlah;
        }
    }
    
    public String getUkuranDisplay() {
        return ukuran != null ? ukuran.getCode() : "";
    }
    
    public String getKondisiDisplay() {
        return kondisi != null ? kondisi.getDisplayName() : "";
    }
    
    public String getNamaBaju() {
        return baju != null ? baju.getNamaBaju() : "";
    }
    
    public String getHargaSewaFormatted() {
        return String.format("Rp %.0f/hari", hargaSewa);
    }
    
    public double hitungHargaTotal(int jumlahHari) {
        return hargaSewa * jumlahHari;
    }
    
    public boolean isValid() {
        return bajuId > 0 && 
               ukuran != null && 
               hargaSewa > 0 && 
               stok >= 0;
    }
    
    @Override
    public String toString() {
        return getNamaBaju() + " - " + getUkuranDisplay() + 
               " (Stok: " + stok + ")";
    }
    
    public String toDetailString() {
        return "DetailBaju{" +
                "detailBajuId=" + detailBajuId +
                ", baju='" + getNamaBaju() + '\'' +
                ", ukuran=" + ukuran +
                ", hargaSewa=" + hargaSewa +
                ", stok=" + stok +
                ", kondisi=" + kondisi +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetailBaju that = (DetailBaju) o;
        return detailBajuId == that.detailBajuId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(detailBajuId);
    }
}