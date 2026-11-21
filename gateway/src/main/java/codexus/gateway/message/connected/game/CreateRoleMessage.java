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

@RegisterMessage(state = ConnectionState.Connected, type = "create_role")
public final class CreateRoleMessage extends JsonMessage<EntityCreateRoleRequest> {
    public CreateRoleMessage() { super(EntityCreateRoleRequest.class); }
    @Override public void readFromMessage(ClientHandler handler, String payload) {
        try { super.readFromMessage(handler, payload); } catch (RuntimeException e) { request = null; }
    }
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        if (request == null) { send(connection, "create_role", EntityResponse.error(1001, "Create role failed, cannot ser deserialize payload")); return; }
        var user = handler.getUsers().getAvailableUser(request.UserId);
        if (user == null) { send(connection, "create_role", EntityResponse.error(1001, "Create role failed, cannot find user")); return; }
        try {
            if ("net_game".equals(request.GameType)) handler.getX19().createCharacter(user.UserId, user.AccessToken, request.GameId, request.RoleName);
            else if ("rental_game".equals(request.GameType)) handler.getX19().addRentalGameRole(user.UserId, user.AccessToken, request.GameId, request.RoleName);
            else throw new IllegalArgumentException("Error game type while creating role");
            send(connection, "success_notification", "创建角色成功!"); send(connection, "create_role", "");
            var roles = new GetRoleMessage(); var roleRequest = new EntityGetRoleRequest(); roleRequest.GameId = request.GameId;
            roleRequest.UserId = request.UserId; roleRequest.GameType = request.GameType; roles.setRequest(roleRequest); roles.handleMessage(handler, connection);
        } catch (Exception e) { send(connection, "error_notification", "创建角色失败: " + e.getMessage()); send(connection, "create_role", ""); }
    }
}
