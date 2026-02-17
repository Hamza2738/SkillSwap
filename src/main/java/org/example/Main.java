package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.util.Optional;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // =========================
        // 1) DEMANDE DU ROLE
        // =========================
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Connexion");
        dialog.setHeaderText("Bienvenue sur SkillSwap");
        dialog.setContentText("Quel est votre rôle ? (admin / user)");

        Optional<String> result = dialog.showAndWait();

        // valeur par défaut
        String role = "USER";

        if (result.isPresent()) {
            role = result.get().trim().toUpperCase();
        }

        // =========================
        // 2) CHOIX DU FXML
        // =========================
        String fxmlPath;

        if (isBackRole(role)) {
            fxmlPath = "/view/fxml/Competences/back/competence.fxml";
        } else {
            fxmlPath = "/view/fxml/Competences/front/CompetenceFront.fxml";
        }

        // =========================
        // 3) LOAD SCENE
        // =========================
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Scene scene = new Scene(loader.load(), 1100, 650);

        scene.getStylesheets().add(
                getClass().getResource("/view/css/competences/style.css").toExternalForm()
        );

        stage.setTitle("SkillSwap - " + role);
        stage.setScene(scene);
        stage.show();
    }

    private boolean isBackRole(String role) {
        if (role == null) return false;

        String r = role.trim().toUpperCase();

        return r.equals("ADMIN")
                || r.equals("BACK")
                || r.equals("RH");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
