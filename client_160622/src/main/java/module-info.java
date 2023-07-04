module gb.safronov.client_160622 {
    requires javafx.controls;
    requires javafx.fxml;


    opens gb.safronov.client_160622 to javafx.fxml;
    exports gb.safronov.client_160622;
    exports gb.safronov.client_160622.controllers;
    opens gb.safronov.client_160622.controllers to javafx.fxml;
}