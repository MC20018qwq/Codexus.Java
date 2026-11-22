package codexus.heypixel.plugin.packets;

import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.packet.*;
import io.netty.buffer.ByteBuf;
import codexus.base1206.plugin.utils.nbt.*;
import static codexus.development.sdk.extensions.NettyExtensions.*;
@RegisterPacket(state=EnumConnectionState.Play,direction=EnumPacketDirection.ClientBound,packetIds=64,versions=EnumProtocolVersion.V1200,skip=true)
public final class ClientboundResourcePackPushPacket implements IPacket {
    public String Url="",Hash="",Prompt="";public boolean Required,HasPrompt;
    public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}
    public void readFromBuffer(ByteBuf b){Url=readStringFromBuffer(b,32767);Hash=readStringFromBuffer(b,40);Required=b.readBoolean();HasPrompt=b.readBoolean();Prompt=HasPrompt?readStringFromBuffer(b,32767):"";}
    public void writeToBuffer(ByteBuf b){writeStringToBuffer(b,Url);writeStringToBuffer(b,Hash,40);b.writeBoolean(Required);b.writeBoolean(HasPrompt);if(HasPrompt)writeStringToBuffer(b,Prompt);}
    public boolean handlePacket(GameConnection c){return false;}
}
