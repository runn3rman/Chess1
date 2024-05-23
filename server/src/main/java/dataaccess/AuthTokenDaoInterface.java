package dataaccess;

import model.AuthData;

public interface AuthTokenDaoInterface {
    void clearAuthTokens();
    void insertAuthToken(AuthData authData);
    AuthData getAuthData(String authToken);
    boolean deleteAuthToken(String authToken);
    String extractUsername(String authToken);
    boolean isValidAuthToken(String authToken);
    // Define additional necessary methods
}
