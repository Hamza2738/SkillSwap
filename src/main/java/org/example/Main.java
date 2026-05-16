package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Charger le fichier FXML de l'accueil
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/view/fxml/accueil.fxml") // Remplacer par accueil.fxml
        );

        // Créer la scène avec l'interface d'accueil
        Scene scene = new Scene(
                loader.load(),1920 , 1000 // Vous pouvez ajuster la taille de la scène ici
        );

        // Appliquer le fichier CSS
        scene.getStylesheets().add(getClass().getResource("/view/css/style.css").toExternalForm());

        // Titre de la fenêtre
        stage.setTitle("SkillSwap");

        // Définir la scène et afficher la fenêtre
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args); // Lancer l'application
    }
}