package codexus.gateway.message.connected;

import codexus.csharpsup.*;
import codexus.gateway.connection.*;
import codexus.gateway.handler.ClientHandler;
import codexus.gateway.message.*;
import codexus.gateway.manager.*;
import codexus.gateway.entities.Entity;
import codexus.gateway.entities.EntityResponse;
import codexus.gateway.entities.web.*;
import codexus.gateway.entities.web.gateway.*;
import codexus.gateway.entities.web.netgame.*;
import codexus.gateway.entities.web.rentalgame.*;
import codexus.gateway.entities.web.role.*;
import codexus.gateway.entities.web.javaedition.skins.*;
import codexus.gateway.enums.Platform;
import codexus.cipher.protocol.*;
import codexus.cipher.entities.Entities;
import codexus.cipher.entities.wpflauncher.netgame.*;
import codexus.cipher.entities.wpflauncher.netgame.skin.*;
import codexus.game.launcher.utils.*;
import codexus.game.launcher.services.java.*;
import codexus.interceptors.Interceptor;
import java.nio.file.*;
import java.util.*;
import static codexus.gateway.handler.ClientHandler.send;

@RegisterMessage(state = ConnectionState.Connected, type = "delete_user")
public final class DeleteUserMessage extends RawMessage {
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        if (payload == null) return;
        var json = DotNetJson.parse(payload); String id = json.path("id").asText("");
        if (json.path("platform").asInt(0) == Platform.Mobile.value()) { send(connection, "error_notification", "PE/Bedrock 不在迁移范围内"); return; }
        if (!id.isEmpty()) { handler.getUsers().removeUser(id); handler.getUsers().removeAvailableUser(id); new GetAccountMessage().handleMessage(handler, connection); }
    }
}
