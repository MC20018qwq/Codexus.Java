package codexus.interceptors.packet.login.server;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import io.netty.buffer.ByteBuf;
import static codexus.development.sdk.extensions.NettyExtensions.*;
import codexus.development.sdk.manager.EventManager;
import codexus.development.sdk.event.EventLoginSuccess;
import codexus.development.sdk.utils.MessageChannels;

@RegisterPacket(state = EnumConnectionState.Login, direction = EnumPacketDirection.ClientBound, packetIds = {2}, versions = {EnumProtocolVersion.V1076, EnumProtocolVersion.V108X, EnumProtocolVersion.V1200, EnumProtocolVersion.V1122, EnumProtocolVersion.V1180, EnumProtocolVersion.V1210, EnumProtocolVersion.V1206, EnumProtocolVersion.V1218, EnumProtocolVersion.V12110, EnumProtocolVersion.V12111})
public class SPacketLoginSuccess implements IPacket {
    private String uuidText = "";
    private final byte[] uuidBytes = new byte[16];
    private String username = "";
    private java.util.List<codexus.development.sdk.entities.Property> profileProperties;
    private boolean strictErrorHandling;
    private EnumProtocolVersion clientProtocolVersion = EnumProtocolVersion.fromValue(0);
    @Override public EnumProtocolVersion getClientProtocolVersion() { return clientProtocolVersion; }
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) { clientProtocolVersion = value; }

    @Override public void readFromBuffer(ByteBuf buffer) {
        if (clientProtocolVersion == null) return;
        switch (clientProtocolVersion) {
            case V1200, V1206, V1210, V1218, V12110, V12111 -> {
                buffer.readBytes(uuidBytes);
                username = readStringFromBuffer(buffer, 16);
                profileProperties = readProperties(buffer);
                if (clientProtocolVersion == EnumProtocolVersion.V1206 || clientProtocolVersion == EnumProtocolVersion.V1210)
                    strictErrorHandling = buffer.readBoolean();
            }
            case V1076, V108X, V1122, V1180 -> {
                uuidText = readStringFromBuffer(buffer, 36);
                username = readStringFromBuffer(buffer, 16);
            }
            default -> { }
        }
    }
    @Override public void writeToBuffer(ByteBuf buffer) {
        if (clientProtocolVersion == null) return;
        switch (clientProtocolVersion) {
            case V1200, V1206, V1210, V1218, V12110, V12111 -> {
                buffer.writeBytes(uuidBytes);
                writeStringToBuffer(buffer, username, 16);
                writeProperties(buffer, profileProperties);
                if (clientProtocolVersion == EnumProtocolVersion.V1206 || clientProtocolVersion == EnumProtocolVersion.V1210)
                    buffer.writeBoolean(strictErrorHandling);
            }
            case V1076, V108X, V1122, V1180 -> {
                writeStringToBuffer(buffer, uuidText, 36);
                writeStringToBuffer(buffer, username, 16);
            }
            default -> { }
        }
    }
    @Override public boolean handlePacket(GameConnection connection) {
        connection.Uuid = uuidBytes;
        if (clientProtocolVersion != null && clientProtocolVersion.value() >= EnumProtocolVersion.V1206.value()) return false;
        if (EventManager.Instance.triggerEvent(MessageChannels.AllVersions,
                new EventLoginSuccess(connection)).IsCancelled) return true;
        connection.State = EnumConnectionState.Play;
        return false;
    }
}
