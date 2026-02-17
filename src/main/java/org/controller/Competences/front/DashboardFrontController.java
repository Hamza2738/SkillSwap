package org.controller.Competences.front;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

public class DashboardFrontController {

    @FXML
    private void onClose(javafx.event.ActionEvent e) {
        Node src = (Node) e.getSource();
        Stage stage = (Stage) src.getScene().getWindow();
        stage.close();
    }
}
