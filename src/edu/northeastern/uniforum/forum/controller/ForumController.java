package edu.northeastern.uniforum.forum.controller;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import edu.northeastern.uniforum.forum.dao.PostDAO;
import edu.northeastern.uniforum.forum.dao.UserDAO;
import edu.northeastern.uniforum.forum.model.Reply;
import edu.northeastern.uniforum.forum.model.User;
import edu.northeastern.uniforum.forum.util.SceneManager;
import edu.northeastern.uniforum.forum.util.TimeUtil;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
//import javafx.scene.control.Separator;  
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.Group;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;


public class ForumController {

    @FXML
    private TextField searchField;

    @FXML
    private Button askButton;

    @FXML
    private Button createPostButton;

    @FXML
    private Label usernameLabel;

    @FXML
    private Button homeNavButton;

    @FXML
    private VBox postContainer;   // holds all posts (each with its replies)

    @FXML
    private VBox recentPostsContainer;   // holds recent posts in sidebar

    @FXML
    private StackPane modalOverlay;   // overlay for modal dialogs

    @FXML
    private VBox dialogContainer;   // container for dialog content

    @FXML
    private Region overlayBackground;   // background overlay

    private List<PostDAO.PostDTO> cachedPosts = new ArrayList<>();
    private PauseTransition searchDebounce;
    private User currentUser;
    private final PostDAO postDAO = new PostDAO();

    @FXML
    private void initialize() {
        // Setup navigation button hover effects
        setupNavButtonHovers();

        setupSearchInteractions();
        
        // Create sample Reddit-style posts
        loadPostsFromDB(); 
    }
    
    public void initData(User user) {
        this.currentUser = user;
        System.out.println("✓ Forum initialized for: " + user.getUsername());
        
        // Set username label
        if (usernameLabel != null && user != null) {
            usernameLabel.setText(user.getUsername());
        }
        
        loadPostsFromDB();
    }

    /**
     * Configures search field enter key handling and debounce
     */
    private void setupSearchInteractions() {
        if (searchField == null) {
            return;
        }

        searchDebounce = new PauseTransition(Duration.millis(400));
        searchDebounce.setOnFinished(e -> performSearch());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Restart debounce timer on every keystroke
            searchDebounce.stop();
            searchDebounce.playFromStart();
        });

        // Trigger search immediately when user presses Enter
        searchField.setOnAction(e -> performSearch());
    }

    public void loadPostsFromDB() {
        try {
            // Default to Home view (posts from joined communities) if user is logged in
            if (currentUser != null) {
                List<PostDAO.PostDTO> posts = postDAO.getPostsFromJoinedCommunities(currentUser.getUserId());
                cachedPosts = posts != null ? posts : new ArrayList<>();
                System.out.println("Controller: Home posts.size = " + cachedPosts.size());
            } else {
                // If no user logged in, show all posts sorted by time
                List<PostDAO.PostDTO> posts = postDAO.getAllPosts();
                cachedPosts = posts != null ? posts : new ArrayList<>();
                System.out.println("Controller: posts.size = " + cachedPosts.size());
            }

            // Re-run current search (or show all if no search input)
            performSearch();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading DB posts: " + e.getMessage());
        }
    }

    /**
     * Renders the given list of posts into the main feed
     */
    private void renderPosts(List<PostDAO.PostDTO> postsToRender) {
        postContainer.getChildren().clear();

        if (postsToRender == null || postsToRender.isEmpty()) {
            String message = "No posts available.";
            String keyword = searchField != null ? searchField.getText() : "";
            if (keyword != null && !keyword.trim().isEmpty()) {
                message = "No posts match \"" + keyword.trim() + "\".";
            }

            Label emptyLabel = new Label(message);
            emptyLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 14; -fx-padding: 24;");
            postContainer.getChildren().add(emptyLabel);
            return;
        }

        for (PostDAO.PostDTO post : postsToRender) {
            createRedditStylePost(
                post.postId,
                post.community,
                post.author,
                post.timeAgo,
                post.title,
                post.content,
                post.upvotes,
                post.comments,
                post.tag,
                false
            );
        }
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
        text.setStyle("-fx-font-size: 12; -fx-text-fill: #555555;");

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
    private void createRedditStylePost(int postId, String community, String author, String timeAgo, 
                                       String title, String content, int upvotes, int comments, String tag, boolean hasJoinButton) {
        HBox postCard = new HBox(8);
        postCard.setPadding(new Insets(8));
        postCard.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0; -fx-cursor: hand; -fx-background-radius: 8;");
        
        // Make entire post clickable to open detail view
        postCard.setOnMouseClicked(e -> openPostDetail(postId));

        // LEFT: Upvote/Downvote buttons
        VBox voteBox = new VBox(4);
        voteBox.setAlignment(Pos.TOP_CENTER);
        voteBox.setPrefWidth(40);
        voteBox.setStyle("-fx-padding: 4 0;");

        // Create vote count label first so it can be referenced in button handlers
        Label voteCount = new Label(String.valueOf(upvotes));
        voteCount.setStyle("-fx-text-fill: #3D348B; -fx-font-size: 12; -fx-font-weight: bold;");
        
        // Check user's current vote status
        int userVote = 0;
        if (currentUser != null) {
            try {
                userVote = postDAO.getUserVote(postId, currentUser.getUserId());
            } catch (SQLException e) {
                System.err.println("Error checking user vote: " + e.getMessage());
            }
        }
        
        // Default styles
        String defaultUpvoteStyle = "-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 2 8;";
        String defaultDownvoteStyle = "-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 2 8;";
        String votedUpvoteStyle = "-fx-background-color: transparent; -fx-text-fill: #7678ED; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 2 8; -fx-font-weight: bold;";
        String votedDownvoteStyle = "-fx-background-color: transparent; -fx-text-fill: #7678ED; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 2 8; -fx-font-weight: bold;";
        
        // Create both buttons first
        Button upvoteBtn = new Button("▲");
        Button downvoteBtn = new Button("▼");
        
        // Set initial styles
        upvoteBtn.setStyle(userVote == 1 ? votedUpvoteStyle : defaultUpvoteStyle);
        downvoteBtn.setStyle(userVote == -1 ? votedDownvoteStyle : defaultDownvoteStyle);
        
        // Set up click handlers after both buttons are created
        upvoteBtn.setOnMouseClicked(e -> {
            e.consume(); // Prevent opening detail when clicking vote
            handleUpvote(postId, voteCount, upvoteBtn, downvoteBtn);
        });
        
        downvoteBtn.setOnMouseClicked(e -> {
            e.consume(); // Prevent opening detail when clicking vote
            handleDownvote(postId, voteCount, upvoteBtn, downvoteBtn);
        });

        voteBox.getChildren().addAll(upvoteBtn, voteCount, downvoteBtn);

        // CENTER: Post content
        VBox contentBox = new VBox(6);
        contentBox.setPadding(new Insets(4, 0, 4, 0));
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        // Community and metadata row
        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label communityLabel = new Label(community);
        communityLabel.setStyle("-fx-text-fill: #3D348B; -fx-font-size: 12; -fx-font-weight: bold; -fx-cursor: hand;");

        // Build author and time label with clickable author name
        HBox authorTimeBox = new HBox(4);
        authorTimeBox.setAlignment(Pos.CENTER_LEFT);
        
        Label postedByLabel = new Label("Posted by ");
        postedByLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 12;");
        
        Label authorNameLabel = new Label(author);
        authorNameLabel.setStyle("-fx-text-fill: #7678ED; -fx-font-size: 12; -fx-cursor: hand; -fx-underline: true;");
        authorNameLabel.setOnMouseClicked(e -> navigateToUserSettings(author));
        authorNameLabel.setOnMouseEntered(e -> authorNameLabel.setStyle("-fx-text-fill: #3D348B; -fx-font-size: 12; -fx-cursor: hand; -fx-underline: true;"));
        authorNameLabel.setOnMouseExited(e -> authorNameLabel.setStyle("-fx-text-fill: #7678ED; -fx-font-size: 12; -fx-cursor: hand; -fx-underline: true;"));
        
        Label timeLabel = new Label(" • " + timeAgo);
        timeLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 12;");
        
        authorTimeBox.getChildren().addAll(postedByLabel, authorNameLabel, timeLabel);

        // Add tag as a styled box if present
        if (tag != null && !tag.trim().isEmpty()) {
            HBox tagBox = new HBox(6);
            tagBox.setAlignment(Pos.CENTER_LEFT);
            tagBox.setStyle("-fx-background-color: #7678ED; -fx-background-radius: 12; -fx-padding: 4 8;");
            
            Label tagLabel = new Label(tag);
            tagLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 12;");
            
            tagBox.getChildren().add(tagLabel);
            
            if (hasJoinButton) {
                Button joinBtn = new Button("Join");
                joinBtn.setStyle("-fx-background-color: #7678ED; -fx-text-fill: white; -fx-font-size: 11; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 2 16; -fx-cursor: hand;");
                metaRow.getChildren().addAll(communityLabel, authorTimeBox, tagBox, new Region(), joinBtn);
                HBox.setHgrow(metaRow.getChildren().get(3), Priority.ALWAYS);
            } else {
                metaRow.getChildren().addAll(communityLabel, authorTimeBox, tagBox);
            }
        } else {
            if (hasJoinButton) {
                Button joinBtn = new Button("Join");
                joinBtn.setStyle("-fx-background-color: #7678ED; -fx-text-fill: white; -fx-font-size: 11; -fx-font-weight: bold; -fx-background-radius: 999; -fx-padding: 2 16; -fx-cursor: hand;");
                metaRow.getChildren().addAll(communityLabel, authorTimeBox, new Region(), joinBtn);
                HBox.setHgrow(metaRow.getChildren().get(2), Priority.ALWAYS);
            } else {
                metaRow.getChildren().addAll(communityLabel, authorTimeBox);
            }
        }

        // Post title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #3D348B; -fx-font-size: 18; -fx-font-weight: bold; -fx-cursor: hand; -fx-wrap-text: true;");
        titleLabel.setWrapText(true);

        // Post content - show only 1 line with ellipsis
        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 14;");
        contentLabel.setWrapText(false);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.setTextOverrun(javafx.scene.control.OverrunStyle.ELLIPSIS);

        // Action buttons row
        HBox actionRow = new HBox(16);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        actionRow.setStyle("-fx-padding: 8 0 0 0;");

        Button commentBtn = createActionButton("💬 Comment", String.valueOf(comments));
        commentBtn.setOnMouseClicked(e -> {
            e.consume(); // Prevent opening detail when clicking comment
            openPostDetail(postId); // Open post detail view with reply functionality
        });

        actionRow.getChildren().addAll(commentBtn);

        contentBox.getChildren().addAll(metaRow, titleLabel, contentLabel, actionRow);

        postCard.getChildren().addAll(voteBox, contentBox);
        postContainer.getChildren().add(postCard);
    }

    /**
     * Opens the post detail popup with replies
     */
    private void openPostDetail(int postId) {
        try {
            // Get the full post data
            PostDAO.PostDTO postData = null;
            
            // Find the post with matching postId
            var allPosts = postDAO.getAllPosts();
            for (PostDAO.PostDTO post : allPosts) {
                if (post.postId == postId) {
                    postData = post;
                    break;
                }
            }
            
            if (postData == null) {
                System.out.println("Post not found with id: " + postId);
                return;
            }

            // Load the post detail FXML
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/edu/northeastern/uniforum/forum/view/post_detail.fxml")
            );
            Parent dialogContent = loader.load();
            
            // Get the ReplyController to set post data
            ReplyController replyController = loader.getController();
            if (replyController != null) {
                replyController.setParentController(this);
                replyController.setCurrentUser(currentUser);
                replyController.setPostData(postData);
            }

            // Clear previous dialog content
            dialogContainer.getChildren().clear();
            dialogContainer.getChildren().add(dialogContent);

            // Size the dialog relative to the main window (80% of window size)
            Stage mainStage = (Stage) modalOverlay.getScene().getWindow();
            double windowWidth = mainStage.getWidth();
            double windowHeight = mainStage.getHeight();
            
            // Set dialog size (70% of window width, 80% of height, but cap at max sizes)
            double dialogWidth = Math.min(windowWidth * 0.70, 750);
            double dialogHeight = Math.min(windowHeight * 0.80, 600);
            
            dialogContainer.setPrefWidth(dialogWidth);
            dialogContainer.setPrefHeight(dialogHeight);
            dialogContainer.setMaxWidth(dialogWidth);
            dialogContainer.setMaxHeight(dialogHeight);
            
            // Style the dialog container
            dialogContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 0);");

            // Show the overlay
            modalOverlay.setVisible(true);
            modalOverlay.setManaged(true);
            
            // Make overlay fill the entire window
            StackPane root = (StackPane) modalOverlay.getParent();
            if (root != null) {
                modalOverlay.prefWidthProperty().bind(root.widthProperty());
                modalOverlay.prefHeightProperty().bind(root.heightProperty());
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error opening post detail: " + e.getMessage());
        }
    }

    /**
     * Creates an action button for posts (Comment, Share, Save, etc.)
     */
    private Button createActionButton(String text, String count) {
        HBox btnContent = new HBox(4);
        btnContent.setAlignment(Pos.CENTER);

        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 12;");

        if (!count.isEmpty()) {
            Label countLabel = new Label(count);
            countLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 12;");
            btnContent.getChildren().addAll(textLabel, countLabel);
        } else {
            btnContent.getChildren().add(textLabel);
        }

        Button btn = new Button();
        btn.setGraphic(btnContent);
        btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4 8;");
        
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #F5F5F5; -fx-cursor: hand; -fx-padding: 4 8; -fx-background-radius: 4;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4 8;"));

        return btn;
    }

    /**
     * Adds a recent post to the sidebar
     */
    private void addRecentPost(String title, String community, String timeAgo, int upvotes, int comments) {
        VBox recentPost = new VBox(4);
        recentPost.setPadding(new Insets(8, 0, 8, 0));
        recentPost.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #3D348B; -fx-font-size: 13; -fx-font-weight: bold; -fx-cursor: hand; -fx-wrap-text: true;");
        titleLabel.setWrapText(true);

        HBox metaRow = new HBox(8);
        Label communityLabel = new Label(community);
        communityLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 11;");

        Label timeLabel = new Label(timeAgo);
        timeLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 11;");

        Label statsLabel = new Label("↑ " + upvotes + " • 💬 " + comments);
        statsLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 11;");

        metaRow.getChildren().addAll(communityLabel, timeLabel, new Region(), statsLabel);
        HBox.setHgrow(metaRow.getChildren().get(2), Priority.ALWAYS);

        recentPost.getChildren().addAll(titleLabel, metaRow);
        recentPostsContainer.getChildren().add(recentPost);
    }



    @FXML
    private void onSearch() {
        performSearch();
    }

    private void performSearch() {
        if (cachedPosts == null) {
            cachedPosts = new ArrayList<>();
        }

        String keyword = searchField != null ? searchField.getText() : "";
        if (keyword == null || keyword.trim().isEmpty()) {
            // No search keyword - display posts by created_time (newest first)
            // Posts are already sorted by created_time DESC from the database
            renderPosts(cachedPosts);
            return;
        }

        String lowered = keyword.trim().toLowerCase();
        List<PostDAO.PostDTO> filtered = new ArrayList<>();

        for (PostDAO.PostDTO post : cachedPosts) {
            String title = post.title != null ? post.title.toLowerCase() : "";
            String content = post.content != null ? post.content.toLowerCase() : "";

            if (title.contains(lowered) || content.contains(lowered)) {
                filtered.add(post);
            }
        }

        // Sort filtered search results by upvotes (most upvoted first)
        filtered.sort((a, b) -> Integer.compare(b.upvotes, a.upvotes));

        renderPosts(filtered);
    }

    @FXML
    private void onCreatePostClicked() {
        try {
            // Load the create post FXML
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/edu/northeastern/uniforum/forum/view/create_post.fxml")
            );
            Parent dialogContent = loader.load();
            
            // Get the CreatePostController to set a reference to this controller for closing
            CreatePostController dialogController = loader.getController();
            if (dialogController != null) {
                dialogController.setParentController(this);
                dialogController.setCurrentUser(currentUser);
            }

            // Clear previous dialog content
            dialogContainer.getChildren().clear();
            dialogContainer.getChildren().add(dialogContent);

            // Size the dialog relative to the main window (70% of window size)
            Stage mainStage = (Stage) modalOverlay.getScene().getWindow();
            double windowWidth = mainStage.getWidth();
            double windowHeight = mainStage.getHeight();
            
            // Set dialog size (70% of window, but cap at max sizes)
            double dialogWidth = Math.min(windowWidth * 0.7, 650);
            double dialogHeight = Math.min(windowHeight * 0.75, 550);
            
            dialogContainer.setPrefWidth(dialogWidth);
            dialogContainer.setPrefHeight(dialogHeight);
            dialogContainer.setMaxWidth(dialogWidth);
            dialogContainer.setMaxHeight(dialogHeight);
            
            // Style the dialog container
            dialogContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 0);");

            // Show the overlay
            modalOverlay.setVisible(true);
            modalOverlay.setManaged(true);
            
            // Make overlay fill the entire window
            StackPane root = (StackPane) modalOverlay.getParent();
            if (root != null) {
                modalOverlay.prefWidthProperty().bind(root.widthProperty());
                modalOverlay.prefHeightProperty().bind(root.heightProperty());
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error opening create post dialog: " + e.getMessage());
        }
    }

    /**
     * Closes the modal overlay
     */
    public void closeModal() {
        if (modalOverlay != null) {
            modalOverlay.setVisible(false);
            modalOverlay.setManaged(false);
            dialogContainer.getChildren().clear();
        }
    }

    /**
     * Handles overlay background click to close dialog
     * Only closes if clicked directly on the background, not on dialog content
     */
    @FXML
    private void onOverlayClicked(javafx.scene.input.MouseEvent event) {
        // Only close if clicked directly on the overlay background, not on dialog
        if (event.getTarget() == overlayBackground) {
            closeModal();
        }
    }

    @FXML
    private void onAskClicked() {
        performSearch();
    }

    @FXML
    private void onProfileClicked(MouseEvent event) {
        if (currentUser != null) {
            SceneManager.switchToSettings(currentUser);
        }
    }

    @FXML
    private void onHomeClicked() {
        if (currentUser == null) {
            System.out.println("User must be logged in to view home feed.");
            return;
        }
        
        try {
            // Get posts from communities the user has joined, sorted by most recent
            List<PostDAO.PostDTO> posts = postDAO.getPostsFromJoinedCommunities(currentUser.getUserId());
            cachedPosts = posts != null ? posts : new ArrayList<>();
            
            System.out.println("Controller: Home posts.size = " + cachedPosts.size());
            
            // Clear search field
            if (searchField != null) {
                searchField.clear();
            }
            
            // Render posts
            renderPosts(cachedPosts);
        } catch (SQLException e) {
            System.err.println("Error loading home posts: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void onPopularClicked() {
        System.out.println("Popular navigation clicked");
    }

    @FXML
    private void onExploreClicked() {
        try {
            // Get all posts sorted by number of likes (descending)
            List<PostDAO.PostDTO> posts = postDAO.getAllPostsSortedByLikes();
            cachedPosts = posts != null ? posts : new ArrayList<>();
            
            System.out.println("Controller: Explore posts.size = " + cachedPosts.size());
            
            // Clear search field
            if (searchField != null) {
                searchField.clear();
            }
            
            // Render posts
            renderPosts(cachedPosts);
        } catch (SQLException e) {
            System.err.println("Error loading explore posts: " + e.getMessage());
            e.printStackTrace();
        }
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
     * Navigates to settings view for a given username
     */
    private void navigateToUserSettings(String username) {
        UserDAO userDAO = new UserDAO();
        User user = userDAO.getUserByUsername(username);
        if (user != null) {
            SceneManager.switchToSettings(user);
        } else {
            System.out.println("User not found: " + username);
        }
    }

    /**
     * Handles upvote button click - checks if user already voted
     */
    private void handleUpvote(int postId, Label voteCountLabel, Button upvoteBtn, Button downvoteBtn) {
        if (currentUser == null) {
            System.out.println("User must be logged in to vote.");
            return;
        }
        
        try {
            boolean voteAdded = postDAO.handleUpvote(postId, currentUser.getUserId());
            
            // Get updated vote count from database
            int newCount = postDAO.getVoteCount(postId);
            voteCountLabel.setText(String.valueOf(newCount));
            
            // Update cached post data
            for (PostDAO.PostDTO post : cachedPosts) {
                if (post.postId == postId) {
                    post.upvotes = newCount;
                    break;
                }
            }
            
            // Update button colors based on vote status
            String defaultUpvoteStyle = "-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 2 8;";
            String defaultDownvoteStyle = "-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 2 8;";
            String votedUpvoteStyle = "-fx-background-color: transparent; -fx-text-fill: #7678ED; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 2 8; -fx-font-weight: bold;";
            String votedDownvoteStyle = "-fx-background-color: transparent; -fx-text-fill: #7678ED; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 2 8; -fx-font-weight: bold;";
            
            if (voteAdded) {
                // Vote was added, check current status
                int currentVote = postDAO.getUserVote(postId, currentUser.getUserId());
                if (currentVote == 1) {
                    upvoteBtn.setStyle(votedUpvoteStyle);
                    downvoteBtn.setStyle(defaultDownvoteStyle);
                } else {
                    upvoteBtn.setStyle(defaultUpvoteStyle);
                    downvoteBtn.setStyle(defaultDownvoteStyle);
                }
            } else {
                // Vote was removed
                upvoteBtn.setStyle(defaultUpvoteStyle);
                downvoteBtn.setStyle(defaultDownvoteStyle);
            }
            
            if (!voteAdded) {
                System.out.println("Upvote removed (user had already upvoted).");
            }
        } catch (SQLException e) {
            System.err.println("Error handling upvote: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handles downvote button click - checks if user already voted
     */
    private void handleDownvote(int postId, Label voteCountLabel, Button upvoteBtn, Button downvoteBtn) {
        if (currentUser == null) {
            System.out.println("User must be logged in to vote.");
            return;
        }
        
        try {
            boolean voteAdded = postDAO.handleDownvote(postId, currentUser.getUserId());
            
            // Get updated vote count from database
            int newCount = postDAO.getVoteCount(postId);
            voteCountLabel.setText(String.valueOf(newCount));
            
            // Update cached post data
            for (PostDAO.PostDTO post : cachedPosts) {
                if (post.postId == postId) {
                    post.upvotes = newCount;
                    break;
                }
            }
            
            // Update button colors based on vote status
            String defaultUpvoteStyle = "-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 2 8;";
            String defaultDownvoteStyle = "-fx-background-color: transparent; -fx-text-fill: #555555; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 2 8;";
            String votedUpvoteStyle = "-fx-background-color: transparent; -fx-text-fill: #7678ED; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 2 8; -fx-font-weight: bold;";
            String votedDownvoteStyle = "-fx-background-color: transparent; -fx-text-fill: #7678ED; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 2 8; -fx-font-weight: bold;";
            
            if (voteAdded) {
                // Vote was added, check current status
                int currentVote = postDAO.getUserVote(postId, currentUser.getUserId());
                if (currentVote == -1) {
                    upvoteBtn.setStyle(defaultUpvoteStyle);
                    downvoteBtn.setStyle(votedDownvoteStyle);
                } else {
                    upvoteBtn.setStyle(defaultUpvoteStyle);
                    downvoteBtn.setStyle(defaultDownvoteStyle);
                }
            } else {
                // Vote was removed
                upvoteBtn.setStyle(defaultUpvoteStyle);
                downvoteBtn.setStyle(defaultDownvoteStyle);
            }
            
            if (!voteAdded) {
                System.out.println("Downvote removed (user had already downvoted).");
            }
        } catch (SQLException e) {
            System.err.println("Error handling downvote: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets up hover effects for navigation buttons
     */
    private void setupNavButtonHovers() {
        // Apply hover effects to all nav buttons in the left sidebar
        // All buttons now have the same styling (including Home)
        if (homeNavButton != null && homeNavButton.getParent() != null) {
            VBox navContainer = (VBox) homeNavButton.getParent();
            String normalStyle = "-fx-alignment: CENTER_LEFT; -fx-background-color: transparent; -fx-text-fill: #3D348B; -fx-background-radius: 12; -fx-padding: 12 16; -fx-font-size: 13; -fx-cursor: hand; -fx-border-color: transparent;";
            String hoverStyle = "-fx-alignment: CENTER_LEFT; -fx-background-color: #F5F5F5; -fx-text-fill: #3D348B; -fx-background-radius: 12; -fx-padding: 12 16; -fx-font-size: 13; -fx-cursor: hand; -fx-border-color: transparent;";
            
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

}
