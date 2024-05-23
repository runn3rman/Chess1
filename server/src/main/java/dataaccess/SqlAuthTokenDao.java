package dataaccess;

import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlAuthTokenDao implements AuthTokenDaoInterface {

    @Override
    public void clearAuthTokens() {
        String sql = "DELETE FROM authtokens";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (DataAccessException e) {
            throw new RuntimeException("Error obtaining DB connection: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("Error clearing auth tokens: " + e.getMessage(), e);
        }
    }

    @Override
    public void insertAuthToken(AuthData authData) {
        String sql = "INSERT INTO authtokens (token, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, authData.authToken());
            ps.setString(2, authData.username());
            ps.executeUpdate();
        } catch (DataAccessException e) {
            throw new RuntimeException("Error obtaining DB connection: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting auth token: " + e.getMessage(), e);
        }
    }

    @Override
    public AuthData getAuthData(String authToken) {
        String sql = "SELECT token, username FROM authtokens WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String token = rs.getString("token");
                    String username = rs.getString("username");
                    return new AuthData(token, username); // Adapt this to match your actual AuthData constructor
                }
            }
        } catch (DataAccessException e) {
            throw new RuntimeException("Error obtaining DB connection: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching auth data: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean deleteAuthToken(String authToken) {
        String sql = "DELETE FROM authtokens WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (DataAccessException e) {
            throw new RuntimeException("Error obtaining DB connection: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting auth token: " + e.getMessage(), e);
        }
    }

    @Override
    public String extractUsername(String authToken) {
        String sql = "SELECT username FROM authtokens WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("username");
                }
            }
        } catch (DataAccessException e) {
            throw new RuntimeException("Error obtaining DB connection: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("Error extracting username: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public boolean isValidAuthToken(String authToken) {
        String sql = "SELECT COUNT(authtoken) FROM authtokens WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (DataAccessException e) {
            throw new RuntimeException("Error obtaining DB connection: " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if auth token is valid: " + e.getMessage(), e);
        }
        return false;
    }

}
