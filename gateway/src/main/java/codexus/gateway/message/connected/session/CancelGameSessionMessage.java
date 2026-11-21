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

@RegisterMessage(state = ConnectionState.Connected, type = "cancel_game_session")
public final class CancelGameSessionMessage extends JsonMessage<UUID[]> {
    public CancelGameSessionMessage() { super(UUID[].class); }
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        if (request != null) { for (var id : request) GameManager.Instance.shutdownLauncher(id); for (var id : request) GameManager.Instance.shutdownInterceptor(id); }
        new QueryGameSessionMessage().handleMessage(handler, connection);
    }
}
