package dataaccess;

import model.GameData;

import java.util.List;

public interface GameDaoInterface {
    int insertGame(String gameName);
    GameData getGame(int gameID);
    List<GameData> listGames();
    boolean joinGame(int gameID, String username, String playerColor);
    boolean isColorTaken(int gameID, String playerColor);
    void clearGames();
    // Add other game-specific methods as needed
}
