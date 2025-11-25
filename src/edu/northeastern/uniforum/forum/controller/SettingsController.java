package edu.northeastern.uniforum.forum.controller;

import edu.northeastern.uniforum.forum.dao.UserDAO;
import edu.northeastern.uniforum.forum.model.User;
import edu.northeastern.uniforum.forum.util.PasswordUtil;
import edu.northeastern.uniforum.forum.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class SettingsController {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;
    @FXML private Label usernameLabel;
    @FXML private Circle profileCircle;
    @FXML private VBox mainContentArea;
    
    private User currentUser;
    private UserDAO userDAO = new UserDAO();
    
    public void initData(User user) {
        this.currentUser = user;
        
        // Set grow constraints programmatically (parent is now available)
        if (mainContentArea != null && mainContentArea.getParent() instanceof HBox) {
            HBox.setHgrow(mainContentArea, Priority.ALWAYS);
        }
        
        // Populate fields with current user data (with null checks)
        if (usernameField != null) {
            usernameField.setText(user.getUsername());
        }
        if (emailField != null) {
            emailField.setText(user.getEmail());
        }
        if (usernameLabel != null) {
            usernameLabel.setText(user.getUsername());
        }
    }
    
    @FXML
    private void handleSaveAction() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }
        
        if (usernameField == null || emailField == null || currentPasswordField == null || 
            newPasswordField == null || confirmPasswordField == null) {
            if (messageLabel != null) {
                messageLabel.setText("Error: Form fields not initialized.");
            }
            return;
        }
        
        String newUsername = usernameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate username and email
        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            if (messageLabel != null) {
                messageLabel.setText("Username and email cannot be empty.");
            }
            return;
        }
        
        // Basic email validation
        if (!newEmail.contains("@") || !newEmail.contains(".")) {
            if (messageLabel != null) {
                messageLabel.setText("Please enter a valid email address.");
            }
            return;
        }
        
        // If password fields are filled, validate password change
        boolean changingPassword = !currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty();
        
        if (changingPassword) {
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                if (messageLabel != null) {
                    messageLabel.setText("All password fields must be filled to change password.");
                }
                return;
            }
            
            // Verify current password
            if (!PasswordUtil.checkPassword(currentPassword, currentUser.getPasswordHash())) {
                if (messageLabel != null) {
                    messageLabel.setText("Current password is incorrect.");
                }
                return;
            }
            
            // Check if new passwords match
            if (!newPassword.equals(confirmPassword)) {
                if (messageLabel != null) {
                    messageLabel.setText("New passwords do not match.");
                }
                return;
            }
            
            // Validate new password length
            if (newPassword.length() < 6) {
                if (messageLabel != null) {
                    messageLabel.setText("New password must be at least 6 characters long.");
                }
                return;
            }
        }
        
        // TODO: Update user in database
        // For now, just show success message
        if (messageLabel != null) {
            messageLabel.setText("Settings saved successfully! (Database update not yet implemented)");
        }
        
        // Clear password fields
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }
    
    @FXML
    private void handleCancelAction() {
        // Reset fields to original values (with null checks)
        if (usernameField != null) {
            usernameField.setText(currentUser.getUsername());
        }
        if (emailField != null) {
            emailField.setText(currentUser.getEmail());
        }
        if (currentPasswordField != null) {
            currentPasswordField.clear();
        }
        if (newPasswordField != null) {
            newPasswordField.clear();
        }
        if (confirmPasswordField != null) {
            confirmPasswordField.clear();
        }
        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }
    
    @FXML
    private void handleBackAction() {
        SceneManager.switchToForum(currentUser);
    }
    
    @FXML
    private void handleHomeAction() {
        SceneManager.switchToForum(currentUser);
    }
    
    @FXML
    private void handleProfileAction() {
        System.out.println("Profile clicked");
        // TODO: Navigate to profile page
    }
    
    @FXML
    private void handleGroupsAction() {
        System.out.println("Groups clicked");
        // TODO: Navigate to groups page
    }
    
    @FXML
    private void handleTrendingAction() {
        System.out.println("Trending clicked");
        // TODO: Navigate to trending page
    }
    
    @FXML
    private void handleSavedAction() {
        System.out.println("Saved clicked");
        // TODO: Navigate to saved items page
    }
    
    @FXML
    private void handleLogoutAction() {
        SceneManager.switchToLogin();
    }
}

