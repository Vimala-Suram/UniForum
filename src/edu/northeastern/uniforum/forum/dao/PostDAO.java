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



}



