package com.example.xml;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import static com.example.xml.Main.mainLoader;

public class Controller {
    public TextArea mainTextArea;
    public TextField browseTextField;
    public TextArea outputTextArea;
    public Button exportToTextAreaButton;
    public Button exportToFileButton;
    public Button fixErrorsButton;
    public Button formatButton;
    public Button postSearchButton;
    public Button convertToJsonButton;
    public Button networkAnalysisButton;
    public TextArea jsonOutputTextArea;
    public Button exportJsonToFileButton;
    XML xmlFile;
    FXMLLoader outputLoader;

    @FXML
    private Label labelText;

    @FXML
    protected void onHelloButtonClick() {
        labelText.setText("Valid");
    }

    @FXML
    public void xmlFileChooser() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(".xml", "*.xml"));
        File f = fc.showOpenDialog(null);
        if (f != null) {
            xmlFile = new XML(f);
            mainTextArea.setText(xmlFile.getXml());
            browseTextField.setText(f.getPath());
        }
    }

    @FXML
    protected void onTextChanged() {
        xmlFile = new XML(mainTextArea.getText());
        labelText.setText("");
        fixErrorsButton.setDisable(true);
        formatButton.setDisable(true);
        networkAnalysisButton.setDisable(true);
        postSearchButton.setDisable(true);
        convertToJsonButton.setDisable(true);
    }

    @FXML
    protected void onMinifyClick() throws IOException {
        if (xmlFile == null) {
            labelText.setText("Choose an XML file");
        } else {
            Controller controller = openOutputWindow("Minify", "output-view.fxml");
            String minifiedXML = xmlFile.minifyXML();
            controller.outputTextArea.setText(minifiedXML);
        }
    }

    @FXML
    protected void exportToTextArea() {
        Controller controller = mainLoader.getController();
        controller.mainTextArea.setText(outputTextArea.getText());
        xmlFile = new XML(controller.mainTextArea.getText());
    }

    @FXML
    protected void saveToFile() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(".xml", "*.xml"));
        File f = fc.showSaveDialog(null);
        if (f != null) {
            xmlFile.stringToXmlFile(f.getPath());
        }
    }

    @FXML
    protected void compress() {
        if (xmlFile == null) {
            labelText.setText("Choose an XML file");
        } else {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(".huff", "*.huff"));
            File f = fc.showSaveDialog(null);
            if (f != null) {
                xmlFile.compress(f.getPath());
            }
        }
    }

    @FXML
    protected void decompress() throws IOException {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(".huff", "*.huff"));
        File f = fc.showOpenDialog(null);
        if (f != null) {
            Controller controller = openOutputWindow("Decompress", "output-view.fxml");
            String decompressed = XML.decompress(f.getPath());
            controller.outputTextArea.setText(decompressed);
        }
    }

    @FXML
    protected void onValidateClick() throws IOException {
        if (xmlFile == null) {
            labelText.setText("Choose an XML file");
        } else {
            if (xmlFile.isValid()) {
                labelText.setText("Valid");
            } else {
                ArrayList<String> errors = xmlFile.getErrors(false);
                xmlFile.setValid(errors == null);
                if (errors != null) {
                    Controller controller = openOutputWindow("Errors", "output-view.fxml");
                    controller.exportToFileButton.setVisible(false);
                    controller.exportToTextAreaButton.setVisible(false);
                    fixErrorsButton.setDisable(false);
                    for (String error : errors) {
                        controller.outputTextArea.setText(controller.outputTextArea.getText() + error + "\n");
                    }
                } else {
                    labelText.setText("Valid");
                    fixErrorsButton.setDisable(true);
                    formatButton.setDisable(false);
                    networkAnalysisButton.setDisable(false);
                    postSearchButton.setDisable(false);
                    convertToJsonButton.setDisable(false);
                }
            }
        }
    }

    @FXML
    protected void onFixErrorsClick() {
        xmlFile.fixErrors();
        mainTextArea.setText(xmlFile.getXml());
        fixErrorsButton.setDisable(true);
    }

    @FXML
    protected void onFormatClick() {
        if (xmlFile == null) {
            labelText.setText("Choose an XML file");
        } else {
            xmlFile.format();
            mainTextArea.setText(xmlFile.getXml());
        }
    }

    @FXML
    protected void onConvertToJsonClick() throws IOException {
        String json = xmlFile.xmlToJson();
        Controller controller = openOutputWindow("JSON", "json-output-view.fxml");
        controller.jsonOutputTextArea.setText(json);
    }

    @FXML
    protected void onExportToJsonFileClick() throws IOException {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(".json", "*.json"));
        File f = fc.showSaveDialog(null);
        if (f != null) {
            try (FileWriter output = new FileWriter(f.getPath())) {
                output.write(jsonOutputTextArea.getText());
            }
        }
    }

    private Controller openOutputWindow(String stageName, String resource) throws IOException {
        outputLoader = new FXMLLoader(Main.class.getResource(resource));
        Scene scene = new Scene(outputLoader.load(), 640, 480);
        Stage stage = new Stage();
        stage.setTitle(stageName);
        stage.setScene(scene);
        stage.show();
        return outputLoader.getController();
    }
}