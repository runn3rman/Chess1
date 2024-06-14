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
        // Configure Gson to serialize null values
        this.gson = new GsonBuilder().serializeNulls().create();
    }

    public AuthData register(String username, String password, String email) throws IOException {
        System.out.println("Registering user: " + username);
        var path = "/user";
        var request = new UserData(username, password, email);
        return sendRequest("POST", path, request, null, AuthData.class);
    }

    public AuthData login(String username, String password) throws IOException {
        System.out.println("Logging in user: " + username);
        var path = "/session";
        var request = Map.of("username", username, "password", password);
        return sendRequest("POST", path, request, null, AuthData.class);
    }

    public void logout(String authToken) throws IOException {
        System.out.println("Logging out user with token: " + authToken);
        var path = "/session";
        sendRequest("DELETE", path, null, authToken, null);
    }

    public void createGame(String authToken, String gameName) throws IOException {
        System.out.println("Creating game: " + gameName);
        var path = "/game";
        var request = Map.of("gameName", gameName);
        sendRequest("POST", path, request, authToken, null);
    }

    public List<GameData> listGames(String authToken) throws IOException {
        System.out.println("Listing games with token: " + authToken);
        var path = "/game";
        Type responseType = new TypeToken<Map<String, List<GameData>>>() {}.getType();
        Map<String, List<GameData>> response = sendRequest("GET", path, null, authToken, responseType);
        System.out.println("Response from server: " + response); // Log the response

        assert response != null;
        return response.get("games");
    }

    public void joinGame(String authToken, JoinGameRequest request) throws IOException {
        System.out.println("Joining game with token: " + authToken + ", Request: " + request);
        var path = "/game";
        sendRequest("PUT", path, request, authToken, null);
    }

    private <T> T sendRequest(String method, String path, Object req, String header, Type type) throws IOException {
        HttpURLConnection http = null;
        try {
            System.out.println("Preparing to send request to " + path + " with method " + method);
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
            System.out.println("HTTP response code: " + http.getResponseCode());
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
            System.out.println("Writing request body: " + gson.toJson(request));
            try (var outputStream = http.getOutputStream()) {
                outputStream.write(gson.toJson(request).getBytes());
            }
        }
    }

    private <T> T readBody(HttpURLConnection http, Type type) throws IOException {
        try (InputStream body = http.getInputStream(); InputStreamReader reader = new InputStreamReader(body)) {
            System.out.println("Reading response body");
            T response = gson.fromJson(reader, type);
            System.out.println("Parsed response: " + gson.toJson(response));
            return response;
        }
    }

    private void throwFailure(HttpURLConnection http) throws IOException {
        if (http.getResponseCode() >= 400) {
            try (InputStream errorStream = http.getErrorStream(); InputStreamReader reader = new InputStreamReader(errorStream)) {
                var error = gson.fromJson(reader, Map.class);
                System.out.println("Error response: " + gson.toJson(error));
                throw new IOException(error.get("message").toString());
            }
        }
    }

    private List<GameData> parseGameListResponse(String response) {
        Type mapType = new TypeToken<Map<String, List<GameData>>>() {}.getType();
        Map<String, List<GameData>> responseMap = gson.fromJson(response, mapType);
        return responseMap.get("games");
    }
}
