package org.example;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import org.example.netty.client.Network;
import org.example.netty.common.dto.AuthRequest;
import org.example.netty.common.dto.BasicRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SecondaryController {

    @FXML
    VBox leftPanel, rightPanel;
    @FXML
    LocalPanelController leftPC;
    @FXML
    ServerPanelController rightPC;
    @FXML
    public void initialize() {
        leftPC = (LocalPanelController) leftPanel.getUserData();
        rightPC = (ServerPanelController) rightPanel.getUserData();
        //BasicRequest request = new AuthRequest(log, pass);
        //network.sendMessage(request);
    }

    public void exitBtnAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void copyBtnAction(ActionEvent actionEvent) {
        // leftPanel - это ссылка на VBox

        if (leftPC.getSelectedFilenameL() == null && rightPC.getSelectedFilenameR() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Файл не выбран", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        Path srcPath = null, dstPath = null;
        if (leftPC.getSelectedFilenameL() != null) {
            srcPath = Paths.get(leftPC.getCurrentPathL(),leftPC.getSelectedFilenameL());
            dstPath = Paths.get(rightPC.getCurrentPathR()).resolve(srcPath.getFileName().toString());
        }
        if (rightPC.getSelectedFilenameR() != null) {
            srcPath = Paths.get(rightPC.getCurrentPathR(),rightPC.getSelectedFilenameR());
            dstPath = Paths.get(leftPC.getCurrentPathL()).resolve(srcPath.getFileName().toString());
        }

        try {
            // копирование с помощью nio
            Files.copy(srcPath,dstPath);
            leftPC.updateListL(Paths.get(leftPC.getCurrentPathL()));
            rightPC.updateListR(Paths.get(rightPC.getCurrentPathR()));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Ошибка при копировании.", ButtonType.OK);
            alert.showAndWait();
        }
    }

}