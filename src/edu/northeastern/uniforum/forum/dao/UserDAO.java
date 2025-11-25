// com.uniforum.model.data.UserDAO.java

package edu.northeastern.uniforum.forum.dao;

import edu.northeastern.uniforum.db.Database;
import edu.northeastern.uniforum.forum.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

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
}