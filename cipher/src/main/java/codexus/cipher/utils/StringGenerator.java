package codexus.cipher.utils;
import java.util.*;
public final class StringGenerator {
    private static final Random RANDOM=new Random();
    private StringGenerator() {}
    public static String generateHexString(int length){byte[] b=new byte[length];RANDOM.nextBytes(b);return HexFormat.of().withUpperCase().formatHex(b);}
    public static String generateRandomString(int length){return generateRandomString(length,true,true,true);}
    public static String generateRandomString(int length,boolean numbers,boolean upper,boolean lower){
        if(length<=0)throw new IllegalArgumentException("Length must be greater than 0");
        if(!numbers&&!upper&&!lower)throw new IllegalArgumentException("Must include at least one character type");
        String chars=(numbers?"0123456789":"")+(upper?"ABCDEFGHIJKLMNOPQRSTUVWXYZ":"")+(lower?"abcdefghijklmnopqrstuvwxyz":"");
        StringBuilder b=new StringBuilder(length);for(int i=0;i<length;i++)b.append(chars.charAt(RANDOM.nextInt(chars.length())));return b.toString();
    }
    public static String generateRandomMacAddress(){return generateRandomMacAddress(":",true);}
    public static String generateRandomMacAddress(String separator,boolean uppercase){
        byte[] bytes=new byte[6];RANDOM.nextBytes(bytes);bytes[0]=(byte)((bytes[0]&0xfe)|2);
        HexFormat h=uppercase?HexFormat.of().withUpperCase():HexFormat.of();String[] values=new String[6];for(int i=0;i<6;i++)values[i]=h.formatHex(bytes,i,i+1);return String.join(separator,values);
    }
}
