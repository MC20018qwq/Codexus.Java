package codexus.cipher.extensions;

import codexus.csharpsup.DotNetEncoding;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public final class MPayExtensions {
    private MPayExtensions() { }
    public static String encodeMd5(String input) {
        return input == null || input.isEmpty() ? "" : encodeMd5(DotNetEncoding.utf8(input));
    }
    public static String encodeMd5(byte[] input) { return ByteArrayExtensions.computeMd5AsHex(input).toLowerCase(Locale.ROOT); }
    public static String encodeBase64(String input) {
        return input == null || input.isEmpty() ? "" : Base64.getEncoder().encodeToString(DotNetEncoding.utf8(input));
    }
    public static String encodeHex(byte[] input) { return HexFormat.of().formatHex(input); }
    public static byte[] decodeHex(String input) { return HexFormat.of().parseHex(input); }
    public static byte[] encodeAes(String input, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
            return cipher.doFinal(DotNetEncoding.utf8(input));
        } catch (java.security.GeneralSecurityException e) { throw new IllegalArgumentException("Cannot encrypt MPay payload", e); }
    }
}
