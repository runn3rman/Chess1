package model;

public class AuthResponse {
    private String username;
    private String authToken;

    public AuthResponse(String username, String authToken) {
        this.username = username;
        this.authToken = authToken;
    }

    // Getters and setters
}