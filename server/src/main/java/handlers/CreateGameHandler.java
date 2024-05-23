package handlers;

import com.google.gson.Gson;
import model.GameCreationRequest;
import service.CreateGameService;
import spark.Request;
import spark.Response;

import java.util.Map;

public class CreateGameHandler {
    private CreateGameService createGameService;
    private Gson gson = new Gson();

    public CreateGameHandler(CreateGameService createGameService) {
        this.createGameService = createGameService;
    }

    public Object handleRequest(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            GameCreationRequest creationRequest = gson.fromJson(req.body(), GameCreationRequest.class);
            int gameId = createGameService.createGame(authToken, creationRequest.getGameName());

            res.status(200); // OK
            return gson.toJson(Map.of("gameID", gameId));
        } catch (Exception e) {
            res.status(e.getMessage().equals("Error: unauthorized") ? 401 : 500);
            return gson.toJson(Map.of("message", e.getMessage()));
        }
    }
}
