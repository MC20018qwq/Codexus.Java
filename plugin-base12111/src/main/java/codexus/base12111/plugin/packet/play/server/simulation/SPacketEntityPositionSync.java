package codexus.base12111.plugin.packet.play.server.simulation;

import codexus.base12111.plugin.utils.*;
import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import io.netty.buffer.ByteBuf;
import static codexus.base12111.plugin.extensions.AttributeExtensions.*;
import static codexus.development.sdk.extensions.NettyExtensions.*;

@RegisterPacket(state=EnumConnectionState.Play, direction=EnumPacketDirection.ClientBound, packetIds=0x23, versions=EnumProtocolVersion.V12111)
public final class SPacketEntityPositionSync implements IPacket {
    public int EntityId;
    public PositionUpdate Position = new PositionUpdate(0, 0, 0, 0, 0, 0, 0, 0);
    public boolean OnGround;
    private EnumProtocolVersion clientProtocolVersion = EnumProtocolVersion.V12111;
    @Override public EnumProtocolVersion getClientProtocolVersion() { return clientProtocolVersion; }
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) { clientProtocolVersion = value; }
    @Override public void readFromBuffer(ByteBuf buffer) { EntityId = readVarIntFromBuffer(buffer); Position = PositionUpdate.read(buffer); OnGround = buffer.readBoolean(); }
    @Override public void writeToBuffer(ByteBuf buffer) { writeVarInt(buffer, EntityId); Position.write(buffer); buffer.writeBoolean(OnGround); }
    @Override public boolean handlePacket(GameConnection connection) {
        Entity entity = getLocalPlayer(connection);
        if (entity == null || entity.EntityId != EntityId) entity = getWorld(connection) == null ? null : getWorld(connection).getEntity(EntityId);
        if (entity != null) { Position.apply(entity, 0); entity.OnGround = OnGround; }
        return false;
    }
}
