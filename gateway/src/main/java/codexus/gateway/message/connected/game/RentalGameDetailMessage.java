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

@RegisterMessage(state = ConnectionState.Connected, type = "rental_games_detail")
public final class RentalGameDetailMessage extends RawMessage {
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        if (payload == null) return;
        var user = handler.getUsers().getLastAvailableUser();
        if (user == null) { send(connection, "error_notification_rb", "请先登录网易账户"); return; }
        try {
            String[] parts = payload.split(":", -1); String id = parts[0], password = parts.length >= 2 ? parts[1] : null;
            var details = handler.getX19().getRentalGameDetails(user.UserId, user.AccessToken, id);
            var address = handler.getX19().getRentalGameServerAddress(user.UserId, user.AccessToken, id, password);
            if (address.Code != 0) { send(connection, "error_notification_rt", DotNetJson.serializeRelaxed(address.Message)); return; }
            if (details.Data != null && address.Data != null) { details.Data.ServerIp = address.Data.McServerHost; details.Data.ServerPort = address.Data.McServerPort; }
            send(connection, "rental_games_detail", DotNetJson.serialize(details.Data));
        } catch (Exception e) { System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "Failed to get rental game details", e); }
    }
}
