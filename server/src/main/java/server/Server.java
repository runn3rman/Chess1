package server;

import dataaccess.*;
import handlers.*;
import service.*;
import spark.*;

public class Server {
    private ClearService clearService;

    public static void main(String[] args){
        new Server().run(8080);
    }

    public int run(int desiredPort) {
        // Initialize the database and tables before starting the server
        /*try {
            DatabaseManager.createDatabase();
            DatabaseManager.initializeDatabaseTables();
        } catch (DataAccessException e) {
            e.printStackTrace();
            System.exit(1); // Exit if we can't set up the database
        }*/
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Singleton instances of DAOs
        UserDaoInterface userDao = new MemoryUserDao();
        GameDaoInterface gameDao = new MemoryGameDao();
        AuthTokenDaoInterface authTokenDao = new MemoryAuthTokenDao(); // Create an instance of AuthTokenDao

        //UserDaoInterface userDao = new SqlUserDao();
        //GameDaoInterface gameDao = new SqlGameDao();
        //AuthTokenDaoInterface authTokenDao = new SqlAuthTokenDao();

        // Services with singleton DAOs
        clearService = new ClearService(userDao, gameDao, authTokenDao);
        LoginService loginService = new LoginService(userDao, authTokenDao); // Updated to use singleton DAOs
        LogoutService logoutService = new LogoutService(authTokenDao); // Uses singleton AuthTokenDao
        CreateGameService createGameService = new CreateGameService(gameDao, authTokenDao); // Uses singleton DAOs
        GameService gameService = new GameService(gameDao); // Uses singleton GameDao
        JoinGameService joinGameService = new JoinGameService(gameDao, authTokenDao); // Now correctly instantiated

        // Handlers with services
        ClearHandler clearHandler = new ClearHandler(clearService);
        LoginHandler loginHandler = new LoginHandler(loginService);
        LogoutHandler logoutHandler = new LogoutHandler(logoutService, authTokenDao);
        CreateGameHandler createGameHandler = new CreateGameHandler(createGameService);
        ListGamesHandler listGamesHandler = new ListGamesHandler(gameService, authTokenDao);
        JoinGameHandler joinGameHandler = new JoinGameHandler(joinGameService, authTokenDao, gameDao); // Pass an instance of AuthTokenDao



        // Register your endpoints and handle exceptions here
        Spark.delete("/db", clearHandler::handleRequest);
        Spark.post("/user", (req, res) -> (new RegisterHandler()).handleRequest(req, res));
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
