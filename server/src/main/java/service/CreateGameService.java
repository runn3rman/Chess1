package service;

import dataaccess.AuthTokenDaoInterface;
import dataaccess.GameDaoInterface;

public class CreateGameService {
    private GameDaoInterface gameDao;
    private AuthTokenDaoInterface authTokenDao;

    public CreateGameService(GameDaoInterface gameDao, AuthTokenDaoInterface authTokenDao) {
        this.gameDao = gameDao;
        this.authTokenDao = authTokenDao;
    }

    public int createGame(String authToken, String gameName) throws Exception {
        // Verify authToken is valid
        if (authTokenDao.getAuthData(authToken) == null) {
            throw new Exception("Error: unauthorized");
        }

        // Insert the new game
        return gameDao.insertGame(gameName);
    }
}

