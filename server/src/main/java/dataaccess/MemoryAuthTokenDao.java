package dataaccess;

import model.AuthData;

import java.util.HashMap;

public class MemoryAuthTokenDao implements AuthTokenDaoInterface {
    private static final HashMap<String, AuthData> authTokens = new HashMap<>();

    @Override
    public void clearAuthTokens() {
        authTokens.clear();
    }

    @Override
    public void insertAuthToken(AuthData authData) {
        authTokens.put(authData.authToken(), authData);
    }

    @Override
    public AuthData getAuthData(String authToken) {
        return authTokens.get(authToken);
    }

    @Override
    public boolean deleteAuthToken(String authToken) {
        if (authTokens.containsKey(authToken)) {
            authTokens.remove(authToken);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String extractUsername(String authToken) {
        AuthData authData = authTokens.get(authToken);
        if (authData != null) {
            return authData.username();
        } else {
            return null;
        }
    }

    @Override
    public boolean isValidAuthToken(String authToken) {
        return authTokens.containsKey(authToken);
    }
}
