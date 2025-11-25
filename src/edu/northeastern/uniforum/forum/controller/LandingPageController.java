package edu.northeastern.uniforum.forum.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class LandingPageController {

    @FXML private Button faqBtn;
    @FXML private Button softwareBtn;
    @FXML private Button projectsBtn;
    @FXML private Button assignmentsBtn;
    @FXML private Button resourcesBtn;
    @FXML private Button contactBtn;

    @FXML
    public void initialize() {
        faqBtn.setOnAction(e -> System.out.println("FAQ clicked"));
        softwareBtn.setOnAction(e -> System.out.println("Software Installations clicked"));
        projectsBtn.setOnAction(e -> System.out.println("Project Discussions clicked"));
        assignmentsBtn.setOnAction(e -> System.out.println("Assignments clicked"));
        resourcesBtn.setOnAction(e -> System.out.println("Resources clicked"));
        contactBtn.setOnAction(e -> System.out.println("Contact TA clicked"));
    }
}
