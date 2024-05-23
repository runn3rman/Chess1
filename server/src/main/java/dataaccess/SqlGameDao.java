package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlGameDao implements GameDaoInterface {
    private Gson gson = new Gson(); // For serializing and deserializing GameData objects

    @Override
    public int insertGame(String gameName) {
        // Prepare SQL statement for inserting a new game
        String sql = "INSERT INTO games (game_name, game_state_json) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Create a GameData instance with default values suitable for a new game
            GameData gameData = new GameData(0, null, null, gameName, new ChessGame());

            // Serialize the gameData including the new ChessGame instance
            String gameStateJson = gson.toJson(gameData);

            ps.setString(1, gameName);
            ps.setString(2, gameStateJson);
            ps.executeUpdate();

            // Attempt to retrieve the generated gameID
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // Return the new gameID
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Database access error in insertGame: " + e.getMessage(), e);
        }
        return -1; // Indicate failure if unable to insert the game or retrieve the gameID
    }


    @Override
    public GameData getGame(int gameID) {
        String sql = "SELECT game_id, game_name, game_state_json FROM games WHERE game_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String gameStateJson = rs.getString("game_state_json");
                    // Deserialize game state JSON back to a GameData object
                    return gson.fromJson(gameStateJson, GameData.class);
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Database access error in getGame: " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public List<GameData> listGames() {
        List<GameData> games = new ArrayList<>();
        String sql = "SELECT game_state_json FROM games";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String gameStateJson = rs.getString("game_state_json");
                    games.add(gson.fromJson(gameStateJson, GameData.class));
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Database access error in listGames: " + e.getMessage(), e);
        }
        return games;
    }

    @Override
    public void updateGame(int gameID, GameData updatedGame) {
        String sql = "UPDATE games SET game_state_json = ? WHERE game_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String gameStateJson = gson.toJson(updatedGame);
            ps.setString(1, gameStateJson);
            ps.setInt(2, gameID);
            ps.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Database access error in updateGame: " + e.getMessage(), e);
        }
    }

    @Override
    public void clearGames() {
        String sql = "DELETE FROM games";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Database access error in clearGames: " + e.getMessage(), e);
        }
    }


    @Override
    public boolean joinGame(int gameID, String username, String playerColor) {
        String columnToUpdate = playerColor.equalsIgnoreCase("WHITE") ? "whiteUsername" : "blackUsername";
        String sqlCheck = String.format("SELECT game_id FROM games WHERE game_id = ? AND %s IS NULL", columnToUpdate);
        String sqlUpdate = String.format("UPDATE games SET %s = ? WHERE game_id = ? AND %s IS NULL", columnToUpdate, columnToUpdate);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(sqlCheck)) {
            checkStmt.setInt(1, gameID);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                try (PreparedStatement updateStmt = conn.prepareStatement(sqlUpdate)) {
                    updateStmt.setString(1, username);
                    updateStmt.setInt(2, gameID);
                    int rowsAffected = updateStmt.executeUpdate();
                    return rowsAffected > 0;
                }
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Database access error in joinGame: " + e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean isColorTaken(int gameID, String playerColor) {
        String columnToCheck = playerColor.equalsIgnoreCase("WHITE") ? "whiteUsername" : "blackUsername";
        String sql = String.format("SELECT %s FROM games WHERE game_id = ?", columnToCheck);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String playerUsername = rs.getString(columnToCheck);
                return playerUsername != null && !playerUsername.isEmpty();
            }
        } catch (SQLException | DataAccessException e) {
            throw new RuntimeException("Database access error in isColorTaken: " + e.getMessage(), e);
        }
        return false;
    }

}
