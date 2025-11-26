package com.mycompany.sewabaju.controllers;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.exceptions.ValidationException;
import com.mycompany.sewabaju.models.Pelanggan;
import com.mycompany.sewabaju.services.AuthService;
import com.mycompany.sewabaju.utils.AlertUtil;
import com.mycompany.sewabaju.utils.PasswordUtil;
import com.mycompany.sewabaju.utils.ValidationUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {
    
    @FXML private TextField namaField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField noHpField;
    @FXML private TextArea alamatArea;
    @FXML private Button btnRegister;
    @FXML private Hyperlink linkLogin;
    @FXML private Label lblPasswordStrength;
    
    private AuthService authService;
    @FXML
    public void initialize() {
        authService = AuthService.getInstance();
        if (passwordField != null && lblPasswordStrength != null) {
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
                updatePasswordStrength(newVal);
            });
        }

        if (linkLogin != null) {
            linkLogin.setOnAction(event -> handleGoToLogin());
        }
        
        System.out.println("RegisterController initialized");
    }
    @FXML
    private void handleRegister() {
        String nama = namaField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String noHp = noHpField.getText().trim();
        String alamat = alamatArea.getText().trim();

        clearValidationStyles();

        try {
            validateInput(nama, email, password, confirmPassword, noHp);
        } catch (ValidationException e) {
            AlertUtil.showValidationError("Validasi", e.getMessage());
            return;
        }
        btnRegister.setDisable(true);
        
        try {
            Pelanggan pelanggan = authService.registerPelanggan(nama, email, password, noHp, alamat);

            System.out.println("Register success: " + pelanggan.getNama());
            
            AlertUtil.showSuccess("Registrasi berhasil!\nSilakan login dengan akun Anda.");
            
            handleGoToLogin();
            
        } catch (ValidationException e) {
            AlertUtil.showValidationError("Validasi", e.getMessage());
            highlightErrorField(e.getFieldName());
            btnRegister.setDisable(false);
            
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("registrasi");
            btnRegister.setDisable(false);
            e.printStackTrace();
            
        } catch (Exception e) {
            AlertUtil.showError("Terjadi kesalahan: " + e.getMessage());
            btnRegister.setDisable(false);
            e.printStackTrace();
        }
    }
    @FXML
    private void handleGoToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/mycompany/sewabaju/fxml/login.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) namaField.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Login - SewaBaju");
            
        } catch (IOException e) {
            AlertUtil.showError("Gagal membuka halaman login: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void validateInput(String nama, String email, String password, 
                               String confirmPassword, String noHp) throws ValidationException {
        
        if (ValidationUtil.isEmpty(nama)) {
            ValidationException ex = new ValidationException("Nama tidak boleh kosong", "nama");
            throw ex;
        }
        
        if (!ValidationUtil.hasMinLength(nama, 3)) {
            throw new ValidationException("Nama minimal 3 karakter", "nama");
        }
        
        if (!ValidationUtil.isValidEmail(email)) {
            throw new ValidationException("Format email tidak valid", "email");
        }
        
        if (ValidationUtil.isEmpty(password)) {
            throw new ValidationException("Password tidak boleh kosong", "password");
        }
        
        if (!PasswordUtil.isStrongPassword(password)) {
            throw new ValidationException(
                PasswordUtil.getPasswordStrengthMessage(password), 
                "password"
            );
        }

        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Konfirmasi password tidak cocok", "confirmPassword");
        }

        if (!ValidationUtil.isEmpty(noHp) && !ValidationUtil.isValidPhone(noHp)) {
            throw new ValidationException("Format nomor HP tidak valid", "noHp");
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

    private void highlightErrorField(String fieldName) {
        if (fieldName == null) return;
        
        String errorStyle = "-fx-border-color: red; -fx-border-width: 2px;";
        
        switch (fieldName) {
            case "nama":
                namaField.setStyle(errorStyle);
                break;
            case "email":
                emailField.setStyle(errorStyle);
                break;
            case "password":
                passwordField.setStyle(errorStyle);
                break;
            case "confirmPassword":
                confirmPasswordField.setStyle(errorStyle);
                break;
            case "noHp":
                noHpField.setStyle(errorStyle);
                break;
        }
    }

    private void clearValidationStyles() {
        String normalStyle = "";
        namaField.setStyle(normalStyle);
        emailField.setStyle(normalStyle);
        passwordField.setStyle(normalStyle);
        confirmPasswordField.setStyle(normalStyle);
        noHpField.setStyle(normalStyle);
    }
}