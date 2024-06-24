package ui;

import chess.ChessMove;
import chess.ChessPiece;
import websocket.WebSocketConnection;

public class Gameplay {
    private WebSocketConnection webSocketConnection;
    private String authToken;
    private int gameID;

    public Gameplay(String authToken, int gameID) {
        this.authToken = authToken;
        this.gameID = gameID;
        this.webSocketConnection = new WebSocketConnection();
    }

    public void connectToGame() {
        try {
            webSocketConnection.connect("ws://localhost:8080/ws");
            // Send CONNECT command
            String connectMessage = String.format("{\"authToken\":\"%s\",\"commandType\":\"CONNECT\",\"gameID\":%d}", authToken, gameID);
            webSocketConnection.sendMessage(connectMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void makeMove(ChessMove move) {
        try {
            String makeMoveMessage = String.format("{\"authToken\":\"%s\",\"commandType\":\"MAKE_MOVE\",\"gameID\":%d,\"move\":%s}", authToken, gameID, move.toString());
            webSocketConnection.sendMessage(makeMoveMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void leaveGame() {
        try {
            String leaveMessage = String.format("{\"authToken\":\"%s\",\"commandType\":\"LEAVE\",\"gameID\":%d}", authToken, gameID);
            webSocketConnection.sendMessage(leaveMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resignGame() {
        try {
            String resignMessage = String.format("{\"authToken\":\"%s\",\"commandType\":\"RESIGN\",\"gameID\":%d}", authToken, gameID);
            webSocketConnection.sendMessage(resignMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void highlightLegalMoves(ChessPiece piece) {
        // Implement the local operation to highlight legal moves on the UI
    }

    public void redrawChessBoard() {
        // Implement the local operation to redraw the chess board on the UI
    }

    public void displayHelp() {
        // Display text informing the user what actions they can take
    }
}
