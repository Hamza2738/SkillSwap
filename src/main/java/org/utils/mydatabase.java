package org.utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class mydatabase {
    final String USERNAME = "root";
    final String PASSWORD = "";
    final String url = "jdbc:mysql://localhost:3306/pi";

    Connection connection;
    static mydatabase instance;

    //constructeur

    private mydatabase(){ //Pour que personne ne puisse faire : new mydatabase() à l’extérieur. il doit rappeler get pour crée la bsae une seul fois

        try {
            connection = DriverManager.getConnection(url, USERNAME, PASSWORD);
            System.out.println("Connected to database successfully");
        } catch (SQLException e) {

            System.out.println(e.getMessage());
        }
    }

    public static mydatabase getInstance() { //methode static pour peut rappeler dans main
        if (instance == null) {// si aucune instance n’a encore été créée
            instance = new mydatabase();// créer l’unique instance

        }
        return instance;// renvoyer l’instance existante
    }

    public Connection getConnection() {
        return connection;
    }
}
