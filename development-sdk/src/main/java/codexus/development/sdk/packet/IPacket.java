package codexus.development.sdk.packet;
import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.EnumProtocolVersion;
import io.netty.buffer.ByteBuf;
public interface IPacket {
    EnumProtocolVersion getClientProtocolVersion();
    void setClientProtocolVersion(EnumProtocolVersion version);
    void readFromBuffer(ByteBuf buffer);
    void writeToBuffer(ByteBuf buffer);
    boolean handlePacket(GameConnection connection);
}
