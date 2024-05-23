package unit;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.JoinGameRequest;
import model.UserData;
import org.junit.jupiter.api.*;
import org.mindrot.jbcrypt.BCrypt;
import service.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceTests {

    private static MemoryUserDao userDao;
    private static MemoryGameDao gameDao;
    private static MemoryAuthTokenDao authTokenDao;
    private static CreateGameService createGameService;
    private static JoinGameService joinGameService;
    private static GameService gameService;
    private static String validAuthToken;
    private static String invalidAuthToken = "invalidToken";
    private static String gameName = "testGame";
    private static int testGameId;
    private static LoginService loginService;
    private static LogoutService logoutService;
    private static RegisterService registerService;
    private static ClearService clearService;

    @BeforeAll
    static void setUp() {
        userDao = new MemoryUserDao();
        gameDao = new MemoryGameDao();
        authTokenDao = new MemoryAuthTokenDao();
        createGameService = new CreateGameService(gameDao, authTokenDao);
        gameService = new GameService(gameDao);
        joinGameService = new JoinGameService(gameDao, authTokenDao);
        loginService = new LoginService(userDao, authTokenDao);
        logoutService = new LogoutService(authTokenDao);
        validAuthToken = "validToken";
        authTokenDao.insertAuthToken(new AuthData(validAuthToken, "testUser"));
        testGameId = gameDao.insertGame("Test Game");
        registerService = new RegisterService();
        clearService = new ClearService(userDao, gameDao, authTokenDao);
        gameDao.joinGame(testGameId, "anotherUser", "WHITE");

        try {
            testGameId = createGameService.createGame(validAuthToken, gameName + System.currentTimeMillis());
        } catch (Exception e) {
            fail("Setup failed due to exception: " + e.getMessage());
        }
    }

    @BeforeEach
    void resetDatabasesBeforeTest() {
        userDao.clearUsers();
        authTokenDao.clearAuthTokens();
        authTokenDao.insertAuthToken(new AuthData(validAuthToken, "testUser"));
    }

    @AfterAll
    static void tearDown() {
        // Clean up resources, if necessary
        gameDao.clearGames();
        authTokenDao.clearAuthTokens();
    }

    @Test
    @Order(1)
    @DisplayName("Create Game Successfully")
    void createGameSuccess() throws Exception {
        String uniqueGameName = gameName + System.currentTimeMillis();
        int gameId = createGameService.createGame(validAuthToken, uniqueGameName);
        assertNotNull(gameId, "Game ID should not be null");
        assertTrue(gameId > 0, "Game ID should be positive");
    }

    @Test
    @Order(2)
    @DisplayName("Create Game Failure - Unauthorized")
    void createGameFailureUnauthorized() {
        Exception exception = assertThrows(Exception.class, () -> createGameService.createGame(invalidAuthToken, gameName + System.currentTimeMillis()),
                "Expected to throw, but it didn't");

        assertTrue(exception.getMessage().contains("Error: unauthorized"), "Exception message should indicate authorization failure");
    }

    @Test
    @Order(3)
    @DisplayName("List Games - Success")
    void listGamesSuccess() {
        List<GameData> games = gameService.listGames();
        assertNotNull(games, "List of games should not be null");
        assertFalse(games.isEmpty(), "List of games should not be empty");
    }

    @Test
    @Order(4)
    @DisplayName("List Games - No Games Available")
    void listGamesNoGames() {
        gameDao.clearGames(); // Ensure this does not affect other tests or move it to a specific setup for this test

        List<GameData> games = gameService.listGames();
        assertNotNull(games, "List of games should not be null");
        assertTrue(games.isEmpty(), "List of games should be empty when no games are available");
    }

    @Test
    @Order(5)
    @DisplayName("Join Game Failure - Invalid Auth Token")
    void joinGameInvalidAuthToken() {
        JoinGameRequest request = new JoinGameRequest("WHITE", testGameId);
        Exception exception = assertThrows(Exception.class, () -> joinGameService.joinGame(invalidAuthToken, request),
                "Expected to throw due to invalid auth token, but it didn't");

        assertTrue(exception.getMessage().contains("Invalid or expired authToken"), "Exception message should indicate auth token issue");
    }

    @Test
    @Order(10)
    @DisplayName("Login Success")
    void loginSuccess() throws Exception {
        String username = "testUser";
        String password = "testPass";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt()); // Hash the password before storing
        userDao.insertUser(new UserData(username, hashedPassword, "test@example.com")); // Insert user directly using userDao

        AuthData result = loginService.login(username, password);

        assertNotNull(result, "AuthData should not be null");
        assertEquals(username, result.username(), "Returned username should match the input username");
        assertNotNull(result.authToken(), "Auth token should not be null");
    }

    @Test
    @Order(11)
    @DisplayName("Login Failure - Invalid Credentials")
    void loginFailureInvalidCredentials() {
        String username = "testUser";
        String password = "testPass";
        // Do not insert user, to simulate invalid credentials scenario

        Exception exception = assertThrows(Exception.class, () -> loginService.login(username, password),
                "Expected to throw due to invalid credentials, but it didn't");

        assertTrue(exception.getMessage().contains("Invalid username or password"), "Exception message should indicate invalid credentials");
    }

    @Test
    @Order(12)
    @DisplayName("Logout Success")
    void logoutSuccess() throws Exception {
        String validToken = "validAuthToken";
        // Pre-insert a valid auth token to simulate a logged-in user
        authTokenDao.insertAuthToken(new AuthData(validToken, "userForLogout"));

        // Attempt to logout
        assertDoesNotThrow(() -> logoutService.logout(validToken),
                "Logout should succeed without throwing an exception for a valid auth token");

        // Verify the auth token has been removed
        assertNull(authTokenDao.getAuthData(validToken), "Auth token should be null after successful logout");
    }

    @Test
    @Order(13)
    @DisplayName("Logout Failure - Invalid Token")
    void logoutFailureInvalidToken() {
        String invalidToken = "invalidAuthToken";
        // No need to insert the token, as it's supposed to be invalid

        Exception exception = assertThrows(Exception.class, () -> logoutService.logout(invalidToken),
                "Expected to throw due to invalid or expired authToken, but it didn't");

        assertTrue(exception.getMessage().contains("Invalid or expired authToken"), "Exception message should indicate invalid or expired authToken");
    }

    @Test
    @Order(14)
    @DisplayName("Register Success")
    void registerSuccess() throws DataAccessException {
        UserData newUser = new UserData("newUser", "password123", "newuser@example.com");

        // Attempt to register the new user
        AuthData result = registerService.register(newUser);

        assertNotNull(result, "AuthData should not be null after successful registration");
        assertEquals(newUser.username(), result.username(), "Returned username should match the registered username");
        assertNotNull(result.authToken(), "Auth token should not be null after successful registration");

        // Verify the user is now in the UserDao
        UserData registeredUser = userDao.getUser(newUser.username());
        assertNotNull(registeredUser, "User should exist in UserDao after successful registration");
        assertEquals(newUser.username(), registeredUser.username(), "Username should match the newly registered user");
    }

    @Test
    @Order(15)
    @DisplayName("Register Failure - Username Already Taken")
    void registerFailureUsernameAlreadyTaken() {
        UserData existingUser = new UserData("existingUser", "password123", "existing@example.com");
        // Pre-insert a user to simulate username conflict
        try {
            userDao.insertUser(existingUser);
        } catch (DataAccessException e) {
            fail("Setup failed due to exception: " + e.getMessage());
        }

        UserData newUserSameUsername = new UserData("existingUser", "newPassword", "new@example.com");

        // Attempt to register a new user with the same username
        DataAccessException exception = assertThrows(DataAccessException.class, () -> registerService.register(newUserSameUsername),
                "Expected to throw due to username conflict, but it didn't");

        assertTrue(exception.getMessage().contains("Username is already taken"), "Exception message should indicate username conflict");
    }

    @Test
    @Order(16)
    @DisplayName("Clear All Data Successfully")
    void clearAllDataSuccess() {
        // Pre-populate each DAO with a sample entry to ensure there's data to clear
        String testUsername = "userToClear";
        int testGameId = gameDao.insertGame("gameToClear");
        String testToken = "tokenToClear";
        try {
            userDao.insertUser(new UserData(testUsername, "password", "email@example.com"));
            authTokenDao.insertAuthToken(new AuthData(testToken, testUsername));
        } catch (Exception e) {
            fail("Setup failed due to exception: " + e.getMessage());
        }

        // Attempt to clear all data
        assertDoesNotThrow(() -> clearService.clearAllData(),
                "Clearing all data should not throw an exception");

        // Verify that each DAO is now empty by checking specific retrieval methods
        assertNull(userDao.getUser(testUsername), "UserDao should not find user after clear");
        assertNull(gameDao.getGame(testGameId), "GameDao should not find game after clear");
        assertNull(authTokenDao.getAuthData(testToken), "AuthTokenDao should not find auth token after clear");
    }

    @Test
    @Order(17)
    @DisplayName("Clear All Data - Empty Database")
    void clearAllDataEmptyDatabase() {
        // Ensure the DAOs are empty
        userDao.clearUsers();
        gameDao.clearGames();
        authTokenDao.clearAuthTokens();

        // Attempt to clear all data on an already empty database
        assertDoesNotThrow(() -> clearService.clearAllData(),
                "Clearing all data on an empty database should not throw an exception");

        // Verify that each DAO remains empty
        assertNull(userDao.getUser("anyUser"), "UserDao should not find any user after clear");
        assertNull(gameDao.getGame(1), "GameDao should not find any game after clear");
        assertNull(authTokenDao.getAuthData("anyToken"), "AuthTokenDao should not find any auth token after clear");
    }


    @Test
    @Order(20)
    @DisplayName("Create Game with Invalid Auth Token")
    void createGameWithInvalidAuthToken() {
        String uniqueGameName = gameName + System.currentTimeMillis();
        Exception exception = assertThrows(Exception.class, () -> createGameService.createGame(invalidAuthToken, uniqueGameName),
                "Expected to throw due to invalid auth token, but it didn't");
        assertTrue(exception.getMessage().contains("Error: unauthorized"), "Exception message should indicate authorization failure");
    }


    @Test
    @Order(23)
    @DisplayName("Logout Failure - Missing Token")
    void logoutFailureMissingToken() {
        Exception exception = assertThrows(Exception.class, () -> logoutService.logout("missingToken"),
                "Expected to throw due to missing auth token, but it didn't");
        assertTrue(exception.getMessage().contains("Invalid or expired authToken"), "Exception message should indicate invalid or expired authToken");
    }


    @Test
    @Order(25)
    @DisplayName("Login Failure - User Not Found")
    void loginFailureUserNotFound() {
        String username = "nonExistentUser";
        String password = "password123";

        Exception exception = assertThrows(Exception.class, () -> loginService.login(username, password),
                "Expected to throw due to user not found, but it didn't");

        assertTrue(exception.getMessage().contains("Invalid username or password"), "Exception message should indicate invalid credentials");
    }
}
