package com.mycompany.sewabaju.controllers.pelanggan;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.models.DetailPenyewaan;
import com.mycompany.sewabaju.models.Pembayaran;
import com.mycompany.sewabaju.models.Penyewaan;
import com.mycompany.sewabaju.models.enums.StatusPenyewaan;
import com.mycompany.sewabaju.services.PembayaranService;
import com.mycompany.sewabaju.services.PenyewaanService;
import com.mycompany.sewabaju.utils.AlertUtil;
import com.mycompany.sewabaju.utils.DateUtil;
import com.mycompany.sewabaju.utils.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class RiwayatSewaController {
    @FXML private TableView<Penyewaan> tablePenyewaan;
    @FXML private TableColumn<Penyewaan, Integer> colId;
    @FXML private TableColumn<Penyewaan, String> colTglSewa;
    @FXML private TableColumn<Penyewaan, String> colTglKembali;
    @FXML private TableColumn<Penyewaan, Integer> colJumlahItem;
    @FXML private TableColumn<Penyewaan, Double> colTotal;
    @FXML private TableColumn<Penyewaan, String> colStatus;
    
    @FXML private ComboBox<StatusPenyewaan> filterStatus;
    
    @FXML private Label lblDetailId;
    @FXML private Label lblDetailTglSewa;
    @FXML private Label lblDetailTglKembali;
    @FXML private Label lblDetailLamaSewa;
    @FXML private Label lblDetailTotal;
    @FXML private Label lblDetailStatus;
    @FXML private Label lblDetailStatusPembayaran;
    @FXML private TextArea txtDetailItems;
    
    @FXML private Button btnViewDetail;
    @FXML private Button btnRefresh;
    @FXML private Button btnBack;
    
    private PenyewaanService penyewaanService;
    private PembayaranService pembayaranService;
    private Penyewaan selectedPenyewaan;
    
    @FXML
    public void initialize() {
        penyewaanService = PenyewaanService.getInstance();
        pembayaranService = PembayaranService.getInstance();
        
        setupTable();
        
        filterStatus.setItems(FXCollections.observableArrayList(StatusPenyewaan.values()));
        filterStatus.setPromptText("Semua Status");

        loadRiwayatSewa();
        
        tablePenyewaan.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                selectedPenyewaan = newSelection;
                loadDetailPenyewaan();
                updateButtonStates();
            }
        );
        
        updateButtonStates();
        
        System.out.println("RiwayatSewaController initialized");
    }
    
    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("sewaId"));
        
        colTglSewa.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                DateUtil.formatDate(cellData.getValue().getTglSewa())
            )
        );
        
        colTglKembali.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                DateUtil.formatDate(cellData.getValue().getTglKembali())
            )
        );
        
        colJumlahItem.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(
                cellData.getValue().getJumlahItem()
            ).asObject()
        );
        
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalHarga"));
        
        colStatus.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus().getDisplayName()
            )
        );
        
        colTotal.setCellFactory(col -> new TableCell<Penyewaan, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("Rp %.0f", price));
                }
            }
        });
        
        colStatus.setCellFactory(col -> new TableCell<Penyewaan, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    
                    if (status.contains("Menunggu")) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else if (status.contains("Dikonfirmasi") || status.contains("Sedang")) {
                        setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                    } else if (status.contains("Dikembalikan")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (status.contains("Dibatalkan")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }
    
    private void loadRiwayatSewa() {
        try {
            int userId = Session.getInstance().getCurrentUserId();
            List<Penyewaan> penyewaanList = penyewaanService.getPenyewaanByUserId(userId);
            
            StatusPenyewaan filterStatusValue = filterStatus.getValue();
            if (filterStatusValue != null) {
                penyewaanList = penyewaanList.stream()
                    .filter(p -> p.getStatus() == filterStatusValue)
                    .toList();
            }
            
            tablePenyewaan.setItems(FXCollections.observableArrayList(penyewaanList));
            
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("memuat riwayat sewa");
            e.printStackTrace();
        }
    }
    
    private void loadDetailPenyewaan() {
        if (selectedPenyewaan == null) {
            clearDetail();
            return;
        }
        
        try {
            lblDetailId.setText("#" + selectedPenyewaan.getSewaId());
            lblDetailTglSewa.setText(DateUtil.formatDate(selectedPenyewaan.getTglSewa()));
            lblDetailTglKembali.setText(DateUtil.formatDate(selectedPenyewaan.getTglKembali()));
            lblDetailLamaSewa.setText(selectedPenyewaan.hitungLamaHari() + " hari");
            lblDetailTotal.setText(String.format("Rp %.0f", selectedPenyewaan.getTotalHarga()));
            lblDetailStatus.setText(selectedPenyewaan.getStatus().getDisplayName());
            
            String status = selectedPenyewaan.getStatus().getDisplayName();
            if (status.contains("Menunggu")) {
                lblDetailStatus.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            } else if (status.contains("Dikonfirmasi") || status.contains("Sedang")) {
                lblDetailStatus.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
            } else if (status.contains("Dikembalikan")) {
                lblDetailStatus.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else if (status.contains("Dibatalkan")) {
                lblDetailStatus.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
            
            Pembayaran pembayaran = pembayaranService.getPembayaranBySewaId(selectedPenyewaan.getSewaId());
            if (pembayaran != null) {
                lblDetailStatusPembayaran.setText(pembayaran.getStatusDisplay());
                
                String statusPembayaran = pembayaran.getStatus().getDisplayName();
                if (statusPembayaran.contains("Menunggu")) {
                    lblDetailStatusPembayaran.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                } else if (statusPembayaran.contains("Berhasil")) {
                    lblDetailStatusPembayaran.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                } else if (statusPembayaran.contains("Ditolak")) {
                    lblDetailStatusPembayaran.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                }
            } else {
                lblDetailStatusPembayaran.setText("Belum ada pembayaran");
                lblDetailStatusPembayaran.setStyle("-fx-text-fill: gray;");
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Items yang disewa:\n\n");
            
            for (DetailPenyewaan detail : selectedPenyewaan.getDetailPenyewaanList()) {
                sb.append("• ").append(detail.getItemDisplay()).append("\n");
                sb.append("  Harga: Rp ").append(String.format("%.0f", detail.getHargaPerItem())).append("\n");
                sb.append("  Subtotal: Rp ").append(String.format("%.0f", detail.getSubtotal())).append("\n\n");
            }
            
            sb.append("Total: Rp ").append(String.format("%.0f", selectedPenyewaan.getTotalHarga()));
            
            txtDetailItems.setText(sb.toString());
            
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("memuat detail penyewaan");
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleViewDetail() {
        if (selectedPenyewaan == null) {
            AlertUtil.showWarning("Pilih penyewaan untuk melihat detail");
            return;
        }
        // TODO: Create detail dialog with better layout
        AlertUtil.showInfo("Detail Penyewaan", txtDetailItems.getText());
    }
    
    @FXML
    private void handleFilterChange() {
        loadRiwayatSewa();
    }
    
    @FXML
    private void handleRefresh() {
        loadRiwayatSewa();
        AlertUtil.showSuccess("Data berhasil direfresh");
    }
    
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/mycompany/sewabaju/fxml/pelanggan/pelanggan_dashboard.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = (Stage) btnBack.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard - SewaBaju");
            
        } catch (IOException e) {
            AlertUtil.showError("Gagal kembali ke dashboard");
            e.printStackTrace();
        }
    }
    
    private void clearDetail() {
        lblDetailId.setText("-");
        lblDetailTglSewa.setText("-");
        lblDetailTglKembali.setText("-");
        lblDetailLamaSewa.setText("-");
        lblDetailTotal.setText("-");
        lblDetailStatus.setText("-");
        lblDetailStatusPembayaran.setText("-");
        txtDetailItems.clear();
    }
    
    private void updateButtonStates() {
        boolean hasSelection = selectedPenyewaan != null;
        btnViewDetail.setDisable(!hasSelection);
    }
}