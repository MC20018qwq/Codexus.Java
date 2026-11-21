package codexus.gateway.message.connected.plugin;
import codexus.development.sdk.manager.PluginManager;
import codexus.gateway.connection.*;
import codexus.gateway.handler.ClientHandler;
import codexus.gateway.message.*;

@RegisterMessage(state = ConnectionState.Connected, type = "uninstall_plugin")
public final class UninstallPluginMessage extends RawMessage {
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        try {
            synchronized (PluginManager.Instance) { PluginManager.Instance.uninstallPlugin(payload); }
            new QueryPluginsMessage().handleMessage(handler, connection);
        } catch (Exception error) { ClientHandler.error(connection, error); }
    }
}
