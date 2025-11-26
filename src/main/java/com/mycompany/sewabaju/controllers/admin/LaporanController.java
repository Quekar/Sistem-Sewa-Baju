package com.mycompany.sewabaju.controllers.admin;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.models.Penyewaan;
import com.mycompany.sewabaju.services.PenyewaanService;
import com.mycompany.sewabaju.utils.AlertUtil;
import com.mycompany.sewabaju.utils.DateUtil;
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
import java.util.List;
public class LaporanController {

    @FXML private DatePicker dateStart;
    @FXML private DatePicker dateEnd;
    @FXML private ComboBox<String> comboJenisLaporan;
    @FXML private Button btnGenerate;
    @FXML private Button btnExport;
    @FXML private Button btnBack;

    @FXML private Label lblTotalPenyewaan;
    @FXML private Label lblTotalPendapatan;
    @FXML private Label lblRataRataPenyewaan;

    @FXML private TableView<Penyewaan> tableData;
    @FXML private TableColumn<Penyewaan, Integer> colId;
    @FXML private TableColumn<Penyewaan, String> colPelanggan;
    @FXML private TableColumn<Penyewaan, String> colTglSewa;
    @FXML private TableColumn<Penyewaan, String> colTglKembali;
    @FXML private TableColumn<Penyewaan, Double> colTotal;
    @FXML private TableColumn<Penyewaan, String> colStatus;
    
    private PenyewaanService penyewaanService;
    @FXML
    public void initialize() {
        penyewaanService = PenyewaanService.getInstance();

        setupTable();

        comboJenisLaporan.setItems(FXCollections.observableArrayList(
            "Laporan Penyewaan",
            "Laporan Pendapatan",
            "Laporan Baju Populer"
        ));
        comboJenisLaporan.setValue("Laporan Penyewaan");

        LocalDate today = LocalDate.now();
        dateStart.setValue(DateUtil.getStartOfMonth(today));
        dateEnd.setValue(DateUtil.getEndOfMonth(today));

        btnExport.setDisable(true);
        
        System.out.println("LaporanController initialized");
    }
    
    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("sewaId"));
        
        colPelanggan.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getNamaPelanggan()
            )
        );
        
        colTglSewa.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                DateUtil.formatDate(cellData.getValue().getTglSewa())
            )
        );
        
        colTglKembali.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                DateUtil.formatDate(cellData.getValue().getTglKembali())
            )
        );
        
        colTotal.setCellValueFactory(new PropertyValueFactory<>("totalHarga"));
        
        colStatus.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus().getDisplayName()
            )
        );
        colTotal.setCellFactory(col -> new TableCell<Penyewaan, Double>() {
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
    @FXML
    private void handleGenerate() {
        LocalDate start = dateStart.getValue();
        LocalDate end = dateEnd.getValue();

        if (start == null || end == null) {
            AlertUtil.showWarning("Pilih tanggal mulai dan akhir");
            return;
        }
        
        if (end.isBefore(start)) {
            AlertUtil.showWarning("Tanggal akhir harus setelah tanggal mulai");
            return;
        }
        
        try {
            String jenisLaporan = comboJenisLaporan.getValue();
            
            if ("Laporan Penyewaan".equals(jenisLaporan)) {
                generateLaporanPenyewaan(start, end);
            } else if ("Laporan Pendapatan".equals(jenisLaporan)) {
                generateLaporanPendapatan(start, end);
            } else {
                AlertUtil.showNotImplemented();
            }

            btnExport.setDisable(false);
            
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("generate laporan");
            e.printStackTrace();
        }
    }

    private void generateLaporanPenyewaan(LocalDate start, LocalDate end) throws DatabaseException {
        List<Penyewaan> penyewaanList = penyewaanService.getPenyewaanByUserId(0);
        List<Penyewaan> filtered = penyewaanList.stream()
            .filter(p -> !p.getTglSewa().isBefore(start) && !p.getTglSewa().isAfter(end))
            .toList();

        tableData.setItems(FXCollections.observableArrayList(filtered));

        int total = filtered.size();
        double totalPendapatan = filtered.stream()
            .mapToDouble(Penyewaan::getTotalHarga)
            .sum();
        
        long days = java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;
        double rataRata = days > 0 ? (double) total / days : 0;

        lblTotalPenyewaan.setText(String.valueOf(total));
        lblTotalPendapatan.setText(String.format("Rp %.0f", totalPendapatan));
        lblRataRataPenyewaan.setText(String.format("%.1f/hari", rataRata));
    }

    private void generateLaporanPendapatan(LocalDate start, LocalDate end) throws DatabaseException {

        double totalPendapatan = penyewaanService.getTotalPendapatan(start, end);
        int totalPenyewaan = penyewaanService.getTotalPenyewaan(start, end);

        lblTotalPenyewaan.setText(String.valueOf(totalPenyewaan));
        lblTotalPendapatan.setText(String.format("Rp %.0f", totalPendapatan));
        
        double rataRata = totalPenyewaan > 0 ? totalPendapatan / totalPenyewaan : 0;
        lblRataRataPenyewaan.setText(String.format("Rp %.0f/transaksi", rataRata));

        generateLaporanPenyewaan(start, end);
    }
    @FXML
    private void handleExport() {

        AlertUtil.showInfo(
            "Export Feature",
            "Export ke Excel/PDF akan diimplementasikan dengan:\n" +
            "- Apache POI untuk Excel\n" +
            "- iText untuk PDF\n\n" +
            "Untuk saat ini, gunakan Print Screen atau copy data dari tabel."
        );
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
}