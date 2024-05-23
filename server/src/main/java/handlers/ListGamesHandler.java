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
            if (authToken == null || authTokenDao.getAuthData(authToken) == null) {
                res.status(401);
                return gson.toJson(Map.of("message", "Error: unauthorized"));
            }

            List<GameData> games = gameService.listGames();
            res.status(200);
            return gson.toJson(Map.of("games", games));
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(Map.of("message", "Error: description"));
        }
    }
}
