package com.mycompany.sewabaju.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

public class AlertUtil {
    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void showSuccess(String message) {
        showSuccess("Berhasil", message);
    }
    
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void showError(String message) {
        showError("Error", message);
    }
    
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void showWarning(String message) {
        showWarning("Peringatan", message);
    }
    
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void showInfo(String message) {
        showInfo("Informasi", message);
    }
    
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    public static boolean showConfirmation(String message) {
        return showConfirmation("Konfirmasi", message);
    }
    
    public static boolean showDeleteConfirmation(String itemName) {
        return showConfirmation(
            "Konfirmasi Hapus",
            "Apakah Anda yakin ingin menghapus " + itemName + "?"
        );
    }
    
    public static boolean showLogoutConfirmation() {
        return showConfirmation(
            "Konfirmasi Logout",
            "Apakah Anda yakin ingin keluar?"
        );
    }
    
    public static String showInputDialog(String title, String header, String prompt) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(prompt);
        
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
    public static String showInputDialog(String prompt) {
        return showInputDialog("Input", null, prompt);
    }
    
    public static void showValidationError(String fieldName, String errorMessage) {
        showError("Validasi Gagal", fieldName + ": " + errorMessage);
    }
    
    public static void showDatabaseError(String operation) {
        showError(
            "Error Database",
            "Terjadi kesalahan saat " + operation + ". Silakan coba lagi."
        );
    }
    
    public static void showAuthenticationError() {
        showError(
            "Login Gagal",
            "Email atau password salah. Silakan coba lagi."
        );
    }
    
    public static void showStokTidakCukupError(String namaBaju, int stokTersedia) {
        showError(
            "Stok Tidak Cukup",
            "Maaf, stok " + namaBaju + " tidak mencukupi.\n" +
            "Stok tersedia: " + stokTersedia
        );
    }
    
    public static void showNotImplemented() {
        showInfo("Coming Soon", "Fitur ini belum diimplementasi.");
    }
}