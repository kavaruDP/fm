package org.example.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import org.example.common.ControllerRegistry;
import org.example.common.dto.BasicRequest;
import org.example.common.dto.GetFileRequest;
import org.example.common.dto.PutFileRequest;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class SecondaryController implements Initializable {

    @FXML
    VBox leftPanel, rightPanel;
    @FXML
    LocalPanelController leftPC;
    @FXML
    ServerPanelController rightPC;
    @FXML
    public void initialize2() {

    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ControllerRegistry.register(this);
        leftPC = (LocalPanelController) leftPanel.getUserData();
        rightPC = (ServerPanelController) rightPanel.getUserData();
    }
    private final Network network = Network.getInstance();
    public void exitBtnAction(ActionEvent actionEvent) {
        network.close();
        Platform.exit();
    }

    public void copyBtnAction(ActionEvent actionEvent) throws IOException {

        if (leftPC.getSelectedFilenameL() == null && rightPC.getSelectedFilenameR() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Файл не выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        //  Выбрана загрузка файла на сервер
        if (leftPC.getSelectedFilenameL() != null) {
            Path srcPath = Paths.get(leftPC.getCurrentPathL(),leftPC.getSelectedFilenameL());
            if(Files.size(srcPath) < 20000) {
                FileMessage fileMessage = new FileMessage(srcPath);
                BasicRequest request = new PutFileRequest(fileMessage,leftPC.getCurrentPathL(),rightPC.getCurrentPathR());
                try {
                    network.sendRequest(request);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Превышен размер файла.", ButtonType.OK);
                alert.showAndWait();
            }
        }

        //  Выбрана загрузка файла с сервера
        if (rightPC.getSelectedFilenameR() != null) {
            BasicRequest request = new GetFileRequest(
                    rightPC.getSelectedFilenameR(),
                    rightPC.getCurrentPathR(),
                    leftPC.getCurrentPathL()
            );
            try {
                network.sendRequest(request);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

}