package codexus.heypixel.plugin.packets;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import io.netty.buffer.ByteBuf;
import codexus.base1206.plugin.utils.nbt.*;
import static codexus.development.sdk.extensions.NettyExtensions.*;
@RegisterPacket(state=EnumConnectionState.Play,direction=EnumPacketDirection.ClientBound,packetIds={26,29},versions={EnumProtocolVersion.V1200,EnumProtocolVersion.V1206},skip=true)
public final class ClientboundPlayDisconnectPacket implements IPacket {
    public TextComponent Reason=new TextComponent();
    public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}
    public void readFromBuffer(ByteBuf b){Reason=TextComponentSerializer.deserialize(b);}
    public void writeToBuffer(ByteBuf b){ByteBuf text=TextComponentSerializer.serialize(Reason);try{b.writeBytes(text);}finally{text.release();}}
    public boolean handlePacket(GameConnection c){return false;}
}
