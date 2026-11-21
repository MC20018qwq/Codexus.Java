package codexus.development.sdk.analysis;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import javax.crypto.*;
import javax.crypto.spec.*;
public class NettyEncryptionEncoder extends MessageToByteEncoder<ByteBuf> {
    private final Cipher cipher;
    public NettyEncryptionEncoder(byte[] key){try{cipher=Cipher.getInstance("AES/CFB8/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key,"AES"),new IvParameterSpec(key));}catch(java.security.GeneralSecurityException e){throw new IllegalArgumentException(e);}}
    @Override protected void encode(ChannelHandlerContext context,ByteBuf message,ByteBuf output) {byte[] input=new byte[message.readableBytes()];message.readBytes(input);output.writeBytes(cipher.update(input));}
}
