package server;

import java.sql.*;

public class DatabaseHandler {
    // These credentials can be loaded from the config.properties file.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/scribbledb";
    private static final String USER = "username";
    private static final String PASS = "password";
    
    private Connection conn;
    
    public DatabaseHandler() {
        try {
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Update or insert the high score for a host.
    public void updateHighScore(String hostUsername, int score) {
        try {
            // Check if record exists.
            PreparedStatement ps = conn.prepareStatement("SELECT high_score FROM high_scores WHERE host_username = ?");
            ps.setString(1, hostUsername);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int currentScore = rs.getInt("high_score");
                if (score > currentScore) {
                    PreparedStatement updatePs = conn.prepareStatement("UPDATE high_scores SET high_score = ? WHERE host_username = ?");
                    updatePs.setInt(1, score);
                    updatePs.setString(2, hostUsername);
                    updatePs.executeUpdate();
                }
            } else {
                PreparedStatement insertPs = conn.prepareStatement("INSERT INTO high_scores (host_username, high_score) VALUES (?, ?)");
                insertPs.setString(1, hostUsername);
                insertPs.setInt(2, score);
                insertPs.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // Retrieve the high score for a given host.
    public int getHighScore(String hostUsername) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT high_score FROM high_scores WHERE host_username = ?");
            ps.setString(1, hostUsername);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("high_score");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}