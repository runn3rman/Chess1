import java.util.Scanner;
import model.AuthData;
import model.JoinGameRequest;
import ui.*;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static ServerFacade serverFacade;
    private static AuthData authData;

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
        String input = scanner.nextLine().trim().toLowerCase();
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
        String input = scanner.nextLine().trim().toLowerCase();
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
        String username = scanner.nextLine().trim();
        System.out.println("Enter password: ");
        String password = scanner.nextLine().trim();
        try {
            authData = serverFacade.login(username, password);
            System.out.println("Login successful");
        } catch (Exception e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private static void handleRegister() {
        System.out.println("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.println("Enter password: ");
        String password = scanner.nextLine().trim();
        System.out.println("Enter email: ");
        String email = scanner.nextLine().trim();
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
        String gameName = scanner.nextLine().trim();
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
            for (int i = 0; i < games.size(); i++) {
                System.out.println((i + 1) + ". " + games.get(i).getGameName());
            }
        } catch (Exception e) {
            System.out.println("Failed to list games: " + e.getMessage());
        }
    }

    private static void handlePlayGame() {
        System.out.println("Enter game number: ");
        int gameNumber = Integer.parseInt(scanner.nextLine().trim());
        System.out.println("Enter color (white/black): ");
        String color = scanner.nextLine().trim().toUpperCase();
        try {
            serverFacade.joinGame(authData.authToken(), new JoinGameRequest(color, gameNumber));
            System.out.println("Joined game successfully");
            ChessBoard.drawInitialBoard(color);
        } catch (Exception e) {
            System.out.println("Failed to join game: " + e.getMessage());
        }
    }

    private static void handleObserveGame() {
        System.out.println("Enter game number: ");
        int gameNumber = Integer.parseInt(scanner.nextLine().trim());
        try {
             // This ensures playerColor is null
            serverFacade.joinGame(authData.authToken(), new JoinGameRequest(null, gameNumber));
            System.out.println("Joined game as observer successfully");
            //ChessBoard.drawInitialBoard(null);
        } catch (Exception e) {
            System.out.println("Failed to join game as observer: " + e.getMessage());
        }
    }


}

