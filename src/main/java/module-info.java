module com.example.xml {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.xml to javafx.fxml;
    exports com.example.xml;
}