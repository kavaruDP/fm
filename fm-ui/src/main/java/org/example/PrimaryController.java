package org.example;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.netty.client.Network;
import org.example.netty.common.ControllerRegistry;
import org.example.netty.common.dto.AuthRequest;
import org.example.netty.common.dto.BasicRequest;
import org.example.netty.common.dto.RegRequest;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class PrimaryController implements Initializable {
    //Объявление для pattern singleton
    private final Network network = Network.getInstance();
    //Обычное объявление
    //private Network network;

    private Stage regStage;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;
    @FXML
    TextField resultField;
    @FXML
    public void btnExitAction(ActionEvent actionEvent) {
        network.close();
        Platform.exit();
    }
    @FXML
    private void btnAuth() throws IOException {
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login == null || login.isEmpty() || login.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Вы не указали логин", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        BasicRequest request = new AuthRequest(login, password);

        try {
            network.sendRequest(request);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //network = new Network();
        ControllerRegistry.register(this);
    }

    public void btnRegAction(ActionEvent actionEvent) {
        //RegistrationController.newWindow();
        String login = loginField.getText();
        String password = passwordField.getText();
        if (login == null || login.isEmpty() || login.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Вы не указали логин", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        BasicRequest request = new RegRequest(login, password);

        try {
            network.sendRequest(request);
        } catch (InterruptedException e) {
            System.out.println("Обработка метода network.sendRequest из PrimaryController привела к исключению");
            throw new RuntimeException(e);
        }
    }
    public void setResultField(String msg) {
        resultField.setText(msg);
    }
}