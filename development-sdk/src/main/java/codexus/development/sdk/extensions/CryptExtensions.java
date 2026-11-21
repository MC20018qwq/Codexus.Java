package codexus.development.sdk.extensions;
import java.security.*;
import java.math.BigInteger;
import java.io.*;
public final class CryptExtensions {
    private CryptExtensions() {}
    public static String toSha1(byte[] data) {
        try {String result=new BigInteger(MessageDigest.getInstance("SHA-1").digest(data)).toString(16);return result.equals("0")?"":result;}
        catch(NoSuchAlgorithmException e){throw new AssertionError(e);}
    }
    public static String toSha1(InputStream stream) throws IOException {return toSha1(stream.readAllBytes());}
}
