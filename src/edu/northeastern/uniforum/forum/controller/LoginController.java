// com.uniforum.controllers.LoginController.java

package edu.northeastern.uniforum.forum.controller;


import edu.northeastern.uniforum.forum.dao.UserDAO;
import edu.northeastern.uniforum.forum.model.User;
import edu.northeastern.uniforum.forum.util.PasswordUtil;
import edu.northeastern.uniforum.forum.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void handleLoginButtonAction() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password.");
            return;
        }

        User user = userDAO.getUserByUsername(username);
        
        if (user != null && PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            // Success: Check if user has joined communities
            messageLabel.setText("Login Successful! Welcome, " + user.getUsername());
            
            // Check if user has joined any communities
            if (!userDAO.hasUserJoinedCommunities(user.getUserId())) {
                // New user - redirect to course selection
                SceneManager.switchToCourseSelection(user);
            } else {
                // Existing user - go to forum
                SceneManager.switchToForum(user);
            }
        } else {
            messageLabel.setText("Invalid username or password.");
        }
    }
    
    @FXML
    public void handleRegisterLinkAction() {
        SceneManager.switchToRegistration();
    }
    
    @FXML
    public void handleForgotPasswordAction() {
        System.out.println("Forgot Password clicked.");
        // TODO: Implementation for password recovery
    }
}
