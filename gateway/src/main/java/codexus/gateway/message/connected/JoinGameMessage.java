package codexus.gateway.message.connected;

import codexus.cipher.connection.NetEaseConnection;
import codexus.cipher.protocol.InterConn;
import codexus.cipher.utils.GameVersionConverter;
import codexus.csharpsup.DotNetJson;
import codexus.game.launcher.services.java.InstallerService;
import codexus.game.launcher.utils.GameVersionUtil;
import codexus.gateway.connection.*;
import codexus.gateway.entities.web.netgame.EntityJoinGame;
import codexus.gateway.handler.ClientHandler;
import codexus.gateway.manager.*;
import codexus.gateway.message.*;
import codexus.interceptors.Interceptor;
import static codexus.gateway.handler.ClientHandler.send;

@RegisterMessage(state = ConnectionState.Connected, type = "join_game")
public final class JoinGameMessage extends JsonMessage<EntityJoinGame> {
    public static final int RentalVersionId = 1169603340;
    public JoinGameMessage() { super(EntityJoinGame.class); }
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        Interceptor interceptor = null;
        try {
            if (request == null) throw new IllegalArgumentException("Invalid Java interceptor request");
            if (request.Role == null || request.Role.isEmpty()) throw new IllegalArgumentException("请选择/创建游戏角色");
            var user = handler.getUsers().getAvailableUser(request.UserId);
            if (user == null) throw new IllegalStateException("Please activate the selected Java account first");
            boolean rental = request.VersionId == RentalVersionId;
            var version = rental ? GameVersionUtil.getEnumFromGameVersion(request.Version) : GameVersionConverter.convert(request.VersionId);
            var api = handler.getX19();
            InstallerService.prepareAuthentication(user.UserId, user.AccessToken, api, version).join();
            String serverVersion = GameVersionUtil.getGameVersionFromEnum(version);
            String mods = DotNetJson.serialize(InstallerService.installGameMods(user.UserId, user.AccessToken, version, api, request.GameId, rental).join());
            TokenManager.Instance.updateToken(request.NexusId, request.NexusToken);
            interceptor = Interceptor.createInterceptor(rental, request.Socks5, mods, request.GameId, request.GameName, serverVersion,
                    request.ServerIp, request.ServerPort, request.Role, user.UserId, user.AccessToken, (config, certificate) -> {
                var current = handler.getUsers().getAvailableUser(config.UserId);
                if (current == null) throw new IllegalStateException("Current interceptor user is not logged in");
                NetEaseConnection.createAuthenticatorAsync(certificate, config.GameId, config.ServerVersion, config.ModInfo,
                        TokenManager.Instance.getToken(request.NexusId), Integer.parseInt(current.UserId), current.AccessToken, () -> { }).join();
            });
            InterConn.gameStart(user.UserId, user.AccessToken, request.GameId).join();
            GameManager.Instance.addInterceptor(interceptor);
            send(connection, "success_notification", "已成功创建新的拦截器!");
            send(connection, "join_game/success", interceptor.Identifier.toString());
            interceptor = null;
        } catch (Exception error) { ClientHandler.error(connection, error); }
        finally { if (interceptor != null) interceptor.shutdownAsync(); }
    }
}
