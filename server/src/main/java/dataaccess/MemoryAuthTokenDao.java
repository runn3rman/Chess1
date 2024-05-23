package dataaccess;

import model.AuthData;

import java.util.HashMap;

public class MemoryAuthTokenDao implements AuthTokenDaoInterface {
    private static final HashMap<String, AuthData> AuthTokens = new HashMap<>();

    @Override
    public void clearAuthTokens() {
        AuthTokens.clear();
    }

    @Override
    public void insertAuthToken(AuthData authData) {
        AuthTokens.put(authData.authToken(), authData);
    }

    @Override
    public AuthData getAuthData(String authToken) {
        return AuthTokens.get(authToken);
    }

    @Override
    public boolean deleteAuthToken(String authToken) {
        if (AuthTokens.containsKey(authToken)) {
            AuthTokens.remove(authToken);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String extractUsername(String authToken) {
        AuthData authData = AuthTokens.get(authToken);
        if (authData != null) {
            return authData.username();
        } else {
            return null;
        }
    }

    @Override
    public boolean isValidAuthToken(String authToken) {
        return AuthTokens.containsKey(authToken);
    }
}
