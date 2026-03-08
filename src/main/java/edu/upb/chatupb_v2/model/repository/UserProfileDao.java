package edu.upb.chatupb_v2.model.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UserProfileDao {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public UserProfileDao() {
        ensureTable();
    }

    public void ensureTable() {
        String query = "CREATE TABLE IF NOT EXISTS user_profile (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id TEXT UNIQUE NOT NULL," +
                "user_name TEXT NOT NULL," +
                "created_at TEXT" +
                ")";
        try (Connection conn = ConnectionDB.getInstance().getConection();
             PreparedStatement st = conn.prepareStatement(query)) {
            st.execute();
        } catch (Exception ignored) {
        }
    }

    public String findUserName(String userId) {
        if (userId == null || userId.isBlank()) {
            return null;
        }
        String query = "SELECT user_name FROM user_profile WHERE user_id = ?";
        try (Connection conn = ConnectionDB.getInstance().getConection();
             PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, userId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString(1);
                    return name != null && !name.isBlank() ? name : null;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public void upsertUserName(String userId, String userName) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        String trimmed = userName == null ? "" : userName.trim();
        if (trimmed.isBlank()) {
            return;
        }
        if (trimmed.length() > 60) {
            trimmed = trimmed.substring(0, 60);
        }
        if (!updateUserName(userId, trimmed)) {
            insertUserName(userId, trimmed);
        }
    }

    private boolean updateUserName(String userId, String userName) {
        String query = "UPDATE user_profile SET user_name = ? WHERE user_id = ?";
        try (Connection conn = ConnectionDB.getInstance().getConection();
             PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, userName);
            st.setString(2, userId);
            return st.executeUpdate() > 0;
        } catch (Exception ignored) {
        }
        return false;
    }

    private void insertUserName(String userId, String userName) {
        String query = "INSERT INTO user_profile(user_id, user_name, created_at) VALUES (?, ?, ?)";
        String createdAt = LocalDateTime.now().format(DATE_FORMAT);
        try (Connection conn = ConnectionDB.getInstance().getConection();
             PreparedStatement st = conn.prepareStatement(query)) {
            st.setString(1, userId);
            st.setString(2, userName);
            st.setString(3, createdAt);
            st.executeUpdate();
        } catch (Exception ignored) {
        }
    }
}
