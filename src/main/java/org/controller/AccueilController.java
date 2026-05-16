package org.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class AccueilController {

    @FXML
    private Button connecteeButton; // Assure-toi que ton bouton est bien lié à ce champ

    @FXML
    void onConnecteeClick(ActionEvent event) {
        // Charger le fichier FXML pour la scène de connexion
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/utilisateur/login.fxml"));

        try {
            // Charger la scène et l'afficher dans une nouvelle fenêtre
            Scene loginScene = new Scene(loader.load(), 1920, 1000);
            loginScene.getStylesheets().add(getClass().getResource("/view/css/utilisateur/login/style.css").toExternalForm());  // Si tu utilises un fichier CSS

            Stage stage = (Stage) connecteeButton.getScene().getWindow(); // Récupère la fenêtre actuelle
            stage.setTitle("SkillSwap - Connexion");
            stage.setScene(loginScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace(); // Gérer l'erreur de chargement de FXML
        }
    }
}