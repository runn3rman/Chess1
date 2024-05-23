package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryGameDao implements GameDaoInterface {
    private final HashMap<Integer, GameData> games = new HashMap<>();
    private final AtomicInteger gameIDCounter = new AtomicInteger();

    @Override
    public int insertGame(String gameName) {
        int gameID = gameIDCounter.incrementAndGet();
        GameData game = new GameData(gameID, null, null, gameName, new ChessGame());
        games.put(gameID, game);
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public List<GameData> listGames() {
        return new ArrayList<>(games.values());
    }

    @Override
    public void updateGame(int gameID, GameData updatedGame) {
        games.put(gameID, updatedGame);
    }

    @Override
    public boolean joinGame(int gameID, String username, String playerColor) {
        GameData game = games.get(gameID);
        if (game == null) return false;

        if ("WHITE".equalsIgnoreCase(playerColor) && (game.getWhiteUsername() == null || game.getWhiteUsername().isEmpty())) {
            game.setWhiteUsername(username);
            games.put(gameID, game);
            return true;
        } else if ("BLACK".equalsIgnoreCase(playerColor) && (game.getBlackUsername() == null || game.getBlackUsername().isEmpty())) {
            game.setBlackUsername(username);
            games.put(gameID, game);
            return true;
        } else {
            game.addObserver(username);
            return true;
        }
    }

    @Override
    public boolean isColorTaken(int gameID, String playerColor) {
        GameData game = games.get(gameID);
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
        games.clear();
    }
}
