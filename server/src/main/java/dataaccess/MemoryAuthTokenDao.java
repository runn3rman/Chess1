package dataaccess;

import model.AuthData;

import java.util.HashMap;

public class MemoryAuthTokenDao implements AuthTokenDaoInterface {
    private static final HashMap<String, AuthData> AUTH_TOKENS = new HashMap<>();

    @Override
    public void clearAuthTokens() {
        AUTH_TOKENS.clear();
    }

    @Override
    public void insertAuthToken(AuthData authData) {
        AUTH_TOKENS.put(authData.authToken(), authData);
    }

    @Override
    public AuthData getAuthData(String authToken) {
        return AUTH_TOKENS.get(authToken);
    }

    @Override
    public boolean deleteAuthToken(String authToken) {
        if (AUTH_TOKENS.containsKey(authToken)) {
            AUTH_TOKENS.remove(authToken);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String extractUsername(String authToken) {
        AuthData authData = AUTH_TOKENS.get(authToken);
        if (authData != null) {
            return authData.username();
        } else {
            return null;
        }
    }

    @Override
    public boolean isValidAuthToken(String authToken) {
        return AUTH_TOKENS.containsKey(authToken);
    }
}
