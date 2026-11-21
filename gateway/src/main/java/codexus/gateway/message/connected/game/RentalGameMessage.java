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

@RegisterMessage(state = ConnectionState.Connected, type = "rental_games")
public final class RentalGameMessage extends JsonMessage<EntityRentalGameRequest> {
    public RentalGameMessage() { super(EntityRentalGameRequest.class); }
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        if (request == null) return;
        var user = handler.getUsers().getLastAvailableUser();
        if (user == null) { send(connection, "error_notification_rb", "请先登录网易账户"); return; }
        try {
            var games = handler.getX19().getRentalGameList(user.UserId, user.AccessToken, request.Offset);
            var response = new EntityRentalGameResponse(); response.Entities = games.Data; response.Total = games.Total;
            send(connection, "rental_games", DotNetJson.serialize(response));
        } catch (Exception e) { System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "Failed to get rental games", e); }
    }
}
