package codexus.gateway.message.connected.mods;
import codexus.gateway.connection.*;
import codexus.gateway.entities.web.gateway.EntityAddMod;
import codexus.gateway.handler.ClientHandler;
import codexus.gateway.message.*;
import codexus.gateway.utils.ModFiles;
import java.nio.file.*;
import java.util.Base64;

@RegisterMessage(state = ConnectionState.Connected, type = "add_mods")
public final class AddModsMessage extends JsonMessage<EntityAddMod[]> {
    public AddModsMessage() { super(EntityAddMod[].class); }
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        try {
            if (request == null) throw new IllegalArgumentException("Invalid mods request");
            for (var mod : request) {
                Path target = ModFiles.resolve(mod.FileName, true);
                if (!Files.exists(target)) Files.write(target, Base64.getDecoder().decode(mod.Base64), StandardOpenOption.CREATE_NEW);
            }
            new QueryModsMessage().handleMessage(handler, connection);
        } catch (Exception error) { ClientHandler.error(connection, error); }
    }
}
