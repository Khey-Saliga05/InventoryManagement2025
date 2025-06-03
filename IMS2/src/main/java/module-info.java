module com.example.ims2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.ims2 to javafx.fxml;
    exports com.example.ims2;
}