package server;

import dataaccess.*;
import handlers.*;
import service.*;
import spark.*;

import java.sql.SQLException;

public class Server {
    private ClearService clearService;

    public static void main(String[] args){
        new Server().run(8080);
    }

    public int run(int desiredPort) {

        // Try-catch block to set up tables
        try {
            DatabaseManager.createDatabase();
            DatabaseManager.initializeDatabase();
        } catch (DataAccessException | SQLException e) {
            e.printStackTrace();
            System.exit(1); // Exit if we can't set up the database
        }
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // SQL DAOs
        UserDaoInterface userDao = new SqlUserDao();
        GameDaoInterface gameDao = new SqlGameDao();
        AuthTokenDaoInterface authTokenDao = new SqlAuthTokenDao();

        // Services with DAOs
        clearService = new ClearService(userDao, gameDao, authTokenDao);
        LoginService loginService = new LoginService(userDao, authTokenDao);
        LogoutService logoutService = new LogoutService(authTokenDao);
        CreateGameService createGameService = new CreateGameService(gameDao, authTokenDao);
        GameService gameService = new GameService(gameDao);
        JoinGameService joinGameService = new JoinGameService(gameDao, authTokenDao);


        // Handlers with services
        ClearHandler clearHandler = new ClearHandler(clearService);
        LoginHandler loginHandler = new LoginHandler(loginService);
        LogoutHandler logoutHandler = new LogoutHandler(logoutService, authTokenDao);
        CreateGameHandler createGameHandler = new CreateGameHandler(createGameService);
        ListGamesHandler listGamesHandler = new ListGamesHandler(gameService, authTokenDao);
        JoinGameHandler joinGameHandler = new JoinGameHandler(joinGameService, authTokenDao, gameDao);
        RegisterHandler registerHandler = new RegisterHandler();

        // Register your endpoints and handle exceptions here
        Spark.delete("/db", clearHandler::handleRequest);
        Spark.post("/user", registerHandler::handleRequest);
        Spark.post("/session", loginHandler::handleRequest);
        Spark.delete("/session", logoutHandler::handleRequest);
        Spark.post("/game", createGameHandler::handleRequest);
        Spark.get("/game", listGamesHandler::handleRequest);
        Spark.put("/game", joinGameHandler::handleRequest);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
