package codexus.heypixel.plugin.packets;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import codexus.base12111.plugin.utils.nbt.*;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import static codexus.development.sdk.extensions.NettyExtensions.*;

@RegisterPacket(state=EnumConnectionState.Play, direction=EnumPacketDirection.ClientBound,
        packetIds=0x4F, versions=EnumProtocolVersion.V12111, skip=true)
public class ClientboundResourcePackPushPacket implements IPacket {
    public UUID Id = new UUID(0, 0);
    public String Url = "", Hash = "";
    public boolean Required;
    /** Null represents an absent optional component; present values retain their NBT bytes. */
    public TextComponent Prompt;
    public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() { return ClientProtocolVersion; }
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) { ClientProtocolVersion = value; }
    @Override public void readFromBuffer(ByteBuf buffer) {
        Id = new UUID(buffer.readLong(), buffer.readLong());
        Url = readStringFromBuffer(buffer, 32767);
        Hash = readStringFromBuffer(buffer, 40);
        Required = buffer.readBoolean();
        Prompt = buffer.readBoolean() ? TextComponentSerializer.deserialize(buffer) : null;
    }
    @Override public void writeToBuffer(ByteBuf buffer) {
        buffer.writeLong(Id.getMostSignificantBits()).writeLong(Id.getLeastSignificantBits());
        writeStringToBuffer(buffer, Url, 32767);
        writeStringToBuffer(buffer, Hash, 40);
        buffer.writeBoolean(Required).writeBoolean(Prompt != null);
        if (Prompt != null) {
            ByteBuf encoded = TextComponentSerializer.serialize(Prompt);
            try { buffer.writeBytes(encoded); } finally { encoded.release(); }
        }
    }
    @Override public boolean handlePacket(GameConnection connection) { return false; }
}
