package com.mycompany.sewabaju.utils;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.function.Function;

public class TableUtil {
    
    public static <T> TableColumn<T, String> createStringColumn(
            String title, String propertyName, double width) {
        
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(width);
        return column;
    }
    
    public static <T> TableColumn<T, Integer> createIntegerColumn(
            String title, String propertyName, double width) {
        
        TableColumn<T, Integer> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(width);
        return column;
    }
    
    public static <T> TableColumn<T, Double> createDoubleColumn(
            String title, String propertyName, double width) {
        
        TableColumn<T, Double> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(width);
        return column;
    }
    
    public static <T> TableColumn<T, Double> createPriceColumn(
            String title, String propertyName, double width) {
        
        TableColumn<T, Double> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(width);
        
        column.setCellFactory(col -> new TableCell<T, Double>() {
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
        
        return column;
    }
    
    public static <T> TableColumn<T, java.time.LocalDate> createDateColumn(
            String title, String propertyName, double width) {
        
        TableColumn<T, java.time.LocalDate> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(width);
        
        column.setCellFactory(col -> new TableCell<T, java.time.LocalDate>() {
            @Override
            protected void updateItem(java.time.LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(DateUtil.formatDate(date));
                }
            }
        });
        
        return column;
    }
    
    public static <T> TableColumn<T, java.time.LocalDateTime> createDateTimeColumn(
            String title, String propertyName, double width) {
        
        TableColumn<T, java.time.LocalDateTime> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(width);
        
        column.setCellFactory(col -> new TableCell<T, java.time.LocalDateTime>() {
            @Override
            protected void updateItem(java.time.LocalDateTime dateTime, boolean empty) {
                super.updateItem(dateTime, empty);
                if (empty || dateTime == null) {
                    setText(null);
                } else {
                    setText(DateUtil.formatDateTime(dateTime));
                }
            }
        });
        
        return column;
    }
    
    public static <T, S extends Enum<S>> TableColumn<T, S> createStatusColumn(
            String title, String propertyName, double width) {
        
        TableColumn<T, S> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(width);
        
        column.setCellFactory(col -> new TableCell<T, S>() {
            @Override
            protected void updateItem(S status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.toString());
                    String statusName = status.name().toLowerCase();
                    if (statusName.contains("berhasil") || statusName.contains("dikembalikan") || 
                        statusName.contains("sudah")) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else if (statusName.contains("pending") || statusName.contains("menunggu")) {
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    } else if (statusName.contains("ditolak") || statusName.contains("dibatalkan") || 
                               statusName.contains("belum")) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else if (statusName.contains("dikonfirmasi") || statusName.contains("sedang")) {
                        setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        return column;
    }
    
    public static <T> TableColumn<T, String> createImageColumn(
            String title, String propertyName, double width) {
        
        TableColumn<T, String> column = new TableColumn<>(title);
        column.setCellValueFactory(new PropertyValueFactory<>(propertyName));
        column.setPrefWidth(width);
        column.setCellFactory(col -> new TableCell<T, String>() {
            private final ImageView imageView = new ImageView();
            
            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
            }
            
            @Override
            protected void updateItem(String imagePath, boolean empty) {
                super.updateItem(imagePath, empty);
                if (empty || imagePath == null) {
                    setGraphic(null);
                } else {
                    try {
                        String fullPath = FileUtil.getBajuPhotoPath(imagePath);
                        if (FileUtil.fileExists(fullPath)) {
                            byte[] imageData = FileUtil.readFileAsBytes(fullPath);
                            Image image = new Image(new ByteArrayInputStream(imageData));
                            imageView.setImage(image);
                            setGraphic(imageView);
                        } else {
                            setText("No Image");
                            setGraphic(null);
                        }
                    } catch (Exception e) {
                        setText("Error");
                        setGraphic(null);
                    }
                }
            }
        });
        
        return column;
    }
    
    public static <T> TableColumn<T, Void> createActionColumn(
            String title, String buttonText, Function<T, Void> action, double width) {
        
        TableColumn<T, Void> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        
        column.setCellFactory(col -> new TableCell<T, Void>() {
            private final Button button = new Button(buttonText);
            
            {
                button.setOnAction(event -> {
                    T item = getTableView().getItems().get(getIndex());
                    action.apply(item);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(button);
                }
            }
        });
        
        return column;
    }
    
    public static <T> ObservableList<T> toObservableList(List<T> list) {
        return FXCollections.observableArrayList(list);
    }
    
    public static <T> void setItems(TableView<T> tableView, List<T> items) {
        tableView.setItems(toObservableList(items));
    }
    
    public static <T> void refresh(TableView<T> tableView) {
        tableView.refresh();
    }
    
    public static <T> T getSelectedItem(TableView<T> tableView) {
        return tableView.getSelectionModel().getSelectedItem();
    }
    
    public static <T> boolean hasSelection(TableView<T> tableView) {
        return tableView.getSelectionModel().getSelectedItem() != null;
    }
    
    public static <T> void clearSelection(TableView<T> tableView) {
        tableView.getSelectionModel().clearSelection();
    }
}