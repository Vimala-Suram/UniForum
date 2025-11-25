package edu.northeastern.uniforum.forum.controller;

import edu.northeastern.uniforum.forum.dao.UserDAO;
import edu.northeastern.uniforum.forum.model.User;
import edu.northeastern.uniforum.forum.util.PasswordUtil;
import edu.northeastern.uniforum.forum.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SettingsController {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField linkedinUrlField;
    @FXML private TextField githubUrlField;
    @FXML private TextField departmentField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;
    @FXML private Label usernameLabel;
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
            emailField.setText(user.getEmail() != null ? user.getEmail() : "");
        }
        if (linkedinUrlField != null) {
            linkedinUrlField.setText(user.getLinkedinUrl() != null ? user.getLinkedinUrl() : "");
        }
        if (githubUrlField != null) {
            githubUrlField.setText(user.getGithubUrl() != null ? user.getGithubUrl() : "");
        }
        if (departmentField != null) {
            departmentField.setText(user.getDepartment() != null ? user.getDepartment() : "");
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
        String newLinkedinUrl = linkedinUrlField != null ? linkedinUrlField.getText().trim() : "";
        String newGithubUrl = githubUrlField != null ? githubUrlField.getText().trim() : "";
        String newDepartment = departmentField != null ? departmentField.getText().trim() : "";
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
        
        // Check if password is being changed
        boolean changingPassword = !currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty();
        String newPasswordHash = null;
        
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
            
            // Hash the new password
            newPasswordHash = PasswordUtil.hashPassword(newPassword);
        }
        
        // Check if fields changed
        boolean usernameChanged = !newUsername.equals(currentUser.getUsername());
        boolean emailChanged = !newEmail.equals(currentUser.getEmail());
        boolean linkedinChanged = !newLinkedinUrl.equals(currentUser.getLinkedinUrl() != null ? currentUser.getLinkedinUrl() : "");
        boolean githubChanged = !newGithubUrl.equals(currentUser.getGithubUrl() != null ? currentUser.getGithubUrl() : "");
        boolean departmentChanged = !newDepartment.equals(currentUser.getDepartment() != null ? currentUser.getDepartment() : "");
        
        // If username changed, check if new username already exists
        if (usernameChanged) {
            User existingUser = userDAO.getUserByUsername(newUsername);
            if (existingUser != null && existingUser.getUserId() != currentUser.getUserId()) {
                if (messageLabel != null) {
                    messageLabel.setText("Username already exists. Please choose a different username.");
                }
                return;
            }
        }
        
        // Update user in database
        boolean updateSuccess = userDAO.updateUser(
            currentUser.getUserId(),
            usernameChanged ? newUsername : null,
            emailChanged ? newEmail : null,
            newPasswordHash,
            linkedinChanged ? newLinkedinUrl : null,
            githubChanged ? newGithubUrl : null,
            departmentChanged ? newDepartment : null
        );
        
        if (updateSuccess) {
            // Update current user object with new values
            String updatedUsername = usernameChanged ? newUsername : currentUser.getUsername();
            String updatedEmail = emailChanged ? newEmail : currentUser.getEmail();
            String updatedPasswordHash = newPasswordHash != null ? newPasswordHash : currentUser.getPasswordHash();
            String updatedLinkedinUrl = linkedinChanged ? newLinkedinUrl : (currentUser.getLinkedinUrl() != null ? currentUser.getLinkedinUrl() : "");
            String updatedGithubUrl = githubChanged ? newGithubUrl : (currentUser.getGithubUrl() != null ? currentUser.getGithubUrl() : "");
            String updatedDepartment = departmentChanged ? newDepartment : (currentUser.getDepartment() != null ? currentUser.getDepartment() : "");
            
            currentUser = new User(
                currentUser.getUserId(),
                updatedUsername,
                updatedPasswordHash,
                updatedEmail,
                updatedLinkedinUrl,
                updatedGithubUrl,
                updatedDepartment
            );
            
            // Update username label in UI
            if (usernameLabel != null) {
                usernameLabel.setText(updatedUsername);
            }
            
            if (messageLabel != null) {
                messageLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
                messageLabel.setText("Settings saved successfully!");
            }
            
            // Clear password fields
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
        } else {
            if (messageLabel != null) {
                messageLabel.setStyle("-fx-text-fill: #F35B04; -fx-font-weight: bold;");
                messageLabel.setText("Error saving settings. Please try again.");
            }
        }
    }
    
    @FXML
    private void handleCancelAction() {
        // Reset fields to original values (with null checks)
        if (usernameField != null) {
            usernameField.setText(currentUser.getUsername());
        }
        if (emailField != null) {
            emailField.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        }
        if (linkedinUrlField != null) {
            linkedinUrlField.setText(currentUser.getLinkedinUrl() != null ? currentUser.getLinkedinUrl() : "");
        }
        if (githubUrlField != null) {
            githubUrlField.setText(currentUser.getGithubUrl() != null ? currentUser.getGithubUrl() : "");
        }
        if (departmentField != null) {
            departmentField.setText(currentUser.getDepartment() != null ? currentUser.getDepartment() : "");
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
        handleLogout();
    }
    
    /**
     * Handles click on username label - shows context menu with logout option
     */
    @FXML
    private void onUsernameClicked(MouseEvent event) {
        if (currentUser == null) {
            return;
        }
        
        // Create context menu
        ContextMenu contextMenu = new ContextMenu();
        
        // Logout menu item
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(e -> {
            handleLogout();
        });
        logoutItem.setStyle("-fx-text-fill: #3D348B; -fx-font-size: 14; -fx-padding: 8 16;");
        
        contextMenu.getItems().add(logoutItem);
        contextMenu.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-radius: 4;");
        
        // Show context menu at the click location
        contextMenu.show(usernameLabel, event.getScreenX(), event.getScreenY());
    }
    
    /**
     * Handles user logout - navigates back to login page
     */
    private void handleLogout() {
        // Clear current user
        currentUser = null;
        
        // Navigate to login page
        SceneManager.switchToLogin();
    }
}

