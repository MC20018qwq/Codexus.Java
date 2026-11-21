package codexus.cipher.utils;
import java.security.SecureRandom;
public final class RandomUtil {
    private static final SecureRandom RANDOM=new SecureRandom();
    private RandomUtil() {}
    public static String getRandomString(int length){return getRandomString(length,null);}
    public static String getRandomString(int length,String chars){
        if(length<=0)return "";
        if(chars==null||chars.isEmpty())chars="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghizklmnopqrstuvwxyz0123456789";
        byte[] bytes=new byte[length];RANDOM.nextBytes(bytes);StringBuilder b=new StringBuilder(length);
        for(byte value:bytes)b.append(chars.charAt((value&255)%chars.length()));return b.toString();
    }
    public static String generateSessionId(){return "captchaReq"+getRandomString(16);}
}
