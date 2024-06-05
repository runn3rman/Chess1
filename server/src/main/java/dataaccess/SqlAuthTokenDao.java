package dataaccess;

import model.AuthData;

import java.sql.*;

public class SqlAuthTokenDao implements AuthTokenDaoInterface {

    @Override
    public void clearAuthTokens() {
        String sql = "DELETE FROM auth_tokens";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void insertAuthToken(AuthData authData) {
        String sql = "INSERT INTO auth_tokens (token, username) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authData.authToken());
            stmt.setString(2, authData.username());
            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public AuthData getAuthData(String authToken) {
        String sql = "SELECT * FROM auth_tokens WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("username");
                    return new AuthData(authToken, username);
                }
            }
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean deleteAuthToken(String authToken) {
        String sql = "DELETE FROM auth_tokens WHERE token = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, authToken);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String extractUsername(String authToken) {
        AuthData authData = getAuthData(authToken);
        return authData != null ? authData.username() : null;
    }

    @Override
    public boolean isValidAuthToken(String authToken) {
        return getAuthData(authToken) != null;
    }
}
