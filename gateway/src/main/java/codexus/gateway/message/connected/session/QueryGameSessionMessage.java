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

@RegisterMessage(state = ConnectionState.Connected, type = "query_game_session")
public final class QueryGameSessionMessage extends RawMessage {
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        var sessions = new ArrayList<EntityQueryGameSessions>();
        for (var interceptor : GameManager.Instance.getQueryInterceptors()) {
            var value = new EntityQueryGameSessions(); value.Id = "interceptor-" + interceptor.Id; value.Name = interceptor.Name;
            value.GameType = interceptor.GameType; value.IsRental = interceptor.IsRental; value.ServerName = interceptor.Server;
            value.Guid = interceptor.Name.toString(); value.CharacterName = interceptor.Role; value.ServerVersion = interceptor.Version;
            value.StatusText = "Running"; value.ProgressValue = 0; value.Type = "Interceptor"; value.LocalAddress = interceptor.LocalAddress; sessions.add(value);
        }
        for (var launcher : GameManager.Instance.getQueryLaunchers()) {
            var value = new EntityQueryGameSessions(); value.Id = "game-" + launcher.Id; value.Name = UUID.randomUUID(); value.GameType = "Java";
            value.IsRental = false; value.ServerName = launcher.Server; value.Guid = launcher.Name.toString(); value.CharacterName = launcher.Role;
            value.ServerVersion = launcher.Version; value.StatusText = launcher.StatusMessage; value.ProgressValue = launcher.Progress; value.Type = "Game"; sessions.add(value);
        }
        send(connection, "query_game_session", DotNetJson.serialize(sessions));
    }
}
