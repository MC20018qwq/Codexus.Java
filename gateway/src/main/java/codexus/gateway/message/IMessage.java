package codexus.gateway.message;

import codexus.gateway.handler.ClientHandler;
import codexus.gateway.connection.SecureConnection;

public interface IMessage {
    void readFromMessage(ClientHandler handler, String payload);
    void handleMessage(ClientHandler handler, SecureConnection connection);
}
