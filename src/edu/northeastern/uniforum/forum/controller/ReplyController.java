package edu.northeastern.uniforum.forum.controller;

import edu.northeastern.uniforum.forum.dao.PostDAO;
import edu.northeastern.uniforum.forum.dao.ReplyDAO;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.net.URI;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReplyController {

    @FXML
    private Button closeButton;

    @FXML
    private VBox postContentContainer;

    @FXML
    private VBox repliesContainer;

    @FXML
    private TextArea replyTextArea;

    @FXML
    private Button addCommentButton;

    @FXML
    private VBox replyEditorSection;

    private ForumController parentController;
    private PostDAO.PostDTO postData;
    private int postId;
    private final ReplyDAO replyDAO = new ReplyDAO();

    /**
     * Sets the parent controller to allow closing the modal and refreshing
     */
    public void setParentController(ForumController parentController) {
        this.parentController = parentController;
    }

    /**
     * Sets the post data to display
     */
    public void setPostData(PostDAO.PostDTO postData) {
        this.postData = postData;
        this.postId = postData.postId;
        displayPost();
        loadReplies();
    }

    /**
     * Displays the full post content
     */
    private void displayPost() {
        postContentContainer.getChildren().clear();

        // Community and metadata
        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label communityLabel = new Label(postData.community);
        communityLabel.setStyle("-fx-text-fill: #d7dadc; -fx-font-size: 12; -fx-font-weight: bold;");

        String authorTimeText = "Posted by u/" + postData.author + " • " + postData.timeAgo;
        if (postData.tag != null && !postData.tag.trim().isEmpty()) {
            authorTimeText += " • " + postData.tag;
        }
        Label authorLabel = new Label(authorTimeText);
        authorLabel.setStyle("-fx-text-fill: #818384; -fx-font-size: 12;");

        metaRow.getChildren().addAll(communityLabel, authorLabel);

        // Post title
        Label titleLabel = new Label(postData.title);
        titleLabel.setStyle("-fx-text-fill: #d7dadc; -fx-font-size: 20; -fx-font-weight: bold; -fx-wrap-text: true;");
        titleLabel.setWrapText(true);

        // Post content - show full description with hyperlink support
        TextFlow contentFlow = createTextFlowWithLinks(postData.content);

        postContentContainer.getChildren().addAll(metaRow, titleLabel, contentFlow);
    }

    /**
     * Loads and displays replies for the current post
     */
    private void loadReplies() {
        repliesContainer.getChildren().clear();

        try {
            var replies = replyDAO.getRepliesByPostId(postId);

            if (replies.isEmpty()) {
                Label noRepliesLabel = new Label("No comments yet. Be the first to comment!");
                noRepliesLabel.setStyle("-fx-text-fill: #818384; -fx-font-size: 14; -fx-padding: 16;");
                repliesContainer.getChildren().add(noRepliesLabel);
            } else {
                for (ReplyDAO.ReplyDTO reply : replies) {
                    createReplyCard(reply);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error loading replies: " + e.getMessage());
        }
    }

    /**
     * Creates a reply card UI element
     */
    private void createReplyCard(ReplyDAO.ReplyDTO reply) {
        HBox replyCard = new HBox(8);
        replyCard.setStyle("-fx-background-color: #1a1a1b; -fx-background-radius: 4; -fx-padding: 12;");

        // Vote buttons (optional - can be added later)
        VBox voteBox = new VBox(4);
        voteBox.setAlignment(Pos.TOP_CENTER);
        voteBox.setPrefWidth(40);

        // Reply content
        VBox contentBox = new VBox(4);
        contentBox.setPadding(new Insets(0, 0, 0, 0));

        // Author and time
        Label authorLabel = new Label("u/" + reply.author + " • " + reply.timeAgo);
        authorLabel.setStyle("-fx-text-fill: #818384; -fx-font-size: 11;");

        // Reply content with hyperlink support
        TextFlow contentFlow = createTextFlowWithLinks(reply.content);

        contentBox.getChildren().addAll(authorLabel, contentFlow);
        replyCard.getChildren().addAll(voteBox, contentBox);
        repliesContainer.getChildren().add(replyCard);
    }

    /**
     * Creates a TextFlow with clickable hyperlinks for URLs in the text
     */
    private TextFlow createTextFlowWithLinks(String text) {
        TextFlow textFlow = new TextFlow();
        textFlow.setStyle("-fx-text-fill: #d7dadc; -fx-font-size: 14;");
        textFlow.setLineSpacing(2.0);
        
        if (text == null || text.isEmpty()) {
            return textFlow;
        }

        // Pattern to match URLs (http://, https://, www., or bare domains with common TLDs)
        Pattern urlPattern = Pattern.compile(
            "(?i)\\b((?:https?://|www\\.)[\\w\\-]+(?:\\.[\\w\\-]+)+(?:[\\w\\-.,@?^=%&:/~+#]*[\\w\\-@?^=%&/~+#])?|" +
            "[\\w\\-]+\\.[a-z]{2,}(?:\\.[a-z]{2,})?(?:/[\\w\\-.,@?^=%&:/~+#]*)?)",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher matcher = urlPattern.matcher(text);
        int lastEnd = 0;

        while (matcher.find()) {
            // Add text before the URL
            if (matcher.start() > lastEnd) {
                String beforeText = text.substring(lastEnd, matcher.start());
                Text beforeTextNode = new Text(beforeText);
                beforeTextNode.setStyle("-fx-fill: #d7dadc; -fx-font-size: 14;");
                textFlow.getChildren().add(beforeTextNode);
            }

            // Add the URL as a hyperlink
            String url = matcher.group(1);
            // Ensure URL has protocol - compute final URL to make it effectively final
            final String fullUrl = (!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) 
                ? "https://" + url 
                : url;

            Hyperlink link = new Hyperlink(url);
            link.setStyle("-fx-text-fill: #4A9EFF; -fx-font-size: 14; -fx-underline: true;");
            link.setOnAction(e -> {
                try {
                    Desktop desktop = Desktop.getDesktop();
                    if (desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(new URI(fullUrl));
                    }
                } catch (Exception ex) {
                    System.out.println("Error opening URL: " + ex.getMessage());
                }
            });
            textFlow.getChildren().add(link);

            lastEnd = matcher.end();
        }

        // Add remaining text after the last URL
        if (lastEnd < text.length()) {
            String afterText = text.substring(lastEnd);
            Text afterTextNode = new Text(afterText);
            afterTextNode.setStyle("-fx-fill: #d7dadc; -fx-font-size: 14;");
            textFlow.getChildren().add(afterTextNode);
        }

        // If no URLs were found, add the entire text as regular text
        if (textFlow.getChildren().isEmpty()) {
            Text textNode = new Text(text);
            textNode.setStyle("-fx-fill: #d7dadc; -fx-font-size: 14;");
            textFlow.getChildren().add(textNode);
        }

        return textFlow;
    }

    @FXML
    private void onPostReplyClicked() {
        String replyText = replyTextArea.getText() != null ? replyTextArea.getText().trim() : "";

        if (replyText.isEmpty()) {
            System.out.println("Reply cannot be empty.");
            return;
        }

        try {
            int userId = 1; // For now: all replies by User 1
            replyDAO.createReply(postId, userId, replyText);
            System.out.println("Reply posted successfully.");

            // Clear the text area
            replyTextArea.clear();

            // Hide the editor section after posting
            replyEditorSection.setVisible(false);
            replyEditorSection.setManaged(false);

            // Reload replies to show the new one
            loadReplies();

            // Refresh main feed to update reply counts
            if (parentController != null) {
                parentController.loadPostsFromDB();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error posting reply: " + e.getMessage());
        }
    }

    @FXML
    private void onAddCommentClicked() {
        // Toggle reply editor visibility
        boolean isVisible = replyEditorSection.isVisible();
        replyEditorSection.setVisible(!isVisible);
        replyEditorSection.setManaged(!isVisible);
        
        if (!isVisible && replyTextArea != null) {
            // Focus on the reply text area when showing
            replyTextArea.requestFocus();
        }
    }

    @FXML
    private void onCancelClicked() {
        replyTextArea.clear();
        // Hide the editor section
        replyEditorSection.setVisible(false);
        replyEditorSection.setManaged(false);
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
}
