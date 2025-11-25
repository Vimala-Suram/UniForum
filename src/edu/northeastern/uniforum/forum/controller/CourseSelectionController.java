package edu.northeastern.uniforum.forum.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.fxml.FXMLLoader;

public class CourseSelectionController {

    @FXML private ComboBox<String> deptCombo;
    @FXML private ComboBox<String> courseCombo;
    @FXML private Button nextBtn;

    @FXML
    public void initialize() {
        deptCombo.getItems().addAll("IS", "SES", "DAMG", "TELE");

        deptCombo.setOnAction(e -> {
            courseCombo.getItems().clear();
            switch (deptCombo.getValue()) {
                case "IS" -> courseCombo.getItems().addAll(
                    "INFO 5002-01 Intro to Python for Info Sys",
                    "INFO 5100-04 Application Engineer & Dev",
                    "INFO 6105-01 Data Sci Eng Methods",
                    "INFO 6106-01 Neural Modeling Methods & Tool"
                );
                case "SES" -> courseCombo.getItems().addAll(
                    "CSYE 6225-03 Netwrk Strctrs & Cloud Cmpting",
                    "CSYE 7105-01 Parallel Machine Learning & AI",
                    "CSYE 7280-01 User Experience Design/Testing",
                    "CSYE 7380-02 Theory & Prac App AI Gen Model"
                );
                case "DAMG" -> courseCombo.getItems().addAll(
                    "DAMG 6210-01 Data Mgt and Database Design",
                    "DAMG 7250-01 Big Data Architec & Governance",
                    "DAMG 7374-01 ST: Gen AI w/ LLM in Data Eng",
                    "DAMG 7245-02 Big Data Sys & Intel Analytics"
                );
                case "TELE" -> courseCombo.getItems().addAll(
                    "TELE 5330-01 Data Networking",
                    "TELE 6530-01 Connected Devices",
                    "TELE 7374-02 Special Topics: Building Digital Twins",
                    "TELE 5600-01 Linux for Network Engineers"
                );
            }
        });

        nextBtn.setOnAction(e -> goToLandingPage());
    }

    private void goToLandingPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("next_buttons.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Student Menu");
            stage.setScene(new Scene(root));
            stage.show();

            // Close course selection window
            nextBtn.getScene().getWindow().hide();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
