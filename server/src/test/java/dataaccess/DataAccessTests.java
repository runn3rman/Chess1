package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataAccessTests {

    private static SqlUserDao userDao;
    private static SqlGameDao gameDao;
    private static SqlAuthTokenDao authTokenDao;
    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "password123";
    private static final String EMAIL = "test@example.com";
    private static final String GAME_NAME = "Test Game";
    private static final String AUTH_TOKEN = "testAuthToken";

    @BeforeAll
    static void setUp() {
        userDao = new SqlUserDao();
        gameDao = new SqlGameDao();
        authTokenDao = new SqlAuthTokenDao();
        try {
            DatabaseManager.createDatabase();
            DatabaseManager.initializeDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    void clearDatabase() {
        userDao.clearUsers();
        gameDao.clearGames();
        authTokenDao.clearAuthTokens();
    }

    // UserDao Tests
    @Test
    @Order(1)
    @DisplayName("Insert User Successfully")
    void insertUserSuccess() {
        UserData user = new UserData(USERNAME, BCrypt.hashpw(PASSWORD, BCrypt.gensalt()), EMAIL);
        assertDoesNotThrow(() -> userDao.insertUser(user));
        UserData retrievedUser = assertDoesNotThrow(() -> userDao.getUser(USERNAME));
        assertNotNull(retrievedUser, "User should be retrieved");
        assertEquals(USERNAME, retrievedUser.username());
    }

    @Test
    @Order(2)
    @DisplayName("Insert User Failure - Duplicate Username")
    void insertUserFailureDuplicateUsername() {
        UserData user = new UserData(USERNAME, BCrypt.hashpw(PASSWORD, BCrypt.gensalt()), EMAIL);
        userDao.insertUser(user);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> userDao.insertUser(user), "Should throw exception for duplicate username");
        assertTrue(exception.getMessage().contains("Duplicate username"));
    }


    @Test
    @Order(3)
    @DisplayName("Get User Successfully")
    void getUserSuccess() {
        UserData user = new UserData(USERNAME, BCrypt.hashpw(PASSWORD, BCrypt.gensalt()), EMAIL);
        userDao.insertUser(user);
        UserData retrievedUser = assertDoesNotThrow(() -> userDao.getUser(USERNAME));
        assertNotNull(retrievedUser, "User should be retrieved");
        assertEquals(USERNAME, retrievedUser.username());
    }

    @Test
    @Order(4)
    @DisplayName("Get User Failure - Nonexistent User")
    void getUserFailureNonexistent() {
        UserData retrievedUser = assertDoesNotThrow(() -> userDao.getUser("nonexistentUser"));
        assertNull(retrievedUser, "User should not be found");
    }

    @Test
    @Order(5)
    @DisplayName("Clear Users Successfully")
    void clearUsersSuccess() {
        UserData user = new UserData(USERNAME, BCrypt.hashpw(PASSWORD, BCrypt.gensalt()), EMAIL);
        userDao.insertUser(user);
        userDao.clearUsers();
        UserData retrievedUser = assertDoesNotThrow(() -> userDao.getUser(USERNAME));
        assertNull(retrievedUser, "All users should be cleared");
    }

    // GameDao Tests
    @Test
    @Order(6)
    @DisplayName("Insert Game Successfully")
    void insertGameSuccess() {
        int gameId = gameDao.insertGame(GAME_NAME);
        assertTrue(gameId > 0, "Game ID should be greater than 0");
    }

    @Test
    @Order(7)
    @DisplayName("Get Game Successfully")
    void getGameSuccess() {
        int gameId = gameDao.insertGame(GAME_NAME);
        GameData game = gameDao.getGame(gameId);
        assertNotNull(game, "Game should be retrieved");
        assertEquals(GAME_NAME, game.getGameName());
    }

    @Test
    @Order(8)
    @DisplayName("Get Game Failure - Nonexistent Game")
    void getGameFailureNonexistent() {
        GameData game = gameDao.getGame(-1);
        assertNull(game, "Game should not be found");
    }

    @Test
    @Order(9)
    @DisplayName("List Games Successfully")
    void listGamesSuccess() {
        gameDao.insertGame(GAME_NAME);
        List<GameData> games = gameDao.listGames();
        assertNotNull(games, "Games list should not be null");
        assertFalse(games.isEmpty(), "Games list should not be empty");
    }

    @Test
    @Order(10)
    @DisplayName("Join Game Successfully")
    void joinGameSuccess() {
        int gameId = gameDao.insertGame(GAME_NAME);
        boolean success = gameDao.joinGame(gameId, "testUser", "WHITE");
        assertTrue(success, "Should join game successfully");
    }

    @Test
    @Order(11)
    @DisplayName("Join Game Failure - Color Taken")
    void joinGameFailureColorTaken() {
        int gameId = gameDao.insertGame(GAME_NAME);
        gameDao.joinGame(gameId, "testUser", "WHITE");
        boolean success = gameDao.joinGame(gameId, "anotherUser", "WHITE");
        assertFalse(success, "Should not join game with color already taken");
    }

    @Test
    @Order(12)
    @DisplayName("Clear Games Successfully")
    void clearGamesSuccess() {
        gameDao.insertGame(GAME_NAME);
        gameDao.clearGames();
        List<GameData> games = gameDao.listGames();
        assertTrue(games.isEmpty(), "All games should be cleared");
    }

    // AuthTokenDao Tests
    @Test
    @Order(13)
    @DisplayName("Insert Auth Token Successfully")
    void insertAuthTokenSuccess() {
        AuthData authData = new AuthData(AUTH_TOKEN, USERNAME);
        assertDoesNotThrow(() -> authTokenDao.insertAuthToken(authData));
        AuthData retrievedAuthData = authTokenDao.getAuthData(AUTH_TOKEN);
        assertNotNull(retrievedAuthData, "AuthData should be retrieved");
        assertEquals(USERNAME, retrievedAuthData.username());
    }

    @Test
    @Order(14)
    @DisplayName("Get Auth Data Successfully")
    void getAuthDataSuccess() {
        AuthData authData = new AuthData(AUTH_TOKEN, USERNAME);
        authTokenDao.insertAuthToken(authData);
        AuthData retrievedAuthData = authTokenDao.getAuthData(AUTH_TOKEN);
        assertNotNull(retrievedAuthData, "AuthData should be retrieved");
        assertEquals(USERNAME, retrievedAuthData.username());
    }

    @Test
    @Order(15)
    @DisplayName("Get Auth Data Failure - Nonexistent Token")
    void getAuthDataFailureNonexistent() {
        AuthData retrievedAuthData = authTokenDao.getAuthData("nonexistentToken");
        assertNull(retrievedAuthData, "AuthData should not be found");
    }

    @Test
    @Order(16)
    @DisplayName("Delete Auth Token Successfully")
    void deleteAuthTokenSuccess() {
        AuthData authData = new AuthData(AUTH_TOKEN, USERNAME);
        authTokenDao.insertAuthToken(authData);
        boolean success = authTokenDao.deleteAuthToken(AUTH_TOKEN);
        assertTrue(success, "Auth token should be deleted");
        AuthData retrievedAuthData = authTokenDao.getAuthData(AUTH_TOKEN);
        assertNull(retrievedAuthData, "AuthData should not be found after deletion");
    }

    @Test
    @Order(17)
    @DisplayName("Delete Auth Token Failure - Nonexistent Token")
    void deleteAuthTokenFailureNonexistent() {
        boolean success = authTokenDao.deleteAuthToken("nonexistentToken");
        assertFalse(success, "Deleting nonexistent auth token should fail");
    }

    @Test
    @Order(18)
    @DisplayName("Clear Auth Tokens Successfully")
    void clearAuthTokensSuccess() {
        AuthData authData = new AuthData(AUTH_TOKEN, USERNAME);
        authTokenDao.insertAuthToken(authData);
        authTokenDao.clearAuthTokens();
        AuthData retrievedAuthData = authTokenDao.getAuthData(AUTH_TOKEN);
        assertNull(retrievedAuthData, "All auth tokens should be cleared");
    }
}
