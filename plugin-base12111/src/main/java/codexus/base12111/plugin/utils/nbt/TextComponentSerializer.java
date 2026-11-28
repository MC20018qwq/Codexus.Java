package codexus.base12111.plugin.utils.nbt;

import io.netty.buffer.*;
import java.nio.charset.StandardCharsets;
import static codexus.base12111.plugin.extensions.ByteBufferExtensions.nbtLength;
public final class TextComponentSerializer {
    private TextComponentSerializer(){}
    public static ByteBuf serialize(TextComponent component){return serialize(component,PooledByteBufAllocator.DEFAULT);}
    public static ByteBuf serialize(TextComponent component,ByteBufAllocator allocator){
        ByteBuf output=(allocator==null?PooledByteBufAllocator.DEFAULT:allocator).buffer();
        try {
            if(component.RawData!=null){output.writeBytes(component.RawData);return output;}
            output.writeByte(10);output.writeByte(8);writeString(output,"text");writeString(output,component.Text);
            output.writeByte(8);writeString(output,"color");writeString(output,component.Color);output.writeByte(0);return output;
        }catch(RuntimeException|Error e){output.release();throw e;}
    }
    public static TextComponent deserialize(ByteBuf buffer){TextComponent value=new TextComponent();value.RawData=new byte[nbtLength(buffer)];buffer.readBytes(value.RawData);return value;}
    private static void writeString(ByteBuf buffer,String value){byte[] bytes=value.getBytes(StandardCharsets.UTF_8);if(bytes.length>65535)throw new IllegalArgumentException("NBT string exceeds 65535 bytes");buffer.writeShort(bytes.length);buffer.writeBytes(bytes);}
}
