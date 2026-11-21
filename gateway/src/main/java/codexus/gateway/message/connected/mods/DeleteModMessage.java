package codexus.gateway.message.connected.mods;
import codexus.gateway.connection.*;
import codexus.gateway.handler.ClientHandler;
import codexus.gateway.message.*;
import codexus.gateway.utils.ModFiles;
import java.nio.file.Files;

@RegisterMessage(state = ConnectionState.Connected, type = "delete_mod")
public final class DeleteModMessage extends RawMessage {
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        try {
            Files.deleteIfExists(ModFiles.resolve(payload, false));
            new QueryModsMessage().handleMessage(handler, connection);
        } catch (Exception error) { ClientHandler.error(connection, error); }
    }
}
