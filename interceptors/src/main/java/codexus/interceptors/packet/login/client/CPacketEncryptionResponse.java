package codexus.interceptors.packet.login.client;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import io.netty.buffer.ByteBuf;
import static codexus.development.sdk.extensions.NettyExtensions.*;

@RegisterPacket(state = EnumConnectionState.Login, direction = EnumPacketDirection.ServerBound, packetIds = {1}, versions = {EnumProtocolVersion.V1076, EnumProtocolVersion.V108X, EnumProtocolVersion.V1200, EnumProtocolVersion.V1122, EnumProtocolVersion.V1180, EnumProtocolVersion.V1210, EnumProtocolVersion.V1206, EnumProtocolVersion.V1218, EnumProtocolVersion.V12110, EnumProtocolVersion.V12111})
public class CPacketEncryptionResponse implements IPacket {
    public byte[] SecretKeyEncrypted = new byte[0];
    public byte[] VerifyTokenEncrypted = new byte[0];
    private EnumProtocolVersion clientProtocolVersion = EnumProtocolVersion.fromValue(0);
    @Override public EnumProtocolVersion getClientProtocolVersion() { return clientProtocolVersion; }
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) { clientProtocolVersion = value; }

    @Override public void readFromBuffer(ByteBuf buffer) {
        if (clientProtocolVersion == EnumProtocolVersion.V1076) {
            SecretKeyEncrypted = readByteArrayFromBuffer(buffer, buffer.readShort());
            VerifyTokenEncrypted = readByteArrayFromBuffer(buffer, buffer.readShort());
        } else {
            SecretKeyEncrypted = readByteArrayFromBuffer(buffer);
            VerifyTokenEncrypted = readByteArrayFromBuffer(buffer);
        }
    }
    @Override public void writeToBuffer(ByteBuf buffer) {
        if (clientProtocolVersion == EnumProtocolVersion.V1076) {
            buffer.writeShort(SecretKeyEncrypted.length).writeBytes(SecretKeyEncrypted);
            buffer.writeShort(VerifyTokenEncrypted.length).writeBytes(VerifyTokenEncrypted);
        } else {
            writeByteArrayToBuffer(buffer, SecretKeyEncrypted);
            writeByteArrayToBuffer(buffer, VerifyTokenEncrypted);
        }
    }
    @Override public boolean handlePacket(GameConnection connection) { return false; }
}
