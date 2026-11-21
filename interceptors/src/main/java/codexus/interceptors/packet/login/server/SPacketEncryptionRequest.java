package codexus.interceptors.packet.login.server;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import io.netty.buffer.ByteBuf;
import static codexus.development.sdk.extensions.NettyExtensions.*;
import codexus.csharpsup.DotNetEncoding;
import codexus.development.sdk.extensions.CryptExtensions;
import codexus.development.sdk.manager.EventManager;
import codexus.development.sdk.event.EventEncryptionRequest;
import codexus.interceptors.packet.login.client.CPacketEncryptionResponse;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

@RegisterPacket(state = EnumConnectionState.Login, direction = EnumPacketDirection.ClientBound, packetIds = {1}, versions = {EnumProtocolVersion.V1076, EnumProtocolVersion.V108X, EnumProtocolVersion.V1200, EnumProtocolVersion.V1122, EnumProtocolVersion.V1180, EnumProtocolVersion.V1210, EnumProtocolVersion.V1206, EnumProtocolVersion.V1218, EnumProtocolVersion.V12110, EnumProtocolVersion.V12111})
public class SPacketEncryptionRequest implements IPacket {
    private String serverId = "";
    private byte[] publicKey = new byte[0];
    private byte[] verifyToken = new byte[0];
    private boolean shouldAuthenticate;
    private EnumProtocolVersion clientProtocolVersion = EnumProtocolVersion.fromValue(0);
    @Override public EnumProtocolVersion getClientProtocolVersion() { return clientProtocolVersion; }
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) { clientProtocolVersion = value; }

    @Override public void readFromBuffer(ByteBuf buffer) {
        serverId = readStringFromBuffer(buffer, 20);
        if (clientProtocolVersion == EnumProtocolVersion.V1076) {
            publicKey = readByteArrayFromBuffer(buffer, buffer.readShort());
            verifyToken = readByteArrayFromBuffer(buffer, buffer.readShort());
        } else {
            publicKey = readByteArrayFromBuffer(buffer);
            verifyToken = readByteArrayFromBuffer(buffer);
            if (clientProtocolVersion != null && clientProtocolVersion.value() >= EnumProtocolVersion.V1206.value())
                shouldAuthenticate = buffer.readBoolean();
        }
    }
    @Override public void writeToBuffer(ByteBuf buffer) {
        writeStringToBuffer(buffer, serverId);
        if (clientProtocolVersion == EnumProtocolVersion.V1076) {
            buffer.writeShort(publicKey.length).writeBytes(publicKey);
            buffer.writeShort(verifyToken.length).writeBytes(verifyToken);
        } else {
            writeByteArrayToBuffer(buffer, publicKey);
            writeByteArrayToBuffer(buffer, verifyToken);
            if (clientProtocolVersion != null && clientProtocolVersion.value() >= EnumProtocolVersion.V1206.value())
                buffer.writeBoolean(shouldAuthenticate);
        }
    }
    @Override public boolean handlePacket(GameConnection connection) {
        try {
            byte[] secretKey = new byte[16];
            new SecureRandom().nextBytes(secretKey);
            var rsaKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKey));
            var hashInput = new java.io.ByteArrayOutputStream();
            hashInput.writeBytes(DotNetEncoding.latin1(serverId));
            hashInput.writeBytes(secretKey);
            hashInput.writeBytes(publicKey);
            String serverHash = CryptExtensions.toSha1(hashInput.toByteArray());
            var event = EventManager.Instance.triggerEvent("channel_interceptor", new EventEncryptionRequest(connection, serverHash));
            if (!event.IsCancelled && connection.OnJoinServer != null)
                connection.OnJoinServer.accept(connection.Config, serverHash);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, rsaKey);
            var response = new CPacketEncryptionResponse();
            response.setClientProtocolVersion(clientProtocolVersion);
            response.SecretKeyEncrypted = cipher.doFinal(secretKey);
            response.VerifyTokenEncrypted = cipher.doFinal(verifyToken);
            var channel = connection.ServerChannel;
            if (channel == null) {
                System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "Server channel is null");
                return false;
            }
            channel.config().setAutoRead(false);
            channel.writeAndFlush(response).addListener(future -> {
                if (!future.isSuccess()) return;
                try {
                    GameConnection.enableEncryption(channel, secretKey);
                    channel.config().setAutoRead(true);
                } catch (Exception e) {
                    System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "Failed to enable encryption", e);
                }
            });
            return true;
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException("Invalid encryption request", e);
        }
    }
}
