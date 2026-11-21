package codexus.interceptors.packet.configuration.server;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import io.netty.buffer.ByteBuf;
import static codexus.development.sdk.extensions.NettyExtensions.*;

@RegisterPacket(state = EnumConnectionState.Play, direction = EnumPacketDirection.ClientBound, packetIds = {105, 105, 111, 116, 116}, versions = {EnumProtocolVersion.V1206, EnumProtocolVersion.V1210, EnumProtocolVersion.V1218, EnumProtocolVersion.V12110, EnumProtocolVersion.V12111})
public class SStartConfiguration implements IPacket {
    private EnumProtocolVersion clientProtocolVersion = EnumProtocolVersion.fromValue(0);
    @Override public EnumProtocolVersion getClientProtocolVersion() { return clientProtocolVersion; }
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) { clientProtocolVersion = value; }
    // These configuration packets have an empty body in the original implementation.
    @Override public void readFromBuffer(ByteBuf buffer) { }
    @Override public void writeToBuffer(ByteBuf buffer) { }
    @Override public boolean handlePacket(GameConnection connection) {
        connection.State = EnumConnectionState.Configuration;
        return false;
    }
}
