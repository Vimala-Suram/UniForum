package edu.northeastern.uniforum.forum.controller;

import edu.northeastern.uniforum.forum.dao.PostDAO;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CreatePostController {

    @FXML
    private ComboBox<PostDAO.CommunityDTO> communityComboBox;

    @FXML
    private TextField titleField;

    @FXML
    private Label titleCharCount;

    @FXML
    private Label validationMessage;

    @FXML
    private TextArea bodyTextArea;

    @FXML
    private Button closeButton;

    @FXML
    private ComboBox<String> tagComboBox;

    @FXML
    private HBox tagsContainer;

    private ForumController parentController;

    private final PostDAO postDAO = new PostDAO();
    
    private List<String> tags = new ArrayList<>();

    /**
     * Sets the parent controller to allow closing the modal
     * and refreshing the main feed.
     */
    public void setParentController(ForumController parentController) {
        this.parentController = parentController;
    }

    @FXML
    private void initialize() {
        // Hide validation at start
        showValidationMessage(null);

        // Update character count as user types in title field
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            int length = newValue != null ? newValue.length() : 0;
            titleCharCount.setText(length + "/300");

            // Change color if approaching limit
            if (length > 280) {
                titleCharCount.setStyle("-fx-text-fill: #ff585b; -fx-font-size: 12;");
            } else {
                titleCharCount.setStyle("-fx-text-fill: #818384; -fx-font-size: 12;");
            }
        });

        // Limit title to 300 characters
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > 300) {
                titleField.setText(oldValue);
            }
        });

        // Load communities into the dropdown
        loadCommunities();
        
        // Load tags into the dropdown
        loadTags();
    }

    private void loadCommunities() {
        try {
            List<PostDAO.CommunityDTO> communities = postDAO.getAllCommunities();
            communityComboBox.getItems().setAll(communities);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Failed to load communities: " + e.getMessage());
        }
    }

    @FXML
    private void onDraftsClicked() {
        System.out.println("Drafts clicked");
        // TODO: Open drafts dialog
    }

    /**
     * Loads available tags into the dropdown
     */
    private void loadTags() {
        tagComboBox.getItems().addAll("FAQs", "Installation Issues", "Project ideas");
    }

    /**
     * Handles tag selection from dropdown - automatically adds the tag
     */
    @FXML
    private void onTagSelected() {
        String selectedTag = tagComboBox.getSelectionModel().getSelectedItem();
        if (selectedTag != null && !selectedTag.isEmpty() && !tags.contains(selectedTag)) {
            tags.add(selectedTag);
            tagComboBox.getSelectionModel().clearSelection();
            updateTagsDisplay();
        }
    }

    /**
     * Updates the tags display with current tags
     */
    private void updateTagsDisplay() {
        tagsContainer.getChildren().clear();
        
        for (String tag : tags) {
            HBox tagBox = new HBox(6);
            tagBox.setAlignment(Pos.CENTER_LEFT);
            tagBox.setStyle("-fx-background-color: #0079d3; -fx-background-radius: 12; -fx-padding: 4 8;");
            
            Label tagLabel = new Label(tag);
            tagLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12;");
            
            Button removeBtn = new Button("×");
            removeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ffffff; -fx-font-size: 14; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 0 0 0 4;");
            removeBtn.setOnAction(e -> removeTag(tag));
            
            tagBox.getChildren().addAll(tagLabel, removeBtn);
            tagsContainer.getChildren().add(tagBox);
        }
    }

    /**
     * Removes a tag from the list
     */
    private void removeTag(String tag) {
        tags.remove(tag);
        updateTagsDisplay();
    }

    // Formatting toolbar actions
    
    @FXML
    private void onBoldClicked() {
        wrapSelectedText("**", "**");
    }

    @FXML
    private void onItalicClicked() {
        wrapSelectedText("*", "*");
    }

    @FXML
    private void onLinkClicked() {
        String selectedText = bodyTextArea.getSelectedText();
        if (!selectedText.isEmpty()) {
            // Wrap selected text as link: [text](url)
            int start = bodyTextArea.getSelection().getStart();
            int end = bodyTextArea.getSelection().getEnd();
            String currentText = bodyTextArea.getText();
            String newText = currentText.substring(0, start) + "[" + selectedText + "](url)" + currentText.substring(end);
            bodyTextArea.setText(newText);
            bodyTextArea.selectRange(start + selectedText.length() + 3, start + selectedText.length() + 6); // Select "url"
        } else {
            // Insert link template at cursor
            insertText("[link text](url)");
            // Select "url" for easy replacement
            int pos = bodyTextArea.getCaretPosition();
            bodyTextArea.selectRange(pos - 4, pos - 1);
        }
    }

    @FXML
    private void onSaveDraftClicked() {
        System.out.println("Save Draft clicked");
        // TODO: Save draft to database
    }

    @FXML
    private void onPostClicked() {
        PostDAO.CommunityDTO selectedCommunity =
                communityComboBox.getSelectionModel().getSelectedItem();
        String title = titleField.getText() != null ? titleField.getText().trim() : "";
        String body  = bodyTextArea.getText() != null ? bodyTextArea.getText().trim() : "";

        // Basic validation
        if (selectedCommunity == null) {
            showValidationMessage("Please select a community.");
            return;
        }
        if (title.isEmpty()) {
            showValidationMessage("Title is required.");
            return;
        }
        if (tags.isEmpty()) {
            showValidationMessage("Please select at least one tag.");
            return;
        }
        String tag = tags.get(0);
        showValidationMessage(null);

        try {
            int userId = 1; // for now: all posts created by User 1

            postDAO.createPost(selectedCommunity.id, userId, title, body, tag);
            System.out.println("Post created successfully.");
            showValidationMessage(null);

            // Refresh main feed if we have a parent controller
            if (parentController != null) {
                parentController.loadPostsFromDB();
            }

            // Close the modal
            onCloseClicked();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error creating post: " + e.getMessage());
        }
    }

    /**
     * Displays or hides the inline validation message
     */
    private void showValidationMessage(String message) {
        if (validationMessage == null) {
            return;
        }
        boolean hasMessage = message != null && !message.isBlank();
        validationMessage.setText(hasMessage ? message : "");
        validationMessage.setVisible(hasMessage);
        validationMessage.setManaged(hasMessage);
    }

    @FXML
    private void onCloseClicked() {
        // Close the modal overlay
        if (parentController != null) {
            parentController.closeModal();
        } else {
            // Fallback: try to close via stage
            Stage stage = (Stage) closeButton.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        }
    }

    @FXML
    private void onCloseButtonHover() {
        closeButton.setStyle("-fx-background-color: #272729; -fx-text-fill: #d7dadc; -fx-font-size: 20; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 8; -fx-min-width: 32; -fx-min-height: 32;");
    }

    @FXML
    private void onCloseButtonExit() {
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #818384; -fx-font-size: 20; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 4 8; -fx-min-width: 32; -fx-min-height: 32;");
    }

    /**
     * Inserts text at the current cursor position in the text area
     */
    private void insertText(String text) {
        int caretPosition = bodyTextArea.getCaretPosition();
        String currentText = bodyTextArea.getText();
        String newText = currentText.substring(0, caretPosition) + text + currentText.substring(caretPosition);
        bodyTextArea.setText(newText);
        bodyTextArea.positionCaret(caretPosition + text.length());
    }

    /**
     * Wraps selected text with markers, or inserts markers for new text
     */
    private void wrapSelectedText(String beforeMarker, String afterMarker) {
        String selectedText = bodyTextArea.getSelectedText();
        int start = bodyTextArea.getSelection().getStart();
        int end = bodyTextArea.getSelection().getEnd();
        
        String currentText = bodyTextArea.getText();
        
        if (!selectedText.isEmpty()) {
            // Wrap selected text
            String newText = currentText.substring(0, start) + beforeMarker + selectedText + afterMarker + currentText.substring(end);
            bodyTextArea.setText(newText);
            bodyTextArea.selectRange(start, start + beforeMarker.length() + selectedText.length() + afterMarker.length());
        } else {
            // Insert markers at cursor position
            String newText = currentText.substring(0, start) + beforeMarker + afterMarker + currentText.substring(start);
            bodyTextArea.setText(newText);
            bodyTextArea.positionCaret(start + beforeMarker.length());
        }
    }
}
