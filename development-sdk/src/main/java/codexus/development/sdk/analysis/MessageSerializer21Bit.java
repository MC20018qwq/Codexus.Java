package codexus.development.sdk.analysis;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import static codexus.development.sdk.extensions.NettyExtensions.*;
public class MessageSerializer21Bit extends MessageToByteEncoder<ByteBuf> {
    @Override protected void encode(ChannelHandlerContext context,ByteBuf message,ByteBuf output) {
        int length=message.readableBytes(),size=getVarIntSize(length);
        if(size<=3){output.ensureWritable(size+length);writeVarInt(output,length);output.writeBytes(message,message.readerIndex(),length);}
    }
}
