package codexus.development.sdk.analysis;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.*;
import java.util.*;
import java.util.zip.*;
import static codexus.development.sdk.extensions.NettyExtensions.*;
public class NettyCompressionDecoder extends ByteToMessageDecoder {
    private final Inflater inflater=new Inflater();
    public int Threshold;
    public NettyCompressionDecoder(int threshold){Threshold=threshold;}
    @Override protected void decode(ChannelHandlerContext context,ByteBuf input,List<Object> output) throws DataFormatException {
        if(!input.isReadable())return;
        int expected=readVarIntFromBuffer(input);
        if(expected==0){output.add(input.readBytes(input.readableBytes()));return;}
        if(expected<Threshold)throw new DecoderException("Decompressed length "+expected+" is below threshold "+Threshold);
        inflater.reset();inflater.setInput(readByteArrayReadableBytes(input));
        if(inflater.needsDictionary())throw new DecoderException("Inflater requires dictionary");
        byte[] scratch=new byte[Math.max(4096,expected)];ByteBuf result=context.alloc().heapBuffer(expected);boolean delivered=false;
        try {
            int total=0;
            while(!inflater.finished()&&total<expected){int n=inflater.inflate(scratch);if(n==0&&inflater.needsInput())throw new DecoderException("Incomplete compressed data");if(n==0&&inflater.needsDictionary())throw new DecoderException("Inflater requires dictionary");result.writeBytes(scratch,0,n);total+=n;}
            if(total!=expected)throw new DecoderException("Decompressed length mismatch: expected "+expected+", got "+total);
            output.add(result);delivered=true;
        } finally {if(!delivered)result.release();}
    }
    @Override protected void handlerRemoved0(ChannelHandlerContext context)throws Exception {inflater.end();super.handlerRemoved0(context);}
}
