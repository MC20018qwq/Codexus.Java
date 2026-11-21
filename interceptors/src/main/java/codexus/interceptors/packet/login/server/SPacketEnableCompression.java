package codexus.interceptors.packet.login.server;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import io.netty.buffer.ByteBuf;
import static codexus.development.sdk.extensions.NettyExtensions.*;

@RegisterPacket(state = EnumConnectionState.Login, direction = EnumPacketDirection.ClientBound, packetIds = {3}, versions = {EnumProtocolVersion.V108X, EnumProtocolVersion.V1200, EnumProtocolVersion.V1122, EnumProtocolVersion.V1180, EnumProtocolVersion.V1210, EnumProtocolVersion.V1206, EnumProtocolVersion.V1218, EnumProtocolVersion.V12110, EnumProtocolVersion.V12111})
public class SPacketEnableCompression implements IPacket {
    private int compressionThreshold;
    private EnumProtocolVersion clientProtocolVersion = EnumProtocolVersion.fromValue(0);
    @Override public EnumProtocolVersion getClientProtocolVersion() { return clientProtocolVersion; }
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) { clientProtocolVersion = value; }
    @Override public void readFromBuffer(ByteBuf buffer) { compressionThreshold = readVarIntFromBuffer(buffer); }
    @Override public void writeToBuffer(ByteBuf buffer) { writeVarInt(buffer, compressionThreshold); }
    @Override public boolean handlePacket(GameConnection connection) {
        GameConnection.enableCompression(connection.ServerChannel, compressionThreshold);
        return true;
    }
}
