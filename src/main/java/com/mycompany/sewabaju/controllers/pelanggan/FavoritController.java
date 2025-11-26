package com.mycompany.sewabaju.controllers.pelanggan;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.models.Baju;
import com.mycompany.sewabaju.models.DetailBaju;
import com.mycompany.sewabaju.models.Kategori;
import com.mycompany.sewabaju.models.Pelanggan;
import com.mycompany.sewabaju.services.BajuService;
import com.mycompany.sewabaju.services.FavoritService;
import com.mycompany.sewabaju.utils.AlertUtil;
import com.mycompany.sewabaju.utils.FileUtil;
import com.mycompany.sewabaju.utils.Session;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FavoritController {
    
    @FXML private BorderPane rootPane;
    @FXML private VBox topBar;
    @FXML private Button btnBack;
    @FXML private Label lblTitle;
    @FXML private Label lblTotalFavorit;
    @FXML private Button btnRefresh;
    
    @FXML private TextField searchField;
    @FXML private ComboBox<Kategori> filterKategori;
    @FXML private Button btnSearch;
    @FXML private Button btnResetFilter;
    
    @FXML private ScrollPane scrollPane;
    @FXML private VBox contentContainer;
    @FXML private Label lblEmpty;
    @FXML private GridPane gridFavorit;
    
    @FXML private ProgressIndicator loadingIndicator;
    
    private FavoritService favoritService;
    private BajuService bajuService;
    
    private Pelanggan currentPelanggan;
    private List<Baju> allFavoritList;
    private List<Baju> displayedFavoritList;
    
    private static final int COLUMNS = 3;
    
    @FXML
    public void initialize() {
        favoritService = new FavoritService();
        bajuService = BajuService.getInstance();
        currentPelanggan = Session.getInstance().getCurrentPelanggan();
        
        if (currentPelanggan == null) {
            AlertUtil.showError("Session expired. Please login again.");
            handleBack();
            return;
        }
        
        setupUI();
        loadKategoriFilter();
        loadFavoritBaju();
        
        System.out.println("FavoritController initialized");
    }
    
    private void setupUI() {
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        
        gridFavorit.setHgap(20);
        gridFavorit.setVgap(20);
        gridFavorit.setPadding(new Insets(20));
        
        lblEmpty.setVisible(false);
        lblEmpty.setManaged(false);
        
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(false);
            loadingIndicator.setManaged(false);
        }
    }
    
    private void loadKategoriFilter() {
        try {
            List<Kategori> kategoriList = bajuService.getAllKategori();
            
            Kategori allOption = new Kategori();
            allOption.setKategoriId(0);
            allOption.setNamaKategori("Semua Kategori");
            
            filterKategori.setItems(FXCollections.observableArrayList(allOption));
            filterKategori.getItems().addAll(kategoriList);
            filterKategori.setValue(allOption);
            
        } catch (DatabaseException e) {
            System.err.println("Error loading kategori: " + e.getMessage());
        }
    }
    
    private void loadFavoritBaju() {
        showLoading(true);
        
        new Thread(() -> {
            try {
                allFavoritList = favoritService.getFavoritBajuList(currentPelanggan.getPelangganId());
                displayedFavoritList = allFavoritList;
                
                Platform.runLater(() -> {
                    updateUI();
                    showLoading(false);
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showLoading(false);
                    AlertUtil.showError("Error loading favorit: " + e.getMessage());
                    e.printStackTrace();
                });
            }
        }).start();
    }
    
    private void updateUI() {
        int totalFavorit = displayedFavoritList != null ? displayedFavoritList.size() : 0;
        lblTotalFavorit.setText(totalFavorit + " Favorit");
        
        if (displayedFavoritList == null || displayedFavoritList.isEmpty()) {
            showEmptyState();
        } else {
            showFavoritGrid();
        }
    }
    
    private void showEmptyState() {
        lblEmpty.setVisible(true);
        lblEmpty.setManaged(true);
        gridFavorit.setVisible(false);
        gridFavorit.setManaged(false);
    }
    
    private void showFavoritGrid() {
        lblEmpty.setVisible(false);
        lblEmpty.setManaged(false);
        gridFavorit.setVisible(true);
        gridFavorit.setManaged(true);
        
        displayFavoritCards();
    }
    
    private void displayFavoritCards() {
        gridFavorit.getChildren().clear();
        
        int col = 0;
        int row = 0;
        
        for (Baju baju : displayedFavoritList) {
            try {
                VBox card = createFavoritCard(baju);
                gridFavorit.add(card, col, row);
                
                col++;
                if (col >= COLUMNS) {
                    col = 0;
                    row++;
                }
                
            } catch (Exception e) {
                System.err.println("Error creating card for baju: " + baju.getNamaBaju());
                e.printStackTrace();
            }
        }
    }
    
    private VBox createFavoritCard(Baju baju) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(220);
        card.setMinHeight(380);
        
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 12; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 12;"
        );
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.1));
        shadow.setRadius(10);
        shadow.setOffsetY(3);
        card.setEffect(shadow);
        
        // Hover effects
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle() + "-fx-border-color: #2196F3; -fx-border-width: 2;");
            DropShadow hoverShadow = new DropShadow();
            hoverShadow.setColor(Color.rgb(33, 150, 243, 0.3));
            hoverShadow.setRadius(15);
            hoverShadow.setOffsetY(5);
            card.setEffect(hoverShadow);
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(card.getStyle().replace("-fx-border-color: #2196F3; -fx-border-width: 2;", 
                                                   "-fx-border-color: #e0e0e0; -fx-border-width: 1;"));
            card.setEffect(shadow);
        });
        
        // Image container
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(180, 180);
        imageContainer.setMaxSize(180, 180);
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8;");
        
        ImageView imageView = new ImageView();
        imageView.setFitWidth(170);
        imageView.setFitHeight(170);
        imageView.setPreserveRatio(true);
        
        loadBajuImage(imageView, baju.getFoto());
        
        imageContainer.getChildren().add(imageView);
        
        // Favorite icon button
        Button btnFavoritIcon = new Button("❤️");
        btnFavoritIcon.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.9); " +
            "-fx-background-radius: 20; " +
            "-fx-font-size: 18px; " +
            "-fx-padding: 5 10; " +
            "-fx-cursor: hand;"
        );
        btnFavoritIcon.setOnAction(e -> handleRemoveFavorit(baju));
        StackPane.setAlignment(btnFavoritIcon, Pos.TOP_RIGHT);
        StackPane.setMargin(btnFavoritIcon, new Insets(5));
        
        imageContainer.getChildren().add(btnFavoritIcon);
        
        // Kategori label
        Label lblKategori = new Label(baju.getNamaKategori());
        lblKategori.setStyle(
            "-fx-background-color: #E3F2FD; " +
            "-fx-text-fill: #2196F3; " +
            "-fx-padding: 3 10; " +
            "-fx-background-radius: 12; " +
            "-fx-font-size: 11px; " +
            "-fx-font-weight: bold;"
        );
        
        // Nama baju
        Label lblNama = new Label(baju.getNamaBaju());
        lblNama.setFont(Font.font("System", FontWeight.BOLD, 15));
        lblNama.setWrapText(true);
        lblNama.setMaxWidth(190);
        lblNama.setAlignment(Pos.CENTER);
        lblNama.setStyle("-fx-text-alignment: center;");
        
        // Harga
        Label lblHarga = new Label(baju.getRangeHarga());
        lblHarga.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblHarga.setStyle("-fx-text-fill: #4CAF50;");
        
        // Stok
        int totalStok = getTotalStok(baju);
        Label lblStok = new Label("Stok: " + totalStok + " pcs");
        lblStok.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-text-fill: " + (totalStok > 0 ? "#4CAF50" : "#F44336") + ";"
        );
        
        // Button box
        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button btnDetail = new Button("👁 Detail");
        btnDetail.setStyle(
            "-fx-background-color: #2196F3; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8 16; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        btnDetail.setOnMouseEntered(e -> btnDetail.setStyle(btnDetail.getStyle() + "-fx-background-color: #1976D2;"));
        btnDetail.setOnMouseExited(e -> btnDetail.setStyle(btnDetail.getStyle().replace("-fx-background-color: #1976D2;", "-fx-background-color: #2196F3;")));
        btnDetail.setOnAction(e -> handleViewDetail(baju));
        
        Button btnRemove = new Button("🗑 Hapus");
        btnRemove.setStyle(
            "-fx-background-color: #F44336; " +
            "-fx-text-fill: white; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8 16; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        btnRemove.setOnMouseEntered(e -> btnRemove.setStyle(btnRemove.getStyle() + "-fx-background-color: #D32F2F;"));
        btnRemove.setOnMouseExited(e -> btnRemove.setStyle(btnRemove.getStyle().replace("-fx-background-color: #D32F2F;", "-fx-background-color: #F44336;")));
        btnRemove.setOnAction(e -> handleRemoveFavorit(baju));
        
        buttonBox.getChildren().addAll(btnDetail, btnRemove);
        
        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        card.getChildren().addAll(
            imageContainer,
            lblKategori,
            lblNama,
            lblHarga,
            lblStok,
            spacer,
            buttonBox
        );
        
        return card;
    }
    
    private void loadBajuImage(ImageView imageView, String fotoFilename) {
        try {
            if (fotoFilename != null && !fotoFilename.isEmpty()) {
                String fotoPath = FileUtil.getBajuPhotoPath(fotoFilename);
                
                if (FileUtil.fileExists(fotoPath)) {
                    byte[] imageData = FileUtil.readFileAsBytes(fotoPath);
                    Image image = new Image(new ByteArrayInputStream(imageData));
                    imageView.setImage(image);
                    return;
                }
            }
            
            // Use default placeholder
            imageView.setImage(null);
            
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            imageView.setImage(null);
        }
    }
    
    private int getTotalStok(Baju baju) {
        try {
            if (baju.getDetailBajuList() != null && !baju.getDetailBajuList().isEmpty()) {
                return baju.getTotalStok();
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    @FXML
    private void handleSearch() {
        String keyword = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        Kategori selectedKategori = filterKategori != null ? filterKategori.getValue() : null;
        
        if (allFavoritList == null) {
            return;
        }
        
        displayedFavoritList = allFavoritList.stream()
            .filter(baju -> {
                // Filter by keyword
                boolean matchKeyword = keyword.isEmpty() || 
                    baju.getNamaBaju().toLowerCase().contains(keyword) ||
                    (baju.getDeskripsi() != null && baju.getDeskripsi().toLowerCase().contains(keyword));
                
                // Filter by kategori
                boolean matchKategori = selectedKategori == null || 
                    selectedKategori.getKategoriId() == 0 ||
                    baju.getKategoriId() == selectedKategori.getKategoriId();
                
                return matchKeyword && matchKategori;
            })
            .collect(Collectors.toList());
        
        updateUI();
    }
    
    @FXML
    private void handleResetFilter() {
        if (searchField != null) {
            searchField.clear();
        }
        
        if (filterKategori != null && !filterKategori.getItems().isEmpty()) {
            filterKategori.setValue(filterKategori.getItems().get(0));
        }
        
        displayedFavoritList = allFavoritList;
        updateUI();
    }
    
    private void handleRemoveFavorit(Baju baju) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Konfirmasi Hapus");
        confirmDialog.setHeaderText("Hapus dari Favorit?");
        confirmDialog.setContentText("Apakah Anda yakin ingin menghapus \"" + baju.getNamaBaju() + "\" dari favorit?");
        
        ButtonType btnYes = new ButtonType("Ya, Hapus", ButtonBar.ButtonData.YES);
        ButtonType btnNo = new ButtonType("Batal", ButtonBar.ButtonData.NO);
        confirmDialog.getButtonTypes().setAll(btnYes, btnNo);
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        
        if (result.isPresent() && result.get() == btnYes) {
            try {
                favoritService.removeFavorit(
                    currentPelanggan.getPelangganId(), 
                    baju.getBajuId()
                );
                
                AlertUtil.showSuccess("\"" + baju.getNamaBaju() + "\" dihapus dari favorit!");
                loadFavoritBaju();
                
            } catch (Exception e) {
                AlertUtil.showError("Error menghapus favorit: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void handleViewDetail(Baju baju) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detail Baju");
        dialog.setHeaderText(baju.getNamaBaju());
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);
        
        // Image
        ImageView imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(300);
        imageView.setPreserveRatio(true);
        loadBajuImage(imageView, baju.getFoto());
        
        StackPane imagePane = new StackPane(imageView);
        imagePane.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
        imagePane.setPadding(new Insets(20));
        
        // Info box
        VBox infoBox = new VBox(10);
        
        addInfoRow(infoBox, "Kategori:", baju.getNamaKategori());
        addInfoRow(infoBox, "Harga:", baju.getRangeHarga());
        addInfoRow(infoBox, "Total Stok:", getTotalStok(baju) + " pcs");
        
        if (baju.getDeskripsi() != null && !baju.getDeskripsi().isEmpty()) {
            Label lblDeskripsi = new Label("Deskripsi:");
            lblDeskripsi.setFont(Font.font("System", FontWeight.BOLD, 13));
            
            TextArea txtDeskripsi = new TextArea(baju.getDeskripsi());
            txtDeskripsi.setWrapText(true);
            txtDeskripsi.setEditable(false);
            txtDeskripsi.setPrefRowCount(3);
            
            infoBox.getChildren().addAll(lblDeskripsi, txtDeskripsi);
        }
        
        if (baju.getDetailBajuList() != null && !baju.getDetailBajuList().isEmpty()) {
            Label lblUkuran = new Label("Ukuran Tersedia:");
            lblUkuran.setFont(Font.font("System", FontWeight.BOLD, 13));
            
            VBox ukuranBox = new VBox(5);
            baju.getDetailBajuList().forEach(detail -> {
                String info = String.format("• %s - Rp %.0f/hari (Stok: %d)", 
                    detail.getUkuranDisplay(), 
                    detail.getHargaSewa(), 
                    detail.getStok()
                );
                Label lblDetail = new Label(info);
                lblDetail.setStyle("-fx-font-size: 12px;");
                ukuranBox.getChildren().add(lblDetail);
            });
            
            infoBox.getChildren().addAll(lblUkuran, ukuranBox);
        }
        
        content.getChildren().addAll(imagePane, infoBox);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }
    
    private void addInfoRow(VBox container, String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label lblLabel = new Label(label);
        lblLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        lblLabel.setPrefWidth(100);
        
        Label lblValue = new Label(value);
        lblValue.setFont(Font.font("System", 13));
        
        row.getChildren().addAll(lblLabel, lblValue);
        container.getChildren().add(row);
    }
    
    @FXML
    private void handleRefresh() {
        loadFavoritBaju();
        AlertUtil.showSuccess("Data favorit berhasil di-refresh!");
    }
    
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/mycompany/sewabaju/fxml/pelanggan/pelanggan_dashboard.fxml")
            );
            Parent root = loader.load();
            
            Stage stage = (Stage) rootPane.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard - SewaBaju");
            
        } catch (IOException e) {
            AlertUtil.showError("Gagal kembali ke dashboard");
            e.printStackTrace();
        } catch (Exception e) {
            // Handle case when rootPane is null (dialog mode)
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.close();
        }
    }
    
    private void showLoading(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
            loadingIndicator.setManaged(show);
        }
        
        if (gridFavorit != null) {
            gridFavorit.setVisible(!show);
        }
    }
    
    public void refresh() {
        loadFavoritBaju();
    }
    
    public int getTotalFavorit() {
        return allFavoritList != null ? allFavoritList.size() : 0;
    }
}