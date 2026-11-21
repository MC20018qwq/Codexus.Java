package codexus.cipher.extensions;
import java.security.*;
import java.util.*;
public final class ByteArrayExtensions {
    private ByteArrayExtensions(){}
    public static byte[] xor(byte[] content,byte[] key){if(content.length!=key.length)throw new IllegalArgumentException("Key length must be equal to content length.");byte[] b=new byte[content.length];for(int i=0;i<b.length;i++)b[i]=(byte)(content[i]^key[i]);return b;}
    public static byte[] combineWith(byte[] first,byte[] second){Objects.requireNonNull(first);Objects.requireNonNull(second);byte[] b=Arrays.copyOf(first,Math.addExact(first.length,second.length));System.arraycopy(second,0,b,first.length,second.length);return b;}
    public static String computeMd5AsHex(byte[] data){try{return HexFormat.of().withUpperCase().formatHex(MessageDigest.getInstance("MD5").digest(data));}catch(NoSuchAlgorithmException e){throw new AssertionError(e);}}
}
