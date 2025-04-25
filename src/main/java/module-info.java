module com.turniermanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.turniermanagement to javafx.fxml;
    opens com.turniermanagement.db;  // Öffne für Tests
    opens com.turniermanagement.service;  // Öffne für Tests
    opens com.turniermanagement.model;  // Öffne für Tests
    
    exports com.turniermanagement;
    exports com.turniermanagement.db;
    exports com.turniermanagement.model;
    exports com.turniermanagement.service;
}