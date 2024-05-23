package handlers;

import com.google.gson.Gson;
import dataaccess.AuthTokenDaoInterface;
import dataaccess.GameDaoInterface;
import model.JoinGameRequest;
import service.JoinGameService;
import spark.Request;
import spark.Response;

import java.util.Map;

public class
JoinGameHandler {
    private final JoinGameService joinGameService;
    private final AuthTokenDaoInterface authTokenDao;
    private final GameDaoInterface gameDao;
    private final Gson gson = new Gson();

    public JoinGameHandler(JoinGameService joinGameService, AuthTokenDaoInterface authTokenDao, GameDaoInterface gameDao) {
        this.joinGameService = joinGameService;
        this.authTokenDao = authTokenDao;
        this.gameDao = gameDao;
    }

    public Object handleRequest(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            if (authToken == null || authToken.isEmpty() || !authTokenDao.isValidAuthToken(authToken)) {
                res.status(401); // Unauthorized
                return gson.toJson(Map.of("message", "Error: Invalid or expired authToken"));
            }

            JoinGameRequest joinRequest = gson.fromJson(req.body(), JoinGameRequest.class);
            if (joinRequest.getGameID() <= 0 || joinRequest.getPlayerColor() == null) {
                res.status(400); // Bad Request
                return gson.toJson(Map.of("message", "Error: Bad Request"));
            }

            // Check if the player color is already taken
            if (joinRequest.getPlayerColor() != null && !joinRequest.getPlayerColor().isEmpty() &&
                    gameDao.isColorTaken(joinRequest.getGameID(), joinRequest.getPlayerColor())) {
                res.status(403); // Forbidden
                return gson.toJson(Map.of("message", "Error: Role already taken"));
            }

            joinGameService.joinGame(authToken, joinRequest);

            res.status(200); // Success
            return "{}";
        } catch (Exception e) {
            res.status(500); // Internal Server Error
            return gson.toJson(Map.of("message", e.getMessage()));
        }
    }
}
