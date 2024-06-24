package websocket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

public class WebSocketConnection {
    private WebSocketClient client;
    private WebSocketListener listener;

    public WebSocketConnection() {
        client = new WebSocketClient();
        listener = new WebSocketListener();
    }

    public void connect(String uri) throws URISyntaxException, Exception {
        URI serverUri = new URI(uri);
        ClientUpgradeRequest request = new ClientUpgradeRequest();
        client.start();
        client.connect(listener, serverUri, request);
        listener.awaitClose(5, TimeUnit.SECONDS);
    }

    public void sendMessage(String message) throws Exception {
        listener.sendMessage(message);
    }

    public void close() throws Exception {
        client.stop();
    }
}
