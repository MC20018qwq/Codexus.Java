package codexus.base12111.plugin.packet.play.server.simulation;

import codexus.base12111.plugin.utils.PositionUpdate;
import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import io.netty.buffer.ByteBuf;
import static codexus.base12111.plugin.extensions.AttributeExtensions.*;
import static codexus.development.sdk.extensions.NettyExtensions.*;

@RegisterPacket(state=EnumConnectionState.Play, direction=EnumPacketDirection.ClientBound,
        packetIds=0x46, versions=EnumProtocolVersion.V12111)
public final class SPacketPlayerPosition implements IPacket {
    public int TeleportId, RelativeFlags;
    public PositionUpdate Position = new PositionUpdate(0, 0, 0, 0, 0, 0, 0, 0);
    private EnumProtocolVersion clientProtocolVersion = EnumProtocolVersion.V12111;
    @Override public EnumProtocolVersion getClientProtocolVersion() { return clientProtocolVersion; }
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) { clientProtocolVersion = value; }
    @Override public void readFromBuffer(ByteBuf buffer) {
        TeleportId = readVarIntFromBuffer(buffer); Position = PositionUpdate.read(buffer); RelativeFlags = buffer.readInt();
    }
    @Override public void writeToBuffer(ByteBuf buffer) {
        writeVarInt(buffer, TeleportId); Position.write(buffer); buffer.writeInt(RelativeFlags);
    }
    @Override public boolean handlePacket(GameConnection connection) {
        var player = getLocalPlayer(connection);
        if (player != null) Position.apply(player, RelativeFlags);
        return false;
    }
}
