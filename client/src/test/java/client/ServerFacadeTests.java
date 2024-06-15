package client;

import org.junit.jupiter.api.*;
import ui.ServerFacade;
import server.Server;
import model.AuthData;
import model.GameData;
import model.JoinGameRequest;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    static AuthData authData;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws IOException {
        facade.clearDatabase();
        authData = facade.register("player1", "password", "p1@email.com");
    }

    @Test
    @Order(1)
    void registerSuccess() throws IOException {
        var authData = facade.register("newPlayer", "password", "newPlayer@email.com");
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    @Order(2)
    void registerFailureUsernameTaken() throws IOException {
        facade.register("existingUser", "password", "existing@example.com");

        IOException exception = assertThrows(IOException.class, () -> {
            facade.register("existingUser", "newPassword", "new@example.com");
        });
        assertTrue(exception.getMessage().contains("already taken"));
    }

    @Test
    @Order(3)
    void loginSuccess() throws IOException {
        var loginAuthData = facade.login("player1", "password");
        assertNotNull(loginAuthData.authToken());
        assertEquals("player1", loginAuthData.username());
    }

    @Test
    @Order(4)
    void loginFailureInvalidCredentials() throws IOException {
        IOException exception = assertThrows(IOException.class, () -> {
            facade.login("player1", "wrongpassword");
        });
        assertTrue(exception.getMessage().contains("Invalid username or password") || exception.getMessage().contains("Error: unauthorized"));
    }

    @Test
    @Order(5)
    void logoutSuccess() throws IOException {
        assertDoesNotThrow(() -> {
            facade.logout(authData.authToken());
        });
    }

    @Test
    @Order(6)
    void createGameSuccess() throws IOException {
        assertDoesNotThrow(() -> {
            facade.createGame(authData.authToken(), "Test Game");
        });
    }

    @Test
    @Order(7)
    void createGameFailureInvalidAuthToken() throws IOException {
        IOException exception = assertThrows(IOException.class, () -> {
            facade.createGame("invalidToken", "Test Game");
        });
        assertTrue(exception.getMessage().contains("unauthorized") || exception.getMessage().contains("Invalid or expired authToken"));
    }

    @Test
    @Order(8)
    void listGamesSuccess() throws IOException {
        facade.createGame(authData.authToken(), "Test Game");

        var games = facade.listGames(authData.authToken());
        assertNotNull(games);
        assertFalse(games.isEmpty());
    }

    @Test
    @Order(9)
    void listGamesFailureInvalidAuthToken() throws IOException {
        IOException exception = assertThrows(IOException.class, () -> {
            facade.listGames("invalidToken");
        });
        assertTrue(exception.getMessage().contains("unauthorized") || exception.getMessage().contains("Invalid or expired authToken"));
    }

    @Test
    @Order(10)
    void joinGameSuccess() throws IOException {
        var gameName = "Test Game";
        facade.createGame(authData.authToken(), gameName);

        List<GameData> games = facade.listGames(authData.authToken());
        int gameId = games.get(0).getGameID();

        JoinGameRequest request = new JoinGameRequest("WHITE", gameId);
        assertDoesNotThrow(() -> {
            facade.joinGame(authData.authToken(), request);
        });
    }

    @Test
    @Order(11)
    void joinGameFailureInvalidGameID() throws IOException {
        JoinGameRequest request = new JoinGameRequest("WHITE", 999); // Invalid game ID
        IOException exception = assertThrows(IOException.class, () -> {
            facade.joinGame(authData.authToken(), request);
        });
        assertTrue(exception.getMessage().contains("Could not join the game. It may not exist or the specified role is already taken."));
    }

    // Test 12: Logout Failure due to Invalid Auth Token
    @Test
    @Order(12)
    void logoutFailureInvalidAuthToken() throws IOException {
        String invalidAuthToken = "invalidToken";

        IOException exception = assertThrows(IOException.class, () -> {
            facade.logout(invalidAuthToken);
        });

        assertTrue(exception.getMessage().contains("Error: unauthorized"));
    }

    // Test 13: Clear Database Success
    @Test
    @Order(13)
    void clearDatabaseSuccess() throws IOException {
        // Populate the database
        populateDatabaseWithTestData();

        // Clear the database
        facade.clearDatabase();

        // Register a new user to get a new valid token
        var newAuthData = facade.register("player2", "password", "p2@email.com");

        // Verify the database is empty using the new auth token
        List<GameData> games = facade.listGames(newAuthData.authToken());
        assertTrue(games.isEmpty(), "Database should be empty after clear");
        facade.clearDatabase();
    }


    @Test
    @Order(14)
    void clearDatabaseFailure() throws IOException {
        populateDatabaseWithTestData();
        IOException exception = assertThrows(IOException.class, () -> {
            facade.listGames("invalidToken");
        });

        assertTrue(exception.getMessage().contains("unauthorized") || exception.getMessage().contains("Invalid or expired authToken"));
    }

    // Helper method to populate the database with test data
    private void populateDatabaseWithTestData() throws IOException {
        facade.createGame(authData.authToken(), "Test Game");
    }
}
