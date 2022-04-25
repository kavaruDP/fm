package org.example;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ServerPanelController implements Initializable {
    @FXML
    TableView<FileInfo> filesTableR;
    @FXML
    TextField pathFieldR;

    private Path upperCatalogName;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getLevel().getName()));
        fileTypeColumn.setPrefWidth(20);

        TableColumn<FileInfo, String> filenameColumn = new TableColumn<>("Имя");
        filenameColumn.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getFilename()));
        filenameColumn.setPrefWidth(160);

        TableColumn<FileInfo, Long> filesizeColumn = new TableColumn<>("Размер");
        filesizeColumn.setCellValueFactory(param ->
                new SimpleObjectProperty<>(param.getValue().getSize()));
        filesizeColumn.setPrefWidth(100);
        filesizeColumn.setCellFactory(param -> {
            return new TableCell<FileInfo, Long>() {
                @Override
                protected void updateItem(Long item, boolean empty) {
                    super.updateItem(item,empty);
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        String text = String.format("%,d bytes",item);
                        if (item == -1L) {
                            text = "[DIR]";
                        }
                        setText(text);
                    }
                }
            };
        });

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfo, String> fileDateColumn = new TableColumn<>("Дата изменения");
        fileDateColumn.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getLastModified().format(dtf)));
        fileDateColumn.setPrefWidth(120);

        filesTableR.getColumns().addAll(fileTypeColumn, filenameColumn, filesizeColumn, fileDateColumn);
        filesTableR.getSortOrder().add(fileTypeColumn);
        filesTableR.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getClickCount() == 2) {
                    // Paths.get(pathField.getText()) = корень текущего каталога
                    // filesTable.getSelectionModel()... = имя выбранного файла
                    // ...resolve... = склейка пути и файла
                    Path path = Paths.get(pathFieldR.getText()).resolve(filesTableR.getSelectionModel().getSelectedItem().getFilename());
                    if (Files.isDirectory(path)) {
                        updateListR(path);
                    }
                }
            }
        });

        updateListR(Paths.get(".","Server"));
        upperCatalogName = Paths.get(pathFieldR.getText());
    }

    public void updateListR(Path path) {
        try {
            pathFieldR.setText(path.normalize().toAbsolutePath().toString());
            filesTableR.getItems().clear();
            filesTableR.getItems().addAll(Files.list(path).map(FileInfo::new).collect(Collectors.toList()));
            filesTableR.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void btnPathUpActionR(ActionEvent actionEvent) {
        Path currentPath = Paths.get(pathFieldR.getText());
        Path upperPath = Paths.get(pathFieldR.getText()).getParent();
        if (currentPath.equals(upperCatalogName)) {
            return;
        }
        if (upperPath == null) {
            return;
        }
        updateListR(upperPath);
    }

    public String getSelectedFilenameR() {
        if (filesTableR == null) {
            return null;
        }

        if (!filesTableR.isFocused()) {
            return null;
        }
        return filesTableR.getSelectionModel().getSelectedItem().getFilename();
    }

    public String getCurrentPathR() {
        return pathFieldR.getText();
    }
}