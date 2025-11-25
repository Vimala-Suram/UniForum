package edu.northeastern.uniforum.forum.controller;

import edu.northeastern.uniforum.forum.dao.PostDAO;
import edu.northeastern.uniforum.forum.dao.UserDAO;
import edu.northeastern.uniforum.forum.model.User;
import edu.northeastern.uniforum.forum.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.ListCell;
import javafx.scene.control.CheckBox;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.util.Callback;
import javafx.application.Platform;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseSelectionController {

    @FXML private ComboBox<String> deptCombo;
    @FXML private ListView<String> courseListView;
    @FXML private Button nextBtn;
    @FXML private Label selectedCoursesLabel;
    
    private User currentUser;
    private final PostDAO postDAO = new PostDAO();
    private final UserDAO userDAO = new UserDAO();
    
    // Map to store course names to community IDs
    private Map<String, Integer> courseToCommunityMap = new HashMap<>();
    private List<Integer> selectedCommunityIds = new ArrayList<>();
    
    // Track all selected courses across all departments
    private List<String> allSelectedCourses = new ArrayList<>();
    
    // Flag to prevent infinite loop when programmatically updating selection
    private boolean isUpdatingSelection = false;

    /**
     * Sets the current logged-in user and loads their existing course selections
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            initializeCommunities();
            // Load existing courses after communities are initialized (so courseToCommunityMap is populated)
            Platform.runLater(() -> {
                loadUserExistingCourses();
            });
        }
    }
    
    /**
     * Loads the user's existing course selections from the database
     */
    private void loadUserExistingCourses() {
        if (currentUser == null) {
            return;
        }
        
        try {
            // Get user's current community IDs
            List<Integer> userCommunityIds = userDAO.getUserCommunities(currentUser.getUserId());
            
            // Map community IDs to course names
            allSelectedCourses.clear();
            for (Map.Entry<String, Integer> entry : courseToCommunityMap.entrySet()) {
                if (userCommunityIds.contains(entry.getValue())) {
                    allSelectedCourses.add(entry.getKey());
                }
            }
            
            System.out.println("Loaded " + allSelectedCourses.size() + " existing courses for user: " + currentUser.getUsername());
            
            // Update the label
            updateSelectedCoursesLabel();
            
            // Refresh the list view if a department is selected
            if (deptCombo != null && deptCombo.getValue() != null) {
                Platform.runLater(() -> {
                    if (courseListView != null) {
                        courseListView.refresh();
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading user's existing courses: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        // Initialize department dropdown
        if (deptCombo != null) {
        deptCombo.getItems().addAll("IS", "SES", "DAMG", "TELE");
            deptCombo.setOnAction(e -> updateCourseList());
        }
        
        // Enable multiple selection in ListView with CheckBoxListCell for easy selection
        if (courseListView != null) {
            courseListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            
            // Use CheckBoxListCell for easy multiple selection (no Ctrl/Cmd needed)
            courseListView.setCellFactory(CheckBoxListCell.forListView(item -> {
                javafx.beans.property.SimpleBooleanProperty property = 
                    new javafx.beans.property.SimpleBooleanProperty(allSelectedCourses.contains(item));
                
                // When checkbox is toggled, update allSelectedCourses
                property.addListener((obs, oldVal, newVal) -> {
                    if (!isUpdatingSelection) {
                        if (newVal) {
                            // Checkbox checked - add to selection
                            if (!allSelectedCourses.contains(item)) {
                                allSelectedCourses.add(item);
                            }
                        } else {
                            // Checkbox unchecked - remove from selection
                            allSelectedCourses.remove(item);
                        }
                        updateSelectedCoursesLabel();
                    }
                });
                
                return property;
            }));
        }
        
        // Update selected courses label when selection changes (backup listener)
        if (courseListView != null) {
            courseListView.getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener.Change<? extends String> change) -> {
                    // Only update if we're not programmatically changing selection
                    if (!isUpdatingSelection) {
                        updateSelectedCoursesFromCurrentDepartment();
                    }
                }
            );
        }

        if (nextBtn != null) {
            nextBtn.setOnAction(e -> saveSelectionsAndContinue());
        }
        
        // Initialize communities if user is already set
        if (currentUser != null) {
            initializeCommunities();
        }
    }
    
    /**
     * Updates the course list based on selected department
     */
    private void updateCourseList() {
        if (courseListView == null || deptCombo == null) {
            return;
        }
        
        String selectedDept = deptCombo.getValue();
        if (selectedDept == null) {
            courseListView.getItems().clear();
            return;
        }
        
        List<String> courses = new ArrayList<>();
        switch (selectedDept) {
            case "IS" -> courses.addAll(List.of(
                    "INFO 5002-01 Intro to Python for Info Sys",
                    "INFO 5100-04 Application Engineer & Dev",
                    "INFO 6105-01 Data Sci Eng Methods",
                    "INFO 6106-01 Neural Modeling Methods & Tool"
            ));
            case "SES" -> courses.addAll(List.of(
                    "CSYE 6225-03 Netwrk Strctrs & Cloud Cmpting",
                    "CSYE 7105-01 Parallel Machine Learning & AI",
                    "CSYE 7280-01 User Experience Design/Testing",
                    "CSYE 7380-02 Theory & Prac App AI Gen Model"
            ));
            case "DAMG" -> courses.addAll(List.of(
                    "DAMG 6210-01 Data Mgt and Database Design",
                    "DAMG 7250-01 Big Data Architec & Governance",
                    "DAMG 7374-01 ST: Gen AI w/ LLM in Data Eng",
                    "DAMG 7245-02 Big Data Sys & Intel Analytics"
            ));
            case "TELE" -> courses.addAll(List.of(
                    "TELE 5330-01 Data Networking",
                    "TELE 6530-01 Connected Devices",
                    "TELE 7374-02 Special Topics: Building Digital Twins",
                    "TELE 5600-01 Linux for Network Engineers"
            ));
        }
        
        courseListView.setItems(FXCollections.observableArrayList(courses));
        
        // Refresh the list view to update checkbox states
        // The CheckBoxListCell will automatically sync with allSelectedCourses
        Platform.runLater(() -> {
            if (courseListView != null) {
                courseListView.refresh();
            }
        });
    }
    
    /**
     * Updates selected courses from current department and adds to overall selection
     */
    private void updateSelectedCoursesFromCurrentDepartment() {
        if (courseListView == null || selectedCoursesLabel == null) {
            return;
        }
        
        ObservableList<String> selected = courseListView.getSelectionModel().getSelectedItems();
        String currentDept = deptCombo != null ? deptCombo.getValue() : null;
        
        if (currentDept == null) {
            return;
        }
        
        // Get all courses from the current department that should be selected
        List<String> coursesInCurrentDept = new ArrayList<>();
        for (String course : courseListView.getItems()) {
            if (isCourseFromDepartment(course, currentDept)) {
                coursesInCurrentDept.add(course);
            }
        }
        
        // Remove all courses from this department from allSelectedCourses
        // (to handle both selection and deselection)
        List<String> coursesToRemove = new ArrayList<>();
        for (String existingCourse : allSelectedCourses) {
            if (isCourseFromDepartment(existingCourse, currentDept)) {
                coursesToRemove.add(existingCourse);
            }
        }
        allSelectedCourses.removeAll(coursesToRemove);
        
        // Add all currently selected courses from this department
        // This ensures multiple selections are preserved
        for (String course : selected) {
            if (isCourseFromDepartment(course, currentDept) && !allSelectedCourses.contains(course)) {
                allSelectedCourses.add(course);
            }
        }
        
        // Update display
        updateSelectedCoursesLabel();
    }
    
    /**
     * Checks if a course belongs to a specific department
     */
    private boolean isCourseFromDepartment(String course, String department) {
        switch (department) {
            case "IS" -> {
                return course.startsWith("INFO");
            }
            case "SES" -> {
                return course.startsWith("CSYE");
            }
            case "DAMG" -> {
                return course.startsWith("DAMG");
            }
            case "TELE" -> {
                return course.startsWith("TELE");
            }
        }
        return false;
    }
    
    /**
     * Initializes communities in database if they don't exist
     */
    private void initializeCommunities() {
        try {
            // Get all existing communities
            List<PostDAO.CommunityDTO> existingCommunities = postDAO.getAllCommunities();
            Map<String, Integer> existingMap = new HashMap<>();
            for (PostDAO.CommunityDTO comm : existingCommunities) {
                existingMap.put(comm.name, comm.id);
            }
            
            // Create course to community mapping and ensure communities exist
            createCourseCommunityMapping(existingMap);
        } catch (SQLException e) {
            System.err.println("Error initializing communities: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates mapping between course names and community IDs, creating communities if needed
     */
    private void createCourseCommunityMapping(Map<String, Integer> existingCommunities) {
        courseToCommunityMap.clear();
        
        // All course names that should exist as communities
        List<String> allCourses = new ArrayList<>();
        allCourses.add("INFO 5002-01 Intro to Python for Info Sys");
        allCourses.add("INFO 5100-04 Application Engineer & Dev");
        allCourses.add("INFO 6105-01 Data Sci Eng Methods");
        allCourses.add("INFO 6106-01 Neural Modeling Methods & Tool");
        allCourses.add("CSYE 6225-03 Netwrk Strctrs & Cloud Cmpting");
        allCourses.add("CSYE 7105-01 Parallel Machine Learning & AI");
        allCourses.add("CSYE 7280-01 User Experience Design/Testing");
        allCourses.add("CSYE 7380-02 Theory & Prac App AI Gen Model");
        allCourses.add("DAMG 6210-01 Data Mgt and Database Design");
        allCourses.add("DAMG 7250-01 Big Data Architec & Governance");
        allCourses.add("DAMG 7374-01 ST: Gen AI w/ LLM in Data Eng");
        allCourses.add("DAMG 7245-02 Big Data Sys & Intel Analytics");
        allCourses.add("TELE 5330-01 Data Networking");
        allCourses.add("TELE 6530-01 Connected Devices");
        allCourses.add("TELE 7374-02 Special Topics: Building Digital Twins");
        allCourses.add("TELE 5600-01 Linux for Network Engineers");
        
        // For each course, ensure a community exists
        for (String course : allCourses) {
            if (!existingCommunities.containsKey(course)) {
                // Create community in database
                createCommunityIfNotExists(course);
            }
        }
        
        // Reload communities to get IDs
        try {
            List<PostDAO.CommunityDTO> allCommunities = postDAO.getAllCommunities();
            for (PostDAO.CommunityDTO comm : allCommunities) {
                courseToCommunityMap.put(comm.name, comm.id);
            }
            System.out.println("Loaded " + courseToCommunityMap.size() + " communities");
        } catch (SQLException e) {
            System.err.println("Error loading communities: " + e.getMessage());
        }
    }
    
    /**
     * Creates a community in the database if it doesn't exist
     */
    private void createCommunityIfNotExists(String communityName) {
        try {
            String sql = "INSERT OR IGNORE INTO Communities (community_name, moderator_id) VALUES (?, 1)";
            java.sql.Connection conn = edu.northeastern.uniforum.db.Database.getConnection();
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, communityName);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("Error creating community: " + e.getMessage());
        }
    }
    
    
    private void updateSelectedCoursesLabel() {
        if (selectedCoursesLabel == null) {
            return;
        }
        
        if (allSelectedCourses.isEmpty()) {
            selectedCoursesLabel.setText("No courses selected");
            selectedCoursesLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px; -fx-background-color: #F5F5F5; -fx-padding: 8; -fx-background-radius: 4;");
        } else {
            String coursesText = allSelectedCourses.size() > 3 
                ? String.join(", ", allSelectedCourses.subList(0, 3)) + " ... (" + allSelectedCourses.size() + " total)"
                : String.join(", ", allSelectedCourses) + " (" + allSelectedCourses.size() + " total)";
            selectedCoursesLabel.setText("Selected: " + coursesText);
            selectedCoursesLabel.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px; -fx-background-color: #F5F5F5; -fx-padding: 8; -fx-background-radius: 4;");
        }
    }
    
    private void saveSelectionsAndContinue() {
        if (currentUser == null) {
            System.out.println("User not logged in");
            if (selectedCoursesLabel != null) {
                selectedCoursesLabel.setText("Error: User not logged in");
                selectedCoursesLabel.setStyle("-fx-text-fill: #ff585b; -fx-font-size: 12px;");
            }
            return;
        }
        
        if (courseListView == null || selectedCoursesLabel == null) {
            return;
        }
        
        ObservableList<String> selectedItems = courseListView.getSelectionModel().getSelectedItems();
        
        // Use the accumulated selected courses from all departments
        if (allSelectedCourses.isEmpty()) {
            selectedCoursesLabel.setText("Please select at least one course to continue");
            selectedCoursesLabel.setStyle("-fx-text-fill: #ff585b; -fx-font-size: 12px; -fx-background-color: #F5F5F5; -fx-padding: 8; -fx-background-radius: 4;");
            return;
        }
        
        // Get community IDs for all selected courses
        selectedCommunityIds.clear();
        for (String courseName : allSelectedCourses) {
            Integer communityId = courseToCommunityMap.get(courseName);
            if (communityId != null) {
                selectedCommunityIds.add(communityId);
            } else {
                System.err.println("Community not found for course: " + courseName + ". Creating it now...");
                // Try to create the community if it doesn't exist
                createCommunityIfNotExists(courseName);
                // Reload mapping
                try {
                    List<PostDAO.CommunityDTO> allCommunities = postDAO.getAllCommunities();
                    for (PostDAO.CommunityDTO comm : allCommunities) {
                        courseToCommunityMap.put(comm.name, comm.id);
                    }
                    communityId = courseToCommunityMap.get(courseName);
                    if (communityId != null) {
                        selectedCommunityIds.add(communityId);
                    }
                } catch (SQLException e) {
                    System.err.println("Error reloading communities: " + e.getMessage());
                }
            }
        }
        
        if (selectedCommunityIds.isEmpty()) {
            selectedCoursesLabel.setText("Error: Could not find or create communities for selected courses");
            selectedCoursesLabel.setStyle("-fx-text-fill: #ff585b; -fx-font-size: 12px;");
            return;
        }
        
        // Update user's communities (adds new ones and removes unselected ones)
        if (userDAO.updateUserCommunities(currentUser.getUserId(), selectedCommunityIds)) {
            System.out.println("Successfully updated user to " + selectedCommunityIds.size() + " communities");
            // Navigate back to forum
            SceneManager.switchToForum(currentUser);
        } else {
            selectedCoursesLabel.setText("Error saving selections. Please try again.");
            selectedCoursesLabel.setStyle("-fx-text-fill: #ff585b; -fx-font-size: 12px; -fx-background-color: #F5F5F5; -fx-padding: 8; -fx-background-radius: 4;");
        }
    }
}
