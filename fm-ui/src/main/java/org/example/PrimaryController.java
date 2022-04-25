package org.example;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import java.io.IOException;

public class PrimaryController {

    @FXML
    public void btnExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }
    @FXML
    private void switchToSecondary() throws IOException {
        App.setRoot("secondary");
    }
}