package codexus.development.sdk.extensions;

import io.netty.buffer.ByteBuf;
import io.netty.util.Attribute;
import codexus.csharpsup.DotNetEncoding;
import codexus.development.sdk.entities.Property;
import codexus.development.sdk.utils.Position;
import java.util.*;
import java.util.function.*;

public final class NettyExtensions {
    private NettyExtensions() {}
    public static int readVarInt(byte[] bytes) {
        int result=0,count=0;
        for(byte raw:bytes) {
            int current=raw & 255;
            result |= (current & 127) << count++*7;
            if(count>5) throw new IllegalArgumentException("VarInt too big");
            if((current & 128)!=128) return result;
        }
        throw new IndexOutOfBoundsException();
    }
    public static int readVarIntFromBuffer(ByteBuf buffer) {
        int result=0,offset=0;
        while(true) {
            int current=buffer.readUnsignedByte(); result |= (current & 127) << offset;
            if((current & 128)==0) return result;
            offset+=7; if(offset>=32) throw new IllegalArgumentException("VarInt is too big");
        }
    }
    public static Position readPosition(ByteBuf buffer) {
        long packed=buffer.readLong();return new Position((int)(packed>>38),(int)(packed<<52>>52),(int)(packed<<26>>38));
    }
    public static List<Property> readProperties(ByteBuf buffer) {
        List<Property> result=new ArrayList<>(); readWithCount(buffer,()->result.add(readProperty(buffer)));return result;
    }
    public static Property readProperty(ByteBuf buffer) {
        Property p=new Property();p.Name=readStringFromBuffer(buffer,32767);p.Value=readStringFromBuffer(buffer,32767);
        p.Signature=readNullable(buffer,()->readStringFromBuffer(buffer,32767));return p;
    }
    public static <T> T readNullable(ByteBuf buffer,Supplier<T> action) {return buffer.readBoolean()?action.get():null;}
    public static void readWithCount(ByteBuf buffer,Runnable action) {int count=readVarIntFromBuffer(buffer);for(int i=0;i<count;i++)action.run();}
    public static String readStringFromBuffer(ByteBuf buffer,int maxLength) {
        int length=readVarIntFromBuffer(buffer);
        if(length>maxLength*4)throw new IllegalArgumentException("The received encoded string buffer length is longer than maximum allowed ("+length+" > "+maxLength*4+")");
        if(length<0)throw new IllegalArgumentException("The received encoded string buffer length is less than zero! Weird string!");
        // Preserve the original's truncated-buffer behavior; packet framing supplies complete messages.
        length=Math.min(length,buffer.readableBytes());String value=DotNetEncoding.utf8(readByteArrayFromBuffer(buffer,length));
        if(value.length()>maxLength)throw new IllegalArgumentException("The received string length is longer than maximum allowed ("+length+" > "+maxLength+")");
        return value;
    }
    public static byte[] readByteArrayFromBuffer(ByteBuf buffer,int length) {byte[] bytes=new byte[length];buffer.readBytes(bytes);return bytes;}
    public static byte[] readByteArrayFromBuffer(ByteBuf buffer) {
        int length=readVarIntFromBuffer(buffer);if(length<0)throw new IllegalArgumentException("The received encoded string buffer length is less than zero! Weird string!");return readByteArrayFromBuffer(buffer,length);
    }
    public static byte[] readByteArrayReadableBytes(ByteBuf buffer) {return readByteArrayFromBuffer(buffer,buffer.readableBytes());}
    public static ByteBuf writeStringToBuffer(ByteBuf buffer,String value) {return writeStringToBuffer(buffer,value,32767);}
    public static ByteBuf writeStringToBuffer(ByteBuf buffer,String value,int maxLength) {
        if(value.length()>maxLength)throw new IllegalArgumentException("String too big (was "+value.length()+" bytes encoded, max "+maxLength+")");
        byte[] bytes=DotNetEncoding.utf8(value);writeVarInt(buffer,bytes.length);return buffer.writeBytes(bytes);
    }
    public static ByteBuf writeByteArrayToBuffer(ByteBuf buffer,byte[] bytes) {writeVarInt(buffer,bytes.length);return buffer.writeBytes(bytes);}
    public static ByteBuf writePosition(ByteBuf buffer,int x,int y,int z) {return writePosition(buffer,new Position(x,y,z));}
    public static ByteBuf writePosition(ByteBuf buffer,Position position) {return buffer.writeLong(((position.X&0x3ffffffL)<<38)|((position.Z&0x3ffffffL)<<12)|(position.Y&0xfffL));}
    public static ByteBuf writeProperties(ByteBuf buffer,List<Property> properties) {
        if(properties==null)return writeVarInt(buffer,0);
        writeWithCount(buffer,properties.size(),i->writeProperty(buffer,properties.get(i)));return buffer;
    }
    public static ByteBuf writeProperty(ByteBuf buffer,Property property) {
        writeStringToBuffer(buffer,property.Name);writeStringToBuffer(buffer,property.Value);
        writeNullable(buffer,property.Signature==null,()->writeStringToBuffer(buffer,property.Signature));return buffer;
    }
    public static ByteBuf writeVarInt(ByteBuf buffer,int value) {while((value&0xffffff80)!=0){buffer.writeByte((value&127)|128);value>>>=7;}return buffer.writeByte(value);}
    public static void writeWithCount(ByteBuf buffer,int count,IntConsumer action) {writeVarInt(buffer,count);for(int i=0;i<count;i++)action.accept(i);}
    public static void writeNullable(ByteBuf buffer,boolean isNull,Runnable action) {buffer.writeBoolean(!isNull);if(!isNull)action.run();}
    public static int getVarIntSize(int value) {for(int i=1;i<5;i++)if((value&(-1<<i*7))==0)return i;return 5;}
    public static <T> T withReaderScope(ByteBuf buffer,Function<ByteBuf,T> action) {buffer.markReaderIndex();try{return action.apply(buffer);}finally{buffer.resetReaderIndex();}}
    public static ByteBuf writeSignedByte(ByteBuf buffer,byte value) {return buffer.writeByte(value);}
    public static <T> T getOrDefault(Attribute<T> attribute,Supplier<T> supplier) {if(attribute.get()==null)attribute.setIfAbsent(supplier.get());return attribute.get();}
}
