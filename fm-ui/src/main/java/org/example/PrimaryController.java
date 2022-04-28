package org.example;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import org.example.netty.client.Network;
import org.example.netty.common.dto.AuthRequest;
import org.example.netty.common.dto.BasicRequest;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PrimaryController implements Initializable {
    private Network network;
    @FXML
    TextField login, password;
    @FXML
    public void btnExitAction(ActionEvent actionEvent) {
        network.close();
        Platform.exit();
    }
    @FXML
    private void btnAuth() throws IOException {
        String log = login.getText();
        String pass = password.getText();
        if (log == null || log.isEmpty() || log.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Вы не указали логин", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        BasicRequest request = new AuthRequest(log, pass);
        network.sendRequest(request);
        App.setRoot("secondary");
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        network = new Network();
        //network = new Network((args) -> textLogger.appendText((String) args[0]));
    }
}