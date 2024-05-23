package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlUserDao implements UserDaoInterface {

    @Override
    public void clearUsers() {
        String sql = "DELETE FROM users";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Database access error in clearUsers: " + e.getMessage(), e);
        }
    }

    @Override
    public UserData getUser(String username) {
        String sql = "SELECT username, password_hash, email FROM users WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String passwordHash = rs.getString("password_hash");
                    String email = rs.getString("email");
                    return new UserData(username, passwordHash, email);
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Database access error in getUser: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void insertUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.username());
            ps.setString(2, BCrypt.hashpw(user.password(), BCrypt.gensalt()));
            ps.setString(3, user.email());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting user: " + e.getMessage());
        }
    }

    public void storeUserPassword(String username, String password) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String sql = "UPDATE users SET password_hash = ? WHERE username = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hashedPassword);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating user password: " + e.getMessage());
        }
    }

    public boolean verifyUser(String username, String providedClearTextPassword) {
        UserData user = getUser(username);
        if (user != null) {
            return BCrypt.checkpw(providedClearTextPassword, user.password());
        }
        return false;
    }

    // Additional methods as needed...
}
