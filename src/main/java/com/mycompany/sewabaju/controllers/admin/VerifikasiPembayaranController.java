package com.mycompany.sewabaju.controllers.admin;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.exceptions.ValidationException;
import com.mycompany.sewabaju.models.Pembayaran;
import com.mycompany.sewabaju.models.Penyewaan;
import com.mycompany.sewabaju.models.enums.StatusPembayaran;
import com.mycompany.sewabaju.services.PembayaranService;
import com.mycompany.sewabaju.services.PenyewaanService;
import com.mycompany.sewabaju.utils.AlertUtil;
import com.mycompany.sewabaju.utils.DateUtil;
import com.mycompany.sewabaju.utils.FileUtil;
import com.mycompany.sewabaju.utils.Session;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class VerifikasiPembayaranController {
    @FXML private TableView<Pembayaran> tablePembayaran;
    @FXML private TableColumn<Pembayaran, Integer> colId;
    @FXML private TableColumn<Pembayaran, Integer> colSewaId;
    @FXML private TableColumn<Pembayaran, String> colPelanggan;
    @FXML private TableColumn<Pembayaran, String> colMetode;
    @FXML private TableColumn<Pembayaran, Double> colJumlah;
    @FXML private TableColumn<Pembayaran, String> colTanggal;
    @FXML private TableColumn<Pembayaran, String> colStatus;
    
    @FXML private Label lblDetailId;
    @FXML private Label lblDetailSewaId;
    @FXML private Label lblDetailPelanggan;
    @FXML private Label lblDetailMetode;
    @FXML private Label lblDetailJumlah;
    @FXML private Label lblDetailTanggal;
    @FXML private Label lblDetailStatus;
    @FXML private ImageView imgBukti;
    @FXML private Button btnZoomBukti;
    @FXML private TextArea detailPenyewaanArea;

    @FXML private Button btnApprove;
    @FXML private Button btnReject;
    @FXML private Button btnRefresh;
    @FXML private Button btnBack;

    @FXML private ComboBox<StatusPembayaran> filterStatus;
    
    private PembayaranService pembayaranService;
    private PenyewaanService penyewaanService;
    private Pembayaran selectedPembayaran;

    @FXML
    public void initialize() {
        pembayaranService = PembayaranService.getInstance();
        penyewaanService = PenyewaanService.getInstance();
        setupTable();

        filterStatus.setItems(FXCollections.observableArrayList(StatusPembayaran.values()));
        filterStatus.setValue(StatusPembayaran.MENUNGGU_VERIFIKASI);

        loadPembayaran();

        tablePembayaran.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                selectedPembayaran = newSelection;
                loadDetailPembayaran();
                updateButtonStates();
            }
        );

        updateButtonStates();
        
        System.out.println("VerifikasiPembayaranController initialized");
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("pembayaranId"));
        colSewaId.setCellValueFactory(new PropertyValueFactory<>("sewaId"));
        colPelanggan.setCellValueFactory(cellData -> {
            try {
                Penyewaan penyewaan = penyewaanService.getPenyewaanById(
                    cellData.getValue().getSewaId()
                );
                return new javafx.beans.property.SimpleStringProperty(
                    penyewaan != null ? penyewaan.getNamaPelanggan() : "-"
                );
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("-");
            }
        });
        
        colMetode.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getMetodePembayaranDisplay()
            )
        );
        
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        
        colTanggal.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                DateUtil.formatDateTime(cellData.getValue().getTanggalBayar())
            )
        );
        
        colStatus.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatusDisplay()
            )
        );
        
        colJumlah.setCellFactory(col -> new TableCell<Pembayaran, Double>() {
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

        colStatus.setCellFactory(col -> new TableCell<Pembayaran, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    if (status.contains("Pending") || status.contains("Menunggu")) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else if (status.contains("Berhasil")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (status.contains("Ditolak")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void loadPembayaran() {
        try {
            StatusPembayaran status = filterStatus.getValue();
            
            List<Pembayaran> pembayaranList;
            
            if (status != null) {
                pembayaranList = pembayaranService.getPembayaranByStatus(status);
            } else {

                pembayaranList = pembayaranService.getPembayaranPending();
            }
            
            tablePembayaran.setItems(FXCollections.observableArrayList(pembayaranList));

            int count = pembayaranList.size();
            // TODO: Update count label if exists            
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("memuat pembayaran");
            e.printStackTrace();
        }
    }

    private void loadDetailPembayaran() {
        if (selectedPembayaran == null) {
            clearDetail();
            return;
        }
        
        try {
            lblDetailId.setText("#" + selectedPembayaran.getPembayaranId());
            lblDetailSewaId.setText("#" + selectedPembayaran.getSewaId());
            lblDetailMetode.setText(selectedPembayaran.getMetodePembayaranDisplay());
            lblDetailJumlah.setText(selectedPembayaran.getJumlahFormatted());
            lblDetailTanggal.setText(DateUtil.formatDateTime(selectedPembayaran.getTanggalBayar()));
            lblDetailStatus.setText(selectedPembayaran.getStatusDisplay());
            Penyewaan penyewaan = penyewaanService.getPenyewaanById(selectedPembayaran.getSewaId());
            
            if (penyewaan != null) {
                lblDetailPelanggan.setText(penyewaan.getNamaPelanggan());
                StringBuilder sb = new StringBuilder();
                sb.append("Detail Penyewaan:\n\n");
                sb.append("Tanggal Sewa: ").append(DateUtil.formatDate(penyewaan.getTglSewa())).append("\n");
                sb.append("Tanggal Kembali: ").append(DateUtil.formatDate(penyewaan.getTglKembali())).append("\n");
                sb.append("Lama Sewa: ").append(penyewaan.hitungLamaHari()).append(" hari\n");
                sb.append("Jumlah Item: ").append(penyewaan.getJumlahItem()).append("\n");
                sb.append("Total Harga: Rp ").append(String.format("%.0f", penyewaan.getTotalHarga())).append("\n\n");
                
                sb.append("Items:\n");
                for (var detail : penyewaan.getDetailPenyewaanList()) {
                    sb.append("- ").append(detail.getItemDisplay()).append("\n");
                }
                
                detailPenyewaanArea.setText(sb.toString());
            }
            if (selectedPembayaran.getBuktiPembayaran() != null) {
                loadBuktiImage(selectedPembayaran.getBuktiPembayaran());
                btnZoomBukti.setDisable(false);
            } else {
                imgBukti.setImage(null);
                btnZoomBukti.setDisable(true);
            }
            
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("memuat detail pembayaran");
            e.printStackTrace();
        }
    }

    private void loadBuktiImage(String filename) {
        try {
            String path = FileUtil.getBuktiPembayaranPath(filename);
            
            if (FileUtil.fileExists(path)) {
                byte[] imageData = FileUtil.readFileAsBytes(path);
                Image image = new Image(new ByteArrayInputStream(imageData));
                imgBukti.setImage(image);
            } else {
                imgBukti.setImage(null);
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("File Tidak Ditemukan");
                alert.setContentText("File bukti pembayaran tidak ditemukan");
                alert.showAndWait();
            }
        } catch (Exception e) {
            AlertUtil.showError("Gagal memuat bukti pembayaran: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleZoomBukti() {
        if (selectedPembayaran == null || selectedPembayaran.getBuktiPembayaran() == null) {
            return;
        }
        
        try {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Bukti Pembayaran - Full Size");
            
            ImageView imageView = new ImageView();
            String path = FileUtil.getBuktiPembayaranPath(selectedPembayaran.getBuktiPembayaran());
            byte[] imageData = FileUtil.readFileAsBytes(path);
            Image image = new Image(new ByteArrayInputStream(imageData));
            imageView.setImage(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(800);
            
            ScrollPane scrollPane = new ScrollPane(imageView);
            scrollPane.setPrefSize(850, 600);
            
            VBox vbox = new VBox(scrollPane);
            vbox.setPadding(new Insets(10));
            
            Scene scene = new Scene(vbox);
            dialog.setScene(scene);
            dialog.show();
            
        } catch (Exception e) {
            AlertUtil.showError("Gagal membuka image: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleApprove() {
        if (selectedPembayaran == null) {
            AlertUtil.showWarning("Pilih pembayaran yang akan diapprove");
            return;
        }
        
        if (selectedPembayaran.getStatus() != StatusPembayaran.MENUNGGU_VERIFIKASI) {
            AlertUtil.showWarning("Hanya bisa approve pembayaran yang pending");
            return;
        }

        if (AlertUtil.showConfirmation(
            "Konfirmasi Approve",
            "Apakah Anda yakin ingin menyetujui pembayaran ini?\n" +
            "Pembayaran: #" + selectedPembayaran.getPembayaranId() + "\n" +
            "Jumlah: " + selectedPembayaran.getJumlahFormatted()
        )) {
            try {
                int adminId = Session.getInstance().getCurrentAdmin().getAdminId();
                
                boolean approved = pembayaranService.approvePembayaran(
                    selectedPembayaran.getPembayaranId(), 
                    adminId
                );
                
                if (approved) {
                    AlertUtil.showSuccess("Pembayaran berhasil diapprove!\nStatus penyewaan telah diupdate.");
                    loadPembayaran();
                    clearDetail();
                } else {
                    AlertUtil.showError("Gagal approve pembayaran");
                }
                
            } catch (ValidationException e) {
                AlertUtil.showValidationError("Validasi", e.getMessage());
            } catch (DatabaseException e) {
                AlertUtil.showDatabaseError("approve pembayaran");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleReject() {
        if (selectedPembayaran == null) {
            AlertUtil.showWarning("Pilih pembayaran yang akan direject");
            return;
        }
        
        if (selectedPembayaran.getStatus() != StatusPembayaran.MENUNGGU_VERIFIKASI) {
            AlertUtil.showWarning("Hanya bisa reject pembayaran yang pending");
            return;
        }

        if (AlertUtil.showConfirmation(
            "Konfirmasi Reject",
            "Apakah Anda yakin ingin menolak pembayaran ini?\n" +
            "Pembayaran: #" + selectedPembayaran.getPembayaranId() + "\n" +
            "Pelanggan harus upload ulang bukti pembayaran."
        )) {
            try {
                int adminId = Session.getInstance().getCurrentAdmin().getAdminId();
                
                boolean rejected = pembayaranService.rejectPembayaran(
                    selectedPembayaran.getPembayaranId(), 
                    adminId
                );
                
                if (rejected) {
                    AlertUtil.showSuccess("Pembayaran ditolak.\nPelanggan dapat upload ulang bukti pembayaran.");
                    loadPembayaran();
                    clearDetail();
                } else {
                    AlertUtil.showError("Gagal reject pembayaran");
                }
                
            } catch (ValidationException e) {
                AlertUtil.showValidationError("Validasi", e.getMessage());
            } catch (DatabaseException e) {
                AlertUtil.showDatabaseError("reject pembayaran");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleFilterChange() {
        loadPembayaran();
    }

    @FXML
    private void handleRefresh() {
        loadPembayaran();
        AlertUtil.showSuccess("Data berhasil direfresh");
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/mycompany/sewabaju/fxml/admin/admin_dashboard.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = (Stage) btnBack.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Admin Dashboard - SewaBaju");
            
        } catch (IOException e) {
            AlertUtil.showError("Gagal kembali ke dashboard");
            e.printStackTrace();
        }
    }

    private void clearDetail() {
        lblDetailId.setText("-");
        lblDetailSewaId.setText("-");
        lblDetailPelanggan.setText("-");
        lblDetailMetode.setText("-");
        lblDetailJumlah.setText("-");
        lblDetailTanggal.setText("-");
        lblDetailStatus.setText("-");
        detailPenyewaanArea.clear();
        imgBukti.setImage(null);
        btnZoomBukti.setDisable(true);
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedPembayaran != null;
        boolean isPending = hasSelection && 
                           selectedPembayaran.getStatus() == StatusPembayaran.MENUNGGU_VERIFIKASI;
        
        btnApprove.setDisable(!isPending);
        btnReject.setDisable(!isPending);
    }
}