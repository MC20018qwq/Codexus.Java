package codexus.gateway.message.connected.plugin;
import codexus.csharpsup.DotNetJson;
import codexus.gateway.connection.*;
import codexus.gateway.handler.ClientHandler;
import codexus.gateway.message.*;
import static codexus.gateway.handler.ClientHandler.send;

/** The C# handler was empty; explicit update requests can reuse the single-plugin contract. */
@RegisterMessage(state = ConnectionState.Connected, type = "update_plugins")
public final class UpdatePluginsMessage extends RawMessage {
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        try {
            var updates = payload == null || payload.isBlank() ? null : DotNetJson.parse(payload);
            if (updates == null || !updates.isArray()) {
                send(connection, "error_notification", "原版未提供自动批量更新；请逐个更新插件，或提交 update_plugin 请求数组"); return;
            }
            for (var update : updates) {
                var message = new UpdatePluginMessage(); message.readFromMessage(handler, update.toString()); message.handleMessage(handler, connection);
            }
        } catch (Exception error) { ClientHandler.error(connection, error); }
    }
}
