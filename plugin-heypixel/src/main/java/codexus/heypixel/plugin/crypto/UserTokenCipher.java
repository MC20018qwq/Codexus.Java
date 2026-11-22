package codexus.heypixel.plugin.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.security.*;
public final class UserTokenCipher {
    private UserTokenCipher(){}
    private static final String ALPHABET="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM=new SecureRandom();
    private static final byte[] KEY="debbde3548928fab".getBytes(StandardCharsets.US_ASCII), IV="afd4c5c5a7c456a1".getBytes(StandardCharsets.US_ASCII);
    public static String encrypt(String token){return encrypt(token,randomText(),randomText());}
    public static String encrypt(String token,String prefix,String suffix){if(prefix.length()!=8||suffix.length()!=8)throw new IllegalArgumentException("Token padding must contain eight characters");byte[] data=(prefix.toUpperCase(Locale.ROOT)+token+suffix.toUpperCase(Locale.ROOT)).getBytes(StandardCharsets.US_ASCII);data=Arrays.copyOf(data,(data.length+15)/16*16);return HexFormat.of().withUpperCase().formatHex(transform(Cipher.ENCRYPT_MODE,data));}
    public static String decrypt(String token){try{String padded=new String(transform(Cipher.DECRYPT_MODE,HexFormat.of().parseHex(token)),StandardCharsets.US_ASCII);int end=padded.length();while(end>0&&padded.charAt(end-1)==0)end--;return end>16?padded.substring(8,end-8):"";}catch(RuntimeException e){throw new IllegalStateException("Cannot decrypt token",e);}}
    private static byte[] transform(int mode,byte[] bytes){try{Cipher c=Cipher.getInstance("AES/CBC/NoPadding");c.init(mode,new SecretKeySpec(KEY,"AES"),new IvParameterSpec(IV));return c.doFinal(bytes);}catch(GeneralSecurityException e){throw new IllegalArgumentException("AES operation failed",e);}}
    private static String randomText(){StringBuilder b=new StringBuilder();for(int i=0;i<8;i++)b.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));return b.toString();}
}
