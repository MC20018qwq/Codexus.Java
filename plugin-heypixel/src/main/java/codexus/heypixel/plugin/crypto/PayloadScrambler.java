package codexus.heypixel.plugin.crypto;

import io.netty.buffer.ByteBuf;
import codexus.heypixel.plugin.models.Uuid128;
import codexus.csharpsup.DotNetRandom;
public final class PayloadScrambler {
    private PayloadScrambler(){}
    public static void write(ByteBuf b,byte[] payload,Uuid128 key){
        int n=payload.length;int transform=(int)Math.floorMod(key.MostSignificantBits^key.LeastSignificantBits,8L);
        switch(transform){
            case 0 -> b.writeBytes(payload);
            case 1 -> {for(int i=n-1;i>=0;i--)b.writeByte(payload[i]);}
            case 2 -> {for(int i=0;i<n;i+=2)b.writeByte(payload[i]);for(int i=1;i<n;i+=2)b.writeByte(payload[i]);}
            case 3 -> {if(n>0){int offset=(n%5+1)%n;b.writeBytes(payload,offset,n-offset);b.writeBytes(payload,0,offset);}}
            case 4 -> {for(byte raw:payload)b.writeByte(raw^0xa5);}
            case 5 -> {int blocks=n/4;for(int block=blocks-1;block>=0;block--)b.writeBytes(payload,block*4,4);if(n%4>0)b.writeBytes(payload,blocks*4,n%4);}
            case 6 -> {for(byte raw:payload){int v=raw&255;b.writeByte((v>>4)|(v<<4));}}
            case 7 -> {for(byte raw:payload){int v=raw&255;v=((v&0xcc)>>2)|((v&0x33)<<2);v=((v&0xaa)>>1)|((v&0x55)<<1);b.writeByte(v);}}
            default -> throw new AssertionError(transform);
        }
    }
    // Retained from the source; modulo eight never selects this transform.
    static void writeDeterministicallyShuffled(ByteBuf b,byte[] payload){int[] ids=java.util.stream.IntStream.range(0,payload.length).toArray();var random=new DotNetRandom(1592639215);for(int i=ids.length-1;i>0;i--){int j=random.next(i+1),old=ids[i];ids[i]=ids[j];ids[j]=old;}for(int i:ids)b.writeByte(payload[i]);}
}
