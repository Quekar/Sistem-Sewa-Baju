package com.mycompany.sewabaju.controllers.pelanggan;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.models.Baju;
import com.mycompany.sewabaju.models.DetailBaju;
import com.mycompany.sewabaju.models.Kategori;
import com.mycompany.sewabaju.models.enums.Ukuran;
import com.mycompany.sewabaju.services.BajuService;
import com.mycompany.sewabaju.utils.AlertUtil;
import com.mycompany.sewabaju.utils.FileUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

    public class CatalogBajuController {
    @FXML private TextField searchField;
    @FXML private ComboBox<Kategori> filterKategori;
    @FXML private ComboBox<Ukuran> filterUkuran;
    @FXML private Slider sliderMinHarga;
    @FXML private Slider sliderMaxHarga;
    @FXML private Label lblMinHarga;
    @FXML private Label lblMaxHarga;
    @FXML private Button btnSearch;
    @FXML private Button btnResetFilter;

    @FXML private GridPane gridCatalog;
    @FXML private ScrollPane scrollPane;
    @FXML private Label lblResultCount;

    @FXML private Button btnKeranjang;
    @FXML private Button btnBack;
    @FXML private Label lblCartCount;
    
    private BajuService bajuService;
    private List<Baju> currentBajuList;

    private static Map<Integer, CartItemTemp> cartItems = new HashMap<>();
    
    @FXML
    public void initialize() {
        bajuService = BajuService.getInstance();

        setupFilters();
 
        setupPriceSliders();
        
        loadAllBaju();
        
        updateCartCount();
        
        System.out.println("CatalogBajuController initialized");
    }
    
    private void setupFilters() {
        try {
            List<Kategori> kategoriList = bajuService.getAllKategori();
            
            Kategori allOption = new Kategori();
            allOption.setKategoriId(0);
            allOption.setNamaKategori("Semua");
            
            filterKategori.setItems(FXCollections.observableArrayList(allOption));
            filterKategori.getItems().addAll(kategoriList);
            filterKategori.setValue(allOption);
            
            Ukuran allUkuran = null;
            filterUkuran.setItems(FXCollections.observableArrayList(Ukuran.values()));
            filterUkuran.setPromptText("Semua Ukuran");
            
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("memuat filter");
            e.printStackTrace();
        }
    }
    private void setupPriceSliders() {
        sliderMinHarga.setMin(0);
        sliderMinHarga.setMax(500000);
        sliderMinHarga.setValue(0);
        
        sliderMaxHarga.setMin(0);
        sliderMaxHarga.setMax(500000);
        sliderMaxHarga.setValue(500000);
        
        sliderMinHarga.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblMinHarga.setText(String.format("Rp %.0f", newVal.doubleValue()));
        });
        
        sliderMaxHarga.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblMaxHarga.setText(String.format("Rp %.0f", newVal.doubleValue()));
        });
        
        lblMinHarga.setText("Rp 0");
        lblMaxHarga.setText("Rp 500,000");
    }
    
    private void loadAllBaju() {
        try {
            currentBajuList = bajuService.getAvailableBaju();
            displayBaju(currentBajuList);
            
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("memuat catalog baju");
            e.printStackTrace();
        }
    }
    
    private void displayBaju(List<Baju> bajuList) {
        gridCatalog.getChildren().clear();
        
        int row = 0;
        int col = 0;
        final int COLUMNS = 3;
        
        for (Baju baju : bajuList) {
            VBox card = createBajuCard(baju);
            gridCatalog.add(card, col, row);
            
            col++;
            if (col >= COLUMNS) {
                col = 0;
                row++;
            }
        }
        
        lblResultCount.setText("Menampilkan " + bajuList.size() + " baju");
    }
    
    private VBox createBajuCard(Baju baju) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(250);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-border-color: #e0e0e0; " +
            "-fx-border-width: 1px; " +
            "-fx-background-radius: 10px; " +
            "-fx-border-radius: 10px; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
        );
        
        ImageView imageView = new ImageView();
        imageView.setFitHeight(180);
        imageView.setFitWidth(180);
        imageView.setPreserveRatio(true);
        
        try {
            if (baju.getFoto() != null) {
                String path = FileUtil.getBajuPhotoPath(baju.getFoto());
                if (FileUtil.fileExists(path)) {
                    byte[] imageData = FileUtil.readFileAsBytes(path);
                    Image image = new Image(new ByteArrayInputStream(imageData));
                    imageView.setImage(image);
                }
            }
        } catch (Exception e) {
            // Use placeholder
        }
        
        Label lblNama = new Label(baju.getNamaBaju());
        lblNama.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        lblNama.setWrapText(true);
        lblNama.setMaxWidth(220);
        
        Label lblKategori = new Label(baju.getNamaKategori());
        lblKategori.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
        
        Label lblHarga = new Label(baju.getRangeHarga() + "/hari");
        lblHarga.setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label lblStok = new Label("Stok: " + baju.getTotalStok());
        lblStok.setStyle("-fx-font-size: 12px;");
        
        if (baju.getTotalStok() <= 0) {
            lblStok.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            lblStok.setText("Stok Habis");
        } else if (baju.getTotalStok() <= 5) {
            lblStok.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
        }
        
        Button btnDetail = new Button("Lihat Detail");
        btnDetail.setStyle(
            "-fx-background-color: #2196F3; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        btnDetail.setPrefWidth(180);
        
        btnDetail.setOnAction(e -> showDetailDialog(baju));
        
        card.getChildren().addAll(imageView, lblNama, lblKategori, lblHarga, lblStok, btnDetail);
        
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: #f5f5f5; " +
                "-fx-border-color: #2196F3; " +
                "-fx-border-width: 2px; " +
                "-fx-background-radius: 10px; " +
                "-fx-border-radius: 10px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3); " +
                "-fx-cursor: hand;"
            );
        });
        
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: white; " +
                "-fx-border-color: #e0e0e0; " +
                "-fx-border-width: 1px; " +
                "-fx-background-radius: 10px; " +
                "-fx-border-radius: 10px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
            );
        });
        
        return card;
    }
    
    private void showDetailDialog(Baju baju) {
        try {
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Detail Baju - " + baju.getNamaBaju());
            
            VBox content = new VBox(15);
            content.setPadding(new Insets(20));
            content.setStyle("-fx-background-color: white;");
            
            ImageView imageView = new ImageView();
            imageView.setFitHeight(300);
            imageView.setFitWidth(300);
            imageView.setPreserveRatio(true);
            
            if (baju.getFoto() != null) {
                String path = FileUtil.getBajuPhotoPath(baju.getFoto());
                if (FileUtil.fileExists(path)) {
                    byte[] imageData = FileUtil.readFileAsBytes(path);
                    Image image = new Image(new ByteArrayInputStream(imageData));
                    imageView.setImage(image);
                }
            }
            
            Label lblNama = new Label(baju.getNamaBaju());
            lblNama.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
            
            Label lblKategori = new Label("Kategori: " + baju.getNamaKategori());
            
            Label lblDeskripsi = new Label(baju.getDeskripsi() != null ? baju.getDeskripsi() : "");
            lblDeskripsi.setWrapText(true);
            lblDeskripsi.setMaxWidth(400);
            
            Label lblSizesTitle = new Label("Ukuran & Harga:");
            lblSizesTitle.setStyle("-fx-font-weight: bold;");
            
            VBox sizesBox = new VBox(5);
            for (DetailBaju detail : baju.getDetailBajuList()) {
                if (detail.getStok() > 0) {
                    Label lblSize = new Label(String.format("%s - Rp %.0f/hari (Stok: %d)",
                        detail.getUkuranDisplay(),
                        detail.getHargaSewa(),
                        detail.getStok()
                    ));
                    sizesBox.getChildren().add(lblSize);
                }
            }
            
            Label lblAddCart = new Label("Tambah ke Keranjang:");
            lblAddCart.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            ComboBox<DetailBaju> comboUkuran = new ComboBox<>();
            comboUkuran.setPromptText("Pilih Ukuran");
            comboUkuran.setPrefWidth(200);
            
            List<DetailBaju> availableSizes = baju.getDetailBajuList().stream()
                .filter(d -> d.getStok() > 0)
                .collect(Collectors.toList());
            
            comboUkuran.setItems(FXCollections.observableArrayList(availableSizes));
            comboUkuran.setCellFactory(lv -> new ListCell<DetailBaju>() {
                @Override
                protected void updateItem(DetailBaju item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getUkuranDisplay() + " - Rp " + 
                               String.format("%.0f", item.getHargaSewa()));
                    }
                }
            });
            comboUkuran.setButtonCell(new ListCell<DetailBaju>() {
                @Override
                protected void updateItem(DetailBaju item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Pilih Ukuran");
                    } else {
                        setText(item.getUkuranDisplay() + " - Rp " + 
                               String.format("%.0f", item.getHargaSewa()));
                    }
                }
            });
            
            Spinner<Integer> spinnerJumlah = new Spinner<>(1, 10, 1);
            spinnerJumlah.setPrefWidth(100);
            spinnerJumlah.setEditable(true);
            
            Button btnAddToCart = new Button("Tambah ke Keranjang");
            btnAddToCart.setStyle(
                "-fx-background-color: #4CAF50; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold;"
            );
            btnAddToCart.setPrefWidth(200);
            
            btnAddToCart.setOnAction(e -> {
                DetailBaju selected = comboUkuran.getValue();
                if (selected == null) {
                    AlertUtil.showWarning("Pilih ukuran terlebih dahulu");
                    return;
                }
                
                int jumlah = spinnerJumlah.getValue();
                
                if (jumlah > selected.getStok()) {
                    AlertUtil.showWarning("Stok tidak cukup. Stok tersedia: " + selected.getStok());
                    return;
                }
                
                addToCart(selected, jumlah);
                AlertUtil.showSuccess("Berhasil ditambahkan ke keranjang!");
                updateCartCount();
                dialog.close();
            });
            
            Button btnClose = new Button("Tutup");
            btnClose.setOnAction(e -> dialog.close());
            
            content.getChildren().addAll(
                imageView, lblNama, lblKategori, lblDeskripsi,
                new Separator(),
                lblSizesTitle, sizesBox,
                new Separator(),
                lblAddCart, 
                new Label("Ukuran:"), comboUkuran,
                new Label("Jumlah:"), spinnerJumlah,
                btnAddToCart, btnClose
            );
            
            ScrollPane scrollPane = new ScrollPane(content);
            scrollPane.setFitToWidth(true);
            
            Scene scene = new Scene(scrollPane, 500, 600);
            dialog.setScene(scene);
            dialog.show();
            
        } catch (Exception e) {
            AlertUtil.showError("Gagal membuka detail: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleSearch() {
        try {
            String keyword = searchField.getText().trim();
            Kategori selectedKategori = filterKategori.getValue();
            Ukuran selectedUkuran = filterUkuran.getValue();
            double minHarga = sliderMinHarga.getValue();
            double maxHarga = sliderMaxHarga.getValue();
            
            List<Baju> result = bajuService.getAvailableBaju();
            
            if (!keyword.isEmpty()) {
                result = result.stream()
                    .filter(b -> b.getNamaBaju().toLowerCase().contains(keyword.toLowerCase()) ||
                                (b.getDeskripsi() != null && 
                                 b.getDeskripsi().toLowerCase().contains(keyword.toLowerCase())))
                    .collect(Collectors.toList());
            }
            
            if (selectedKategori != null && selectedKategori.getKategoriId() > 0) {
                result = result.stream()
                    .filter(b -> b.getKategoriId() == selectedKategori.getKategoriId())
                    .collect(Collectors.toList());
            }
            
            if (selectedUkuran != null) {
                result = result.stream()
                    .filter(b -> b.getDetailBajuList().stream()
                        .anyMatch(d -> d.getUkuran() == selectedUkuran && d.getStok() > 0))
                    .collect(Collectors.toList());
            }
            
            result = result.stream()
                .filter(b -> {
                    double minBajuHarga = b.getDetailBajuList().stream()
                        .mapToDouble(DetailBaju::getHargaSewa)
                        .min().orElse(0);
                    return minBajuHarga >= minHarga && minBajuHarga <= maxHarga;
                })
                .collect(Collectors.toList());
            
            displayBaju(result);
            
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("search baju");
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleResetFilter() {
        searchField.clear();
        filterKategori.setValue(filterKategori.getItems().get(0));
        filterUkuran.setValue(null);
        sliderMinHarga.setValue(0);
        sliderMaxHarga.setValue(500000);
        loadAllBaju();
    }
    
    @FXML
    private void handleKeranjang() {
        navigateToPage(
            "/com/mycompany/sewabaju/fxml/pelanggan/keranjang.fxml",
            "Keranjang - SewaBaju"
        );
    }
    
    @FXML
    private void handleBack() {
        navigateToPage(
            "/com/mycompany/sewabaju/fxml/pelanggan/pelanggan_dashboard.fxml",
            "Dashboard - SewaBaju"
        );
    }
    
    private void navigateToPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage stage = (Stage) btnBack.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            
        } catch (IOException e) {
            AlertUtil.showError("Gagal membuka halaman: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void addToCart(DetailBaju detailBaju, int jumlah) {
        int key = detailBaju.getDetailBajuId();
        
        if (cartItems.containsKey(key)) {
            CartItemTemp existing = cartItems.get(key);
            existing.jumlah += jumlah;
        } else {
            cartItems.put(key, new CartItemTemp(detailBaju, jumlah));
        }
    }
    
    private void updateCartCount() {
        int totalItems = cartItems.values().stream()
            .mapToInt(c -> c.jumlah)
            .sum();
        
        if (lblCartCount != null) {
            lblCartCount.setText(String.valueOf(totalItems));
            lblCartCount.setVisible(totalItems > 0);
        }
    }
    
    public static Map<Integer, CartItemTemp> getCartItems() {
        return cartItems;
    }
    
    public static void clearCart() {
        cartItems.clear();
    }
    
    public static class CartItemTemp {
        public DetailBaju detailBaju;
        public int jumlah;
        
        public CartItemTemp(DetailBaju detailBaju, int jumlah) {
            this.detailBaju = detailBaju;
            this.jumlah = jumlah;
        }
    }
}