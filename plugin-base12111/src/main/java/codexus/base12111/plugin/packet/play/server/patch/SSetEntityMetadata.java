package codexus.base12111.plugin.packet.play.server.patch;

import codexus.base12111.plugin.Base12111;
import codexus.base12111.plugin.event.*;
import codexus.base12111.plugin.utils.*;
import codexus.base12111.plugin.utils.nbt.*;
import codexus.development.sdk.connection.GameConnection;
import codexus.development.sdk.enums.*;
import codexus.development.sdk.extensions.NettyExtensions;
import codexus.development.sdk.manager.EventManager;
import codexus.development.sdk.packet.*;
import codexus.development.sdk.utils.Position;
import io.netty.buffer.ByteBuf;
import static codexus.base12111.plugin.extensions.AttributeExtensions.*;
import java.util.ArrayList;
import java.util.List;
import codexus.base12111.plugin.utils.patch.metadata.*;
import static codexus.development.sdk.extensions.NettyExtensions.*;
@RegisterPacket(state=EnumConnectionState.Play,direction=EnumPacketDirection.ClientBound,packetIds=0x61,versions=EnumProtocolVersion.V12111)
public class SSetEntityMetadata implements IPacket {
    public int EntityId;
    public List<IDataValue> PackedItems = new ArrayList<>();
    public byte[] RawData;
    public EnumProtocolVersion ClientProtocolVersion = EnumProtocolVersion.None;
    @Override public EnumProtocolVersion getClientProtocolVersion() {return ClientProtocolVersion;}
    @Override public void setClientProtocolVersion(EnumProtocolVersion value) {ClientProtocolVersion=value;}
    public void readFromBuffer(ByteBuf buffer) {
        EntityId=readVarIntFromBuffer(buffer); int start=buffer.readerIndex(); PackedItems.clear(); RawData=null;
        try {
            while(buffer.isReadable()){int id=buffer.readUnsignedByte();if(id==255){if(buffer.isReadable())throw new IllegalArgumentException("Trailing metadata bytes");return;} PackedItems.add(DataValue.read(buffer,id));}
            preserveRawData(buffer,start);
        }catch(UnsupportedOperationException|IllegalArgumentException|IndexOutOfBoundsException|ArithmeticException e){preserveRawData(buffer,start);}
    }
    private void preserveRawData(ByteBuf buffer,int start){buffer.readerIndex(start);PackedItems.clear();RawData=readByteArrayReadableBytes(buffer);}
    public void writeToBuffer(ByteBuf buffer){writeVarInt(buffer,EntityId);if(RawData!=null){buffer.writeBytes(RawData);return;}for(var item:PackedItems)item.write(buffer);buffer.writeByte(255);}
    public boolean handlePacket(GameConnection connection){return EventManager.Instance.triggerEvent(Base12111.PluginChannel,new EventSetEntityMetadata(connection,this)).IsCancelled;}
}
