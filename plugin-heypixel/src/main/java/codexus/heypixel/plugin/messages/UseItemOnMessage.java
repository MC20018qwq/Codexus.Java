package codexus.heypixel.plugin.messages;

import io.netty.buffer.*;
import codexus.development.sdk.connection.GameConnection;
import codexus.heypixel.plugin.HeypixelProtocolPlugin;
import codexus.heypixel.plugin.anticheat.*;
import codexus.heypixel.plugin.models.*;
import codexus.heypixel.plugin.networking.*;
import static codexus.heypixel.plugin.networking.MessagePackBufferExtensions.*;
public final class UseItemOnMessage implements IAntiCheatMessage {
    public float PlayerX;
    public float PlayerY;
    public float PlayerZ;
    public int Direction;
    public int InteractionType;
    public float HitX;
    public float HitY;
    public float HitZ;
    public double BlockX;
    public double BlockY;
    public double BlockZ;
    public boolean InsideBlock;
    public float Yaw;
    public float Pitch;
    public boolean IsHand;
    public void read(ByteBuf b,GameConnection c){throw new IllegalStateException("Client message, cannot receive.");}
    public void write(ByteBuf buffer){
        writeSingle(buffer,PlayerX);
        writeSingle(buffer,PlayerY);
        writeSingle(buffer,PlayerZ);
        writeInt32(buffer,Direction);
        writeInt32(buffer,InteractionType);
        writeSingle(buffer,HitX);
        writeSingle(buffer,HitY);
        writeSingle(buffer,HitZ);
        writeDouble(buffer,BlockX);
        writeDouble(buffer,BlockY);
        writeDouble(buffer,BlockZ);
        writeBoolean(buffer,InsideBlock);
        writeSingle(buffer,Yaw);
        writeSingle(buffer,Pitch);
        writeBoolean(buffer,IsHand);
    }
}
