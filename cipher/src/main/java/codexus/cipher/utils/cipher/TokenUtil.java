package codexus.cipher.utils.cipher;

import codexus.csharpsup.DotNetEncoding;
import codexus.cipher.extensions.MPayExtensions;
import codexus.cipher.utils.RandomUtil;
import java.io.ByteArrayOutputStream;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.*;

public final class TokenUtil {
    private TokenUtil() { }
    public static Map<String, String> computeHttpRequestToken(String requestPath, String sendBody, String userId, String userToken) {
        if (!requestPath.startsWith("/")) requestPath = "/" + requestPath;
        var input = new ByteArrayOutputStream();
        input.writeBytes(DotNetEncoding.utf8(MPayExtensions.encodeMd5(userToken).toLowerCase(Locale.ROOT)));
        input.writeBytes(DotNetEncoding.utf8(sendBody));
        input.writeBytes(DotNetEncoding.utf8("0eGsBkhl"));
        input.writeBytes(DotNetEncoding.utf8(requestPath));
        String digest = MPayExtensions.encodeMd5(input.toByteArray()).toLowerCase(Locale.ROOT);
        StringBuilder bits = new StringBuilder();
        // The original converts each hex character's UTF-16 value, rather than parsing a hex nibble.
        for (char ch : digest.toCharArray()) {
            String binary = Integer.toBinaryString(ch);
            bits.append("0".repeat(Math.max(0, 8 - binary.length()))).append(binary);
        }
        String rotated = bits.substring(6) + bits.substring(0, 6);
        byte[] tokenBytes = DotNetEncoding.utf8(digest);
        for (int i = 0; i < rotated.length() / 8; i++) tokenBytes[i] ^= (byte) Integer.parseInt(rotated.substring(i * 8, i * 8 + 8), 2);
        String token = (Base64.getEncoder().encodeToString(Arrays.copyOf(tokenBytes, 12)) + "1").replace('+', 'm').replace('/', 'o');
        Map<String, String> result = new LinkedHashMap<>(); result.put("user-id", userId); result.put("user-token", token); return result;
    }
    public static String generateEncryptToken(String userToken) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String prefix = RandomUtil.getRandomString(8, chars).toUpperCase();
        String suffix = RandomUtil.getRandomString(8, chars).toUpperCase();
        byte[] input = DotNetEncoding.ascii(prefix + (userToken == null ? "" : userToken) + suffix);
        input = Arrays.copyOf(input, (input.length + 15) / 16 * 16);
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(DotNetEncoding.utf8("debbde3548928fab"), "AES"),
                    new IvParameterSpec(DotNetEncoding.utf8("afd4c5c5a7c456a1")));
            return HexFormat.of().withUpperCase().formatHex(cipher.doFinal(input));
        } catch (java.security.GeneralSecurityException e) { throw new IllegalArgumentException("Cannot encrypt user token", e); }
    }
}
