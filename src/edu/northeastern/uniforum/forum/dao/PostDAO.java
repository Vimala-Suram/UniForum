package edu.northeastern.uniforum.forum.dao;

import edu.northeastern.uniforum.db.Database;
import edu.northeastern.uniforum.forum.util.TimeUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PostDAO {

    public static class PostDTO {
        public int postId;
        public String community;
        public String author;
        public String timeAgo;
        public String title;
        public String content;
        public String tag;
        public int upvotes;
        public int comments;
    }

    public List<PostDTO> getAllPosts() throws SQLException {
        List<PostDTO> list = new ArrayList<>();

        String sql = """
            SELECT p.post_id,
                   p.title,
                   p.content,
                   p.tag,
                   p.number_of_likes,
                   p.number_of_replies,
                   p.created_time,
                   u.user_name,
                   c.community_name
            FROM Posts p
            JOIN Users u       ON p.user_id = u.user_id
            JOIN Communities c ON p.community_id = c.community_id
            ORDER BY p.created_time DESC
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PostDTO dto = new PostDTO();
                dto.postId   = rs.getInt("post_id");
                dto.title     = rs.getString("title");
                dto.content   = rs.getString("content");
                dto.upvotes   = rs.getInt("number_of_likes");
                dto.comments  = rs.getInt("number_of_replies");
                dto.author    = rs.getString("user_name");
                dto.community = rs.getString("community_name");
                dto.tag = rs.getString("tag");

                Timestamp ts = rs.getTimestamp("created_time");
                if (ts != null) {
                    LocalDateTime created = ts.toLocalDateTime();
                    dto.timeAgo = TimeUtil.timeAgo(created);
                } else {
                    dto.timeAgo = "unknown";
                }

                list.add(dto);
            }
        }

        System.out.println("DAO: loaded posts count = " + list.size());
        return list;
    }
    
    /**
     * Gets all posts sorted by number of likes (descending) - for Explore view
     */
    public List<PostDTO> getAllPostsSortedByLikes() throws SQLException {
        List<PostDTO> list = new ArrayList<>();

        String sql = """
            SELECT p.post_id,
                   p.title,
                   p.content,
                   p.tag,
                   p.number_of_likes,
                   p.number_of_replies,
                   p.created_time,
                   u.user_name,
                   c.community_name
            FROM Posts p
            JOIN Users u       ON p.user_id = u.user_id
            JOIN Communities c ON p.community_id = c.community_id
            ORDER BY p.number_of_likes DESC, p.created_time DESC
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PostDTO dto = new PostDTO();
                dto.postId   = rs.getInt("post_id");
                dto.title     = rs.getString("title");
                dto.content   = rs.getString("content");
                dto.upvotes   = rs.getInt("number_of_likes");
                dto.comments  = rs.getInt("number_of_replies");
                dto.author    = rs.getString("user_name");
                dto.community = rs.getString("community_name");
                dto.tag = rs.getString("tag");

                Timestamp ts = rs.getTimestamp("created_time");
                if (ts != null) {
                    LocalDateTime created = ts.toLocalDateTime();
                    dto.timeAgo = TimeUtil.timeAgo(created);
                } else {
                    dto.timeAgo = "unknown";
                }

                list.add(dto);
            }
        }

        System.out.println("DAO: loaded posts sorted by likes count = " + list.size());
        return list;
    }

    /**
     * Gets posts from communities the user has joined, sorted by created_time (most recent first) - for Home view
     */
    public List<PostDTO> getPostsFromJoinedCommunities(int userId) throws SQLException {
        List<PostDTO> list = new ArrayList<>();

        String sql = """
            SELECT p.post_id,
                   p.title,
                   p.content,
                   p.tag,
                   p.number_of_likes,
                   p.number_of_replies,
                   p.created_time,
                   u.user_name,
                   c.community_name
            FROM Posts p
            JOIN Users u       ON p.user_id = u.user_id
            JOIN Communities c ON p.community_id = c.community_id
            JOIN Community_User cu ON c.community_id = cu.community_id
            WHERE cu.user_id = ?
            ORDER BY p.created_time DESC
            """;

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PostDTO dto = new PostDTO();
                    dto.postId   = rs.getInt("post_id");
                    dto.title     = rs.getString("title");
                    dto.content   = rs.getString("content");
                    dto.upvotes   = rs.getInt("number_of_likes");
                    dto.comments  = rs.getInt("number_of_replies");
                    dto.author    = rs.getString("user_name");
                    dto.community = rs.getString("community_name");
                    dto.tag = rs.getString("tag");

                    Timestamp ts = rs.getTimestamp("created_time");
                    if (ts != null) {
                        LocalDateTime created = ts.toLocalDateTime();
                        dto.timeAgo = TimeUtil.timeAgo(created);
                    } else {
                        dto.timeAgo = "unknown";
                    }

                    list.add(dto);
                }
            }
        }

        System.out.println("DAO: loaded posts from joined communities count = " + list.size());
        return list;
    }
    
  //------------- Community DTO + loader -------------
    public static class CommunityDTO {
    	 public int id;
    	 public String name;

    	 @Override
    	 public String toString() {
    	     return name; // this is what ComboBox will display
    	 }
    	}

    	public List<CommunityDTO> getAllCommunities() throws SQLException {
    	 List<CommunityDTO> list = new ArrayList<>();

    	 String sql = """
    	     SELECT community_id, community_name
    	     FROM Communities
    	     ORDER BY community_name
    	     """;

    	 try (Connection conn = Database.getConnection();
    	      PreparedStatement ps = conn.prepareStatement(sql);
    	      ResultSet rs = ps.executeQuery()) {

    	     while (rs.next()) {
    	         CommunityDTO dto = new CommunityDTO();
    	         dto.id   = rs.getInt("community_id");
    	         dto.name = rs.getString("community_name");
    	         list.add(dto);
    	     }
    	 }

    	 return list;
    	}

    	//------------- Insert new post -------------

    	public void createPost(int communityId, int userId, String title, String content, String tag)
    	     throws SQLException {

    	 String sql = """
    	     INSERT INTO Posts
    	         (community_id, user_id, title, content,
    	          number_of_likes, number_of_replies, created_time,tag)
    	     VALUES (?, ?, ?, ?, 0, 0, CURRENT_TIMESTAMP,?)
    	     """;

    	 try (Connection conn = Database.getConnection();
    	      PreparedStatement ps = conn.prepareStatement(sql)) {

    	     ps.setInt(1, communityId);
    	     ps.setInt(2, userId);
    	     ps.setString(3, title);
    	     ps.setString(4, content);
    	     ps.setString(5, tag); 
    	     ps.executeUpdate();
    	 }
    	}

    	/**
    	 * Gets the vote type for a user on a post
    	 * @return 1 if upvoted, -1 if downvoted, 0 if not voted
    	 */
    	public int getUserVote(int postId, int userId) throws SQLException {
    	    return getUserVote(postId, userId, Database.getConnection());
    	}
    	
    	/**
    	 * Gets the vote type for a user on a post (using provided connection)
    	 * @return 1 if upvoted, -1 if downvoted, 0 if not voted
    	 */
    	private int getUserVote(int postId, int userId, Connection conn) throws SQLException {
    	    String sql = "SELECT vote_type FROM Post_Votes WHERE post_id = ? AND user_id = ?";
    	    try (PreparedStatement ps = conn.prepareStatement(sql)) {
    	        ps.setInt(1, postId);
    	        ps.setInt(2, userId);
    	        try (ResultSet rs = ps.executeQuery()) {
    	            if (rs.next()) {
    	                return rs.getInt("vote_type");
    	            }
    	        }
    	    }
    	    return 0; // No vote found
    	}

    	/**
    	 * Handles upvote - checks if user already voted and updates accordingly
    	 * @return true if vote was successful, false if user already upvoted
    	 */
    	public boolean handleUpvote(int postId, int userId) throws SQLException {
    	    Connection conn = Database.getConnection();
    	    boolean wasAutoCommit = conn.getAutoCommit();
    	    try {
    	        conn.setAutoCommit(false);
    	        // Check existing vote
    	        int existingVote = getUserVote(postId, userId, conn);
    	            
    	            if (existingVote == 1) {
    	                // User already upvoted, remove the vote
    	                String deleteSql = "DELETE FROM Post_Votes WHERE post_id = ? AND user_id = ?";
    	                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
    	                    ps.setInt(1, postId);
    	                    ps.setInt(2, userId);
    	                    ps.executeUpdate();
    	                }
    	                
    	                // Decrement vote count
    	                String updateSql = "UPDATE Posts SET number_of_likes = CASE WHEN number_of_likes > 0 THEN number_of_likes - 1 ELSE 0 END WHERE post_id = ?";
    	                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
    	                    ps.setInt(1, postId);
    	                    ps.executeUpdate();
    	                }
    	                
    	                conn.commit();
    	                return false; // Vote removed
    	            } else if (existingVote == -1) {
    	                // User previously downvoted, change to upvote
    	                String updateVoteSql = "UPDATE Post_Votes SET vote_type = 1 WHERE post_id = ? AND user_id = ?";
    	                try (PreparedStatement ps = conn.prepareStatement(updateVoteSql)) {
    	                    ps.setInt(1, postId);
    	                    ps.setInt(2, userId);
    	                    ps.executeUpdate();
    	                }
    	                
    	                // Increment vote count by 2 (removing downvote + adding upvote)
    	                String updatePostSql = "UPDATE Posts SET number_of_likes = number_of_likes + 2 WHERE post_id = ?";
    	                try (PreparedStatement ps = conn.prepareStatement(updatePostSql)) {
    	                    ps.setInt(1, postId);
    	                    ps.executeUpdate();
    	                }
    	                
    	                conn.commit();
    	                return true;
    	            } else {
    	                // No existing vote, add new upvote
    	                String insertSql = "INSERT INTO Post_Votes (post_id, user_id, vote_type) VALUES (?, ?, 1)";
    	                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
    	                    ps.setInt(1, postId);
    	                    ps.setInt(2, userId);
    	                    ps.executeUpdate();
    	                }
    	                
    	                // Increment vote count
    	                String updatePostSql = "UPDATE Posts SET number_of_likes = number_of_likes + 1 WHERE post_id = ?";
    	                try (PreparedStatement ps = conn.prepareStatement(updatePostSql)) {
    	                    ps.setInt(1, postId);
    	                    ps.executeUpdate();
    	                }
    	                
    	                conn.commit();
    	                return true;
    	            }
    	        } catch (SQLException e) {
    	            conn.rollback();
    	            throw e;
    	        } finally {
    	            conn.setAutoCommit(wasAutoCommit);
    	        }
    	}

    	/**
    	 * Handles downvote - checks if user already voted and updates accordingly
    	 * @return true if vote was successful, false if user already downvoted
    	 */
    	public boolean handleDownvote(int postId, int userId) throws SQLException {
    	    Connection conn = Database.getConnection();
    	    boolean wasAutoCommit = conn.getAutoCommit();
    	    try {
    	        conn.setAutoCommit(false);
    	        // Check existing vote
    	        int existingVote = getUserVote(postId, userId, conn);
    	            
    	            if (existingVote == -1) {
    	                // User already downvoted, remove the vote
    	                String deleteSql = "DELETE FROM Post_Votes WHERE post_id = ? AND user_id = ?";
    	                try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
    	                    ps.setInt(1, postId);
    	                    ps.setInt(2, userId);
    	                    ps.executeUpdate();
    	                }
    	                
    	                // Increment vote count (removing downvote)
    	                String updateSql = "UPDATE Posts SET number_of_likes = number_of_likes + 1 WHERE post_id = ?";
    	                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
    	                    ps.setInt(1, postId);
    	                    ps.executeUpdate();
    	                }
    	                
    	                conn.commit();
    	                return false; // Vote removed
    	            } else if (existingVote == 1) {
    	                // User previously upvoted, change to downvote
    	                String updateVoteSql = "UPDATE Post_Votes SET vote_type = -1 WHERE post_id = ? AND user_id = ?";
    	                try (PreparedStatement ps = conn.prepareStatement(updateVoteSql)) {
    	                    ps.setInt(1, postId);
    	                    ps.setInt(2, userId);
    	                    ps.executeUpdate();
    	                }
    	                
    	                // Decrement vote count by 2 (removing upvote + adding downvote)
    	                String updatePostSql = "UPDATE Posts SET number_of_likes = CASE WHEN number_of_likes >= 2 THEN number_of_likes - 2 ELSE 0 END WHERE post_id = ?";
    	                try (PreparedStatement ps = conn.prepareStatement(updatePostSql)) {
    	                    ps.setInt(1, postId);
    	                    ps.executeUpdate();
    	                }
    	                
    	                conn.commit();
    	                return true;
    	            } else {
    	                // No existing vote, add new downvote
    	                String insertSql = "INSERT INTO Post_Votes (post_id, user_id, vote_type) VALUES (?, ?, -1)";
    	                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
    	                    ps.setInt(1, postId);
    	                    ps.setInt(2, userId);
    	                    ps.executeUpdate();
    	                }
    	                
    	                // Decrement vote count
    	                String updatePostSql = "UPDATE Posts SET number_of_likes = CASE WHEN number_of_likes > 0 THEN number_of_likes - 1 ELSE 0 END WHERE post_id = ?";
    	                try (PreparedStatement ps = conn.prepareStatement(updatePostSql)) {
    	                    ps.setInt(1, postId);
    	                    ps.executeUpdate();
    	                }
    	                
    	                conn.commit();
    	                return true;
    	            }
    	        } catch (SQLException e) {
    	            conn.rollback();
    	            throw e;
    	        } finally {
    	            conn.setAutoCommit(wasAutoCommit);
    	        }
    	}

    	/**
    	 * Gets the current vote count for a post (for UI updates)
    	 */
    	public int getVoteCount(int postId) throws SQLException {
    	    String sql = "SELECT number_of_likes FROM Posts WHERE post_id = ?";
    	    try (Connection conn = Database.getConnection();
    	         PreparedStatement ps = conn.prepareStatement(sql)) {
    	        ps.setInt(1, postId);
    	        try (ResultSet rs = ps.executeQuery()) {
    	            if (rs.next()) {
    	                return rs.getInt("number_of_likes");
    	            }
    	        }
    	    }
    	    return 0;
    	}

}



