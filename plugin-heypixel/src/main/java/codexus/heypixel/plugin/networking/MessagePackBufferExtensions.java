package codexus.heypixel.plugin.networking;

import io.netty.buffer.ByteBuf;
import codexus.csharpsup.DotNetEncoding;
import codexus.heypixel.plugin.models.*;
public final class MessagePackBufferExtensions {
    private MessagePackBufferExtensions(){}
    public static void writeBoolean(ByteBuf b,boolean value){b.writeByte(value?0xc3:0xc2);}
    public static void writeSingle(ByteBuf b,float value){b.writeByte(0xca);b.writeFloat(value);}
    public static void writeDouble(ByteBuf b,double value){b.writeByte(0xcb);b.writeDouble(value);}
    public static void writeInt32(ByteBuf b,int value){writeInt64(b,value);}
    public static void writeInt64(ByteBuf b,long v){
        if(v>=-32){if(v<128)b.writeByte((int)v);else if(v<256){b.writeByte(0xcc);b.writeByte((int)v);}else if(v<65536){b.writeByte(0xcd);b.writeShort((int)v);}else if(v<4294967296L){b.writeByte(0xce);b.writeInt((int)v);}else{b.writeByte(0xcf);b.writeLong(v);}}
        else if(v>=-128){b.writeByte(0xd0);b.writeByte((int)v);}else if(v>=-32768){b.writeByte(0xd1);b.writeShort((int)v);}else if(v>=Integer.MIN_VALUE){b.writeByte(0xd2);b.writeInt((int)v);}else{b.writeByte(0xd3);b.writeLong(v);}
    }
    /** The original byte overload treats the input as a signed eight-bit value. */
    public static void writeByte(ByteBuf b,int value){if((byte)value>=-32)b.writeByte(value);else{b.writeByte(0xd0);b.writeByte(value);}}
    public static void writeArrayHeader(ByteBuf b,int count){writeHeader(b,count,0x90,0xdc,0xdd);}
    public static void writeMapHeader(ByteBuf b,int count){writeHeader(b,count,0x80,0xde,0xdf);}
    private static void writeHeader(ByteBuf b,int count,int fixed,int small,int large){if(count<0)throw new IllegalArgumentException("Negative collection size");if(count<16)b.writeByte(fixed|count);else if(count<65536){b.writeByte(small);b.writeShort(count);}else{b.writeByte(large);b.writeInt(count);}}
    public static void writeString(ByteBuf b,String value){writeString(b,value,true);}
    public static void writeString(ByteBuf b,String value,boolean uuid){if(value==null||value.isEmpty()){b.writeByte(0xa0);return;}if(uuid){try{value=Uuid128.parse(value).toEncodedString();}catch(UuidFormatException ignored){}}byte[] data=DotNetEncoding.utf8(value);if(data.length<=255){b.writeByte(0xd9);b.writeByte(data.length);}else if(data.length<=65535){b.writeByte(0xda);b.writeShort(data.length);}else{b.writeByte(0xdb);b.writeInt(data.length);}b.writeBytes(data);}
    public static String readString(ByteBuf b){int code=b.readUnsignedByte();int n;if((code&0xe0)==0xa0)n=code&31;else n=switch(code){case 0xd9->b.readUnsignedByte();case 0xda->b.readUnsignedShort();case 0xdb->b.readInt();default->throw new IllegalArgumentException("Expected string header: "+code);};if(n<0||n>b.readableBytes())throw new IllegalArgumentException("Invalid or truncated string length: "+n);byte[] data=new byte[n];b.readBytes(data);return DotNetEncoding.utf8(data);}
    public static long readInt64(ByteBuf b){int code=b.readUnsignedByte();if((code&0x80)==0)return code;if((code&0xe0)==0xe0)return (byte)code;return switch(code){case 0xcc->b.readUnsignedByte();case 0xcd->b.readUnsignedShort();case 0xce->b.readUnsignedInt();case 0xcf,0xd3->b.readLong();case 0xd0->b.readByte();case 0xd1->b.readShort();case 0xd2->b.readInt();default->throw new IllegalArgumentException("Expected integer header: "+code);};}
    public static int readInt32(ByteBuf b){return Math.toIntExact(readInt64(b));}
}
