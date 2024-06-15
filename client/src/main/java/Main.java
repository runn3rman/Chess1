import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import model.AuthData;
import model.GameData;
import model.JoinGameRequest;
import ui.ServerFacade;
import ui.ChessBoard;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static ServerFacade serverFacade;
    private static AuthData authData;
    private static Map<Integer, Integer> gameNumberToIdMap = new HashMap<>();

    public static void main(String[] args) {
        System.out.println("♕ Welcome to Chess Client!");

        serverFacade = new ServerFacade("http://localhost:8080");

        while (true) {
            if (authData == null) {
                handlePreLoginCommands();
            } else {
                handlePostLoginCommands();
            }
        }
    }

    private static void handlePreLoginCommands() {
        System.out.println("Available commands: help, quit, login, register");
        String input = SCANNER.nextLine().trim().toLowerCase();
        switch (input) {
            case "help":
                System.out.println("Commands: help, quit, login, register");
                break;
            case "quit":
                System.exit(0);
                break;
            case "login":
                handleLogin();
                break;
            case "register":
                handleRegister();
                break;
            default:
                System.out.println("Unknown command");
        }
    }

    private static void handlePostLoginCommands() {
        System.out.println("Available commands: help, logout, create game, list games, play game, observe game");
        String input = SCANNER.nextLine().trim().toLowerCase();
        switch (input) {
            case "help":
                System.out.println("Commands: help, logout, create game, list games, play game, observe game");
                break;
            case "logout":
                handleLogout();
                break;
            case "create game":
                handleCreateGame();
                break;
            case "list games":
                handleListGames();
                break;
            case "play game":
                handlePlayGame();
                break;
            case "observe game":
                handleObserveGame();
                break;
            default:
                System.out.println("Unknown command");
        }
    }

    private static void handleLogin() {
        System.out.println("Enter username: ");
        String username = SCANNER.nextLine().trim();
        System.out.println("Enter password: ");
        String password = SCANNER.nextLine().trim();
        try {
            authData = serverFacade.login(username, password);
            System.out.println("Login successful");
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private static void handleRegister() {
        System.out.println("Enter username: ");
        String username = SCANNER.nextLine().trim();
        System.out.println("Enter password: ");
        String password = SCANNER.nextLine().trim();
        System.out.println("Enter email: ");
        String email = SCANNER.nextLine().trim();
        try {
            authData = serverFacade.register(username, password, email);
            System.out.println("Registration successful");
        } catch (Exception e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private static void handleLogout() {
        try {
            serverFacade.logout(authData.authToken());
            System.out.println("Logout successful");
            authData = null;
        } catch (Exception e) {
            System.out.println("Logout failed: " + e.getMessage());
        }
    }

    private static void handleCreateGame() {
        System.out.println("Enter game name: ");
        String gameName = SCANNER.nextLine().trim();
        try {
            serverFacade.createGame(authData.authToken(), gameName);
            System.out.println("Game created successfully");
        } catch (Exception e) {
            System.out.println("Failed to create game: " + e.getMessage());
        }
    }

    private static void handleListGames() {
        try {
            var games = serverFacade.listGames(authData.authToken());
            System.out.println("List of games:");
            gameNumberToIdMap.clear();
            for (int i = 0; i < games.size(); i++) {
                GameData game = games.get(i);
                gameNumberToIdMap.put(i + 1, game.getGameID());
                System.out.println((i + 1) + ". " + game.getGameName() + " - White: " + game.getWhiteUsername() + ", Black: " + game.getBlackUsername());
            }
        } catch (Exception e) {
            System.out.println("Failed to list games: " + e.getMessage());
        }
    }
//TODO move all inside of try block
    private static void handlePlayGame() {
        try {
            System.out.println("Enter game number: ");
            int gameNumber = Integer.parseInt(SCANNER.nextLine().trim());
            System.out.println("Enter color (white/black): ");
            String color = SCANNER.nextLine().trim().toUpperCase();
            Integer gameId = gameNumberToIdMap.get(gameNumber);
            if (gameId == null) {
                System.out.println("Invalid game number");
                return;
            }
            serverFacade.joinGame(authData.authToken(), new JoinGameRequest(color, gameId));
            System.out.println("Joined game successfully");
            ChessBoard.drawInitialBoard(color);
        } catch (Exception e) {
            System.out.println("Failed to join game: " + e.getMessage());
        }
    }

    private static void handleObserveGame() {
        System.out.println("Enter game number: ");
        int gameNumber = Integer.parseInt(SCANNER.nextLine().trim());
        try {
            Integer gameId = gameNumberToIdMap.get(gameNumber);
            if (gameId == null) {
                System.out.println("Invalid game number");
                return;
            }
            //TODO test this
            ChessBoard.drawInitialBoard("White");
            //serverFacade.joinGame(authData.authToken(), new JoinGameRequest(null, gameId));
            System.out.println("Joined game as observer successfully");
        } catch (Exception e) {
            System.out.println("Failed to join game as observer: " + e.getMessage());
        }
    }
}
