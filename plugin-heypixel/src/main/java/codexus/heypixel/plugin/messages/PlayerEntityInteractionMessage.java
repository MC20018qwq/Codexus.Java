package codexus.heypixel.plugin.messages;

import io.netty.buffer.*;
import codexus.development.sdk.connection.GameConnection;
import codexus.heypixel.plugin.HeypixelProtocolPlugin;
import codexus.heypixel.plugin.anticheat.*;
import codexus.heypixel.plugin.models.*;
import codexus.heypixel.plugin.networking.*;
import static codexus.heypixel.plugin.networking.MessagePackBufferExtensions.*;
import codexus.base1206.plugin.utils.*;
public final class PlayerEntityInteractionMessage implements IAntiCheatMessage {
    public final Player Player; public final Entity Entity;
    public PlayerEntityInteractionMessage(Player player,Entity entity){Player=player;Entity=entity;}
    public void read(ByteBuf b,GameConnection c){throw new IllegalStateException("Client message, cannot receive.");}
    /** The recovered source defines no payload for message 6. */
    public void write(ByteBuf b){}
}
