package codexus.gateway.extensions;

import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/** Original IV-prefixed AES-256-CBC/PKCS7 wire format. */
public final class CipherExtensions {
    private CipherExtensions() { }
    public static byte[] encrypt(byte[] payload, byte[] key) {
        Objects.requireNonNull(payload, "payload");
        byte[] iv = new byte[16]; new SecureRandom().nextBytes(iv);
        return encrypt(payload, key, iv);
    }
    static byte[] encrypt(byte[] payload, byte[] key, byte[] iv) {
        byte[] encrypted = crypt(Cipher.ENCRYPT_MODE, payload, key, iv);
        byte[] result = Arrays.copyOf(iv, iv.length + encrypted.length);
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
        return result;
    }
    public static byte[] decrypt(byte[] payload, byte[] key) {
        if (payload == null || payload.length < 16) throw new IllegalArgumentException("Payload is null or too short to contain IV");
        return crypt(Cipher.DECRYPT_MODE, Arrays.copyOfRange(payload, 16, payload.length), key, Arrays.copyOf(payload, 16));
    }
    private static byte[] crypt(int mode, byte[] data, byte[] key, byte[] iv) {
        if (key == null || key.length != 32) throw new IllegalArgumentException("Key must be 32 bytes (256 bits) for AES-256");
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(mode, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            return cipher.doFinal(data);
        } catch (GeneralSecurityException e) { throw new IllegalArgumentException("Invalid encrypted gateway payload", e); }
    }
}
