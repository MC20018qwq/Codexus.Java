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

@RegisterMessage(state = ConnectionState.Connected, type = "java_edition/purchase_skin")
public final class PurchaseSkinMsg extends JsonMessage<EntityPurchaseSkinRequest> {
    public PurchaseSkinMsg() { super(EntityPurchaseSkinRequest.class); }
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        if (request == null) return;
        var user = handler.getUsers().getAvailableUser(request.UserId);
        if (user == null) { send(connection, "error_notification_rb", "请先登录网易账户"); return; }
        var result = handler.getX19().purchaseSkin(user.UserId, user.AccessToken, request.ItemId);
        if (result.Code == 0) send(connection, "java_edition/purchase_skin", DotNetJson.serialize(result.Data));
        else { send(connection, "error_notification", result.Code == 35 ? "混合登录(Mixed)的账户无法暂时购买/设置皮肤" : result.Message); send(connection, "java_edition/skin_error", ""); }
    }
}
