package com.mycompany.sewabaju.controllers.admin;

import com.mycompany.sewabaju.models.Admin;
import com.mycompany.sewabaju.models.Penyewaan;
import com.mycompany.sewabaju.models.enums.StatusPenyewaan;
import com.mycompany.sewabaju.services.AuthService;
import com.mycompany.sewabaju.services.PembayaranService;
import com.mycompany.sewabaju.services.PenyewaanService;
import com.mycompany.sewabaju.utils.AlertUtil;
import com.mycompany.sewabaju.utils.DateUtil;
import com.mycompany.sewabaju.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class AdminDashboardController {
    @FXML private Label lblWelcome;
    @FXML private Label lblJabatan;
    @FXML private Button btnLogout;

    @FXML private Label lblTotalPenyewaanHariIni;
    @FXML private Label lblTotalPendapatanBulanIni;
    @FXML private Label lblJumlahPelanggan;
    @FXML private Label lblPembayaranPending;

    @FXML private ListView<String> listRecentPenyewaan;
    @FXML private ListView<String> listPembayaranPending;

    @FXML private Button btnManageKategori;
    @FXML private Button btnManageBaju;
    @FXML private Button btnVerifikasiPembayaran;
    @FXML private Button btnManagePenyewaan;
    @FXML private Button btnLaporan;
    
    private AuthService authService;
    private PenyewaanService penyewaanService;
    private PembayaranService pembayaranService;
    @FXML
    public void initialize() {
        authService = AuthService.getInstance();
        penyewaanService = PenyewaanService.getInstance();
        pembayaranService = PembayaranService.getInstance();
        loadAdminInfo();
        loadStatistics();
        loadRecentActivity();
        
        System.out.println("AdminDashboardController initialized");
    }
    
    private void loadAdminInfo() {
        try {
            Admin admin = Session.getInstance().getCurrentAdmin();
            
            if (admin != null) {
                lblWelcome.setText("Selamat datang, " + admin.getNama());
                lblJabatan.setText(admin.getJabatan() != null ? admin.getJabatan() : "Admin");
            }
        } catch (Exception e) {
            System.err.println("Error loading admin info: " + e.getMessage());
        }
    }

    private void loadStatistics() {
        try {
            LocalDate today = LocalDate.now();
            int totalHariIni = penyewaanService.getTotalPenyewaan(today, today);
            lblTotalPenyewaanHariIni.setText(String.valueOf(totalHariIni));
            LocalDate startOfMonth = DateUtil.getStartOfMonth(today);
            LocalDate endOfMonth = DateUtil.getEndOfMonth(today);
            double pendapatan = penyewaanService.getTotalPendapatan(startOfMonth, endOfMonth);
            lblTotalPendapatanBulanIni.setText(String.format("Rp %.0f", pendapatan));
            lblJumlahPelanggan.setText("-");
            int pendingCount = pembayaranService.countPembayaranPending();
            lblPembayaranPending.setText(String.valueOf(pendingCount));
            if (pendingCount > 0) {
                lblPembayaranPending.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
            }
            
        } catch (Exception e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadRecentActivity() {
        try {
            List<Penyewaan> recentList = penyewaanService.getRecentPenyewaan(10);
            listRecentPenyewaan.getItems().clear();
            
            for (Penyewaan p : recentList) {
                String item = String.format("#%d - %s - %s - %s",
                    p.getSewaId(),
                    p.getNamaPelanggan(),
                    p.getStatus().getDisplayName(),
                    DateUtil.formatDate(p.getTglSewa())
                );
                listRecentPenyewaan.getItems().add(item);
            }
            int pendingCount = pembayaranService.countPembayaranPending();
            listPembayaranPending.getItems().clear();
            
            if (pendingCount > 0) {
                listPembayaranPending.getItems().add(
                    "Ada " + pendingCount + " pembayaran menunggu verifikasi"
                );
                listPembayaranPending.getItems().add("Klik 'Verifikasi Pembayaran' untuk melihat");
            } else {
                listPembayaranPending.getItems().add("Tidak ada pembayaran pending");
            }
            
        } catch (Exception e) {
            System.err.println("Error loading recent activity: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void handleManageKategori() {
        navigateToPage(
            "/com/mycompany/sewabaju/fxml/admin/manage_kategori.fxml",
            "Manage Kategori - SewaBaju"
        );
    }
    @FXML
    private void handleManageBaju() {
        navigateToPage(
            "/com/mycompany/sewabaju/fxml/admin/manage_baju.fxml",
            "Manage Baju - SewaBaju"
        );
    }
    @FXML
    private void handleVerifikasiPembayaran() {
        navigateToPage(
            "/com/mycompany/sewabaju/fxml/admin/verifikasi_pembayaran.fxml",
            "Verifikasi Pembayaran - SewaBaju"
        );
    }
    @FXML
    private void handleManagePenyewaan() {
        navigateToPage(
            "/com/mycompany/sewabaju/fxml/admin/manage_penyewaan.fxml",
            "Manage Penyewaan - SewaBaju"
        );
    }
    @FXML
    private void handleLaporan() {
        navigateToPage(
            "/com/mycompany/sewabaju/fxml/admin/laporan.fxml",
            "Laporan - SewaBaju"
        );
    }
    @FXML
    private void handleRefresh() {
        loadStatistics();
        loadRecentActivity();
        AlertUtil.showSuccess("Data berhasil direfresh");
    }
    @FXML
    private void handleLogout() {
        if (AlertUtil.showLogoutConfirmation()) {
            authService.logout();
            try {
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/mycompany/sewabaju/fxml/login.fxml")
                );
                Parent root = loader.load();
                
                Stage stage = (Stage) btnLogout.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Login - SewaBaju");
                
            } catch (IOException e) {
                AlertUtil.showError("Gagal logout: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void navigateToPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            
        } catch (IOException e) {
            AlertUtil.showError("Gagal membuka halaman: " + e.getMessage());
            e.printStackTrace();
        }
    }
}