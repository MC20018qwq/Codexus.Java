package codexus.gateway.message.connected;

import codexus.cipher.utils.GameVersionConverter;
import codexus.csharpsup.DotNetJson;
import codexus.game.launcher.entities.EntityLaunchGame;
import codexus.game.launcher.services.java.LauncherService;
import codexus.game.launcher.utils.GameVersionUtil;
import codexus.gateway.connection.*;
import codexus.gateway.entities.Entity;
import codexus.gateway.handler.ClientHandler;
import codexus.gateway.manager.GameManager;
import codexus.gateway.message.*;
import static codexus.gateway.handler.ClientHandler.send;

@RegisterMessage(state = ConnectionState.Connected, type = "launch_game")
public final class LaunchGameMessage extends JsonMessage<EntityLaunchGame> {
    public LaunchGameMessage() { super(EntityLaunchGame.class); }
    @Override public void handleMessage(ClientHandler handler, SecureConnection connection) {
        try {
            if (request == null) throw new IllegalArgumentException("Invalid credentials");
            if (request.RoleName == null || request.RoleName.isEmpty()) throw new IllegalArgumentException("请选择游戏角色");
            if (request.GameVersionId == JoinGameMessage.RentalVersionId)
                request.GameVersionId = GameVersionConverter.convert(GameVersionUtil.getEnumFromGameVersion(request.GameVersion));
            var user = handler.getUsers().getAvailableUser(request.UserId);
            if (user == null) throw new IllegalStateException("请重新登录网易账户");
            var api = handler.getX19();
            var launcher = LauncherService.createLauncher(request, user.AccessToken, api, api.MPay.GameVersion,
                    update -> handler.broadcastMessage(new Entity("launch_progress", DotNetJson.serialize(update))));
            GameManager.Instance.addLauncher(launcher);
            String identify = connection.CurrentIdentify.get();
            // Keep this client's queue free for cancel/query while installation runs.
            launcher.getLaunchTask().whenComplete((unused, error) -> {
                String previous = connection.CurrentIdentify.get(); connection.CurrentIdentify.set(identify);
                try {
                    if (error == null) send(connection, "success_notification", "Game launched successfully");
                    else ClientHandler.error(connection, error);
                } finally {
                    if (previous == null) connection.CurrentIdentify.remove(); else connection.CurrentIdentify.set(previous);
                }
            });
        } catch (Exception error) { ClientHandler.error(connection, error); }
    }
}
