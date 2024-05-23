package service;

import dataaccess.AuthTokenDaoInterface;
import dataaccess.DataAccessException;
import dataaccess.GameDaoInterface;
import dataaccess.UserDaoInterface;

public class ClearService {
    private UserDaoInterface userDao;
    private GameDaoInterface gameDao;
    private AuthTokenDaoInterface authTokenDao;

    // Constructor with DAOs
    public ClearService(UserDaoInterface userDao, GameDaoInterface gameDao, AuthTokenDaoInterface authTokenDao) {
        this.userDao = userDao;
        this.gameDao = gameDao;
        this.authTokenDao = authTokenDao;
    }

    // Method to clear all data
    public void clearAllData() throws DataAccessException {
        try {
            // Clear all users
            userDao.clearUsers();

            // Clear all games
            gameDao.clearGames();

            // Clear all auth tokens
            authTokenDao.clearAuthTokens();
        } catch (Exception e) {
            throw new DataAccessException("Failed to clear all data: " + e.getMessage());
        }
    }
}

