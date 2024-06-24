package handlers;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

@WebSocket
public class WebSocketHandler {
    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connected: " + session.getRemoteAddress().getAddress());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        // Handle incoming messages (deserialize UserGameCommand)
        UserGameCommand command = deserializeUserGameCommand(message);
        if (command != null) {
            handleUserGameCommand(session, command);
        } else {
            sendErrorMessage(session, "Invalid command format");
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("Closed: " + session.getRemoteAddress().getAddress() + " with status: " + statusCode + " for reason: " + reason);
    }

    private UserGameCommand deserializeUserGameCommand(String message) {
        // Deserialize JSON message to UserGameCommand object
        // Implementation depends on the JSON library you are using (e.g., Gson, Jackson)
        // For example, using Gson:
        // return new Gson().fromJson(message, UserGameCommand.class);
        return null; // Replace with actual deserialization logic
    }

    private void handleUserGameCommand(Session session, UserGameCommand command) {
        // Handle different types of UserGameCommand
        switch (command.getCommandType()) {
            case CONNECT:
                // Handle CONNECT command
                break;
            case MAKE_MOVE:
                // Handle MAKE_MOVE command
                break;
            case LEAVE:
                // Handle LEAVE command
                break;
            case RESIGN:
                // Handle RESIGN command
                break;
        }
    }

    private void sendErrorMessage(Session session, String errorMessage) {
        // Send error message to client
        ServerMessage errorResponse = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        errorResponse.errorMessage = errorMessage;
        //
        // session.getRemote().sendString(new Gson().toJson(errorResponse));
    }
}
