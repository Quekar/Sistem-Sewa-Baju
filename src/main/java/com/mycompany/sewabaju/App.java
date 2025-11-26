package com.mycompany.sewabaju;

import com.mycompany.sewabaju.utils.FileUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App - Main Entry Point
 * Sistem Penyewaan Baju/Pakaian
 */
public class App extends Application {

    private static Scene scene;
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        
        try {
            // Initialize upload directories
            FileUtil.initializeDirectories();
            
            // Load login screen as first screen
            scene = new Scene(loadFXML("login"), 600, 400);
            
            // Set stage properties
            stage.setScene(scene);
            stage.setTitle("SewaBaju - Sistem Penyewaan Baju");
            stage.setResizable(false);
            
            // Show stage
            stage.show();
            
            System.out.println("Application started successfully!");
            
        } catch (Exception e) {
            System.err.println("ERROR starting application:");
            e.printStackTrace();
            
            // Show error dialog
            showErrorDialog("Startup Error", 
                "Gagal memulai aplikasi: " + e.getMessage() + 
                "\n\nPeriksa:\n" +
                "1. Database connection di database.properties\n" +
                "2. File FXML ada di resources/com/mycompany/sewabaju/\n" +
                "3. MySQL server sudah running");
        }
    }

    /**
     * Set root FXML (change screen)
     */
    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    /**
     * Set root FXML with custom size
     */
    public static void setRoot(String fxml, double width, double height) throws IOException {
        Parent root = loadFXML(fxml);
        scene.setRoot(root);
        
        // Resize window
        primaryStage.setWidth(width);
        primaryStage.setHeight(height);
        primaryStage.centerOnScreen();
    }

    /**
     * Load FXML file
     */
    private static Parent loadFXML(String fxml) throws IOException {
        // Add .fxml extension if not present
        if (!fxml.endsWith(".fxml")) {
            fxml = fxml + ".fxml";
        }
        
        System.out.println("Attempting to load FXML: " + fxml);
        
        // Try multiple possible paths
        String[] possiblePaths = {
            "/com/mycompany/sewabaju/" + fxml,
            "/com/mycompany/sewabaju/fxml/" + fxml,
            fxml
        };
        
        for (String path : possiblePaths) {
            try {
                System.out.println("Trying path: " + path);
                FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(path));
                Parent root = fxmlLoader.load();
                System.out.println("✓ FXML loaded successfully from: " + path);
                return root;
            } catch (Exception e) {
                System.out.println("✗ Failed to load from: " + path);
            }
        }
        
        // If all paths fail, throw error
        throw new IOException("Cannot find FXML file: " + fxml + 
            "\n\nChecked paths:\n" + String.join("\n", possiblePaths));
    }

    /**
     * Get primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Get current scene
     */
    public static Scene getScene() {
        return scene;
    }

    /**
     * Main method
     */
    public static void main(String[] args) {
        // Check Java version
        String javaVersion = System.getProperty("java.version");
        System.out.println("Java Version: " + javaVersion);
        
        // Check JavaFX availability
        try {
            Class.forName("javafx.application.Application");
            System.out.println("JavaFX is available");
        } catch (ClassNotFoundException e) {
            System.err.println("ERROR: JavaFX is not available!");
            System.err.println("Please make sure JavaFX is properly configured in your pom.xml");
            return;
        }
        
        // Launch application
        launch();
    }

    @Override
    public void stop() {
        System.out.println("Application stopping...");
        
        // Cleanup resources here if needed
        // For example: close database connections, save state, etc.
        
        System.out.println("Application stopped");
    }
    
    /**
     * Show error dialog
     */
    private void showErrorDialog(String title, String message) {
        try {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("Cannot show error dialog: " + e.getMessage());
        }
    }
}