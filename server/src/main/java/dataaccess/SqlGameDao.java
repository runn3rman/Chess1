package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlGameDao implements GameDaoInterface {
    private final Gson gson = new Gson();

    @Override
    public int insertGame(String gameName) {
        String sql = "INSERT INTO games (game_name, state) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ChessGame chessGame = new ChessGame();
            String state = gson.toJson(chessGame);
            stmt.setString(1, gameName);
            stmt.setString(2, state);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Inserting game failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GameData getGame(int gameID) {
        String sql = "SELECT * FROM games WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, gameID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String gameName = rs.getString("game_name");
                    String whiteUsername = rs.getString("white_username");
                    String blackUsername = rs.getString("black_username");
                    String state = rs.getString("state");
                    ChessGame chessGame = gson.fromJson(state, ChessGame.class);
                    return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
                }
            }
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<GameData> listGames() {
        String sql = "SELECT * FROM games";
        List<GameData> games = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int gameID = rs.getInt("id");
                String gameName = rs.getString("game_name");
                String whiteUsername = rs.getString("white_username");
                String blackUsername = rs.getString("black_username");
                String state = rs.getString("state");
                ChessGame chessGame = gson.fromJson(state, ChessGame.class);
                games.add(new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame));
            }
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        }
        return games;
    }

    @Override
    public boolean joinGame(int gameID, String username, String playerColor) {
        String sql = "UPDATE games SET white_username = ?, black_username = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            GameData game = getGame(gameID);
            if (game == null) return false;

            if ("WHITE".equalsIgnoreCase(playerColor) && (game.getWhiteUsername() == null || game.getWhiteUsername().isEmpty())) {
                stmt.setString(1, username);
                stmt.setString(2, game.getBlackUsername());
            } else if ("BLACK".equalsIgnoreCase(playerColor) && (game.getBlackUsername() == null || game.getBlackUsername().isEmpty())) {
                stmt.setString(1, game.getWhiteUsername());
                stmt.setString(2, username);
            } else {
                return false;
            }

            stmt.setInt(3, gameID);
            stmt.executeUpdate();
            return true;
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isColorTaken(int gameID, String playerColor) {
        GameData game = getGame(gameID);
        if (game == null) return false;

        if ("WHITE".equalsIgnoreCase(playerColor)) {
            return game.getWhiteUsername() != null && !game.getWhiteUsername().isEmpty();
        } else if ("BLACK".equalsIgnoreCase(playerColor)) {
            return game.getBlackUsername() != null && !game.getBlackUsername().isEmpty();
        } else {
            return false;
        }
    }

    @Override
    public void clearGames() {
        String sql = "DELETE FROM games";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
        }
    }
}
