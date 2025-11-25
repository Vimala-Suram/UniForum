package edu.northeastern.uniforum.forum.controller;

import application.Main;
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
    @FXML private javafx.scene.control.Button scheduleCallButton;
    @FXML private javafx.scene.control.Button saveButton;
    @FXML private javafx.scene.control.Button cancelButton;
    @FXML private VBox securitySection;
    @FXML private HBox actionButtonsContainer;
    
    private User currentUser; // The logged-in user
    private User viewedUser; // The user whose profile is being viewed
    private UserDAO userDAO = new UserDAO();
    
    public void initData(User user) {
        this.viewedUser = user;
        // If currentUser is not set, assume user is viewing their own profile
        if (this.currentUser == null) {
            this.currentUser = user;
        }
        
        // Set grow constraints programmatically (parent is now available)
        if (mainContentArea != null && mainContentArea.getParent() instanceof HBox) {
            HBox.setHgrow(mainContentArea, Priority.ALWAYS);
        }
        
        // Populate fields with viewed user data (with null checks)
        if (usernameField != null) {
            usernameField.setText(viewedUser.getUsername());
        }
        if (emailField != null) {
            emailField.setText(viewedUser.getEmail() != null ? viewedUser.getEmail() : "");
        }
        if (linkedinUrlField != null) {
            linkedinUrlField.setText(viewedUser.getLinkedinUrl() != null ? viewedUser.getLinkedinUrl() : "");
        }
        if (githubUrlField != null) {
            githubUrlField.setText(viewedUser.getGithubUrl() != null ? viewedUser.getGithubUrl() : "");
        }
        if (departmentField != null) {
            departmentField.setText(viewedUser.getDepartment() != null ? viewedUser.getDepartment() : "");
        }
        if (usernameLabel != null) {
            usernameLabel.setText(viewedUser.getUsername());
        }
        
        // Determine if viewing own profile or another user's profile
        boolean isViewingOtherUser = currentUser != null && viewedUser != null && 
                                     currentUser.getUserId() != viewedUser.getUserId();
        boolean isOwnProfile = !isViewingOtherUser;
        
        // Show "Schedule a Call" button only if viewing another user's profile
        if (scheduleCallButton != null) {
            scheduleCallButton.setVisible(isViewingOtherUser);
            scheduleCallButton.setManaged(isViewingOtherUser);
        }
        
        // Hide security section if viewing another user's profile
        if (securitySection != null) {
            securitySection.setVisible(isOwnProfile);
            securitySection.setManaged(isOwnProfile);
        }
        
        // Hide save/cancel buttons if viewing another user's profile
        if (actionButtonsContainer != null) {
            actionButtonsContainer.setVisible(isOwnProfile);
            actionButtonsContainer.setManaged(isOwnProfile);
        }
        if (saveButton != null) {
            saveButton.setVisible(isOwnProfile);
            saveButton.setManaged(isOwnProfile);
        }
        if (cancelButton != null) {
            cancelButton.setVisible(isOwnProfile);
            cancelButton.setManaged(isOwnProfile);
        }
        
        // Make fields read-only (non-editable) if viewing another user's profile
        // Also style them to look like display-only fields (gray background, no border)
        String readOnlyStyle = "-fx-background-color: #F5F5F5; -fx-text-fill: #333; -fx-border-color: transparent; -fx-cursor: default;";
        
        if (usernameField != null) {
            usernameField.setEditable(isOwnProfile);
            usernameField.setStyle(isOwnProfile ? null : readOnlyStyle);
        }
        if (emailField != null) {
            emailField.setEditable(isOwnProfile);
            emailField.setStyle(isOwnProfile ? null : readOnlyStyle);
        }
        if (linkedinUrlField != null) {
            linkedinUrlField.setEditable(isOwnProfile);
            linkedinUrlField.setStyle(isOwnProfile ? null : readOnlyStyle);
        }
        if (githubUrlField != null) {
            githubUrlField.setEditable(isOwnProfile);
            githubUrlField.setStyle(isOwnProfile ? null : readOnlyStyle);
        }
        if (departmentField != null) {
            departmentField.setEditable(isOwnProfile);
            departmentField.setStyle(isOwnProfile ? null : readOnlyStyle);
        }
        
        // Hide password fields (already handled by hiding security section, but keep for safety)
        if (currentPasswordField != null) {
            currentPasswordField.setVisible(isOwnProfile);
            currentPasswordField.setManaged(isOwnProfile);
        }
        if (newPasswordField != null) {
            newPasswordField.setVisible(isOwnProfile);
            newPasswordField.setManaged(isOwnProfile);
        }
        if (confirmPasswordField != null) {
            confirmPasswordField.setVisible(isOwnProfile);
            confirmPasswordField.setManaged(isOwnProfile);
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
            if (!PasswordUtil.checkPassword(currentPassword, viewedUser.getPasswordHash())) {
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
        boolean usernameChanged = !newUsername.equals(viewedUser.getUsername());
        boolean emailChanged = !newEmail.equals(viewedUser.getEmail());
        boolean linkedinChanged = !newLinkedinUrl.equals(viewedUser.getLinkedinUrl() != null ? viewedUser.getLinkedinUrl() : "");
        boolean githubChanged = !newGithubUrl.equals(viewedUser.getGithubUrl() != null ? viewedUser.getGithubUrl() : "");
        boolean departmentChanged = !newDepartment.equals(viewedUser.getDepartment() != null ? viewedUser.getDepartment() : "");
        
        // If username changed, check if new username already exists
        if (usernameChanged) {
            User existingUser = userDAO.getUserByUsername(newUsername);
            if (existingUser != null && existingUser.getUserId() != viewedUser.getUserId()) {
                if (messageLabel != null) {
                    messageLabel.setText("Username already exists. Please choose a different username.");
                }
                return;
            }
        }
        
        // Only allow updates if viewing own profile
        if (currentUser == null || viewedUser == null || currentUser.getUserId() != viewedUser.getUserId()) {
            if (messageLabel != null) {
                messageLabel.setText("You can only edit your own profile.");
            }
            return;
        }
        
        // Update user in database
        boolean updateSuccess = userDAO.updateUser(
            viewedUser.getUserId(),
            usernameChanged ? newUsername : null,
            emailChanged ? newEmail : null,
            newPasswordHash,
            linkedinChanged ? newLinkedinUrl : null,
            githubChanged ? newGithubUrl : null,
            departmentChanged ? newDepartment : null
        );
        
        if (updateSuccess) {
            // Update viewed user object with new values
            String updatedUsername = usernameChanged ? newUsername : viewedUser.getUsername();
            String updatedEmail = emailChanged ? newEmail : viewedUser.getEmail();
            String updatedPasswordHash = newPasswordHash != null ? newPasswordHash : viewedUser.getPasswordHash();
            String updatedLinkedinUrl = linkedinChanged ? newLinkedinUrl : (viewedUser.getLinkedinUrl() != null ? viewedUser.getLinkedinUrl() : "");
            String updatedGithubUrl = githubChanged ? newGithubUrl : (viewedUser.getGithubUrl() != null ? viewedUser.getGithubUrl() : "");
            String updatedDepartment = departmentChanged ? newDepartment : (viewedUser.getDepartment() != null ? viewedUser.getDepartment() : "");
            
            viewedUser = new User(
                viewedUser.getUserId(),
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
        if (usernameField != null && viewedUser != null) {
            usernameField.setText(viewedUser.getUsername());
        }
        if (emailField != null && viewedUser != null) {
            emailField.setText(viewedUser.getEmail() != null ? viewedUser.getEmail() : "");
        }
        if (linkedinUrlField != null && viewedUser != null) {
            linkedinUrlField.setText(viewedUser.getLinkedinUrl() != null ? viewedUser.getLinkedinUrl() : "");
        }
        if (githubUrlField != null && viewedUser != null) {
            githubUrlField.setText(viewedUser.getGithubUrl() != null ? viewedUser.getGithubUrl() : "");
        }
        if (departmentField != null && viewedUser != null) {
            departmentField.setText(viewedUser.getDepartment() != null ? viewedUser.getDepartment() : "");
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
    
    /**
     * Sets the logged-in user (separate from the viewed user)
     */
    public void setLoggedInUser(User loggedInUser) {
        this.currentUser = loggedInUser;
    }
    
    /**
     * Handles the "Schedule a Call" button click
     */
    @FXML
    private void handleScheduleCall() {
        if (viewedUser == null || currentUser == null) {
            return;
        }
        
        // Open meeting scheduler with the viewed user's email
        openMeetingScheduler(viewedUser.getEmail());
    }
    
    /**
     * Opens the meeting scheduler dialog with pre-filled email
     */
    private void openMeetingScheduler(String attendeeEmail) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/edu/northeastern/uniforum/forum/view/MeetingScheduler.fxml")
            );
            javafx.scene.Parent meetingRoot = loader.load();
            
            MeetingController meetingController = loader.getController();
            if (meetingController != null) {
                // Set logged-in user and target user
                if (currentUser != null) {
                    meetingController.setLoggedInUser(currentUser);
                }
                if (viewedUser != null) {
                    meetingController.setTargetUser(viewedUser);
                }
                // Pre-fill email if available
                if (attendeeEmail != null && !attendeeEmail.isEmpty()) {
                    meetingController.setAttendeeEmail(attendeeEmail);
                }
            }
            
            javafx.stage.Stage meetingStage = new javafx.stage.Stage();
            meetingStage.setTitle("Schedule a Meeting");
            meetingStage.setScene(new javafx.scene.Scene(meetingRoot, 500, 600));
            meetingStage.setResizable(false);
            meetingStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            meetingStage.initOwner(Main.getPrimaryStage());
            meetingStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            if (messageLabel != null) {
                messageLabel.setText("Error opening meeting scheduler: " + e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleBackAction() {
        if (currentUser != null) {
            SceneManager.switchToForum(currentUser);
        } else {
            SceneManager.switchToLogin();
        }
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
        // Only show context menu if viewing own profile
        if (currentUser == null || viewedUser == null || currentUser.getUserId() != viewedUser.getUserId()) {
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

