package codexus.development.sdk.utils;
import codexus.development.sdk.connection.GameConnection;
import io.netty.util.AttributeKey;
public final class ChannelAttribute {
    public static final AttributeKey<GameConnection> Connection=AttributeKey.valueOf("Connection");
    private ChannelAttribute() {}
}
