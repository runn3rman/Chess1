//I should get rid of the unused methods
package model;

public class JoinGameRequest {
    private String playerColor; // WHITE, BLACK, or null for observers
    private int gameID;

    // Constructors, getters, and setters
    public JoinGameRequest() {}

    public JoinGameRequest(String playerColor, int gameID) {
        this.playerColor = playerColor;
        this.gameID = gameID;
    }

    public String getPlayerColor() {
        return playerColor;
    }

    public void setPlayerColor(String playerColor) {
        this.playerColor = playerColor;
    }

    public int getGameID() {
        return gameID;
    }

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }
}
