package com.mycompany.sewabaju.controllers.pelanggan;

import com.mycompany.sewabaju.exceptions.AuthenticationException;
import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.exceptions.ValidationException;
import com.mycompany.sewabaju.models.Pelanggan;
import com.mycompany.sewabaju.services.AuthService;
import com.mycompany.sewabaju.utils.AlertUtil;
import com.mycompany.sewabaju.utils.PasswordUtil;
import com.mycompany.sewabaju.utils.Session;
import com.mycompany.sewabaju.utils.ValidationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class ProfileController {
    
    @FXML private Label lblEmail;
    @FXML private TextField namaField;
    @FXML private TextField noHpField;
    @FXML private TextArea alamatArea;
    @FXML private Label lblPoinLoyalitas;
    @FXML private Label lblMemberSince;
    
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label lblPasswordStrength;
    
    @FXML private Button btnUpdateProfile;
    @FXML private Button btnChangePassword;
    @FXML private Button btnBack;
    
    @FXML private TabPane tabPane;
    @FXML private Tab tabProfile;
    @FXML private Tab tabPassword;
    
    private AuthService authService;
    private Pelanggan currentPelanggan;
    
    @FXML
    public void initialize() {
        authService = AuthService.getInstance();
        currentPelanggan = Session.getInstance().getCurrentPelanggan();
        
        if (currentPelanggan == null) {
            AlertUtil.showError("Session expired. Please login again.");
            handleBack();
            return;
        }
        
        loadProfileData();
        setupPasswordStrengthListener();
        
        System.out.println("ProfileController initialized");
    }
    
    private void loadProfileData() {
        lblEmail.setText(currentPelanggan.getEmail());
        namaField.setText(currentPelanggan.getNama());
        noHpField.setText(currentPelanggan.getNoHp() != null ? currentPelanggan.getNoHp() : "");
        alamatArea.setText(currentPelanggan.getAlamat() != null ? currentPelanggan.getAlamat() : "");
        lblPoinLoyalitas.setText(currentPelanggan.getPoinLoyalitas() + " Poin");
        
        if (currentPelanggan.getCreatedAt() != null) {
            lblMemberSince.setText("Member sejak: " + 
                com.mycompany.sewabaju.utils.DateUtil.formatDate(
                    currentPelanggan.getCreatedAt().toLocalDate()
                )
            );
        }
    }
    
    private void setupPasswordStrengthListener() {
        if (newPasswordField != null && lblPasswordStrength != null) {
            newPasswordField.textProperty().addListener((obs, oldVal, newVal) -> {
                updatePasswordStrength(newVal);
            });
        }
    }
    
    private void updatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            lblPasswordStrength.setText("");
            lblPasswordStrength.setStyle("");
            return;
        }
        
        if (PasswordUtil.isStrongPassword(password)) {
            lblPasswordStrength.setText("✓ Password kuat");
            lblPasswordStrength.setStyle("-fx-text-fill: green;");
        } else if (password.length() >= 8) {
            lblPasswordStrength.setText("⚠ Password lemah (perlu huruf besar, kecil, dan angka)");
            lblPasswordStrength.setStyle("-fx-text-fill: orange;");
        } else {
            lblPasswordStrength.setText("✗ Password terlalu pendek (min 8 karakter)");
            lblPasswordStrength.setStyle("-fx-text-fill: red;");
        }
    }
    
    @FXML
    private void handleUpdateProfile() {
        String nama = namaField.getText().trim();
        String noHp = noHpField.getText().trim();
        String alamat = alamatArea.getText().trim();
        
        // Validasi
        try {
            validateProfileInput(nama, noHp);
        } catch (ValidationException e) {
            AlertUtil.showValidationError("Validasi", e.getMessage());
            highlightErrorField(e.getFieldName());
            return;
        }
        
        if (!AlertUtil.showConfirmation(
            "Konfirmasi Update",
            "Apakah Anda yakin ingin mengupdate profil?"
        )) {
            return;
        }
        
        btnUpdateProfile.setDisable(true);
        
        try {
            authService.updateProfile(
                currentPelanggan.getUserId(), 
                nama, 
                noHp, 
                alamat
            );
            
            // Reload current user
            currentPelanggan = Session.getInstance().getCurrentPelanggan();
            loadProfileData();
            
            AlertUtil.showSuccess("Profil berhasil diupdate!");
            clearValidationStyles();
            
        } catch (ValidationException e) {
            AlertUtil.showValidationError("Validasi", e.getMessage());
            highlightErrorField(e.getFieldName());
            
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("update profil");
            e.printStackTrace();
            
        } catch (Exception e) {
            AlertUtil.showError("Gagal update profil: " + e.getMessage());
            e.printStackTrace();
            
        } finally {
            btnUpdateProfile.setDisable(false);
        }
    }
    
    @FXML
    private void handleChangePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validasi
        try {
            validatePasswordInput(oldPassword, newPassword, confirmPassword);
        } catch (ValidationException e) {
            AlertUtil.showValidationError("Validasi", e.getMessage());
            return;
        }
        
        if (!AlertUtil.showConfirmation(
            "Konfirmasi Ganti Password",
            "Apakah Anda yakin ingin mengganti password?"
        )) {
            return;
        }
        
        btnChangePassword.setDisable(true);
        
        try {
            authService.changePassword(
                currentPelanggan.getUserId(), 
                oldPassword, 
                newPassword
            );
            
            AlertUtil.showSuccess(
                "Password berhasil diubah!\n\n" +
                "Gunakan password baru untuk login berikutnya."
            );
            
            clearPasswordFields();
            
        } catch (ValidationException e) {
            AlertUtil.showValidationError("Validasi", e.getMessage());
            
        } catch (AuthenticationException e) {
            AlertUtil.showError(e.getMessage());
            oldPasswordField.clear();
            oldPasswordField.requestFocus();
            
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("ganti password");
            e.printStackTrace();
            
        } catch (Exception e) {
            AlertUtil.showError("Gagal ganti password: " + e.getMessage());
            e.printStackTrace();
            
        } finally {
            btnChangePassword.setDisable(false);
        }
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
    
    @FXML
    private void handleResetProfile() {
        if (AlertUtil.showConfirmation(
            "Reset Form",
            "Reset perubahan dan kembali ke data awal?"
        )) {
            loadProfileData();
            clearValidationStyles();
        }
    }
    
    @FXML
    private void handleResetPassword() {
        if (AlertUtil.showConfirmation(
            "Reset Form",
            "Clear semua field password?"
        )) {
            clearPasswordFields();
        }
    }
    
    private void validateProfileInput(String nama, String noHp) throws ValidationException {
        if (ValidationUtil.isEmpty(nama)) {
            throw new ValidationException("Nama tidak boleh kosong", "nama");
        }
        
        if (!ValidationUtil.hasMinLength(nama, 3)) {
            throw new ValidationException("Nama minimal 3 karakter", "nama");
        }
        
        if (!ValidationUtil.isEmpty(noHp) && !ValidationUtil.isValidPhone(noHp)) {
            throw new ValidationException("Format nomor HP tidak valid", "noHp");
        }
    }
    
    private void validatePasswordInput(String oldPassword, String newPassword, String confirmPassword) 
            throws ValidationException {
        
        if (ValidationUtil.isEmpty(oldPassword)) {
            throw new ValidationException("Password lama tidak boleh kosong");
        }
        
        if (ValidationUtil.isEmpty(newPassword)) {
            throw new ValidationException("Password baru tidak boleh kosong");
        }
        
        if (!PasswordUtil.isStrongPassword(newPassword)) {
            throw new ValidationException(
                PasswordUtil.getPasswordStrengthMessage(newPassword)
            );
        }
        
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("Konfirmasi password tidak cocok");
        }
        
        if (oldPassword.equals(newPassword)) {
            throw new ValidationException("Password baru tidak boleh sama dengan password lama");
        }
    }
    
    private void highlightErrorField(String fieldName) {
        if (fieldName == null) return;
        
        String errorStyle = "-fx-border-color: red; -fx-border-width: 2px;";
        
        switch (fieldName) {
            case "nama":
                namaField.setStyle(errorStyle);
                namaField.requestFocus();
                break;
            case "noHp":
                noHpField.setStyle(errorStyle);
                noHpField.requestFocus();
                break;
        }
    }
    
    private void clearValidationStyles() {
        String normalStyle = "";
        namaField.setStyle(normalStyle);
        noHpField.setStyle(normalStyle);
    }
    
    private void clearPasswordFields() {
        oldPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        lblPasswordStrength.setText("");
        lblPasswordStrength.setStyle("");
    }
}