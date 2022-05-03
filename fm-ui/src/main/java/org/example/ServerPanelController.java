package org.example;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.example.netty.client.Network;
import org.example.netty.common.ControllerRegistry;
import org.example.netty.common.dto.BasicRequest;
import org.example.netty.common.dto.GetFileListRequest;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ServerPanelController implements Initializable {
    public static List<File> CURRENT_USER_SERVER_FILES = new ArrayList<>();
    public static Path CURRENT_USER_SERVER_DIR;
    private final Network network = Network.getInstance();
    @FXML
    TableView<FileInfo> filesTableR;
    @FXML
    TextField pathFieldR;

    private Path upperCatalogName;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ControllerRegistry.register(this);
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

        //updateListR(Paths.get(".","root-dir"));
        //updateServerFilesList(CURRENT_USER_SERVER_DIR, CURRENT_USER_SERVER_FILES);
        //upperCatalogName = Paths.get(pathFieldR.getText());
        upperCatalogName = CURRENT_USER_SERVER_DIR;
    }
//  TODO Реализовать через сетевое взаимодействие (метод получения списка файлов из текущего каталога на сервере)
    public void getServerFilesList(Path path) {
        String currentDir = path.normalize().toAbsolutePath().toString();
        BasicRequest request = new GetFileListRequest();
        try {
            network.sendRequest(request);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
//  TODO адаптировать метод под сетевое взаимодействие
    public void updateListR(Path path) {
        try {
            pathFieldR.setText(path.normalize().toAbsolutePath().toString());
            filesTableR.getItems().clear();
            filesTableR.getItems().addAll(Files.list(path)
                    .map(FileInfo::new)
                    .collect(Collectors.toList()));
            filesTableR.sort();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов", ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void updateServerFilesList(Path path, List<File> serverFileList) {
        pathFieldR.setText(path.normalize().toAbsolutePath().toString());
        filesTableR.getItems().clear();
        List<FileInfo> serverFileInfoList = serverFileList.stream()
                .map(File::toPath)
                .map(FileInfo::new)
                .collect(Collectors.toList());
        filesTableR.getItems().addAll(serverFileInfoList);
        filesTableR.sort();
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
//    TODO убрать метод
    public void renderServerFileList(List<File> serverItemList) {
        updateServerFilesList(Paths.get(".", "root-dir"), serverItemList);
    }
}