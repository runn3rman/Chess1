package service;

import dataaccess.AuthTokenDaoInterface;
import dataaccess.GameDaoInterface;
import model.JoinGameRequest;

public class JoinGameService {
    private GameDaoInterface gameDao;
    private AuthTokenDaoInterface authTokenDao;

    public JoinGameService(GameDaoInterface gameDao, AuthTokenDaoInterface authTokenDao) {
        this.gameDao = gameDao;
        this.authTokenDao = authTokenDao;
    }

    public void joinGame(String authToken, JoinGameRequest request) throws Exception { //added the correct authentication logic
        // Check if the authToken is valid
        if (!authTokenDao.isValidAuthToken(authToken)) {
            throw new Exception("Invalid or expired authToken.");
        }
        String username = authTokenDao.extractUsername(authToken);
        if (username == null) {
            throw new Exception("Invalid or expired authToken.");
        }

        // Check if the team color is valid
        String playerColor = request.getPlayerColor();
        if (playerColor != null && !playerColor.isEmpty() && !isValidTeamColor(playerColor)) {
            throw new Exception("Invalid team color. It must be WHITE, BLACK, or empty for observers.");
        }

        // Check if the team color is already assigned
        if (playerColor != null && !playerColor.isEmpty()) {
            boolean colorTaken = gameDao.isColorTaken(request.getGameID(), playerColor);
            if (colorTaken) {
                throw new Exception("The team color is already taken.");
            }
        }

        // Join the game
        boolean success;
        if (playerColor == null || playerColor.isEmpty()) {
            // No color specified, add as observer directly.
            success = gameDao.joinGame(request.getGameID(), username, ""); // Empty string to indicate observer
        } else {
            // Color specified, attempt to join as the specified color.
            success = gameDao.joinGame(request.getGameID(), username, playerColor);
        }

        if (!success) {
            throw new Exception("Could not join the game. It may not exist or the specified role is already taken.");
        }
    }

    // Helper method to check if the team color is valid
    private boolean isValidTeamColor(String color) {
        return color.equalsIgnoreCase("WHITE") || color.equalsIgnoreCase("BLACK");
    }
}
