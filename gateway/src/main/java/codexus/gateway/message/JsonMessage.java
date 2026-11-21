package codexus.gateway.message;

import codexus.csharpsup.DotNetJson;
import codexus.gateway.handler.ClientHandler;

public abstract class JsonMessage<T> implements IMessage {
    private final Class<T> type;
    protected T request;
    protected JsonMessage(Class<T> type) { this.type = type; }
    @Override public void readFromMessage(ClientHandler handler, String payload) { request = DotNetJson.deserialize(payload, type); }
}
