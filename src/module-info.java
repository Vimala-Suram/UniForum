module Uniforum_Project {
    requires javafx.controls;
    requires javafx.fxml;

    // Let FXML use reflection on these packages:
    opens application to javafx.graphics, javafx.fxml;
    opens edu.northeastern.uniforum.forum.controller to javafx.fxml;
    opens edu.northeastern.uniforum.forum.view to javafx.fxml;

    // Optional: export if other modules ever use them
    exports application;
    exports edu.northeastern.uniforum.forum.controller;
}
