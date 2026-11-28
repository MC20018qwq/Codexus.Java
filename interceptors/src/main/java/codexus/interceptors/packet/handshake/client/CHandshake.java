package codexus.interceptors.packet.handshake.client;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import io.netty.buffer.ByteBuf;
import static codexus.development.sdk.extensions.NettyExtensions.*;
import codexus.development.sdk.manager.EventManager;
import codexus.development.sdk.utils.MessageChannels;
import codexus.interceptors.event.EventHandshake;

@RegisterPacket(state = EnumConnectionState.Handshaking, direction = EnumPacketDirection.ServerBound, packetIds = {0})
public class CHandshake implements IPacket {
    public int ProtocolVersion;
    public String ServerAddress = "";
    public int ServerPort;
    public int NextState;
    private EnumProtocolVersion clientProtocolVersion = EnumProtocolVersion.fromValue(0);
    @Override public EnumProtocolVersion getClientProtocolVersion() { return clientProtocolVersion; }
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) { clientProtocolVersion = value; }

    @Override public void readFromBuffer(ByteBuf buffer) {
        ProtocolVersion = readVarIntFromBuffer(buffer);
        ServerAddress = readStringFromBuffer(buffer, 255);
        ServerPort = buffer.readUnsignedShort();
        NextState = readVarIntFromBuffer(buffer);
    }
    @Override public void writeToBuffer(ByteBuf buffer) {
        writeVarInt(buffer, ProtocolVersion);
        writeStringToBuffer(buffer, ServerAddress);
        buffer.writeShort(ServerPort);
        writeVarInt(buffer, NextState);
    }
    @Override public boolean handlePacket(GameConnection connection) {
        if (EventManager.Instance.triggerEvent(MessageChannels.AllVersions,
                new EventHandshake(connection, this)).IsCancelled) return true;
        connection.setProtocolVersionValue(ProtocolVersion);
        connection.setStateValue(NextState);
        ServerPort = connection.ForwardPort & 0xffff;
        String suffix;
        if (ProtocolVersion == EnumProtocolVersion.V12111.value()) {
            // Native NeoForge negotiates in Configuration. Preserve any client-supplied
            // handshake suffix instead of fabricating the old Forge marker.
            int separator = ServerAddress.indexOf('\0');
            suffix = separator < 0 ? "" : ServerAddress.substring(separator);
        } else {
            suffix = ProtocolVersion <= EnumProtocolVersion.V1122.value() ? "\0FML\0"
                    : ProtocolVersion <= EnumProtocolVersion.V1180.value() ? "\0FML2\0"
                    : ProtocolVersion <= EnumProtocolVersion.V1206.value() ? "\0FML3\0" : "\0FORGE";
        }
        ServerAddress = connection.ForwardAddress + suffix;
        System.getLogger(getClass().getName()).log(System.Logger.Level.INFO,
                "Protocol version: {0}, next state: {1}, address: {2}",
                ProtocolVersion, NextState, ServerAddress.replace('\0', '|'));
        return false;
    }
}
