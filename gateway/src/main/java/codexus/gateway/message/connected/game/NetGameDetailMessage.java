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

@RegisterMessage(state = ConnectionState.Connected, type = "net_games_detail")
public final class NetGameDetailMessage extends RawMessage {
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        if (payload == null) return;
        var user = handler.getUsers().getLastAvailableUser();
        if (user == null) { send(connection, "error_notification_rb", "请先登录网易账户"); return; }
        try {
            var details = handler.getX19().queryNetGameDetailById(user.UserId, user.AccessToken, payload);
            var address = handler.getX19().getNetGameServerAddress(user.UserId, user.AccessToken, payload);
            if (details.Data != null && address.Data != null) { details.Data.ServerAddress = address.Data.Ip; details.Data.ServerPort = address.Data.Port; }
            send(connection, "net_games_detail", DotNetJson.serialize(details.Data));
        } catch (Exception e) { System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "Failed to get game details", e); }
    }
}
