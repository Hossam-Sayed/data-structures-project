package com.example.xml;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    static FXMLLoader mainLoader;

    @Override
    public void start(Stage stage) throws IOException {
        mainLoader = new FXMLLoader(Main.class.getResource("main-view.fxml"));
        Scene scene = new Scene(mainLoader.load(), 640, 480);
        stage.setTitle("XML Editor");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}