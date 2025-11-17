package edu.northeastern.uniforum.forum.controller;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ForumController {

    @FXML
    private TextField searchField;

    @FXML
    private Button newPostButton;

    @FXML
    private VBox postContainer;   // holds all posts (each with its replies)

    @FXML
    private void initialize() {
        // temporary dummy data to see layout
        addPostWithReplies(
                "JavaFX: ClassNotFoundException when running from Eclipse",
                "I’m trying to run my JavaFX app but keep getting ClassNotFoundException...",
                new String[]{
                        "Check if your module-info exports/opens the correct packages.",
                        "Also verify that the VM options include the JavaFX module path."
                });

        addPostWithReplies(
                "MySQL connection refused on localhost",
                "Database is running but Java can’t connect. Getting 'Connection refused'.",
                new String[]{
                        "Make sure MySQL is listening on 3306 and not bound to 127.0.0.1 only.",
                        "Also confirm your JDBC URL and user permissions."
                });
    }

    /**
     * Creates a “post card” with title, body and replies underneath.
     */
    private void addPostWithReplies(String title, String body, String[] replies) {
        VBox postCard = new VBox();
        postCard.setSpacing(6);
        postCard.setPadding(new Insets(10));
        postCard.setStyle(
                "-fx-background-color: #f9f9f9; " +
                "-fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 4, 0, 0, 1);"
        );

        // ---------- TITLE ONLY (default view) ----------
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-cursor: hand;");

        // Hidden replies container
        VBox repliesBox = new VBox(4);
        repliesBox.setPadding(new Insets(6, 0, 0, 12));
        repliesBox.setVisible(false);    // hidden initially
        repliesBox.setManaged(false);    // does not take space when hidden

        // add reply lines
        if (replies != null && replies.length > 0) {
            Label repliesHeader = new Label("Replies:");
            repliesHeader.setStyle("-fx-font-size: 12; -fx-font-weight: bold;");

            repliesBox.getChildren().add(repliesHeader);

            for (String reply : replies) {
                HBox replyRow = new HBox(6);
                Label bullet = new Label("•");
                Label replyLabel = new Label(reply);
                replyLabel.setWrapText(true);
                replyLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #444;");
                replyRow.getChildren().addAll(bullet, replyLabel);

                repliesBox.getChildren().add(replyRow);
            }
        }

        // ---------- CLICK BEHAVIOR ----------
        titleLabel.setOnMouseClicked(e -> {
            boolean show = !repliesBox.isVisible();
            repliesBox.setVisible(show);
            repliesBox.setManaged(show);
        });

        postCard.getChildren().addAll(titleLabel, repliesBox);
        postContainer.getChildren().add(postCard);
    }


    @FXML
    private void onSearch() {
        String keyword = searchField.getText();
        System.out.println("Search clicked: " + keyword);
        // later: call service to filter posts and re-render postContainer
    }

    @FXML
    private void onNewPostClicked() {
        System.out.println("Pencil (new post) clicked");
        // later: load create-post page (new scene or dialog)
    }
}
