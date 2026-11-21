package codexus.interceptors.packet.login.server;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import io.netty.buffer.ByteBuf;
import static codexus.development.sdk.extensions.NettyExtensions.*;

@RegisterPacket(state = EnumConnectionState.Login, direction = EnumPacketDirection.ClientBound, packetIds = {0})
public class SPacketDisconnect implements IPacket {
    public String Reason = "";
    private EnumProtocolVersion clientProtocolVersion = EnumProtocolVersion.fromValue(0);
    @Override public EnumProtocolVersion getClientProtocolVersion() { return clientProtocolVersion; }
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) { clientProtocolVersion = value; }
    @Override public void readFromBuffer(ByteBuf buffer) { Reason = readStringFromBuffer(buffer, 32767); }
    @Override public void writeToBuffer(ByteBuf buffer) { writeStringToBuffer(buffer, Reason); }
    @Override public boolean handlePacket(GameConnection connection) {
        System.getLogger(getClass().getName()).log(System.Logger.Level.DEBUG, "Disconnect reason: {0}", Reason);
        return false;
    }
}
