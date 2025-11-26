package com.mycompany.sewabaju.controllers;

import com.mycompany.sewabaju.exceptions.AuthenticationException;
import com.mycompany.sewabaju.models.User;
import com.mycompany.sewabaju.models.enums.Role;
import com.mycompany.sewabaju.services.AuthService;
import com.mycompany.sewabaju.utils.AlertUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button btnLogin;
    @FXML private Hyperlink linkRegister;
    @FXML private CheckBox chkRememberMe;
    @FXML private Label lblError;
    
    private AuthService authService;
    
    @FXML
    public void initialize() {
        authService = AuthService.getInstance();

        if (lblError != null) {
            lblError.setVisible(false);
        }

        if (passwordField != null) {
            passwordField.setOnAction(event -> handleLogin());
        }

        if (linkRegister != null) {
            linkRegister.setOnAction(event -> handleGoToRegister());
        }
        
        System.out.println("LoginController initialized");
    }

    @FXML
    private void handleLogin() {
        if (lblError != null) {
            lblError.setVisible(false);
        }

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email dan password harus diisi");
            return;
        }

        btnLogin.setDisable(true);
        
        try {
            User user = authService.login(email, password);

            System.out.println("Login success: " + user.getNama() + " (" + user.getRole() + ")");

            redirectToDashboard(user.getRole());
            
        } catch (AuthenticationException e) {
            showError(e.getMessage());
            btnLogin.setDisable(false);
            
        } catch (Exception e) {
            showError("Terjadi kesalahan: " + e.getMessage());
            btnLogin.setDisable(false);
            e.printStackTrace();
        }
    }
    @FXML
    private void handleGoToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/mycompany/sewabaju/fxml/register.fxml")
            );
            Parent root = loader.load();
            Stage stage = (Stage) emailField.getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Register - SewaBaju");
            
        } catch (IOException e) {
            AlertUtil.showError("Gagal membuka halaman register: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void redirectToDashboard(Role role) {
        try {
            String fxmlPath;
            String title;
            
            if (role == Role.ADMIN) {
                fxmlPath = "/com/mycompany/sewabaju/fxml/admin/admin_dashboard.fxml";
                title = "Admin Dashboard - SewaBaju";
            } else {
                fxmlPath = "/com/mycompany/sewabaju/fxml/pelanggan/pelanggan_dashboard.fxml";
                title = "Dashboard - SewaBaju";
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setMaximized(true);
            
        } catch (IOException e) {
            AlertUtil.showError("Gagal membuka dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }
   
    private void showError(String message) {
        if (lblError != null) {
            lblError.setText(message);
            lblError.setVisible(true);
        } else {
            AlertUtil.showError(message);
        }
    }
}