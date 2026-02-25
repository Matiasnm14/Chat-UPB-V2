package edu.upb.chatupb_v2.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

public class BlacklistDao {
    public BlacklistDao() {
        Tabla();
    }

    private void Tabla() {
        String query = """
                CREATE TABLE IF NOT EXISTS blacklist (
                    user_id TEXT PRIMARY KEY,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP
                )
                """;
        try (Connection conn = ConnectionDB.getInstance().getConection();
             Statement st = conn.createStatement()) {
            st.execute(query);
        } catch (SQLException e) {
            System.out.println("No se pudo crear: " + e.getMessage());
        }
    }

    public boolean exists(String userId) {
        String query = "SELECT 1 FROM blacklist WHERE user_id = ? LIMIT 1";
        try (Connection conn = ConnectionDB.getInstance().getConection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, userId);
            try (ResultSet rs = pst.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("No se pudo consultar blacklist: " + e.getMessage());
            return false;
        }
    }

    public void save(String userId) {
        String query = "INSERT OR IGNORE INTO blacklist(user_id) VALUES (?)";
        try (Connection conn = ConnectionDB.getInstance().getConection();
             PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setString(1, userId);
            pst.executeUpdate();
        } catch (SQLException e) {
            System.out.println("No se pudo guardar en blacklist: " + e.getMessage());
        }
    }

    public Set<String> findAllIds() {
        Set<String> users = new HashSet<>();
        String query = "SELECT user_id FROM blacklist";
        try (Connection conn = ConnectionDB.getInstance().getConection();
             PreparedStatement pst = conn.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                users.add(rs.getString("user_id"));
            }
        } catch (SQLException e) {
            System.out.println("No se pudo leer blacklist: " + e.getMessage());
        }
        return users;
    }
}
