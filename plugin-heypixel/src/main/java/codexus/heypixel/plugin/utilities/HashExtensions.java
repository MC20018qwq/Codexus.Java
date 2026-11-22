package codexus.heypixel.plugin.utilities;

import java.security.*;
import java.util.HexFormat;
import codexus.csharpsup.DotNetEncoding;
public final class HashExtensions {
    private HashExtensions(){}
    public static String sha256Hex(String value){return digest("SHA-256",DotNetEncoding.utf8(value));}
    public static String sha1Hex(byte[] value){return digest("SHA-1",value);}
    public static int javaHashCode(String value){return value.hashCode();}
    private static String digest(String name,byte[] value){try{return HexFormat.of().formatHex(MessageDigest.getInstance(name).digest(value));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
}
