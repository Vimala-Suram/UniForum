package edu.northeastern.uniforum.forum.controller;

import edu.northeastern.uniforum.forum.dao.UserDAO;
import edu.northeastern.uniforum.forum.model.User;
import edu.northeastern.uniforum.forum.util.PasswordUtil;
import edu.northeastern.uniforum.forum.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistrationController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void handleRegisterButtonAction() {
        // Clear any previous messages first
        messageLabel.setText("");
        
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validate input
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        // Basic email validation
        if (!email.contains("@") || !email.contains(".")) {
            messageLabel.setText("Please enter a valid email address.");
            return;
        }

        // Password validation
        if (password.length() < 6) {
            messageLabel.setText("Password must be at least 6 characters long.");
            return;
        }

        // Hash the password
        try {
            String passwordHash = PasswordUtil.hashPassword(password);

            // Create user object
            User newUser = new User(username, passwordHash, email);

            // Attempt registration
            if (userDAO.registerUser(newUser)) {
                messageLabel.setText("Registration successful! Redirecting to login...");
                // Wait a moment then switch to login
                new Thread(() -> {
                    try {
                        Thread.sleep(1500);
                        javafx.application.Platform.runLater(() -> {
                            SceneManager.switchToLogin();
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            } else {
                messageLabel.setText("Registration failed. Username or email may already exist.");
            }
        } catch (Exception e) {
            messageLabel.setText("Registration error: " + e.getMessage());
            System.err.println("Registration exception: ");
            e.printStackTrace();
        }
    }

    /**
     * Handles the "Already registered? Log In" button click
     * Switches back to the login view
     */
    @FXML
    public void handleLoginLinkAction() {
        SceneManager.switchToLogin();
    }
}