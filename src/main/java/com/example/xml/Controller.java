package com.example.xml;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Valid");
    }

    @FXML
    protected void onMinifyClick() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("output-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 640, 480);
        Stage stage = new Stage();
        stage.setTitle("Minify");
        stage.setScene(scene);
        stage.show();
    }
}