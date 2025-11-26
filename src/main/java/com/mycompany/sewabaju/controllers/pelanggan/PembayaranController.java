package com.mycompany.sewabaju.controllers.pelanggan;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.exceptions.PembayaranException;
import com.mycompany.sewabaju.exceptions.ValidationException;
import com.mycompany.sewabaju.models.Pembayaran;
import com.mycompany.sewabaju.models.enums.MetodePembayaran;
import com.mycompany.sewabaju.services.PembayaranService;
import com.mycompany.sewabaju.utils.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class PembayaranController {
    
    @FXML private Label lblSewaId;
    @FXML private Label lblMetode;
    @FXML private Label lblJumlah;
    @FXML private Label lblInstruksi;
    @FXML private ImageView imgPreview;
    @FXML private Label lblFileName;
    @FXML private Button btnUploadBukti;
    @FXML private Button btnSubmit;
    @FXML private Button btnCancel;
    
    private PembayaranService pembayaranService;
    private int sewaId;
    private MetodePembayaran metode;
    private double jumlah;
    private File selectedFile;
   
    @FXML
    public void initialize() {
        pembayaranService = PembayaranService.getInstance();
        btnSubmit.setDisable(true);
        
        System.out.println("PembayaranController initialized");
    }
    
    public void setData(int sewaId, MetodePembayaran metode, double jumlah) {
        this.sewaId = sewaId;
        this.metode = metode;
        this.jumlah = jumlah;
        
        lblSewaId.setText("#" + sewaId);
        lblMetode.setText(metode.getDisplayName());
        lblJumlah.setText(String.format("Rp %.0f", jumlah));
        lblInstruksi.setText(metode.getInstructions());
    }
    
    @FXML
    private void handleUploadBukti() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Bukti Pembayaran");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File file = fileChooser.showOpenDialog(btnUploadBukti.getScene().getWindow());
        
        if (file != null) {
            if (file.length() > 5 * 1024 * 1024) {
                AlertUtil.showError("Ukuran file maksimal 5MB");
                return;
            }
            
            selectedFile = file;
            lblFileName.setText(file.getName());
            
            try {
                Image image = new Image(file.toURI().toString());
                imgPreview.setImage(image);
                btnSubmit.setDisable(false);
                
            } catch (Exception e) {
                AlertUtil.showError("Gagal load preview image");
            }
        }
    }
    
    @FXML
    private void handleSubmit() {
        if (selectedFile == null) {
            AlertUtil.showWarning("Upload bukti pembayaran terlebih dahulu");
            return;
        }
        
        if (!AlertUtil.showConfirmation(
            "Konfirmasi",
            "Upload bukti pembayaran untuk penyewaan #" + sewaId + "?\n\n" +
            "Bukti akan diverifikasi oleh admin."
        )) {
            return;
        }
        
        btnSubmit.setDisable(true);
        
        try {
            Pembayaran pembayaran = pembayaranService.createPembayaran(
                sewaId,
                metode,
                jumlah,
                selectedFile
            );
            
            if (pembayaran != null) {
                AlertUtil.showSuccess(
                    "Bukti pembayaran berhasil diupload!\n\n" +
                    "Pembayaran: #" + pembayaran.getPembayaranId() + "\n" +
                    "Status: " + pembayaran.getStatusDisplay() + "\n\n" +
                    "Silakan tunggu verifikasi dari admin."
                );
                
                CatalogBajuController.clearCart();
                navigateToRiwayatSewa();
                
            } else {
                AlertUtil.showError("Gagal membuat pembayaran");
                btnSubmit.setDisable(false);
            }
            
        } catch (ValidationException e) {
            AlertUtil.showValidationError("Validasi", e.getMessage());
            btnSubmit.setDisable(false);
            
        } catch (PembayaranException e) {
            AlertUtil.showError("Pembayaran Error: " + e.getMessage());
            btnSubmit.setDisable(false);
            
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("menyimpan pembayaran");
            btnSubmit.setDisable(false);
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleCancel() {
        if (AlertUtil.showConfirmation(
            "Batalkan pembayaran?",
            "Anda akan kembali ke halaman keranjang.\n" +
            "Penyewaan akan tetap tersimpan dengan status Menunggu Pembayaran."
        )) {
            navigateToKeranjang();
        }
    }
    
    private void navigateToRiwayatSewa() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/mycompany/sewabaju/fxml/pelanggan/riwayat_sewa.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = (Stage) btnSubmit.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Riwayat Sewa - SewaBaju");
            
        } catch (IOException e) {
            AlertUtil.showError("Gagal membuka halaman riwayat");
            e.printStackTrace();
        }
    }
    
    private void navigateToKeranjang() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/mycompany/sewabaju/fxml/pelanggan/keranjang.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Keranjang - SewaBaju");
            
        } catch (IOException e) {
            AlertUtil.showError("Gagal kembali ke keranjang");
            e.printStackTrace();
        }
    }
}