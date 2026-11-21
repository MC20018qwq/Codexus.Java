package codexus.interceptors.packet.login.client;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import io.netty.buffer.ByteBuf;
import static codexus.development.sdk.extensions.NettyExtensions.*;

@RegisterPacket(state = EnumConnectionState.Login, direction = EnumPacketDirection.ServerBound, packetIds = {0}, versions = {EnumProtocolVersion.V1076, EnumProtocolVersion.V108X, EnumProtocolVersion.V1200, EnumProtocolVersion.V1122, EnumProtocolVersion.V1180, EnumProtocolVersion.V1210, EnumProtocolVersion.V1206, EnumProtocolVersion.V1218, EnumProtocolVersion.V12110, EnumProtocolVersion.V12111})
public class CPacketLoginStart implements IPacket {
    private String username = "";
    private boolean hasPlayerUuid;
    private final byte[] playerUuid = new byte[16];
    private EnumProtocolVersion clientProtocolVersion = EnumProtocolVersion.fromValue(0);
    @Override public EnumProtocolVersion getClientProtocolVersion() { return clientProtocolVersion; }
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) { clientProtocolVersion = value; }

    @Override public void readFromBuffer(ByteBuf buffer) {
        if (clientProtocolVersion == null) return;
        switch (clientProtocolVersion) {
            case V1206, V1210, V1218, V12110, V12111 -> {
                username = readStringFromBuffer(buffer, 16);
                buffer.readBytes(playerUuid);
            }
            case V1200 -> {
                username = readStringFromBuffer(buffer, 16);
                hasPlayerUuid = buffer.readBoolean();
                if (hasPlayerUuid) buffer.readBytes(playerUuid);
            }
            case V1076, V108X, V1122, V1180 -> username = readStringFromBuffer(buffer, 16);
            default -> { }
        }
    }
    @Override public void writeToBuffer(ByteBuf buffer) {
        if (clientProtocolVersion == null) return;
        switch (clientProtocolVersion) {
            case V1206, V1210, V1218, V12110, V12111 -> {
                writeStringToBuffer(buffer, username, 16);
                buffer.writeBytes(playerUuid);
            }
            case V1200 -> {
                writeStringToBuffer(buffer, username, 16);
                buffer.writeBoolean(hasPlayerUuid);
                if (hasPlayerUuid) buffer.writeBytes(playerUuid);
            }
            case V1180 -> writeStringToBuffer(buffer, username);
            case V1076, V108X, V1122 -> writeStringToBuffer(buffer, username, 16);
            default -> { }
        }
    }
    @Override public boolean handlePacket(GameConnection connection) {
        username = connection.NickName;
        System.getLogger(getClass().getName()).log(System.Logger.Level.INFO, "{0} trying to login...", username);
        return false;
    }
}
