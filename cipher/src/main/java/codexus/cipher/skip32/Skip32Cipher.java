package codexus.cipher.skip32;

import codexus.csharpsup.DotNetEncoding;
import java.nio.*;
import java.security.*;
import java.util.*;

public class Skip32Cipher {
    private static final int[] TABLE = {
		163, 215, 9, 131, 248, 72, 246, 244, 179, 33,
		21, 120, 153, 177, 175, 249, 231, 45, 77, 138,
		206, 76, 202, 46, 82, 149, 217, 30, 78, 56,
		68, 40, 10, 223, 2, 160, 23, 241, 96, 104,
		18, 183, 122, 195, 233, 250, 61, 83, 150, 132,
		107, 186, 242, 99, 154, 25, 124, 174, 229, 245,
		247, 22, 106, 162, 57, 182, 123, 15, 193, 147,
		129, 27, 238, 180, 26, 234, 208, 145, 47, 184,
		85, 185, 218, 133, 63, 65, 191, 224, 90, 88,
		128, 95, 102, 11, 216, 144, 53, 213, 192, 167,
		51, 6, 101, 105, 69, 0, 148, 86, 109, 152,
		155, 118, 151, 252, 178, 194, 176, 254, 219, 32,
		225, 235, 214, 228, 221, 71, 74, 29, 66, 237,
		158, 110, 73, 60, 205, 67, 39, 210, 7, 212,
		222, 199, 103, 24, 137, 203, 48, 31, 141, 198,
		143, 170, 200, 116, 220, 201, 93, 92, 49, 164,
		112, 136, 97, 44, 159, 13, 43, 135, 80, 130,
		84, 100, 38, 125, 3, 64, 52, 75, 28, 115,
		209, 196, 253, 59, 204, 251, 127, 171, 230, 62,
		91, 165, 173, 4, 35, 156, 20, 81, 34, 240,
		41, 121, 113, 126, 255, 140, 14, 226, 12, 239,
		188, 114, 117, 111, 55, 161, 236, 211, 142, 98,
		139, 134, 16, 232, 8, 119, 17, 190, 146, 79,
		36, 197, 50, 54, 157, 207, 243, 166, 187, 172,
		94, 108, 169, 19, 87, 37, 181, 227, 189, 168,
		58, 1, 5, 89, 42, 70
	};
    private final byte[] key;
    public Skip32Cipher(byte[] key) {
        if (key.length != 10) throw new IllegalArgumentException("Key must be 10 bytes.");
        this.key = key;
    }
    private static int g(byte[] key, int round, int word) {
        int high = word >> 8, low = word & 255;
        int first = TABLE[low ^ (key[4 * round % 10] & 255)] ^ high;
        int second = TABLE[first ^ (key[(4 * round + 1) % 10] & 255)] ^ low;
        int third = TABLE[second ^ (key[(4 * round + 2) % 10] & 255)] ^ first;
        int fourth = TABLE[third ^ (key[(4 * round + 3) % 10] & 255)] ^ second;
        return (third << 8) + fourth;
    }
    private int transform(int value, boolean encrypt) {
        int increment = encrypt ? 1 : -1, round = encrypt ? 0 : 23;
        int left = (value >>> 16) & 65535, right = value & 65535;
        for (int i = 0; i < 12; i++) {
            right ^= g(key, round, left) ^ round; round += increment;
            left ^= g(key, round, right) ^ round; round += increment;
        }
        return (right << 16) | left;
    }
    public int encrypt(int value) { return transform(value, true); }
    public int decrypt(int value) { return transform(value, false); }
    public long encrypt(long value) { return Integer.toUnsignedLong(encrypt(uint32(value))); }
    public long decrypt(long value) { return Integer.toUnsignedLong(decrypt(uint32(value))); }
    private static int uint32(long value) {
        if (value < 0 || value > 0xffffffffL) throw new IllegalArgumentException("Value is outside uint32 range");
        return (int) value;
    }
    public String generateRoleUuid(String roleName, long userId) {
        try {
            byte[] uuid = MessageDigest.getInstance("MD5").digest(DotNetEncoding.utf8(roleName));
            ByteBuffer.wrap(uuid).order(ByteOrder.LITTLE_ENDIAN).putInt(12, encrypt(uint32(userId)));
            uuid[6] = (byte) ((uuid[6] & 15) | 64); uuid[8] = (byte) ((uuid[8] & 63) | 128);
            return HexFormat.of().formatHex(uuid);
        } catch (NoSuchAlgorithmException e) { throw new AssertionError(e); }
    }
    public long computeUserIdFromUuid(String uuid) {
        uuid = uuid.replace("-", "");
        if (uuid.length() != 32) return 0;
        int value = ByteBuffer.wrap(HexFormat.of().parseHex(uuid)).order(ByteOrder.LITTLE_ENDIAN).getInt(12);
        return Integer.toUnsignedLong(decrypt(value));
    }
}
