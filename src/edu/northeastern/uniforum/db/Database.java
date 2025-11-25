package edu.northeastern.uniforum.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

	private static final String DB_URL =
		    "jdbc:sqlite:db/uniforum_db.db";

    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                throw new SQLException("SQLite JDBC driver not found", e);
            }
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }
}
