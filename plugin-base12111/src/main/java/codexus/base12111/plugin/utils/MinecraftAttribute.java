package codexus.base12111.plugin.utils;

import codexus.development.sdk.connection.GameConnection;
import codexus.base12111.plugin.utils.patch.Teams;
import io.netty.util.AttributeKey;
import static codexus.development.sdk.extensions.NettyExtensions.getOrDefault;
public final class MinecraftAttribute {
    private MinecraftAttribute() {}
    private static final AttributeKey<LocalPlayer> PLAYER = AttributeKey.valueOf("minecraft:player");
    private static final AttributeKey<World> WORLD = AttributeKey.valueOf("minecraft:world");
    private static final AttributeKey<Teams> TEAMS = AttributeKey.valueOf("minecraft:teams");
    public static LocalPlayer getLocalPlayer(GameConnection connection) { return connection.ClientChannel.attr(PLAYER).get(); }
    public static void setLocalPlayer(GameConnection connection, LocalPlayer player) { connection.ClientChannel.attr(PLAYER).set(player); }
    public static World getWorld(GameConnection connection) { return connection.ClientChannel.attr(WORLD).get(); }
    public static void setWorld(GameConnection connection, World world) { connection.ClientChannel.attr(WORLD).set(world); }
    public static Teams getTeams(GameConnection connection) { return getOrDefault(connection.ClientChannel.attr(TEAMS), Teams::new); }
}
