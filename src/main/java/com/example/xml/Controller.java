package com.example.xml;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
    public Label resultLabel;
    public Button mostInfButton;
    public Button mostActiveButton;
    public Button mutualButton;
    public Button suggestButton;
    public Button okButton;
    public TextArea resultText;
    public TextField inputTextField2;
    public TextField inputTextField1;
    public Button saveButton;
    public Button saveAsButton;
    XML xmlFile;

    public XML getXmlFile() {
        return xmlFile;
    }

    FXMLLoader outputLoader;

    @FXML
    private Label labelText;

    @FXML
    public void xmlFileChooser() {
        labelText.setText("");
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(".xml", "*.xml"));
        File f = fc.showOpenDialog(null);
        if (f != null) {
            xmlFile = new XML(f);
            mainTextArea.setText(xmlFile.getXml());
            browseTextField.setText(f.getPath());
            saveButton.setDisable(false);
        }
    }

    @FXML
    protected void onTextChanged() {
        labelText.setText("");
        if (!mainTextArea.getText().equals("")) {
            xmlFile = new XML(mainTextArea.getText());
        } else {
            xmlFile = null;
        }
        labelText.setText("");
        fixErrorsButton.setDisable(true);
        formatButton.setDisable(true);
        networkAnalysisButton.setDisable(true);
        postSearchButton.setDisable(true);
        convertToJsonButton.setDisable(true);
    }

    @FXML
    protected void onMinifyClick() throws IOException {
        labelText.setText("");
        if (xmlFile == null) {
            labelText.setFont(Font.font("arial", FontWeight.BOLD, 11));
            labelText.setTextFill(Color.RED);
            labelText.setText("Choose an XML file");
        } else {
            Controller controller = openOutputWindow("Minify", "output-view.fxml");
            String minifiedXML = xmlFile.minifyXML();
            controller.outputTextArea.setText(minifiedXML);
            labelText.setFont(Font.font("arial", FontWeight.BOLD, 11));
            labelText.setTextFill(Color.GREEN);
            labelText.setText("Minified");
        }
    }

    @FXML
    protected void exportToTextArea() {
        Controller controller = mainLoader.getController();
        controller.mainTextArea.setText(outputTextArea.getText());
//        xmlFile = new XML(controller.mainTextArea.getText());
    }

    @FXML
    protected void saveToFile() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(".xml", "*.xml"));
        File f = fc.showSaveDialog(null);
        xmlFile = new XML(outputTextArea.getText());
        if (f != null) {
            xmlFile.stringToXmlFile(f.getPath());
        }
    }

    @FXML
    protected void compress() {
        labelText.setText("");
        if (xmlFile == null) {
            labelText.setFont(Font.font("arial", FontWeight.BOLD, 11));
            labelText.setTextFill(Color.RED);
            labelText.setText("Choose an XML file");
        } else {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(".huff", "*.huff"));
            File f = fc.showSaveDialog(null);
            if (f != null) {
                xmlFile.compress(f.getPath());
                labelText.setFont(Font.font("arial", FontWeight.BOLD, 11));
                labelText.setTextFill(Color.GREEN);
                labelText.setText("Compressed");
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
        labelText.setText("");
        if (xmlFile == null) {
            labelText.setFont(Font.font("arial", FontWeight.BOLD, 11));
            labelText.setTextFill(Color.RED);
            labelText.setText("Choose an XML file");
        } else {
            if (xmlFile.isValid()) {
                labelText.setFont(Font.font("arial", FontWeight.BOLD, 11));
                labelText.setTextFill(Color.GREEN);
                labelText.setText("Valid");
            } else {
                ArrayList<String> errors = xmlFile.getErrors(false);
                xmlFile.setValid(errors == null);
                if (errors != null) {
                    Controller controller = openOutputWindow("Errors", "output-view.fxml");
                    labelText.setFont(Font.font("arial", FontWeight.BOLD, 11));
                    labelText.setTextFill(Color.RED);
                    labelText.setText("Invalid");
                    controller.exportToFileButton.setVisible(false);
                    controller.exportToTextAreaButton.setVisible(false);
                    fixErrorsButton.setDisable(false);
                    for (String error : errors) {
                        controller.outputTextArea.setText(controller.outputTextArea.getText() + error + "\n");
                    }
                } else {
                    labelText.setFont(Font.font("arial", FontWeight.BOLD, 11));
                    labelText.setTextFill(Color.GREEN);
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
        labelText.setText("");
        xmlFile.fixErrors();
        mainTextArea.setText(xmlFile.getXml());
        fixErrorsButton.setDisable(true);
        labelText.setFont(Font.font("arial", FontWeight.BOLD, 11));
        labelText.setTextFill(Color.GREEN);
        labelText.setText("Errors Fixed");
    }

    @FXML
    protected void onFormatClick() {
        labelText.setText("");
        if (xmlFile == null) {
            labelText.setFont(Font.font("arial", FontWeight.BOLD, 11));
            labelText.setTextFill(Color.RED);
            labelText.setText("Choose an XML file");
        } else {
            xmlFile.format();
            mainTextArea.setText(xmlFile.getXml());
            labelText.setFont(Font.font("arial", FontWeight.BOLD, 11));
            labelText.setTextFill(Color.GREEN);
            labelText.setText("Formatted");
        }
    }

    @FXML
    protected void onConvertToJsonClick() throws IOException {
        labelText.setText("");
        String json = xmlFile.xmlToJson();
        Controller controller = openOutputWindow("JSON", "json-output-view.fxml");
        controller.jsonOutputTextArea.setText(json);
        labelText.setFont(Font.font("arial", FontWeight.BOLD, 11));
        labelText.setTextFill(Color.GREEN);
        labelText.setText("Converted To JSON");
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

    @FXML
    protected void onNetworkAnalysisClick() throws IOException {
        labelText.setText("");
        xmlFile.xmlToGraph();
        openOutputWindow("Network Analysis", "network-analysis-view.fxml");
    }

    @FXML
    protected void onMostInfClick() {
        Controller controller = mainLoader.getController();
        xmlFile = controller.getXmlFile();
        User user = xmlFile.getMostInfluencer();
        resultLabel.setFont(Font.font("arial", FontWeight.BOLD, 20));
        resultLabel.setText("Most Influencer User\nName: " + user.getName() + "\n" + "ID: " + user.getId());
    }

    @FXML
    protected void onMostActiveClick() {
        Controller controller = mainLoader.getController();
        xmlFile = controller.getXmlFile();
        User user = xmlFile.getMostActive();
        resultLabel.setFont(Font.font("arial", FontWeight.BOLD, 20));
        resultLabel.setText("Most Active User\nName: " + user.getName() + "\n" + "ID: " + user.getId());
    }

    @FXML
    protected void onOkClick() {
        Controller controller = mainLoader.getController();
        xmlFile = controller.getXmlFile();
        resultText.setText("");
        resultText.setFont(Font.font("arial", FontWeight.BOLD, 11));
        if (!inputTextField1.getText().equals("") && !inputTextField1.getText().equals("")) {
            ArrayList<User> mutualFollowers = xmlFile.getMutualFollowers(inputTextField1.getText(), inputTextField2.getText());
            if (mutualFollowers == null) {
                resultText.setStyle("-fx-text-fill: red ;");
                resultText.setText("\nInvalid input");
                return;
            } else if (mutualFollowers.isEmpty()) {
                resultText.setStyle("-fx-text-fill: red ;");
                resultText.setText("\nNo mutual followers");
                return;
            }
            for (User user : mutualFollowers) {
                resultText.setStyle("-fx-text-fill: black ;");
                resultText.setText(resultText.getText() + "\n" + "Name: " + user.getName() + "    " + "ID: " + user.getId() + "\n");
            }
        }
    }

    @FXML
    protected void onMutualFollowersClick() throws IOException {
        openOutputWindow("Mutual Followers", "input-window.fxml");
    }

    @FXML
    protected void onSuggestButtonClick() throws IOException {
        openOutputWindow("Suggest Followers", "suggest-input-window.fxml");
    }

    @FXML
    protected void onSuggestOkClick() {
        Controller controller = mainLoader.getController();
        xmlFile = controller.getXmlFile();
        resultText.setText("");
        resultText.setFont(Font.font("arial", FontWeight.BOLD, 11));
        if (!inputTextField1.getText().equals("")) {
            ArrayList<User> suggestedFollowers = xmlFile.suggestFollowers(inputTextField1.getText());
            if (suggestedFollowers == null) {
                resultText.setStyle("-fx-text-fill: red ;");
                resultText.setText("\nInvalid input");
                return;
            } else if (suggestedFollowers.isEmpty()) {
                resultText.setStyle("-fx-text-fill: red ;");
                resultText.setText("\nNo suggested followers");
                return;
            }
            for (User user : suggestedFollowers) {
                resultText.setStyle("-fx-text-fill: black ;");
                resultText.setText(resultText.getText() + "\n" + "Name: " + user.getName() + "    " + "ID: " + user.getId() + "\n");
            }
        }
    }

    @FXML
    protected void onSearchPostClick() throws IOException {
        labelText.setText("");
        xmlFile.xmlToGraph();
        openOutputWindow("Search Posts", "search-input-window.fxml");
    }

    @FXML
    protected void onPostOkClick() {
        Controller controller = mainLoader.getController();
        xmlFile = controller.getXmlFile();
        resultText.setText("");
        resultText.setFont(Font.font("arial", FontWeight.BOLD, 10));
        if (!inputTextField1.getText().equals("")) {
            ArrayList<Post> posts = xmlFile.searchPosts(inputTextField1.getText());
            if (posts == null) {
                resultText.setFont(Font.font("arial", FontWeight.BOLD, 11));
                resultText.setStyle("-fx-text-fill: red ;");
                resultText.setText("\nInvalid input");
                return;
            } else if (posts.isEmpty()) {
                resultText.setFont(Font.font("arial", FontWeight.BOLD, 11));
                resultText.setStyle("-fx-text-fill: red ;");
                resultText.setText("\nNo Posts Found");
                return;
            }
            for (Post post : posts) {
                resultText.setStyle("-fx-text-fill: black ;");
                resultText.setText(resultText.getText() + "\n" + post.getBody() + "\n\n");
            }
        }
    }

    @FXML
    protected void onSaveClick() throws IOException {
        labelText.setText("");
        if (xmlFile == null) {
            labelText.setFont(Font.font("arial", FontWeight.BOLD, 11));
            labelText.setTextFill(Color.RED);
            labelText.setText("Text Area Is Empty");
        } else {
            try (FileWriter output = new FileWriter(browseTextField.getText())) {
                output.write(mainTextArea.getText());
            }
            labelText.setFont(Font.font("arial", FontWeight.BOLD, 11));
            labelText.setTextFill(Color.GREEN);
            labelText.setText("File Saved");
        }
    }

    @FXML
    protected void onSaveAsClick() throws IOException {
        labelText.setText("");
        if (xmlFile == null) {
            labelText.setFont(Font.font("arial", FontWeight.BOLD, 11));
            labelText.setTextFill(Color.RED);
            labelText.setText("Text Area Is Empty");
        } else {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(".xml", "*.xml"));
            File f = fc.showSaveDialog(null);
            if (f != null) {
                try (FileWriter output = new FileWriter(f.getPath())) {
                    output.write(mainTextArea.getText());
                }
                labelText.setFont(Font.font("arial", FontWeight.BOLD, 11));
                labelText.setTextFill(Color.GREEN);
                labelText.setText("Saved To New File");
            }
        }
    }

    private Controller openOutputWindow(String stageName, String resource) throws IOException {
        outputLoader = new FXMLLoader(Main.class.getResource(resource));
        Scene scene = new Scene(outputLoader.load());
        Stage stage = new Stage();
        stage.setTitle(stageName);
        stage.setScene(scene);
        stage.show();
        return outputLoader.getController();
    }
}