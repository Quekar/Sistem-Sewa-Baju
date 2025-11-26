package com.mycompany.sewabaju.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Baju {
    private int bajuId;
    private int kategoriId;
    private String namaBaju;
    private String deskripsi;
    private String foto; // path/URL ke file foto
    private LocalDateTime createdAt;
    private Kategori kategori;
    private List<DetailBaju> detailBajuList;
    
    public Baju() {
        this.createdAt = LocalDateTime.now();
        this.detailBajuList = new ArrayList<>();
    }
    
    public Baju(int kategoriId, String namaBaju, String deskripsi, String foto) {
        this();
        this.kategoriId = kategoriId;
        this.namaBaju = namaBaju;
        this.deskripsi = deskripsi;
        this.foto = foto;
    }
    
    public Baju(int bajuId, int kategoriId, String namaBaju, String deskripsi, String foto) {
        this(kategoriId, namaBaju, deskripsi, foto);
        this.bajuId = bajuId;
    }
    
    public int getBajuId() {
        return bajuId;
    }
    
    public void setBajuId(int bajuId) {
        this.bajuId = bajuId;
    }
    
    public int getKategoriId() {
        return kategoriId;
    }
    
    public void setKategoriId(int kategoriId) {
        this.kategoriId = kategoriId;
    }
    
    public String getNamaBaju() {
        return namaBaju;
    }
    
    public void setNamaBaju(String namaBaju) {
        this.namaBaju = namaBaju;
    }
    
    public String getDeskripsi() {
        return deskripsi;
    }
    
    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }
    
    public String getFoto() {
        return foto;
    }
    
    public void setFoto(String foto) {
        this.foto = foto;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Kategori getKategori() {
        return kategori;
    }
    
    public void setKategori(Kategori kategori) {
        this.kategori = kategori;
        if (kategori != null) {
            this.kategoriId = kategori.getKategoriId();
        }
    }
    
    public List<DetailBaju> getDetailBajuList() {
        return detailBajuList;
    }
    
    public void setDetailBajuList(List<DetailBaju> detailBajuList) {
        this.detailBajuList = detailBajuList;
    }
    
    public void addDetailBaju(DetailBaju detailBaju) {
        if (!this.detailBajuList.contains(detailBaju)) {
            this.detailBajuList.add(detailBaju);
            detailBaju.setBajuId(this.bajuId);
        }
    }
    
    public int getTotalStok() {
        return detailBajuList.stream()
                .mapToInt(DetailBaju::getStok)
                .sum();
    }
    
    public boolean isAvailable() {
        return getTotalStok() > 0;
    }
    
    public int getJumlahVarian() {
        return detailBajuList.size();
    }
    
    public String getRangeHarga() {
        if (detailBajuList.isEmpty()) {
            return "Rp 0";
        }
        
        double min = detailBajuList.stream()
                .mapToDouble(DetailBaju::getHargaSewa)
                .min()
                .orElse(0);
        
        double max = detailBajuList.stream()
                .mapToDouble(DetailBaju::getHargaSewa)
                .max()
                .orElse(0);
        
        if (min == max) {
            return String.format("Rp %.0f", min);
        } else {
            return String.format("Rp %.0f - Rp %.0f", min, max);
        }
    }
    
    public String getNamaKategori() {
        return kategori != null ? kategori.getNamaKategori() : "";
    }
    
    public boolean isValid() {
        return namaBaju != null && !namaBaju.trim().isEmpty() &&
               kategoriId > 0;
    }
    
    @Override
    public String toString() {
        return namaBaju;
    }
    
    public String toDetailString() {
        return "Baju{" +
                "bajuId=" + bajuId +
                ", kategori='" + getNamaKategori() + '\'' +
                ", namaBaju='" + namaBaju + '\'' +
                ", totalStok=" + getTotalStok() +
                ", varian=" + getJumlahVarian() +
                ", harga=" + getRangeHarga() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Baju baju = (Baju) o;
        return bajuId == baju.bajuId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(bajuId);
    }
}