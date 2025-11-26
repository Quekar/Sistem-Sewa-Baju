package com.mycompany.sewabaju.controllers.pelanggan;

import com.mycompany.sewabaju.controllers.pelanggan.CatalogBajuController.CartItemTemp;
import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.exceptions.StokTidakCukupException;
import com.mycompany.sewabaju.exceptions.ValidationException;
import com.mycompany.sewabaju.models.Pembayaran;
import com.mycompany.sewabaju.models.Penyewaan;
import com.mycompany.sewabaju.models.enums.MetodePembayaran;
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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeranjangController {
    @FXML private TableView<CartItemDisplay> tableCart;
    @FXML private TableColumn<CartItemDisplay, String> colNamaBaju;
    @FXML private TableColumn<CartItemDisplay, String> colUkuran;
    @FXML private TableColumn<CartItemDisplay, Double> colHarga;
    @FXML private TableColumn<CartItemDisplay, Integer> colJumlah;
    @FXML private TableColumn<CartItemDisplay, Double> colSubtotal;
    
    @FXML private DatePicker dateSewa;
    @FXML private DatePicker dateKembali;
    @FXML private Label lblJumlahHari;
    @FXML private Label lblTotalHarga;
    @FXML private ComboBox<MetodePembayaran> comboMetodePembayaran;
    
    @FXML private Button btnUpdateJumlah;
    @FXML private Button btnRemove;
    @FXML private Button btnCheckout;
    @FXML private Button btnContinueShopping;
    @FXML private Button btnClearCart;
    
    private PenyewaanService penyewaanService;
    private PembayaranService pembayaranService;
    private CartItemDisplay selectedCartItem;
    
    @FXML
    public void initialize() {
        penyewaanService = PenyewaanService.getInstance();
        pembayaranService = PembayaranService.getInstance();
        
        setupTable();
        
        loadCartItems();
        
        comboMetodePembayaran.setItems(FXCollections.observableArrayList(MetodePembayaran.values()));
        comboMetodePembayaran.setValue(MetodePembayaran.TRANSFER_BANK);
        
        setupDatePickers();
        
        tableCart.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                selectedCartItem = newSelection;
                updateButtonStates();
            }
        );
        
        updateButtonStates();
        
        System.out.println("KeranjangController initialized");
    }
    
    private void setupTable() {
        colNamaBaju.setCellValueFactory(new PropertyValueFactory<>("namaBaju"));
        colUkuran.setCellValueFactory(new PropertyValueFactory<>("ukuran"));
        colHarga.setCellValueFactory(new PropertyValueFactory<>("harga"));
        colJumlah.setCellValueFactory(new PropertyValueFactory<>("jumlah"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        
        colHarga.setCellFactory(col -> new TableCell<CartItemDisplay, Double>() {
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
        
        colSubtotal.setCellFactory(col -> new TableCell<CartItemDisplay, Double>() {
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
    }
    
    private void setupDatePickers() {
        dateSewa.setValue(LocalDate.now());
        dateKembali.setValue(LocalDate.now().plusDays(2));
        
        dateSewa.valueProperty().addListener((obs, oldVal, newVal) -> calculateTotal());
        dateKembali.valueProperty().addListener((obs, oldVal, newVal) -> calculateTotal());
        
        calculateTotal();
    }
    
    private void loadCartItems() {
        Map<Integer, CartItemTemp> cartItems = CatalogBajuController.getCartItems();
        
        if (cartItems.isEmpty()) {
            lblTotalHarga.setText("Rp 0");
            return;
        }
        
        List<CartItemDisplay> displayItems = new ArrayList<>();
        
        for (CartItemTemp item : cartItems.values()) {
            CartItemDisplay display = new CartItemDisplay();
            display.setDetailBajuId(item.detailBaju.getDetailBajuId());
            display.setNamaBaju(item.detailBaju.getNamaBaju());
            display.setUkuran(item.detailBaju.getUkuranDisplay());
            display.setHarga(item.detailBaju.getHargaSewa());
            display.setJumlah(item.jumlah);
            display.setSubtotal(item.detailBaju.getHargaSewa() * item.jumlah);
            display.setCartItemTemp(item);
            
            displayItems.add(display);
        }
        
        tableCart.setItems(FXCollections.observableArrayList(displayItems));
        calculateTotal();
    }
    
    private void calculateTotal() {
        LocalDate start = dateSewa.getValue();
        LocalDate end = dateKembali.getValue();
        
        if (start == null || end == null) {
            lblJumlahHari.setText("0 hari");
            lblTotalHarga.setText("Rp 0");
            return;
        }
        
        long days = ChronoUnit.DAYS.between(start, end);
        
        if (days < 1) {
            lblJumlahHari.setText("Min 1 hari");
            days = 1;
        } else {
            lblJumlahHari.setText(days + " hari");
        }
        
        double total = 0;
        for (CartItemDisplay item : tableCart.getItems()) {
            total += item.getSubtotal() * days;
        }
        
        lblTotalHarga.setText(String.format("Rp %.0f", total));
    }
    
    @FXML
    private void handleUpdateJumlah() {
        if (selectedCartItem == null) {
            AlertUtil.showWarning("Pilih item yang akan diupdate");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(String.valueOf(selectedCartItem.getJumlah()));
        dialog.setTitle("Update Jumlah");
        dialog.setHeaderText("Update jumlah untuk: " + selectedCartItem.getNamaBaju());
        dialog.setContentText("Jumlah baru:");
        
        dialog.showAndWait().ifPresent(result -> {
            try {
                int newJumlah = Integer.parseInt(result);
                
                if (newJumlah <= 0) {
                    AlertUtil.showWarning("Jumlah harus lebih dari 0");
                    return;
                }
                
                selectedCartItem.getCartItemTemp().jumlah = newJumlah;
                loadCartItems();
                
                AlertUtil.showSuccess("Jumlah berhasil diupdate");
                
            } catch (NumberFormatException e) {
                AlertUtil.showError("Jumlah tidak valid");
            }
        });
    }
    
    @FXML
    private void handleRemove() {
        if (selectedCartItem == null) {
            AlertUtil.showWarning("Pilih item yang akan dihapus");
            return;
        }
        
        if (AlertUtil.showConfirmation("Hapus item dari keranjang?")) {
            Map<Integer, CartItemTemp> cartItems = CatalogBajuController.getCartItems();
            cartItems.remove(selectedCartItem.getDetailBajuId());
            
            loadCartItems();
            AlertUtil.showSuccess("Item berhasil dihapus");
        }
    }
    
    @FXML
    private void handleClearCart() {
        if (tableCart.getItems().isEmpty()) {
            AlertUtil.showWarning("Keranjang sudah kosong");
            return;
        }
        
        if (AlertUtil.showConfirmation("Hapus semua item dari keranjang?")) {
            CatalogBajuController.clearCart();
            loadCartItems();
            AlertUtil.showSuccess("Keranjang berhasil dikosongkan");
        }
    }
    
    @FXML
    private void handleCheckout() {
        if (tableCart.getItems().isEmpty()) {
            AlertUtil.showWarning("Keranjang kosong");
            return;
        }
        
        LocalDate tglSewa = dateSewa.getValue();
        LocalDate tglKembali = dateKembali.getValue();
        
        if (tglSewa == null || tglKembali == null) {
            AlertUtil.showWarning("Pilih tanggal sewa dan kembali");
            return;
        }
        
        if (tglKembali.isBefore(tglSewa)) {
            AlertUtil.showWarning("Tanggal kembali harus setelah tanggal sewa");
            return;
        }
        
        if (tglSewa.isBefore(LocalDate.now())) {
            AlertUtil.showWarning("Tanggal sewa tidak boleh di masa lalu");
            return;
        }
        
        MetodePembayaran metode = comboMetodePembayaran.getValue();
        if (metode == null) {
            AlertUtil.showWarning("Pilih metode pembayaran");
            return;
        }
        
        long days = ChronoUnit.DAYS.between(tglSewa, tglKembali);
        double total = Double.parseDouble(lblTotalHarga.getText().replace("Rp ", "").replace(",", ""));
        
        String confirmMsg = String.format(
            "Konfirmasi Checkout:\n\n" +
            "Tanggal Sewa: %s\n" +
            "Tanggal Kembali: %s\n" +
            "Lama Sewa: %d hari\n" +
            "Total Item: %d\n" +
            "Total Harga: Rp %.0f\n" +
            "Metode Pembayaran: %s\n\n" +
            "Lanjutkan?",
            DateUtil.formatDate(tglSewa),
            DateUtil.formatDate(tglKembali),
            days,
            tableCart.getItems().size(),
            total,
            metode.getDisplayName()
        );
        
        if (!AlertUtil.showConfirmation("Checkout", confirmMsg)) {
            return;
        }
        try {
            int userId = Session.getInstance().getCurrentUserId();
            
            List<PenyewaanService.CartItem> cartItems = new ArrayList<>();
            for (CartItemDisplay display : tableCart.getItems()) {
                cartItems.add(new PenyewaanService.CartItem(
                    display.getCartItemTemp().detailBaju,
                    display.getJumlah()
                ));
            }
            
            Penyewaan penyewaan = penyewaanService.createPenyewaan(
                userId, 
                cartItems, 
                tglSewa, 
                tglKembali
            );
            
            if (penyewaan == null) {
                AlertUtil.showError("Gagal membuat penyewaan");
                return;
            }
            if (metode.requiresProof()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Pembayaran");
                alert.setHeaderText("Checkout Berhasil!");
                alert.setContentText(
                    "Penyewaan berhasil dibuat!\n\n" +
                    "Silakan lakukan pembayaran:\n" +
                    metode.getInstructions() + "\n\n" +
                    "Upload bukti pembayaran di halaman berikutnya."
                );
                alert.showAndWait();
                
                redirectToPembayaran(penyewaan.getSewaId(), metode, penyewaan.getTotalHarga());
                
            } else {
                pembayaranService.createPembayaran(
                    penyewaan.getSewaId(),
                    metode,
                    penyewaan.getTotalHarga(),
                    null
                );
                
                AlertUtil.showSuccess(
                    "Checkout Berhasil!\n\n" +
                    "Penyewaan #" + penyewaan.getSewaId() + "\n" +
                    "Pembayaran: Cash\n\n" +
                    "Silakan ambil baju sesuai jadwal sewa."
                );
                
                CatalogBajuController.clearCart();
                
                navigateToPage(
                    "/com/mycompany/sewabaju/fxml/pelanggan/riwayat_sewa.fxml",
                    "Riwayat Sewa - SewaBaju"
                );
            }
            
        } catch (ValidationException e) {
            AlertUtil.showValidationError("Validasi", e.getMessage());
        } catch (StokTidakCukupException e) {
            AlertUtil.showStokTidakCukupError(e.getNamaBaju(), e.getStokTersedia());
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("checkout");
            e.printStackTrace();
        }
    }
    
    private void redirectToPembayaran(int sewaId, MetodePembayaran metode, double jumlah) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/mycompany/sewabaju/fxml/pelanggan/pembayaran.fxml")
            );
            Parent root = loader.load();
            
            PembayaranController controller = loader.getController();
            controller.setData(sewaId, metode, jumlah);
            
            Stage stage = (Stage) btnCheckout.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Pembayaran - SewaBaju");
            
        } catch (IOException e) {
            AlertUtil.showError("Gagal membuka halaman pembayaran");
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleContinueShopping() {
        navigateToPage(
            "/com/mycompany/sewabaju/fxml/pelanggan/catalog_baju.fxml",
            "Catalog Baju - SewaBaju"
        );
    }
    
    private void navigateToPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage stage = (Stage) btnCheckout.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            
        } catch (IOException e) {
            AlertUtil.showError("Gagal membuka halaman: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateButtonStates() {
        boolean hasSelection = selectedCartItem != null;
        btnUpdateJumlah.setDisable(!hasSelection);
        btnRemove.setDisable(!hasSelection);
    }
    
    public static class CartItemDisplay {
        private int detailBajuId;
        private String namaBaju;
        private String ukuran;
        private double harga;
        private int jumlah;
        private double subtotal;
        private CartItemTemp cartItemTemp;
        
        public int getDetailBajuId() { return detailBajuId; }
        public void setDetailBajuId(int detailBajuId) { this.detailBajuId = detailBajuId; }
        
        public String getNamaBaju() { return namaBaju; }
        public void setNamaBaju(String namaBaju) { this.namaBaju = namaBaju; }
        
        public String getUkuran() { return ukuran; }
        public void setUkuran(String ukuran) { this.ukuran = ukuran; }
        
        public double getHarga() { return harga; }
        public void setHarga(double harga) { this.harga = harga; }
        
        public int getJumlah() { return jumlah; }
        public void setJumlah(int jumlah) { this.jumlah = jumlah; }
        
        public double getSubtotal() { return subtotal; }
        public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
        
        public CartItemTemp getCartItemTemp() { return cartItemTemp; }
        public void setCartItemTemp(CartItemTemp cartItemTemp) { this.cartItemTemp = cartItemTemp; }
    }
}