package com.example.xml;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class Controller {
    XML xmlFile;
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Valid");
    }
    @FXML
    public void xmlFileChooser(){
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(".xml","*.xml"));
        File f = fc.showOpenDialog(null);
        if( f != null){
            xmlFile = new XML(f);
        }
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