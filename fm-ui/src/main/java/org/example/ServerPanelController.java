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
    private List<File> CURRENT_USER_SERVER_FILES = new ArrayList<>();
    private static Path fullClientCurrentPath;    // имеет вид: "C:\Java\fm\root-dir\dp\dp1"
    public static Path FULL_CLIENT_HOME_PATH;     // имеет вид: "C:\Java\fm\root-dir\dp"
    private final Network network = Network.getInstance();
    @FXML
    TableView<FileInfo> filesTableR;
    @FXML
    TextField pathFieldR;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        ControllerRegistry.register(this);
        TableColumn<FileInfo, String> fileLevelColumn = new TableColumn<>();
        fileLevelColumn.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getLevel().getName()));
        fileLevelColumn.setPrefWidth(20);

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

        filesTableR.getColumns().addAll(fileLevelColumn, filenameColumn, filesizeColumn, fileDateColumn);
        filesTableR.getSortOrder().add(fileLevelColumn);
        filesTableR.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getClickCount() == 2) {
                    // Данный блок работает только с локальной файловой системой
                    /*
                    Path path = Paths.get(pathFieldR.getText())
                            .resolve(filesTableR.getSelectionModel()
                                    .getSelectedItem().getFilename());
                    if (Files.isDirectory(path)) {
                        getServerFilesList(path);
                    }
                     */

                }
            }
        });

        //pathFieldR.setText("user");
    }
    public void getServerFilesList(Path path) {
        String currentDir = path.normalize().toAbsolutePath().toString();
        BasicRequest request = new GetFileListRequest(currentDir);
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

    public void updateServerFilesListFromString(String currentClientDir, List<File> serverFileList) {
        pathFieldR.setText(currentClientDir);
        filesTableR.getItems().clear();
        List<FileInfo> serverFileInfoList = serverFileList.stream()
                .map(File::toPath)
                .map(FileInfo::new)
                .collect(Collectors.toList());
        filesTableR.getItems().addAll(serverFileInfoList);
        filesTableR.sort();
    }

    public void btnPathUpActionR(ActionEvent actionEvent) {
        Path upperPath = Paths.get(pathFieldR.getText()).getParent();
        if (fullClientCurrentPath.equals(FULL_CLIENT_HOME_PATH)) {
            return;
        }
        if (upperPath == null) {
            return;
        }
        getServerFilesList(upperPath);
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