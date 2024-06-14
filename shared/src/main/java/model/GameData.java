//consider removing functions that I never actually use. future reference
package model;

import chess.ChessGame;

import java.util.ArrayList;
import java.util.List;

public class GameData {
    private int gameID;
    private String whiteUsername;
    private String blackUsername;
    public String gameName;
    private ChessGame game;
    private List<String> observers;

    // Constructor
    public GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
        this.gameID = gameID;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;
        this.gameName = gameName;
        this.game = game;
        this.observers = new ArrayList<>();
    }

    // Getters and Setters
    public int getGameID() { return gameID; }
    public String getWhiteUsername() { return whiteUsername; }
    public void setWhiteUsername(String whiteUsername) { this.whiteUsername = whiteUsername; }
    public String getBlackUsername() { return blackUsername; }
    public void setBlackUsername(String blackUsername) { this.blackUsername = blackUsername; }
    public String getGameName() { return gameName; }
    public ChessGame getGame() { return game; }
    public List<String> getObservers() { return observers; }

    // Method to add an observer
    public void addObserver(String username) {
        if (!observers.contains(username)) {
            observers.add(username);
        }
    }
}
