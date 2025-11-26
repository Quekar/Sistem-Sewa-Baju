package com.mycompany.sewabaju.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Favorit {
    private int favoritId;
    private int pelangganId;
    private int bajuId;
    private LocalDateTime createdAt;
    private Pelanggan pelanggan;
    private Baju baju;
    
    public Favorit() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Favorit(int pelangganId, int bajuId) {
        this();
        this.pelangganId = pelangganId;
        this.bajuId = bajuId;
    }
    
    public Favorit(int favoritId, int pelangganId, int bajuId) {
        this(pelangganId, bajuId);
        this.favoritId = favoritId;
    }
    
    public Favorit(int favoritId, int pelangganId, int bajuId, LocalDateTime createdAt) {
        this(favoritId, pelangganId, bajuId);
        this.createdAt = createdAt;
    }
    
    public int getFavoritId() {
        return favoritId;
    }
    
    public void setFavoritId(int favoritId) {
        this.favoritId = favoritId;
    }
    
    public int getPelangganId() {
        return pelangganId;
    }
    
    public void setPelangganId(int pelangganId) {
        this.pelangganId = pelangganId;
    }
    
    public int getBajuId() {
        return bajuId;
    }
    
    public void setBajuId(int bajuId) {
        this.bajuId = bajuId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Pelanggan getPelanggan() {
        return pelanggan;
    }
    
    public void setPelanggan(Pelanggan pelanggan) {
        this.pelanggan = pelanggan;
        if (pelanggan != null) {
            this.pelangganId = pelanggan.getPelangganId();
        }
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
    
    public String getNamaPelanggan() {
        return pelanggan != null ? pelanggan.getNama() : "";
    }
    
    public String getNamaBaju() {
        return baju != null ? baju.getNamaBaju() : "";
    }
    
    public String getKategoriBaju() {
        return baju != null ? baju.getNamaKategori() : "";
    }
    
    public boolean isValid() {
        return pelangganId > 0 && bajuId > 0;
    }
    
    public boolean isOwnedBy(int pelangganId) {
        return this.pelangganId == pelangganId;
    }
    
    public boolean isFavoritOf(int bajuId) {
        return this.bajuId == bajuId;
    }
    
    @Override
    public String toString() {
        return getNamaPelanggan() + " → " + getNamaBaju();
    }
    
    public String toDetailString() {
        return "Favorit{" +
                "favoritId=" + favoritId +
                ", pelanggan='" + getNamaPelanggan() + '\'' +
                ", baju='" + getNamaBaju() + '\'' +
                ", kategori='" + getKategoriBaju() + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Favorit favorit = (Favorit) o;
        return pelangganId == favorit.pelangganId && bajuId == favorit.bajuId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(pelangganId, bajuId);
    }
}