package codexus.heypixel.plugin.packets;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import io.netty.buffer.ByteBuf;
import codexus.base1206.plugin.utils.nbt.*;
import static codexus.development.sdk.extensions.NettyExtensions.*;
import codexus.heypixel.plugin.HeypixelProtocolPlugin;
@RegisterPacket(state=EnumConnectionState.Play,direction=EnumPacketDirection.ClientBound,packetIds=0x60,versions=EnumProtocolVersion.V1206,append=true)
public final class ClientboundSetPlayerTeamFixPacket implements IPacket {
    private int mode;
    public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}
    public void readFromBuffer(ByteBuf b){readStringFromBuffer(b,32767);mode=b.readUnsignedByte();}
    /** This appended observer never replaces the original packet. */
    public void writeToBuffer(ByteBuf b){}
    public boolean handlePacket(GameConnection c){return mode==4&&HeypixelProtocolPlugin.isTargetGame(c);}
}
