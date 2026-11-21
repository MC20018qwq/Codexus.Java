package codexus.gateway.message.connected.mods;
import codexus.csharpsup.DotNetJson;
import codexus.gateway.connection.*;
import codexus.gateway.handler.ClientHandler;
import codexus.gateway.message.*;
import codexus.gateway.utils.ModFiles;
import java.nio.file.*;
import java.util.*;
import static codexus.gateway.handler.ClientHandler.send;

@RegisterMessage(state = ConnectionState.Connected, type = "query_mods")
public final class QueryModsMessage extends RawMessage {
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        try (var files = Files.walk(ModFiles.directory())) {
            var result = files.filter(Files::isRegularFile).filter(file -> file.toString().toLowerCase(Locale.ROOT).endsWith(".jar"))
                    .sorted().map(file -> Map.of("path", file.toString())).toList();
            send(connection, "query_mods", DotNetJson.serialize(result));
        } catch (Exception error) { ClientHandler.error(connection, error); }
    }
}
