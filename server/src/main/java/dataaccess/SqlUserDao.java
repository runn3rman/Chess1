package dataaccess;

import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlUserDao implements UserDaoInterface {
    @Override
    public void addUser(String username, String password) throws SQLException, DataAccessException {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.executeUpdate();
        }
    }

    @Override
    public String getPassword(String username) throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT password FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
        }
        return null;
    }

    @Override
    public boolean userExists(String username) throws SQLException, DataAccessException {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    @Override
    public void clearUsers() {

    }

    @Override
    public UserData getUser(String username) {
        return null;
    }

    @Override
    public void insertUser(UserData user) throws DataAccessException {

    }
}
