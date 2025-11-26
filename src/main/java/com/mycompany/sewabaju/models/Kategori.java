package com.mycompany.sewabaju.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Kategori {
    private int kategoriId;
    private String namaKategori;
    private String deskripsi;
    private List<Baju> bajuList;
    
    public Kategori() {
        this.bajuList = new ArrayList<>();
    }
    
    public Kategori(String namaKategori, String deskripsi) {
        this();
        this.namaKategori = namaKategori;
        this.deskripsi = deskripsi;
    }
    
    public Kategori(int kategoriId, String namaKategori, String deskripsi) {
        this(namaKategori, deskripsi);
        this.kategoriId = kategoriId;
    }
    
    public int getKategoriId() {
        return kategoriId;
    }
    
    public void setKategoriId(int kategoriId) {
        this.kategoriId = kategoriId;
    }
    
    public String getNamaKategori() {
        return namaKategori;
    }
    
    public void setNamaKategori(String namaKategori) {
        this.namaKategori = namaKategori;
    }
    
    public String getDeskripsi() {
        return deskripsi;
    }
    
    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }
    
    public List<Baju> getBajuList() {
        return bajuList;
    }
    
    public void setBajuList(List<Baju> bajuList) {
        this.bajuList = bajuList;
    }
    
    public void addBaju(Baju baju) {
        if (!this.bajuList.contains(baju)) {
            this.bajuList.add(baju);
        }
    }
    
    public int getTotalBaju() {
        return bajuList.size();
    }
    
    public boolean isValid() {
        return namaKategori != null && !namaKategori.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return namaKategori;
    }
    
    public String toDetailString() {
        return "Kategori{" +
                "kategoriId=" + kategoriId +
                ", namaKategori='" + namaKategori + '\'' +
                ", deskripsi='" + deskripsi + '\'' +
                ", totalBaju=" + bajuList.size() +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kategori kategori = (Kategori) o;
        return kategoriId == kategori.kategoriId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(kategoriId);
    }
}