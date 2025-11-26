package com.mycompany.sewabaju.controllers.admin;

import com.mycompany.sewabaju.dao.KategoriDAO;
import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.exceptions.ValidationException;
import com.mycompany.sewabaju.models.Kategori;
import com.mycompany.sewabaju.services.BajuService;
import com.mycompany.sewabaju.utils.AlertUtil;
import com.mycompany.sewabaju.utils.TableUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ManageKategoriController {
    @FXML private TableView<Kategori> tableKategori;
    @FXML private TableColumn<Kategori, Integer> colId;
    @FXML private TableColumn<Kategori, String> colNama;
    @FXML private TableColumn<Kategori, String> colDeskripsi;

    @FXML private TextField namaField;
    @FXML private TextArea deskripsiArea;
    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Label lblFormTitle;

    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnBack;
    
    private BajuService bajuService;
    private KategoriDAO kategoriDAO;
    private Kategori selectedKategori;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        bajuService = BajuService.getInstance();
        kategoriDAO = new KategoriDAO();

        setupTable();
        loadKategori();

        tableKategori.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                selectedKategori = newSelection;
                updateButtonStates();
            }
        );
        updateButtonStates();     
        System.out.println("ManageKategoriController initialized");
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("kategoriId"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaKategori"));
        colDeskripsi.setCellValueFactory(new PropertyValueFactory<>("deskripsi"));

        colId.setPrefWidth(80);
        colNama.setPrefWidth(200);
        colDeskripsi.setPrefWidth(400);
    }

    private void loadKategori() {
        try {
            List<Kategori> kategoriList = bajuService.getAllKategori();
            tableKategori.setItems(FXCollections.observableArrayList(kategoriList));
            
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("memuat kategori");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAdd() {
        clearForm();
        isEditMode = false;
        lblFormTitle.setText("Tambah Kategori Baru");
        namaField.requestFocus();
    }

    @FXML
    private void handleEdit() {
        if (selectedKategori == null) {
            AlertUtil.showWarning("Pilih kategori yang akan diedit");
            return;
        }
        
        isEditMode = true;
        lblFormTitle.setText("Edit Kategori");

        namaField.setText(selectedKategori.getNamaKategori());
        deskripsiArea.setText(selectedKategori.getDeskripsi());
        namaField.requestFocus();
    }

    @FXML
    private void handleSave() {
        String nama = namaField.getText().trim();
        String deskripsi = deskripsiArea.getText().trim();

        if (nama.isEmpty()) {
            AlertUtil.showValidationError("Nama Kategori", "Nama kategori tidak boleh kosong");
            namaField.requestFocus();
            return;
        }
        
        try {
            if (isEditMode) {
                selectedKategori.setNamaKategori(nama);
                selectedKategori.setDeskripsi(deskripsi);
                
                boolean updated = kategoriDAO.update(selectedKategori);
                
                if (updated) {
                    AlertUtil.showSuccess("Kategori berhasil diupdate");
                    clearForm();
                    loadKategori();
                } else {
                    AlertUtil.showError("Gagal update kategori");
                }
                
            } else {
                Kategori kategori = new Kategori(nama, deskripsi);
                
                int id = kategoriDAO.save(kategori);
                
                if (id > 0) {
                    AlertUtil.showSuccess("Kategori berhasil ditambahkan");
                    clearForm();
                    loadKategori();
                } else {
                    AlertUtil.showError("Gagal menambah kategori");
                }
            }
            
        } catch (DatabaseException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                AlertUtil.showError("Nama kategori sudah ada");
            } else {
                AlertUtil.showDatabaseError("menyimpan kategori");
            }
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleDelete() {
        if (selectedKategori == null) {
            AlertUtil.showWarning("Pilih kategori yang akan dihapus");
            return;
        }
        try {
            int bajuCount = kategoriDAO.countBajuInKategori(selectedKategori.getKategoriId());
            
            if (bajuCount > 0) {
                AlertUtil.showWarning(
                    "Tidak dapat menghapus kategori",
                    "Kategori ini memiliki " + bajuCount + " baju.\n" +
                    "Hapus atau pindahkan baju terlebih dahulu."
                );
                return;
            }
            
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("cek kategori");
            return;
        }
        if (AlertUtil.showDeleteConfirmation("kategori '" + selectedKategori.getNamaKategori() + "'")) {
            try {
                boolean deleted = kategoriDAO.delete(selectedKategori.getKategoriId());
                
                if (deleted) {
                    AlertUtil.showSuccess("Kategori berhasil dihapus");
                    clearForm();
                    loadKategori();
                } else {
                    AlertUtil.showError("Gagal menghapus kategori");
                }
                
            } catch (DatabaseException e) {
                AlertUtil.showDatabaseError("menghapus kategori");
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCancel() {
        clearForm();
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
            AlertUtil.showError("Gagal kembali ke dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearForm() {
        namaField.clear();
        deskripsiArea.clear();
        selectedKategori = null;
        isEditMode = false;
        lblFormTitle.setText("Tambah Kategori Baru");
        tableKategori.getSelectionModel().clearSelection();
        updateButtonStates();
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedKategori != null;
        btnEdit.setDisable(!hasSelection);
        btnDelete.setDisable(!hasSelection);
    }
}