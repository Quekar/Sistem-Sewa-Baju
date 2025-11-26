package com.mycompany.sewabaju.controllers.pelanggan;

import com.mycompany.sewabaju.models.Baju;
import com.mycompany.sewabaju.models.Pelanggan;
import com.mycompany.sewabaju.models.Penyewaan;
import com.mycompany.sewabaju.models.enums.StatusPenyewaan;
import com.mycompany.sewabaju.services.AuthService;
import com.mycompany.sewabaju.services.BajuService;
import com.mycompany.sewabaju.services.PenyewaanService;
import com.mycompany.sewabaju.utils.AlertUtil;
import com.mycompany.sewabaju.utils.DateUtil;
import com.mycompany.sewabaju.utils.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class PelangganDashboardController {
    
    @FXML private Label lblWelcome;
    @FXML private Label lblPoinLoyalitas;
    @FXML private Button btnLogout;
    
    @FXML private Label lblTotalPenyewaan;
    @FXML private Label lblPenyewaanAktif;
    
    @FXML private GridPane gridBajuPopuler;
    
    @FXML private ListView<String> listPenyewaanAktif;
    
    @FXML private Button btnCatalog;
    @FXML private Button btnKeranjang;
    @FXML private Button btnRiwayatSewa;
    @FXML private Button btnProfile;
    
    private AuthService authService;
    private BajuService bajuService;
    private PenyewaanService penyewaanService;
    
    @FXML
    public void initialize() {
        authService = AuthService.getInstance();
        bajuService = BajuService.getInstance();
        penyewaanService = PenyewaanService.getInstance();
        
        loadPelangganInfo();
        loadStatistics();
        loadBajuPopuler();
        loadPenyewaanAktif();
        
        System.out.println("PelangganDashboardController initialized");
    }
    
    private void loadPelangganInfo() {
        try {
            Pelanggan pelanggan = Session.getInstance().getCurrentPelanggan();
            
            if (pelanggan != null) {
                lblWelcome.setText("Selamat datang, " + pelanggan.getNama());
                lblPoinLoyalitas.setText(pelanggan.getPoinLoyalitas() + " Poin");
            }
        } catch (Exception e) {
            System.err.println("Error loading pelanggan info: " + e.getMessage());
        }
    }
    
    private void loadStatistics() {
        try {
            int userId = Session.getInstance().getCurrentUserId();
            List<Penyewaan> allPenyewaan = penyewaanService.getPenyewaanByUserId(userId);
            lblTotalPenyewaan.setText(String.valueOf(allPenyewaan.size()));
            
            long aktifCount = allPenyewaan.stream()
                .filter(p -> p.getStatus() == StatusPenyewaan.SEDANG_DISEWA ||
                             p.getStatus() == StatusPenyewaan.DIKONFIRMASI)
                .count();
            lblPenyewaanAktif.setText(String.valueOf(aktifCount));
            
        } catch (Exception e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadBajuPopuler() {
        try {
            List<Baju> popularBaju = bajuService.getMostPopularBaju(4);
            
            gridBajuPopuler.getChildren().clear();
            
            int row = 0;
            int col = 0;
            
            for (Baju baju : popularBaju) {
                VBox bajuCard = createBajuCard(baju);
                gridBajuPopuler.add(bajuCard, col, row);
                
                col++;
                if (col > 1) {
                    col = 0;
                    row++;
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error loading baju populer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private VBox createBajuCard(Baju baju) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-width: 1px; " +
            "-fx-background-radius: 8px; " +
            "-fx-border-radius: 8px;"
        );
        
        ImageView imageView = new ImageView();
        imageView.setFitHeight(120);
        imageView.setFitWidth(120);
        imageView.setPreserveRatio(true);
        
        try {
            if (baju.getFoto() != null) {
                // TODO: Load actual image from file
                // For now, use placeholder
            }
        } catch (Exception e) {
            // Use placeholder
        }
        
        Label lblNama = new Label(baju.getNamaBaju());
        lblNama.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label lblKategori = new Label(baju.getNamaKategori());
        lblKategori.setStyle("-fx-text-fill: #666;");
        
        Label lblHarga = new Label(baju.getRangeHarga());
        lblHarga.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
        
        card.getChildren().addAll(imageView, lblNama, lblKategori, lblHarga);
        
        card.setOnMouseClicked(event -> {
            handleViewBajuDetail(baju.getBajuId());
        });
        
        card.setOnMouseEntered(event -> {
            card.setStyle(
                "-fx-background-color: #f5f5f5; " +
                "-fx-border-color: #2196F3; " +
                "-fx-border-width: 2px; " +
                "-fx-background-radius: 8px; " +
                "-fx-border-radius: 8px; " +
                "-fx-cursor: hand;"
            );
        });
        
        card.setOnMouseExited(event -> {
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-width: 1px; " +
                "-fx-background-radius: 8px; " +
                "-fx-border-radius: 8px;"
            );
        });
        
        return card;
    }
    
    private void loadPenyewaanAktif() {
        try {
            int userId = Session.getInstance().getCurrentUserId();
            List<Penyewaan> allPenyewaan = penyewaanService.getPenyewaanByUserId(userId);
            
            listPenyewaanAktif.getItems().clear();
            
            boolean hasActive = false;
            
            for (Penyewaan p : allPenyewaan) {
                if (p.getStatus() == StatusPenyewaan.SEDANG_DISEWA ||
                    p.getStatus() == StatusPenyewaan.DIKONFIRMASI ||
                    p.getStatus() == StatusPenyewaan.MENUNGGU_PEMBAYARAN) {
                    
                    String item = String.format("#%d - %s - %s",
                        p.getSewaId(),
                        p.getStatus().getDisplayName(),
                        DateUtil.formatDate(p.getTglSewa())
                    );
                    listPenyewaanAktif.getItems().add(item);
                    hasActive = true;
                }
            }
            
            if (!hasActive) {
                listPenyewaanAktif.getItems().add("Tidak ada penyewaan aktif");
            }
            
        } catch (Exception e) {
            System.err.println("Error loading penyewaan aktif: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCatalog() {
        navigateToPage(
            "/com/mycompany/sewabaju/fxml/pelanggan/catalog_baju.fxml",
            "Catalog Baju - SewaBaju"
        );
    }
    
    @FXML
    private void handleKeranjang() {
        navigateToPage(
            "/com/mycompany/sewabaju/fxml/pelanggan/keranjang.fxml",
            "Keranjang - SewaBaju"
        );
    }
    
    @FXML
    private void handleRiwayatSewa() {
        navigateToPage(
            "/com/mycompany/sewabaju/fxml/pelanggan/riwayat_sewa.fxml",
            "Riwayat Sewa - SewaBaju"
        );
    }
    
    @FXML
    private void handleProfile() {
        navigateToPage(
            "/com/mycompany/sewabaju/fxml/pelanggan/profile.fxml",
            "Profile - SewaBaju"
        );
    }
    
    private void handleViewBajuDetail(int bajuId) {
        // TODO: Open detail dialog or navigate to detail page
        AlertUtil.showInfo("Detail Baju #" + bajuId);
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