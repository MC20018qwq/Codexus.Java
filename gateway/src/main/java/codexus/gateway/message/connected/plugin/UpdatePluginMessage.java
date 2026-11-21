package codexus.gateway.message.connected.plugin;
import codexus.csharpsup.DotNetJson;
import codexus.development.sdk.manager.PluginManager;
import codexus.gateway.connection.*;
import codexus.gateway.handler.ClientHandler;
import codexus.gateway.message.*;

@RegisterMessage(state = ConnectionState.Connected, type = "update_plugin")
public final class UpdatePluginMessage extends RawMessage {
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        try {
            var fields = DotNetJson.parse(payload);
            if (fields == null || !fields.isObject() || !fields.path("id").isTextual() || !fields.path("old").isTextual() || !fields.path("info").isTextual())
                throw new IllegalArgumentException("Invalid plugin update request");
            String id = fields.path("id").asText();
            var install = new InstallPluginMessage(); install.LoadNow = false;
            install.OnInstalled = installed -> {
                var paths = PluginManager.Instance.getPluginAndDependencyPaths(id, installed::contains);
                PluginManager.Instance.uninstallPluginWithPaths(paths);
            };
            install.readFromMessage(handler, fields.path("info").asText()); install.handleMessage(handler, connection);
        } catch (Exception error) { ClientHandler.error(connection, error); }
    }
}
