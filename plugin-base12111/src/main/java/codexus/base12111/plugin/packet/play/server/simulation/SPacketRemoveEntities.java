package codexus.base12111.plugin.packet.play.server.simulation;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import io.netty.buffer.ByteBuf;
import static codexus.base12111.plugin.extensions.AttributeExtensions.*;
import static codexus.development.sdk.extensions.NettyExtensions.*;

@RegisterPacket(state=EnumConnectionState.Play, direction=EnumPacketDirection.ClientBound,
        packetIds=0x4B, versions=EnumProtocolVersion.V12111)
public final class SPacketRemoveEntities implements IPacket {
    public int[] EntityIds = new int[0];
    private EnumProtocolVersion clientProtocolVersion = EnumProtocolVersion.V12111;
    @Override public EnumProtocolVersion getClientProtocolVersion() { return clientProtocolVersion; }
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) { clientProtocolVersion = value; }
    @Override public void readFromBuffer(ByteBuf buffer) {
        int count = readVarIntFromBuffer(buffer);
        if (count < 0 || count > buffer.readableBytes()) throw new IllegalArgumentException("Invalid removed entity count");
        EntityIds = new int[count];
        for (int i = 0; i < count; i++) EntityIds[i] = readVarIntFromBuffer(buffer);
    }
    @Override public void writeToBuffer(ByteBuf buffer) {
        writeVarInt(buffer, EntityIds.length);
        for (int id : EntityIds) writeVarInt(buffer, id);
    }
    @Override public boolean handlePacket(GameConnection connection) {
        var world = getWorld(connection);
        if (world != null) for (int id : EntityIds) world.removeEntity(id);
        return false;
    }
}
