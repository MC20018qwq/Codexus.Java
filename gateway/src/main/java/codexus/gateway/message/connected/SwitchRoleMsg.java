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

@RegisterMessage(state = ConnectionState.Connected, type = "java_edition/session/switch_role")
public final class SwitchRoleMsg extends JsonMessage<EntitySwitchRole> {
    public SwitchRoleMsg() { super(EntitySwitchRole.class); }
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        if (request == null) { send(connection, "error_notification", "传入参数为空"); return; }
        UUID id; try { id = UUID.fromString(request.Id); } catch (IllegalArgumentException e) { send(connection, "error_notification", "无法解析 ID!"); return; }
        var interceptor = GameManager.Instance.Interceptors.get(id);
        if (interceptor == null) { send(connection, "error_notification", "未找到匹配的 Interceptor"); return; }
        var user = handler.getUsers().getAvailableUser(request.UserId);
        if (user == null) { send(connection, "error_notification_rb", "请先登录网易账户"); return; }
        interceptor.changeRole(request.RoleName, user.UserId, user.AccessToken); send(connection, "java_edition/session/switch_role", "");
    }
}
