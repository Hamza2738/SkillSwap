package org.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/fxml/Competences/competence.fxml"));
        Scene scene = new Scene(loader.load(), 1100, 650);


        scene.getStylesheets().add(
                getClass().getResource("/view/css/competences/style.css").toExternalForm()
        );

        stage.setTitle("SkillSwap");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
