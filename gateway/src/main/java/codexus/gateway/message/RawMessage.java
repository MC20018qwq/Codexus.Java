package codexus.gateway.message;

import codexus.gateway.handler.ClientHandler;

public abstract class RawMessage implements IMessage {
    protected String payload;
    @Override public void readFromMessage(ClientHandler handler, String payload) { this.payload = payload; }
}
