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

@RegisterMessage(state = ConnectionState.Connected, type = "get_roles")
public final class GetRoleMessage extends JsonMessage<EntityGetRoleRequest> {
    public GetRoleMessage() { super(EntityGetRoleRequest.class); }
    public void setRequest(EntityGetRoleRequest request) { this.request = request; }
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        if (request == null) return;
        var user = handler.getUsers().getAvailableUser(request.UserId); if (user == null) return;
        try {
            if ("net_game".equals(request.GameType)) send(connection, "get_roles", DotNetJson.serialize(handler.getX19().queryNetGameCharacters(user.UserId, user.AccessToken, request.GameId).Data));
            else if ("rental_game".equals(request.GameType)) send(connection, "get_roles", DotNetJson.serialize(handler.getX19().getRentalGameRolesList(user.UserId, user.AccessToken, request.GameId).Data));
        } catch (Exception e) { System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "Error getting role", e); }
    }
}
