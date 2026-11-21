package codexus.interceptors.packet.login.client;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import io.netty.buffer.ByteBuf;
import static codexus.development.sdk.extensions.NettyExtensions.*;
import codexus.development.sdk.manager.EventManager;
import codexus.development.sdk.event.EventLoginSuccess;
import codexus.development.sdk.utils.MessageChannels;

@RegisterPacket(state = EnumConnectionState.Login, direction = EnumPacketDirection.ServerBound, packetIds = {3}, versions = {EnumProtocolVersion.V1210, EnumProtocolVersion.V1206, EnumProtocolVersion.V1218, EnumProtocolVersion.V12110, EnumProtocolVersion.V12111})
public class CPacketLoginAcknowledged implements IPacket {
    private EnumProtocolVersion clientProtocolVersion = EnumProtocolVersion.fromValue(0);
    @Override public EnumProtocolVersion getClientProtocolVersion() { return clientProtocolVersion; }
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) { clientProtocolVersion = value; }
    @Override public void readFromBuffer(ByteBuf buffer) { }
    @Override public void writeToBuffer(ByteBuf buffer) { }
    @Override public boolean handlePacket(GameConnection connection) {
        if (EventManager.Instance.triggerEvent(MessageChannels.AllVersions,
                new EventLoginSuccess(connection)).IsCancelled) return true;
        connection.State = EnumConnectionState.Configuration;
        System.getLogger(getClass().getName()).log(System.Logger.Level.INFO, "Configuration started.");
        return false;
    }
}
