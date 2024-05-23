package model;

public class GameCreationRequest {
    private String gameName;

    // Constructor, getters, and setters
    public GameCreationRequest() {}

    public GameCreationRequest(String gameName) {
        this.gameName = gameName;
    }

    public String getGameName() {
        return gameName;
    }

}
