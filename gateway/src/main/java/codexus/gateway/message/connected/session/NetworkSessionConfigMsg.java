package codexus.gateway.message.connected.session;

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

@RegisterMessage(state = ConnectionState.Connected, type = "java_edition/network/session/config")
public final class NetworkSessionConfigMsg extends RawMessage {
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        Interceptor interceptor;
        try { interceptor = GameManager.Instance.Interceptors.get(UUID.fromString(payload)); }
        catch (IllegalArgumentException e) { interceptor = null; }
        if (interceptor == null) { send(connection, "error_notification_rb", "不存在该拦截器!"); return; }
        send(connection, "java_edition/network/session/config", DotNetJson.serialize(interceptor.getCurrentConfig()));
    }
}
