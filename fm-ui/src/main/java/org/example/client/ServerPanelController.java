package org.example.client;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import org.example.common.ControllerRegistry;
import org.example.common.dto.BasicRequest;
import org.example.common.dto.GetFileListRequest;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

public class ServerPanelController implements Initializable {
    public static Path FULL_CLIENT_HOME_PATH;     // имеет вид: "C:\Java\fm\root-dir\dp"
    private final Network network = Network.getInstance();
    @FXML
    TableView<ServerDirInfo> filesTableR;
    @FXML
    TextField pathFieldR;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        ControllerRegistry.register(this);

        TableColumn<ServerDirInfo, String> filenameColumn = new TableColumn<>("Имя");
        filenameColumn.setCellValueFactory(param ->
                new SimpleStringProperty(param.getValue().getFilename()));
        filenameColumn.setPrefWidth(160);

        TableColumn<ServerDirInfo, Long> filesizeColumn = new TableColumn<>("Размер");
        filesizeColumn.setCellValueFactory(param ->
                new SimpleObjectProperty<>(param.getValue().getSize()));
        filesizeColumn.setPrefWidth(100);

        filesTableR.getColumns().addAll(filenameColumn, filesizeColumn);
        filesTableR.getSortOrder().add(filenameColumn);
        filesTableR.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(mouseEvent.getClickCount() == 2) {
                   ServerDirInfo selectedItem = filesTableR.getSelectionModel()
                           .getSelectedItem();
                   Path path = Paths.get(getCurrentPathR())
                            .resolve(selectedItem.getFilename());
                   try {
                       if(selectedItem.getSize() == 0) {
                           getServerFilesList(path.toString());
                       }
                   } catch (Exception e) {
                       Alert alert = new Alert(Alert.AlertType.WARNING, "Не удалось обновить список файлов", ButtonType.OK);
                       alert.showAndWait();
                   }
                }
            }
        });
    }
    public void getServerFilesList(String currentDir) {
        BasicRequest request = new GetFileListRequest(currentDir);
        try {
            network.sendRequest(request);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void updateServerFilesList(String currentClientDir, List<ServerDirInfo> serverFileList) {
        pathFieldR.setText(currentClientDir);
        filesTableR.getItems().clear();
        filesTableR.getItems().addAll(serverFileList);
        filesTableR.sort();
    }
    public void btnPathUpActionR(ActionEvent actionEvent) {
        Path upperPath = Paths.get(getCurrentPathR()).getParent();
        if (upperPath == null) {
            return;
        }
        getServerFilesList(upperPath.toString());
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