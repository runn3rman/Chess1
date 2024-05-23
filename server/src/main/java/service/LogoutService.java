package service;

import dataaccess.AuthTokenDaoInterface;

public class LogoutService {
    private AuthTokenDaoInterface authTokenDao;

    public LogoutService(AuthTokenDaoInterface authTokenDao) {
        this.authTokenDao = authTokenDao;
    }

    public void logout(String authToken) throws Exception {
        boolean success = authTokenDao.deleteAuthToken(authToken);
        if (!success) {
            throw new Exception("Invalid or expired authToken");
        }
    }
}
