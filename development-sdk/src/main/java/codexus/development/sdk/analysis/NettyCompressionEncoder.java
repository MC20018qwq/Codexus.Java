package codexus.development.sdk.analysis;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import codexus.csharpsup.deflate.Deflater;
import static codexus.development.sdk.extensions.NettyExtensions.*;
public class NettyCompressionEncoder extends MessageToByteEncoder<ByteBuf> {
    private final byte[] buffer=new byte[4096];
    private final Deflater deflater=new Deflater();
    public int Threshold;
    public NettyCompressionEncoder(int threshold){Threshold=threshold;}
    @Override protected void encode(ChannelHandlerContext context,ByteBuf message,ByteBuf output) {
        int length=message.readableBytes();
        if(length<Threshold){writeVarInt(output,0);output.writeBytes(message);return;}
        deflater.Reset();deflater.SetInput(message.array(),message.arrayOffset()+message.readerIndex(),length);
        message.skipBytes(length);deflater.Finish();writeVarInt(output,length);
        while(!deflater.getIsFinished())output.writeBytes(buffer,0,deflater.Deflate(buffer));
    }
}
