package codexus.heypixel.plugin.networking;

import io.netty.util.*;
import codexus.development.sdk.connection.GameConnection;
import codexus.heypixel.plugin.anticheat.AntiCheatSession;
public final class ConnectionStateAccessor {
    private ConnectionStateAccessor(){}
    private static final AttributeKey<PluginChannelRegistry> CHANNELS=AttributeKey.valueOf("minecraft:netregistry");
    private static final AttributeKey<PluginChannelRegistry> SERVER_CHANNELS=AttributeKey.valueOf("minecraft:server-netregistry");
    private static final AttributeKey<AntiCheatSession> SESSION=AttributeKey.valueOf("heypixel:anticheat");
    public static PluginChannelRegistry getPluginChannels(AttributeMap map){var a=map.attr(CHANNELS);synchronized(a){if(a.get()==null)a.set(new PluginChannelRegistry());return a.get();}}
    public static PluginChannelRegistry getServerPluginChannels(AttributeMap map){var a=map.attr(SERVER_CHANNELS);synchronized(a){if(a.get()==null)a.set(new PluginChannelRegistry());return a.get();}}
    public static AntiCheatSession getAntiCheatSession(AttributeMap map,GameConnection connection){var a=map.attr(SESSION);synchronized(a){if(a.get()==null)a.set(new AntiCheatSession(connection));return a.get();}}
    public static void setAntiCheatSession(AttributeMap map,AntiCheatSession session){map.attr(SESSION).set(session);}
}
