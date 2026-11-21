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
import codexus.gateway.entities.web.javaedition.skins.EntitySkinDetailsRequest;
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

@RegisterMessage(state = ConnectionState.Connected, type = "java_edition/skin_details")
public final class SkinDetailsMsg extends JsonMessage<EntitySkinDetailsRequest> {
    public SkinDetailsMsg() { super(EntitySkinDetailsRequest.class); }
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        if (request == null) return;
        var user = handler.getUsers().getAvailableUser(request.UserId);
        if (user == null) { send(connection, "error_notification_rb", "请先登录网易账户"); return; }
        var query = new Entities<EntitySkin>(); var skin = new EntitySkin(); skin.EntityId = request.ItemId; query.Data = new EntitySkin[]{skin};
        var details = handler.getX19().getSkinDetails(user.UserId, user.AccessToken, query);
        send(connection, "java_edition/skin_details", DotNetJson.serialize(details.Data[0]));
    }
}
