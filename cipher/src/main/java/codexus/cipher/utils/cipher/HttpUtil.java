package codexus.cipher.utils.cipher;

import codexus.csharpsup.DotNetEncoding;
import codexus.cipher.utils.StringGenerator;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.*;

public final class HttpUtil {
    private static final String[] KEYS = "MK6mipwmOUedplb6,OtEylfId6dyhrfdn,VNbhn5mvUaQaeOo9,bIEoQGQYjKd02U0J,fuaJrPwaH2cfXXLP,LEkdyiroouKQ4XN1,jM1h27H4UROu427W,DhReQada7gZybTDk,ZGXfpSTYUvcdKqdY,AZwKf7MWZrJpGR5W,amuvbcHw38TcSyPU,SI4QotspbjhyFdT0,VP4dhjKnDGlSJtbB,UXDZx4KhZywQ2tcn,NIK73ZNvNqzva4kd,WeiW7qU766Q1YQZI".split(",");
    private HttpUtil() { }
    public static byte[] httpEncrypt(byte[] bodyIn) {
        byte[] padded = Arrays.copyOf(bodyIn, (int) Math.ceil((bodyIn.length + 16) / 16.0) * 16);
        byte[] iv = DotNetEncoding.ascii(StringGenerator.generateRandomString(16, false, true, true));
        System.arraycopy(iv, 0, padded, bodyIn.length, 16);
        byte flags = (byte) ((ThreadLocalRandom.current().nextInt(0, KEYS.length - 1) << 4) | 2);
        byte[] encrypted = crypt(padded, iv, (flags >> 4) & 15, true);
        byte[] payload = new byte[16 + encrypted.length + 1];
        System.arraycopy(iv, 0, payload, 0, 16); System.arraycopy(encrypted, 0, payload, 16, encrypted.length);
        payload[payload.length - 1] = flags;
        return payload;
    }
    public static byte[] httpDecrypt(byte[] body) {
        if (body.length < 18) return null;
        byte[] decrypted = crypt(Arrays.copyOfRange(body, 16, body.length - 1), Arrays.copyOf(body, 16), (body[body.length - 1] >> 4) & 15, false);
        int nonZero = 0, end = decrypted.length - 1;
        while (nonZero < 16) if (decrypted[end--] != 0) nonZero++;
        return Arrays.copyOf(decrypted, end + 1);
    }
    private static byte[] crypt(byte[] input, byte[] iv, int key, boolean encrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(encrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE,
                    new SecretKeySpec(DotNetEncoding.ascii(KEYS[key]), "AES"), new IvParameterSpec(iv));
            return cipher.doFinal(input);
        } catch (java.security.GeneralSecurityException e) { throw new IllegalArgumentException("Cannot process HTTP cipher payload", e); }
    }
}
