// com.uniforum.model.data.DBInitializer.java

package edu.northeastern.uniforum.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles database initialization, including table creation.
 * This class ensures the database and required tables exist before the application uses them.
 */
public class DBInitializer {
    
    private static final String CREATE_USERS_TABLE_SQL = 
        "CREATE TABLE IF NOT EXISTS Users (" +
        "    user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "    user_name TEXT NOT NULL, " +
        "    role TEXT DEFAULT 'student', " +
        "    created_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
        "    PasswordHash TEXT, " +
        "    Email TEXT" +
        ")";
    
    /**
     * Initializes the database by creating the Users table if it doesn't exist.
     * This method should be called once when the application starts.
     * 
     * @return true if initialization was successful, false otherwise
     */
    public static boolean initializeDatabase() {
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {
            
            // Create the Users table if it doesn't exist
            statement.executeUpdate(CREATE_USERS_TABLE_SQL);
            
            // Add PasswordHash column if it doesn't exist
            try {
                statement.executeUpdate("ALTER TABLE Users ADD COLUMN PasswordHash TEXT");
                System.out.println("Added PasswordHash column to Users table.");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            
            // Add Email column if it doesn't exist
            try {
                statement.executeUpdate("ALTER TABLE Users ADD COLUMN Email TEXT");
                System.out.println("Added Email column to Users table.");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            
            // Add LinkedIn URL column if it doesn't exist
            try {
                statement.executeUpdate("ALTER TABLE Users ADD COLUMN LinkedInURL TEXT");
                System.out.println("Added LinkedInURL column to Users table.");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            
            // Add GitHub URL column if it doesn't exist
            try {
                statement.executeUpdate("ALTER TABLE Users ADD COLUMN GitHubURL TEXT");
                System.out.println("Added GitHubURL column to Users table.");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            
            // Add Department column if it doesn't exist
            try {
                statement.executeUpdate("ALTER TABLE Users ADD COLUMN Department TEXT");
                System.out.println("Added Department column to Users table.");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            
            // Add number_of_likes column to Posts table if it doesn't exist
            try {
                statement.executeUpdate("ALTER TABLE Posts ADD COLUMN number_of_likes INTEGER DEFAULT 0");
                System.out.println("Added number_of_likes column to Posts table.");
            } catch (SQLException e) {
                // Column already exists, ignore
            }
            
            System.out.println("Database initialized successfully. Users table is ready.");
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

