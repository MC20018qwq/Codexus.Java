package codexus.base12111.plugin.extensions;

import codexus.base12111.plugin.utils.*;
import codexus.development.sdk.connection.GameConnection;
import codexus.base12111.plugin.utils.patch.Teams;
import io.netty.util.AttributeKey;
import static codexus.development.sdk.extensions.NettyExtensions.getOrDefault;
public final class AttributeExtensions {
    private AttributeExtensions() {}
    public static LocalPlayer getLocalPlayer(GameConnection connection) { return MinecraftAttribute.getLocalPlayer(connection); }
    public static void setLocalPlayer(GameConnection connection, LocalPlayer player) { MinecraftAttribute.setLocalPlayer(connection, player); }
    public static World getWorld(GameConnection connection) { return MinecraftAttribute.getWorld(connection); }
    public static void setWorld(GameConnection connection, World world) { MinecraftAttribute.setWorld(connection, world); }
    public static Teams getTeams(GameConnection connection) { return MinecraftAttribute.getTeams(connection); }
}

