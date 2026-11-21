package codexus.development.sdk.analysis;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import codexus.development.sdk.packet.IPacket;
import codexus.development.sdk.manager.PacketManager;
import codexus.development.sdk.utils.ChannelAttribute;
import static codexus.development.sdk.extensions.NettyExtensions.*;
public class MessageSerializer extends MessageToByteEncoder<IPacket> {
    public MessageSerializer(){super(false);}
    @Override protected void encode(ChannelHandlerContext context,IPacket message,ByteBuf output){
        var connection=context.channel().attr(ChannelAttribute.Connection).get();int id=PacketManager.Instance.getPacketId(connection.ProtocolVersion,message);
        if(id!=-1){writeVarInt(output,id);message.writeToBuffer(output);}
    }
}
