// com.uniforum.model.data.UserDAO.java

package edu.northeastern.uniforum.forum.dao;

import edu.northeastern.uniforum.db.Database;
import edu.northeastern.uniforum.forum.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;

public class UserDAO {
    
    // SQL Statement for inserting a new user during registration
    private static final String INSERT_USER_SQL = 
            "INSERT INTO Users (user_name, PasswordHash, Email) VALUES (?, ?, ?)";
    
    // SQL Statement for retrieving a user during login
    private static final String SELECT_USER_BY_USERNAME_SQL = 
            "SELECT user_id, user_name, PasswordHash, Email FROM Users WHERE user_name = ?";

    /**
     * Attempts to register a new user in the database.
     * @param user The User object containing username, hashed password, and email.
     * @return True if registration was successful, false otherwise (e.g., username/email exists, DB error).
     */
    public boolean registerUser(User user) {
        // Use try-with-resources to automatically close the Connection and PreparedStatement
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_USER_SQL)) {

            // Set the values from the User object into the prepared statement
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getEmail());

            int rowsAffected = statement.executeUpdate();
            
            // Registration is successful if exactly one row was inserted
            return rowsAffected == 1; 

        } catch (SQLException e) {
            // Check for unique constraint violations (username or email already exists)
            String errorMessage = e.getMessage();
            System.err.println("Database error during user registration: " + errorMessage);
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            
            // SQLite returns error code 19 for constraint violations
            if (e.getErrorCode() == 19 || (errorMessage != null && errorMessage.contains("UNIQUE"))) {
                System.err.println("Username or email already exists.");
            }
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a User object from the database based on the provided username.
     * Used during the login process to fetch the stored password hash.
     * @param username The username provided by the user during login.
     * @return A User object if found, or null if no matching user is found or a DB error occurs.
     */
    public User getUserByUsername(String username) {
        
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_USERNAME_SQL)) {
            
            statement.setString(1, username);
            
            // Execute the query and get the results
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    // Found user, create and return the User object
                    return new User(
                        rs.getInt("user_id"),
                        rs.getString("user_name"),
                        rs.getString("PasswordHash"),
                        rs.getString("Email")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        return null; // User not found or error occurred
    }

    /**
     * Checks if a user has joined any communities
     * @param userId The user ID to check
     * @return true if user has joined at least one community, false otherwise
     */
    public boolean hasUserJoinedCommunities(int userId) {
        String sql = "SELECT COUNT(*) FROM Community_User WHERE user_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking user communities: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Joins a user to multiple communities
     * @param userId The user ID
     * @param communityIds List of community IDs to join
     * @return true if successful, false otherwise
     */
    public boolean joinUserToCommunities(int userId, List<Integer> communityIds) {
        if (communityIds == null || communityIds.isEmpty()) {
            return false;
        }

        String sql = "INSERT OR IGNORE INTO Community_User (community_id, user_id) VALUES (?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            for (Integer communityId : communityIds) {
                statement.setInt(1, communityId);
                statement.setInt(2, userId);
                statement.addBatch();
            }
            
            statement.executeBatch();
            return true;
        } catch (SQLException e) {
            System.err.println("Error joining user to communities: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets all community IDs that a user has joined
     * @param userId The user ID
     * @return List of community IDs the user has joined
     */
    public List<Integer> getUserCommunities(int userId) {
        List<Integer> communityIds = new ArrayList<>();
        String sql = "SELECT community_id FROM Community_User WHERE user_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    communityIds.add(rs.getInt("community_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting user communities: " + e.getMessage());
            e.printStackTrace();
        }
        return communityIds;
    }

    /**
     * Removes a user from multiple communities
     * @param userId The user ID
     * @param communityIds List of community IDs to remove
     * @return true if successful, false otherwise
     */
    public boolean removeUserFromCommunities(int userId, List<Integer> communityIds) {
        if (communityIds == null || communityIds.isEmpty()) {
            return true; // Nothing to remove
        }

        String sql = "DELETE FROM Community_User WHERE community_id = ? AND user_id = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            for (Integer communityId : communityIds) {
                statement.setInt(1, communityId);
                statement.setInt(2, userId);
                statement.addBatch();
            }
            
            statement.executeBatch();
            return true;
        } catch (SQLException e) {
            System.err.println("Error removing user from communities: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates user's communities - removes old ones and adds new ones
     * @param userId The user ID
     * @param newCommunityIds List of community IDs the user should be in
     * @return true if successful, false otherwise
     */
    public boolean updateUserCommunities(int userId, List<Integer> newCommunityIds) {
        try {
            // Get current communities
            List<Integer> currentCommunities = getUserCommunities(userId);
            
            // Find communities to remove (in current but not in new)
            List<Integer> toRemove = new ArrayList<>();
            for (Integer currentId : currentCommunities) {
                if (!newCommunityIds.contains(currentId)) {
                    toRemove.add(currentId);
                }
            }
            
            // Find communities to add (in new but not in current)
            List<Integer> toAdd = new ArrayList<>();
            for (Integer newId : newCommunityIds) {
                if (!currentCommunities.contains(newId)) {
                    toAdd.add(newId);
                }
            }
            
            // Remove old communities
            if (!toRemove.isEmpty()) {
                removeUserFromCommunities(userId, toRemove);
            }
            
            // Add new communities
            if (!toAdd.isEmpty()) {
                joinUserToCommunities(userId, toAdd);
            }
            
            return true;
        } catch (Exception e) {
            System.err.println("Error updating user communities: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}