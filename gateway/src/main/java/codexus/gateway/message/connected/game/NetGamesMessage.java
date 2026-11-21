package codexus.gateway.message.connected.game;

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

@RegisterMessage(state = ConnectionState.Connected, type = "net_games")
public final class NetGamesMessage extends JsonMessage<EntityNetGamesRequest> {
    public NetGamesMessage() { super(EntityNetGamesRequest.class); }
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        if (request == null) return;
        var user = handler.getUsers().getLastAvailableUser();
        if (user == null) { send(connection, "error_notification_rb", "请先登录网易账户"); return; }
        try {
            var games = handler.getX19().getAvailableNetGames(user.UserId, user.AccessToken, request.Offset, request.Length);
            if (games.Data.length != 0) {
                var details = handler.getX19().queryNetGameItemByIds(user.UserId, user.AccessToken, Arrays.stream(games.Data).map(game -> game.EntityId).toArray(String[]::new));
                for (int i = 0; i < games.Data.length; i++) games.Data[i].TitleImageUrl = details.Data[i].TitleImageUrl;
            }
            var response = new EntityNetGamesResponse(); response.Entities = games.Data; response.Total = games.Total;
            send(connection, "net_games", DotNetJson.serialize(response));
        } catch (Exception e) { System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "Failed to get net games", e); }
    }
}
