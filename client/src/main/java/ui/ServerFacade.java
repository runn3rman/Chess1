package ui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson;

    public ServerFacade(String url) {
        this.serverUrl = url;
        this.gson = new GsonBuilder().serializeNulls().create();
    }

    public void clearDatabase() throws IOException {
        var path = "/db";
        sendRequest("DELETE", path, null, null, null);
    }


    public AuthData register(String username, String password, String email) throws IOException {
        var path = "/user";
        var request = new UserData(username, password, email);
        return sendRequest("POST", path, request, null, AuthData.class);
    }

    public AuthData login(String username, String password) throws IOException {
        var path = "/session";
        var request = Map.of("username", username, "password", password);
        return sendRequest("POST", path, request, null, AuthData.class);
    }

    public void logout(String authToken) throws IOException {
        var path = "/session";
        sendRequest("DELETE", path, null, authToken, null);
    }

    public void createGame(String authToken, String gameName) throws IOException {
        var path = "/game";
        var request = Map.of("gameName", gameName);
        sendRequest("POST", path, request, authToken, null);
    }

    public List<GameData> listGames(String authToken) throws IOException {
        var path = "/game";
        Type responseType = new TypeToken<Map<String, List<GameData>>>() {}.getType();
        Map<String, List<GameData>> response = sendRequest("GET", path, null, authToken, responseType);

        assert response != null;
        return response.get("games");
    }

    public void joinGame(String authToken, JoinGameRequest request) throws IOException {
        var path = "/game";
        sendRequest("PUT", path, request, authToken, null);
    }

    private <T> T sendRequest(String method, String path, Object req, String header, Type type) throws IOException {
        HttpURLConnection http = null;
        try {
            URI url = new URI(serverUrl + path);
            http = (HttpURLConnection) url.toURL().openConnection();
            http.setRequestMethod(method);
            if (header != null) {
                http.setRequestProperty("Authorization", header);
            }
            if (method.equals("POST") || method.equals("PUT")) {
                writeRequestBody(req, http);
            }
            http.connect();
            throwFailure(http);
            return type != null ? readBody(http, type) : null;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IOException(ex);
        } finally {
            if (http != null) {
                http.disconnect();
            }
        }
    }

    private void writeRequestBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.setDoOutput(true);
            try (var outputStream = http.getOutputStream()) {
                outputStream.write(gson.toJson(request).getBytes());
            }
        }
    }

    private <T> T readBody(HttpURLConnection http, Type type) throws IOException {
        try (InputStream body = http.getInputStream(); InputStreamReader reader = new InputStreamReader(body)) {
            T response = gson.fromJson(reader, type);
            return response;
        }
    }

    private void throwFailure(HttpURLConnection http) throws IOException {
        if (http.getResponseCode() >= 400) {
            try (InputStream errorStream = http.getErrorStream(); InputStreamReader reader = new InputStreamReader(errorStream)) {
                var error = gson.fromJson(reader, Map.class);
                throw new IOException(error.get("message").toString());
            }
        }
    }

}
