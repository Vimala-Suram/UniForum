package edu.northeastern.uniforum.forum.controller;

import java.util.List;

import edu.northeastern.uniforum.forum.model.Reply;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
//import javafx.scene.control.Separator;  
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.Group;


public class ForumController {

    @FXML
    private TextField searchField;

    @FXML
    private Button askButton;

    @FXML
    private Button createPostButton;

    @FXML
    private Button profileButton;

    @FXML
    private Button homeNavButton;

    @FXML
    private VBox postContainer;   // holds all posts (each with its replies)

    @FXML
    private VBox recentPostsContainer;   // holds recent posts in sidebar

    @FXML
    private void initialize() {
        // Create user icon for profile button
        createUserIcon();
        
        // Setup navigation button hover effects
        setupNavButtonHovers();
        
        // Create sample Reddit-style posts
        createRedditStylePost(
            "r/JavaHelp",
            "u/javadev123",
            "2 hours ago",
            "JavaFX: ClassNotFoundException when running from Eclipse",
            "This exception usually occurs when module isn't opened/exported. Check your module-info and add 'opens ... to javafx.fxml'.",
            42,
            8,
            false
        );

        createRedditStylePost(
            "r/DatabaseHelp",
            "u/dbadmin",
            "5 hours ago",
            "MySQL connection refused on localhost",
            "Connection refused means MySQL isn't listening. Check if MySQL is running and bound to 0.0.0.0. Verify JDBC URL, username, password.",
            127,
            23,
            true
        );

        createRedditStylePost(
            "r/Programming",
            "u/coder99",
            "1 day ago",
            "Best practices for JavaFX application architecture?",
            "I'm building a JavaFX app and wondering about best practices for structuring controllers, services, and models. Any recommendations?",
            89,
            15,
            false
        );

        // Add recent posts to sidebar
        addRecentPost("Elements", "r/QuizPlanetGame", "3h", 45, 12);
        addRecentPost("Previously Authorized CPT or OPT", "r/f1visa", "5h", 23, 5);
        addRecentPost("Three-Ingredient Chocolate Banana Pancakes", "r/GifRecipes", "8h", 312, 89);
    }
    private Node createReplyNode(Reply reply, int level) {

        HBox row = new HBox(6);

        // LEFT: vertical thread line for nested replies
        if (level > 0) {
            VBox lineBox = new VBox();
            lineBox.setPrefWidth(16);
            lineBox.setAlignment(Pos.TOP_CENTER);

            Region vertical = new Region();
            VBox.setVgrow(vertical, Priority.ALWAYS);
            vertical.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 0 0 2;");

            lineBox.getChildren().add(vertical);
            row.getChildren().add(lineBox);
        }

        // RIGHT: reply content with horizontal top line
        VBox card = new VBox(4);
        card.setPadding(new Insets(4, 8, 4, 8));

        String style = "-fx-background-color: transparent;";
        if (level > 0) {
            // this is the horizontal line at each reply
            style += "-fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;";
        }
        card.setStyle(style);

        Label text = new Label(reply.getText());
        text.setWrapText(true);
        text.setStyle("-fx-font-size: 12; -fx-text-fill: #333333;");

        card.getChildren().add(text);

        // children (nested replies)
        if (reply.getChildren() != null && !reply.getChildren().isEmpty()) {
            VBox childrenBox = new VBox(2);
            for (Reply child : reply.getChildren()) {
                Node childNode = createReplyNode(child, level + 1);
                childrenBox.getChildren().add(childNode);
            }
            card.getChildren().add(childrenBox);
        }

        row.getChildren().add(card);
        return row;
    }




    /**
     * Creates a post card with upvote/downvote, community info, and action buttons
     */
    private void createRedditStylePost(String community, String author, String timeAgo, 
                                       String title, String content, int upvotes, int comments, boolean hasJoinButton) {
        HBox postCard = new HBox(8);
        postCard.setPadding(new Insets(8));
        postCard.setStyle("-fx-background-color: #1a1a1b; -fx-border-color: #343536; -fx-border-width: 0 0 1 0;");

        // LEFT: Upvote/Downvote buttons
        VBox voteBox = new VBox(4);
        voteBox.setAlignment(Pos.TOP_CENTER);
        voteBox.setPrefWidth(40);
        voteBox.setStyle("-fx-padding: 4 0;");

        Button upvoteBtn = new Button("▲");
        upvoteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #818384; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 2 8;");
        
        Label voteCount = new Label(String.valueOf(upvotes));
        voteCount.setStyle("-fx-text-fill: #d7dadc; -fx-font-size: 12; -fx-font-weight: bold;");
        
        Button downvoteBtn = new Button("▼");
        downvoteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #818384; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 2 8;");

        voteBox.getChildren().addAll(upvoteBtn, voteCount, downvoteBtn);

        // CENTER: Post content
        VBox contentBox = new VBox(6);
        contentBox.setPadding(new Insets(4, 0, 4, 0));
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        // Community and metadata row
        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label communityLabel = new Label(community);
        communityLabel.setStyle("-fx-text-fill: #d7dadc; -fx-font-size: 12; -fx-font-weight: bold; -fx-cursor: hand;");

        Label authorLabel = new Label("Posted by " + author + " • " + timeAgo);
        authorLabel.setStyle("-fx-text-fill: #818384; -fx-font-size: 12;");

        if (hasJoinButton) {
            Button joinBtn = new Button("Join");
            joinBtn.setStyle("-fx-background-color: #ff4500; -fx-text-fill: #ffffff; -fx-font-size: 11; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 2 16; -fx-cursor: hand;");
            metaRow.getChildren().addAll(communityLabel, authorLabel, new Region(), joinBtn);
            HBox.setHgrow(metaRow.getChildren().get(2), Priority.ALWAYS);
        } else {
            metaRow.getChildren().addAll(communityLabel, authorLabel);
        }

        // Post title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #d7dadc; -fx-font-size: 18; -fx-font-weight: bold; -fx-cursor: hand; -fx-wrap-text: true;");
        titleLabel.setWrapText(true);

        // Post content
        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-text-fill: #d7dadc; -fx-font-size: 14; -fx-wrap-text: true;");
        contentLabel.setWrapText(true);

        // Action buttons row
        HBox actionRow = new HBox(16);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setStyle("-fx-padding: 8 0 0 0;");

        Button commentBtn = createActionButton("💬 Comment", String.valueOf(comments));
        Button shareBtn = createActionButton("Share", "");
        Button saveBtn = createActionButton("Save", "");
        Button moreBtn = createActionButton("⋯", "");

        actionRow.getChildren().addAll(commentBtn, shareBtn, saveBtn, moreBtn);

        contentBox.getChildren().addAll(metaRow, titleLabel, contentLabel, actionRow);

        postCard.getChildren().addAll(voteBox, contentBox);
        postContainer.getChildren().add(postCard);
    }

    /**
     * Creates an action button for posts (Comment, Share, Save, etc.)
     */
    private Button createActionButton(String text, String count) {
        HBox btnContent = new HBox(4);
        btnContent.setAlignment(Pos.CENTER);

        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-text-fill: #818384; -fx-font-size: 12;");

        if (!count.isEmpty()) {
            Label countLabel = new Label(count);
            countLabel.setStyle("-fx-text-fill: #818384; -fx-font-size: 12;");
            btnContent.getChildren().addAll(textLabel, countLabel);
        } else {
            btnContent.getChildren().add(textLabel);
        }

        Button btn = new Button();
        btn.setGraphic(btnContent);
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4 8;");
        
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #272729; -fx-cursor: hand; -fx-padding: 4 8;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4 8;"));

        return btn;
    }

    /**
     * Adds a recent post to the sidebar
     */
    private void addRecentPost(String title, String community, String timeAgo, int upvotes, int comments) {
        VBox recentPost = new VBox(4);
        recentPost.setPadding(new Insets(8, 0, 8, 0));
        recentPost.setStyle("-fx-border-color: #343536; -fx-border-width: 0 0 1 0;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #d7dadc; -fx-font-size: 13; -fx-font-weight: bold; -fx-cursor: hand; -fx-wrap-text: true;");
        titleLabel.setWrapText(true);

        HBox metaRow = new HBox(8);
        Label communityLabel = new Label(community);
        communityLabel.setStyle("-fx-text-fill: #818384; -fx-font-size: 11;");

        Label timeLabel = new Label(timeAgo);
        timeLabel.setStyle("-fx-text-fill: #818384; -fx-font-size: 11;");

        Label statsLabel = new Label("↑ " + upvotes + " • 💬 " + comments);
        statsLabel.setStyle("-fx-text-fill: #818384; -fx-font-size: 11;");

        metaRow.getChildren().addAll(communityLabel, timeLabel, new Region(), statsLabel);
        HBox.setHgrow(metaRow.getChildren().get(2), Priority.ALWAYS);

        recentPost.getChildren().addAll(titleLabel, metaRow);
        recentPostsContainer.getChildren().add(recentPost);
    }



    @FXML
    private void onSearch() {
        String keyword = searchField.getText();
        System.out.println("Search clicked: " + keyword);
        // later: call service to filter posts and re-render postContainer
    }

    @FXML
    private void onCreatePostClicked() {
        System.out.println("Create Post button clicked");
        // later: load create-post page (new scene or dialog)
    }

    @FXML
    private void onAskClicked() {
        onSearch();
    }

    @FXML
    private void onProfileClicked() {
        System.out.println("Profile button clicked");
        // later: load profile page (new scene or dialog)
    }

    @FXML
    private void onHomeClicked() {
        System.out.println("Home navigation clicked");
    }

    @FXML
    private void onPopularClicked() {
        System.out.println("Popular navigation clicked");
    }

    @FXML
    private void onExploreClicked() {
        System.out.println("Explore navigation clicked");
    }

    @FXML
    private void onAllClicked() {
        System.out.println("All navigation clicked");
    }

    @FXML
    private void onStartCommunityClicked() {
        System.out.println("Start a community clicked");
    }

    /**
     * Sets up hover effects for navigation buttons
     */
    private void setupNavButtonHovers() {
        // Apply hover effects to all nav buttons in the left sidebar
        // All buttons now have the same styling (including Home)
        if (homeNavButton != null && homeNavButton.getParent() != null) {
            VBox navContainer = (VBox) homeNavButton.getParent();
            String normalStyle = "-fx-alignment: CENTER_LEFT; -fx-background-color: transparent; -fx-text-fill: #d7dadc; -fx-background-radius: 12; -fx-padding: 12 16; -fx-font-size: 13; -fx-cursor: hand; -fx-border-color: transparent;";
            String hoverStyle = "-fx-alignment: CENTER_LEFT; -fx-background-color: #2a2b2f; -fx-text-fill: #ffffff; -fx-background-radius: 12; -fx-padding: 12 16; -fx-font-size: 13; -fx-cursor: hand; -fx-border-color: transparent;";
            
            for (javafx.scene.Node node : navContainer.getChildren()) {
                if (node instanceof Button) {
                    Button btn = (Button) node;
                    btn.setStyle(normalStyle);
                    btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
                    btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
                }
            }
        }
    }

    /**
     * Creates a user icon and sets it as the graphic for the profile button
     */
    private void createUserIcon() {
        // Create a simple user icon: circle for head + ellipse for body
        Group userIcon = new Group();
        
        // Head (circle)
        Circle head = new Circle(6);
        head.setFill(javafx.scene.paint.Color.valueOf("#d7dadc"));
        head.setTranslateX(12);
        head.setTranslateY(10);
        
        // Body (ellipse for shoulders/torso)
        Ellipse body = new Ellipse(8, 6);
        body.setFill(javafx.scene.paint.Color.valueOf("#d7dadc"));
        body.setTranslateX(12);
        body.setTranslateY(20);
        
        userIcon.getChildren().addAll(head, body);
        
        // Set the icon as the button's graphic
        profileButton.setGraphic(userIcon);
        profileButton.setText(""); // Remove any text
    }
}
