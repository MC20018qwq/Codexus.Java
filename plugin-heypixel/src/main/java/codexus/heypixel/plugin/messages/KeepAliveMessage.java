package codexus.heypixel.plugin.messages;

import io.netty.buffer.*;
import codexus.development.sdk.connection.GameConnection;
import codexus.heypixel.plugin.HeypixelProtocolPlugin;
import codexus.heypixel.plugin.anticheat.*;
import codexus.heypixel.plugin.models.*;
import codexus.heypixel.plugin.networking.*;
import static codexus.heypixel.plugin.networking.MessagePackBufferExtensions.*;
public final class KeepAliveMessage implements IAntiCheatMessage {
    public void read(ByteBuf b,GameConnection c){throw new IllegalStateException("Client message, cannot receive.");}
    public void write(ByteBuf b){writeInt64(b,System.currentTimeMillis());}
}
