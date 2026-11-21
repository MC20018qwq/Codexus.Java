package codexus.gateway.message.connected.plugin;
import codexus.csharpsup.DotNetJson;
import codexus.development.sdk.manager.PluginManager;
import codexus.gateway.connection.*;
import codexus.gateway.entities.web.gateway.EntityPluginsResponse;
import codexus.gateway.handler.ClientHandler;
import codexus.gateway.message.*;
import java.util.ArrayList;
import static codexus.gateway.handler.ClientHandler.send;

@RegisterMessage(state = ConnectionState.Connected, type = "query_plugins")
public final class QueryPluginsMessage extends RawMessage {
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        var result = new ArrayList<EntityPluginsResponse>(); var manager = PluginManager.Instance;
        synchronized (manager) {
            for (var plugin : manager.Plugins.values()) {
                var value = new EntityPluginsResponse(); value.PluginId = plugin.Id; value.PluginName = plugin.Name;
                value.PluginDescription = plugin.Description; value.PluginVersion = plugin.Version; value.PluginAuthor = plugin.Author;
                value.PluginStatus = plugin.Status; value.PluginWaitingRestart = QueryInstallMessage.waiting(plugin.Id); result.add(value);
            }
        }
        send(connection, "query_plugins", DotNetJson.serialize(result));
    }
}
