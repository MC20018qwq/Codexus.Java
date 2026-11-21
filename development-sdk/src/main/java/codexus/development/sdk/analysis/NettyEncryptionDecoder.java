package codexus.development.sdk.analysis;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.util.List;
public class NettyEncryptionDecoder extends ByteToMessageDecoder {
    private final Cipher cipher;
    public NettyEncryptionDecoder(byte[] key){try{cipher=Cipher.getInstance("AES/CFB8/NoPadding");cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key,"AES"),new IvParameterSpec(key));}catch(java.security.GeneralSecurityException e){throw new IllegalArgumentException(e);}}
    @Override protected void decode(ChannelHandlerContext context,ByteBuf input,List<Object> output){byte[] bytes=new byte[input.readableBytes()];input.readBytes(bytes);byte[] decoded=cipher.update(bytes);ByteBuf result=context.alloc().heapBuffer(decoded.length);result.writeBytes(decoded);output.add(result);}
}
