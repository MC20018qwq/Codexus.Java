package codexus.development.sdk.analysis;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import static codexus.development.sdk.extensions.NettyExtensions.*;
public class MessageDeserializer21Bit extends ByteToMessageDecoder {
    @Override protected void decode(ChannelHandlerContext context,ByteBuf message,List<Object> output) {
        message.markReaderIndex();byte[] bytes=new byte[3];
        for(int i=0;i<3;i++) {
            if(!message.isReadable()){message.resetReaderIndex();break;}
            bytes[i]=message.readByte();if((bytes[i]&255)>=128)continue;
            try {int length=readVarInt(bytes);if(message.readableBytes()>=length)output.add(message.readBytes(length));else message.resetReaderIndex();break;}
            catch(Exception e){System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR,"Failed to decode message.",e);break;}
        }
    }
}
