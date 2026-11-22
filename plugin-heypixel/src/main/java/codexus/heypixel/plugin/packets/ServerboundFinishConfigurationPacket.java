package codexus.heypixel.plugin.packets;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import io.netty.buffer.ByteBuf;
import codexus.base1206.plugin.utils.nbt.*;
import static codexus.development.sdk.extensions.NettyExtensions.*;
@RegisterPacket(state=EnumConnectionState.Configuration,direction=EnumPacketDirection.ServerBound,packetIds=3,versions={EnumProtocolVersion.V1206,EnumProtocolVersion.V1210})
public final class ServerboundFinishConfigurationPacket implements IPacket {
    public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}
    public void readFromBuffer(ByteBuf b){}
    public void writeToBuffer(ByteBuf b){}
    public boolean handlePacket(GameConnection connection){if(connection.ServerChannel==null)return true;connection.ServerChannel.writeAndFlush(this).addListener(f->{if(f.isSuccess())connection.State=EnumConnectionState.Play;else connection.shutdown();});return true;}
}
