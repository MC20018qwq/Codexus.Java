package codexus.gateway.message.connected.plugin;
import codexus.csharpsup.DotNetJson;
import codexus.development.sdk.manager.PluginManager;
import codexus.gateway.connection.*;
import codexus.gateway.entities.web.gateway.EntityQueryInstall;
import codexus.gateway.handler.ClientHandler;
import codexus.gateway.message.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static codexus.gateway.handler.ClientHandler.send;

@RegisterMessage(state = ConnectionState.Connected, type = "query_plugin_install")
public final class QueryInstallMessage extends JsonMessage<EntityQueryInstall> {
    public static final Set<String> WaitRestartPlugins = ConcurrentHashMap.newKeySet();
    public QueryInstallMessage() { super(EntityQueryInstall.class); }
    public QueryInstallMessage(EntityQueryInstall entity) { this(); request = entity; }
    public static boolean waiting(String id) { return WaitRestartPlugins.contains(id.toUpperCase(Locale.ROOT)); }
    public static void waitForRestart(String id) { WaitRestartPlugins.add(id.toUpperCase(Locale.ROOT)); }
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        if (request == null) throw new IllegalArgumentException("Invalid plugin query");
        var manager = PluginManager.Instance;
        var result = new EntityQueryInstall(); result.PluginId = request.PluginId; result.PluginVersion = request.PluginVersion;
        synchronized (manager) {
            result.PluginIsInstalled = manager.hasPlugin(request.PluginId);
            result.PluginInstalledVersion = result.PluginIsInstalled ? manager.getPlugin(request.PluginId).Version : "";
        }
        result.PluginIsWaitingRestart = waiting(request.PluginId);
        send(connection, "query_plugin_install", DotNetJson.serialize(result));
    }
}
