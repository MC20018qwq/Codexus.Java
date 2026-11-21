package codexus.gateway.extensions;
import java.security.*;
import java.util.HexFormat;
import codexus.csharpsup.DotNetEncoding;
public final class StringExtensions {
    private StringExtensions(){}
    public static String toSha256(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(DotNetEncoding.utf8(value)));}catch(NoSuchAlgorithmException e){throw new AssertionError(e);}}
}
