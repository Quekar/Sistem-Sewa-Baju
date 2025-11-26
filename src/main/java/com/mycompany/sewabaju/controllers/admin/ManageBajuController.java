package com.mycompany.sewabaju.controllers.admin;

import com.mycompany.sewabaju.exceptions.DatabaseException;
import com.mycompany.sewabaju.exceptions.ValidationException;
import com.mycompany.sewabaju.models.Baju;
import com.mycompany.sewabaju.models.DetailBaju;
import com.mycompany.sewabaju.models.Kategori;
import com.mycompany.sewabaju.models.enums.Kondisi;
import com.mycompany.sewabaju.models.enums.Ukuran;
import com.mycompany.sewabaju.services.BajuService;
import com.mycompany.sewabaju.utils.AlertUtil;
import com.mycompany.sewabaju.utils.FileUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ManageBajuController {
    @FXML private TableView<Baju> tableBaju;
    @FXML private TableColumn<Baju, Integer> colId;
    @FXML private TableColumn<Baju, String> colNama;
    @FXML private TableColumn<Baju, String> colKategori;
    @FXML private TableColumn<Baju, String> colHarga;
    @FXML private TableColumn<Baju, Integer> colStok;

    @FXML private TextField searchField;
    @FXML private ComboBox<Kategori> filterKategori;
    @FXML private Button btnSearch;
    @FXML private Button btnResetFilter;

    @FXML private ComboBox<Kategori> comboKategori;
    @FXML private TextField namaBajuField;
    @FXML private TextArea deskripsiArea;
    @FXML private ImageView imgPreview;
    @FXML private Button btnUploadFoto;
    @FXML private Label lblFotoName;

    @FXML private ComboBox<Ukuran> comboUkuran;
    @FXML private TextField hargaField;
    @FXML private TextField stokField;
    @FXML private ComboBox<Kondisi> comboKondisi;
    @FXML private Button btnTambahUkuran;
    @FXML private TableView<DetailBaju> tableDetailBaju;
    @FXML private TableColumn<DetailBaju, String> colUkuran;
    @FXML private TableColumn<DetailBaju, Double> colHargaSewa;
    @FXML private TableColumn<DetailBaju, Integer> colStokUkuran;
    @FXML private TableColumn<DetailBaju, String> colKondisi;

    @FXML private Button btnSave;
    @FXML private Button btnCancel;
    @FXML private Button btnEdit;
    @FXML private Button btnDelete;
    @FXML private Button btnBack;
    @FXML private Label lblFormTitle;
    
    private BajuService bajuService;
    private Baju selectedBaju;
    private File selectedFotoFile;
    private List<DetailBaju> tempDetailBajuList;
    private boolean isEditMode = false;
    @FXML
    public void initialize() {
        bajuService = BajuService.getInstance();
        tempDetailBajuList = new ArrayList<>();

        setupBajuTable();
        setupDetailBajuTable();

        loadKategoriComboBox();

        comboUkuran.setItems(FXCollections.observableArrayList(Ukuran.values()));
        comboKondisi.setItems(FXCollections.observableArrayList(Kondisi.values()));
        comboKondisi.setValue(Kondisi.BAIK);

        loadAllBaju();

        tableBaju.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                selectedBaju = newSelection;
                updateButtonStates();
            }
        );

        updateButtonStates();
        
        System.out.println("ManageBajuController initialized");
    }

    private void setupBajuTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("bajuId"));
        colNama.setCellValueFactory(new PropertyValueFactory<>("namaBaju"));
        colKategori.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNamaKategori())
        );
        colHarga.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getRangeHarga())
        );
        colStok.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getTotalStok()).asObject()
        );
        
        colId.setPrefWidth(60);
        colNama.setPrefWidth(200);
        colKategori.setPrefWidth(120);
        colHarga.setPrefWidth(150);
        colStok.setPrefWidth(80);
    }

    private void setupDetailBajuTable() {
        colUkuran.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getUkuran().getCode()
            )
        );
        colHargaSewa.setCellValueFactory(new PropertyValueFactory<>("hargaSewa"));
        colStokUkuran.setCellValueFactory(new PropertyValueFactory<>("stok"));
        colKondisi.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getKondisi().getDisplayName()
            )
        );

        colHargaSewa.setCellFactory(col -> new TableCell<DetailBaju, Double>() {
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

        TableColumn<DetailBaju, Void> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(80);
        colAction.setCellFactory(col -> new TableCell<DetailBaju, Void>() {
            private final Button btnRemove = new Button("Hapus");
            
            {
                btnRemove.setOnAction(event -> {
                    DetailBaju detail = getTableView().getItems().get(getIndex());
                    handleRemoveUkuran(detail);
                });
                btnRemove.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnRemove);
                }
            }
        });
        
        tableDetailBaju.getColumns().add(colAction);
    }

    private void loadKategoriComboBox() {
        try {
            List<Kategori> kategoriList = bajuService.getAllKategori();
            
            comboKategori.setItems(FXCollections.observableArrayList(kategoriList));
            filterKategori.setItems(FXCollections.observableArrayList(kategoriList));

            Kategori allOption = new Kategori();
            allOption.setKategoriId(0);
            allOption.setNamaKategori("Semua Kategori");
            filterKategori.getItems().add(0, allOption);
            filterKategori.setValue(allOption);
            
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("memuat kategori");
            e.printStackTrace();
        }
    }

    private void loadAllBaju() {
        try {
            List<Baju> bajuList = bajuService.getAllBaju();
            tableBaju.setItems(FXCollections.observableArrayList(bajuList));
            
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("memuat baju");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSearch() {
        try {
            String keyword = searchField.getText().trim();
            Kategori selectedKategori = filterKategori.getValue();
            
            List<Baju> result;
            
            if (!keyword.isEmpty()) {
                result = bajuService.searchBaju(keyword);
            } else {
                result = bajuService.getAllBaju();
            }
            if (selectedKategori != null && selectedKategori.getKategoriId() > 0) {
                result = result.stream()
                    .filter(b -> b.getKategoriId() == selectedKategori.getKategoriId())
                    .collect(Collectors.toList());
            }
            
            tableBaju.setItems(FXCollections.observableArrayList(result));
            
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("search baju");
            e.printStackTrace();
        }
    }
    @FXML
    private void handleResetFilter() {
        searchField.clear();
        filterKategori.setValue(filterKategori.getItems().get(0));
        loadAllBaju();
    }
    @FXML
    private void handleAdd() {
        clearForm();
        isEditMode = false;
        lblFormTitle.setText("Tambah Baju Baru");
        namaBajuField.requestFocus();
    }
    @FXML
    private void handleEdit() {
        if (selectedBaju == null) {
            AlertUtil.showWarning("Pilih baju yang akan diedit");
            return;
        }
        
        isEditMode = true;
        lblFormTitle.setText("Edit Baju");
 
        comboKategori.setValue(selectedBaju.getKategori());
        namaBajuField.setText(selectedBaju.getNamaBaju());
        deskripsiArea.setText(selectedBaju.getDeskripsi());

        if (selectedBaju.getFoto() != null) {
            lblFotoName.setText(selectedBaju.getFoto());
            loadImagePreview(selectedBaju.getFoto());
        }

        tempDetailBajuList = new ArrayList<>(selectedBaju.getDetailBajuList());
        tableDetailBaju.setItems(FXCollections.observableArrayList(tempDetailBajuList));
        
        namaBajuField.requestFocus();
    }

    @FXML
    private void handleUploadFoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Pilih Foto Baju");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File file = fileChooser.showOpenDialog(btnUploadFoto.getScene().getWindow());
        
        if (file != null) {
            selectedFotoFile = file;
            lblFotoName.setText(file.getName());

            try {
                Image image = new Image(file.toURI().toString());
                imgPreview.setImage(image);
            } catch (Exception e) {
                AlertUtil.showError("Gagal load preview image");
            }
        }
    }
    @FXML
    private void handleTambahUkuran() {
        Ukuran ukuran = comboUkuran.getValue();
        String hargaStr = hargaField.getText().trim();
        String stokStr = stokField.getText().trim();
        Kondisi kondisi = comboKondisi.getValue();

        if (ukuran == null) {
            AlertUtil.showWarning("Pilih ukuran");
            return;
        }
        
        if (hargaStr.isEmpty() || stokStr.isEmpty()) {
            AlertUtil.showWarning("Harga dan stok harus diisi");
            return;
        }
        
        try {
            double harga = Double.parseDouble(hargaStr);
            int stok = Integer.parseInt(stokStr);
            
            if (harga <= 0 || stok < 0) {
                AlertUtil.showWarning("Harga harus > 0 dan stok harus >= 0");
                return;
            }
            boolean exists = tempDetailBajuList.stream()
                .anyMatch(d -> d.getUkuran() == ukuran);
            
            if (exists) {
                AlertUtil.showWarning("Ukuran " + ukuran + " sudah ada");
                return;
            }

            DetailBaju detail = new DetailBaju();
            detail.setUkuran(ukuran);
            detail.setHargaSewa(harga);
            detail.setStok(stok);
            detail.setKondisi(kondisi);
            
            tempDetailBajuList.add(detail);
            tableDetailBaju.setItems(FXCollections.observableArrayList(tempDetailBajuList));

            comboUkuran.setValue(null);
            hargaField.clear();
            stokField.clear();
            comboKondisi.setValue(Kondisi.BAIK);
            
        } catch (NumberFormatException e) {
            AlertUtil.showError("Format harga atau stok tidak valid");
        }
    }

    private void handleRemoveUkuran(DetailBaju detail) {
        tempDetailBajuList.remove(detail);
        tableDetailBaju.setItems(FXCollections.observableArrayList(tempDetailBajuList));
    }

    @FXML
    private void handleSave() {
        Kategori kategori = comboKategori.getValue();
        String nama = namaBajuField.getText().trim();
        String deskripsi = deskripsiArea.getText().trim();
        if (kategori == null) {
            AlertUtil.showValidationError("Kategori", "Kategori harus dipilih");
            return;
        }
        
        if (nama.isEmpty()) {
            AlertUtil.showValidationError("Nama Baju", "Nama baju tidak boleh kosong");
            return;
        }
        
        if (tempDetailBajuList.isEmpty()) {
            AlertUtil.showWarning("Minimal tambahkan 1 ukuran");
            return;
        }
        
        try {
            if (isEditMode) {
                selectedBaju.setKategoriId(kategori.getKategoriId());
                selectedBaju.setNamaBaju(nama);
                selectedBaju.setDeskripsi(deskripsi);
                
                boolean updated = bajuService.updateBaju(selectedBaju, selectedFotoFile);
                
                if (updated) {
                    // Update detail baju (simple approach: delete all, insert new)
                    // TODO: Better approach with proper update logic
                    
                    AlertUtil.showSuccess("Baju berhasil diupdate");
                    clearForm();
                    loadAllBaju();
                } else {
                    AlertUtil.showError("Gagal update baju");
                }
                
            } else {
                Baju baju = new Baju();
                baju.setKategoriId(kategori.getKategoriId());
                baju.setNamaBaju(nama);
                baju.setDeskripsi(deskripsi);
                
                Baju created = bajuService.createBaju(baju, tempDetailBajuList, selectedFotoFile);
                
                if (created != null) {
                    AlertUtil.showSuccess("Baju berhasil ditambahkan");
                    clearForm();
                    loadAllBaju();
                } else {
                    AlertUtil.showError("Gagal menambah baju");
                }
            }
            
        } catch (ValidationException e) {
            AlertUtil.showValidationError("Validasi", e.getMessage());
        } catch (DatabaseException e) {
            AlertUtil.showDatabaseError("menyimpan baju");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedBaju == null) {
            AlertUtil.showWarning("Pilih baju yang akan dihapus");
            return;
        }
        
        if (AlertUtil.showDeleteConfirmation("baju '" + selectedBaju.getNamaBaju() + "'")) {
            try {
                boolean deleted = bajuService.deleteBaju(selectedBaju.getBajuId());
                
                if (deleted) {
                    AlertUtil.showSuccess("Baju berhasil dihapus");
                    clearForm();
                    loadAllBaju();
                } else {
                    AlertUtil.showError("Gagal menghapus baju");
                }
                
            } catch (DatabaseException e) {
                AlertUtil.showDatabaseError("menghapus baju");
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
            AlertUtil.showError("Gagal kembali ke dashboard");
            e.printStackTrace();
        }
    }

    private void clearForm() {
        comboKategori.setValue(null);
        namaBajuField.clear();
        deskripsiArea.clear();
        imgPreview.setImage(null);
        lblFotoName.setText("Belum ada foto");
        selectedFotoFile = null;
        
        comboUkuran.setValue(null);
        hargaField.clear();
        stokField.clear();
        comboKondisi.setValue(Kondisi.BAIK);
        
        tempDetailBajuList.clear();
        tableDetailBaju.setItems(FXCollections.observableArrayList(tempDetailBajuList));
        
        selectedBaju = null;
        isEditMode = false;
        lblFormTitle.setText("Tambah Baju Baru");
        tableBaju.getSelectionModel().clearSelection();
        updateButtonStates();
    }

    private void loadImagePreview(String filename) {
        try {
            String path = FileUtil.getBajuPhotoPath(filename);
            if (FileUtil.fileExists(path)) {
                byte[] imageData = FileUtil.readFileAsBytes(path);
                Image image = new Image(new ByteArrayInputStream(imageData));
                imgPreview.setImage(image);
            }
        } catch (Exception e) {
            System.err.println("Error loading image preview: " + e.getMessage());
        }
    }

    private void updateButtonStates() {
        boolean hasSelection = selectedBaju != null;
        btnEdit.setDisable(!hasSelection);
        btnDelete.setDisable(!hasSelection);
    }
}