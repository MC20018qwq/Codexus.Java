package codexus.gateway.message.connected.javaedition.skins;

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

@RegisterMessage(state = ConnectionState.Connected, type = "java_edition/skin_list")
public final class SkinListMsg extends JsonMessage<EntitySkinListRequest> {
    public SkinListMsg() { super(EntitySkinListRequest.class); }
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        if (request == null) return;
        var user = handler.getUsers().getLastAvailableUser();
        if (user == null) { send(connection, "error_notification_rb", "请先登录网易账户"); return; }
        var skins = handler.getX19().getFreeSkinList(user.UserId, user.AccessToken, request.Offset, request.Length);
        var details = handler.getX19().getSkinDetails(user.UserId, user.AccessToken, skins);
        var response = new EntitySkinListResponse(); response.Entities = details.Data; response.Total = skins.Total;
        send(connection, "java_edition/skin_list", DotNetJson.serialize(response));
    }
}
