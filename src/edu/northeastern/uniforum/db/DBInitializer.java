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
        "    UserID INTEGER PRIMARY KEY AUTOINCREMENT, " +
        "    Username TEXT NOT NULL UNIQUE, " +
        "    PasswordHash TEXT NOT NULL, " +
        "    Email TEXT NOT NULL UNIQUE" +
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
            
            System.out.println("Database initialized successfully. Users table is ready.");
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

