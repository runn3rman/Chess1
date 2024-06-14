package handlers;

import com.google.gson.Gson;
import dataaccess.AuthTokenDaoInterface;
import model.GameData;
import service.GameService;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;

public class ListGamesHandler {
    private final GameService gameService;
    private final AuthTokenDaoInterface authTokenDao;
    private final Gson gson = new Gson();

    public ListGamesHandler(GameService gameService, AuthTokenDaoInterface authTokenDao) {
        this.gameService = gameService;
        this.authTokenDao = authTokenDao;
    }

    public Object handleRequest(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            System.out.println("Authorization header: " + authToken);
            if (authToken == null || authTokenDao.getAuthData(authToken) == null) {
                res.status(401);
                System.out.println("Unauthorized request");
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            // Fetch games and create the JSON response
            List<GameData> games = gameService.listGames();
            String jsonResponse = gson.toJson(Map.of("games", games));
            System.out.println("JSON Response being sent: " + jsonResponse); // Log the JSON response being sent
            res.status(200);
            return jsonResponse;
        } catch (Exception e) {
            e.printStackTrace();
            res.status(500);
            System.out.println("Exception occurred: " + e.getMessage());
            return gson.toJson(Map.of("message", "Error: description"));
        }
    }
}
