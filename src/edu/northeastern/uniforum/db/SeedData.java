package edu.northeastern.uniforum.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SeedData {

    public static void main(String[] args) {
        try {
            Connection conn = Database.getConnection();

            insertUsers(conn);
            insertCommunities(conn);
            insertPosts(conn);
            insertReplies(conn);
            insertCommunityUsers(conn);

            System.out.println("Dummy data inserted successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertUsers(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Users (user_name, role) VALUES (?, ?)")) {

            ps.setString(1, "User 1");
            ps.setString(2, "admin");
            ps.executeUpdate();

            ps.setString(1, "User 2");
            ps.setString(2, "student");
            ps.executeUpdate();

            ps.setString(1, "User 3");
            ps.setString(2, "student");
            ps.executeUpdate();

            ps.setString(1, "User 4");
            ps.setString(2, "student");
            ps.executeUpdate();
        }
    }

    private static void insertCommunities(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Communities (community_name, moderator_id) VALUES (?, ?)")) {

            ps.setString(1, "Community 1");
            ps.setInt(2, 1);     // User 1 moderator
            ps.executeUpdate();

            ps.setString(1, "Community 2");
            ps.setInt(2, 1);     // User 1 moderator
            ps.executeUpdate();
        }
    }

    private static void insertPosts(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Posts (community_id, user_id, title, content, number_of_likes, number_of_replies) " +
                        "VALUES (?, ?, ?, ?, ?, ?)")) {

            // Post 1
            ps.setInt(1, 1);        // Community 1
            ps.setInt(2, 2);        // User 2
            ps.setString(3, "Post 1");
            ps.setString(4, "This is the content for Post 1.");
            ps.setInt(5, 5);
            ps.setInt(6, 1);
            ps.executeUpdate();

            // Post 2
            ps.setInt(1, 1);
            ps.setInt(2, 3);        // User 3
            ps.setString(3, "Post 2");
            ps.setString(4, "This is the content for Post 2.");
            ps.setInt(5, 2);
            ps.setInt(6, 0);
            ps.executeUpdate();

            // Post 3 (Community 2)
            ps.setInt(1, 2);        // Community 2
            ps.setInt(2, 4);        // User 4
            ps.setString(3, "Post 3");
            ps.setString(4, "This is the content for Post 3.");
            ps.setInt(5, 10);
            ps.setInt(6, 2);
            ps.executeUpdate();
        }
    }

    private static void insertReplies(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Replies (post_id, user_id, content, number_of_likes) VALUES (?, ?, ?, ?)")) {

            // Reply 1
            ps.setInt(1, 1);   // Reply to Post 1
            ps.setInt(2, 1);   // User 1
            ps.setString(3, "Reply 1 content");
            ps.setInt(4, 3);
            ps.executeUpdate();

            // Reply 2
            ps.setInt(1, 3);   // Reply to Post 3
            ps.setInt(2, 2);   // User 2
            ps.setString(3, "Reply 2 content");
            ps.setInt(4, 1);
            ps.executeUpdate();
        }
    }

    private static void insertCommunityUsers(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Community_User (community_id, user_id) VALUES (?, ?)")) {

            // Community 1
            ps.setInt(1, 1); ps.setInt(2, 1); ps.executeUpdate();
            ps.setInt(1, 1); ps.setInt(2, 2); ps.executeUpdate();
            ps.setInt(1, 1); ps.setInt(2, 3); ps.executeUpdate();

            // Community 2
            ps.setInt(1, 2); ps.setInt(2, 1); ps.executeUpdate();
            ps.setInt(1, 2); ps.setInt(2, 4); ps.executeUpdate();
        }
    }
}


