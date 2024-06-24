package websocket.messages;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * 
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public class LoadGameMessage extends ServerMessage {
        private final Object game;

        public LoadGameMessage(Object game) {
            super(ServerMessageType.LOAD_GAME);
            this.game = game;
        }

        public Object getGame() {
            return game;
        }
    }

    public class ErrorMessage extends ServerMessage {
        public String errorMessage;

        public ErrorMessage(String errorMessage) {
            super(ServerMessageType.ERROR);
            this.errorMessage = errorMessage;
        }
    }

    public class NotificationMessage extends ServerMessage {
        private final String message;

        public NotificationMessage(String message) {
            super(ServerMessageType.NOTIFICATION);
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ServerMessage))
            return false;
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
