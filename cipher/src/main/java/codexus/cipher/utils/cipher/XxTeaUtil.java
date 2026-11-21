package codexus.cipher.utils.cipher;

import codexus.csharpsup.DotNetEncoding;
import java.nio.*;
import java.util.*;

public final class XxTeaUtil {
    private static final String KEY = "942894570397f6d1c9cca2535ad18a2b";
    private static final long DELTA = 2654435769L;
    private static final String PREFIX = "!x19sign!";
    private XxTeaUtil() { }
    public static String x19SignEncrypt(String input) {
        long[] blocks = toLongArray(DotNetEncoding.utf8(input));
        long[] key = toLongArray(DotNetEncoding.utf8(KEY));
        encryptBlocks(blocks, key);
        StringBuilder result = new StringBuilder(PREFIX);
        for (long block : blocks) result.append(String.format(Locale.ROOT, "%016x", block));
        return result.toString();
    }
    public static String x19SignDecrypt(String input) {
        String hex = input != null && input.startsWith(PREFIX) ? input.substring(PREFIX.length()) : input;
        if (hex == null || hex.codePoints().allMatch(ch -> Character.isWhitespace(ch) || Character.isSpaceChar(ch))) return hex;
        long[] blocks = new long[hex.length() / 16];
        for (int i = 0; i < blocks.length; i++) blocks[i] = Long.parseUnsignedLong(hex.substring(i * 16, i * 16 + 16), 16);
        decryptBlocks(blocks, toLongArray(DotNetEncoding.utf8(KEY)));
        ByteBuffer output = ByteBuffer.allocate(blocks.length * 8).order(ByteOrder.LITTLE_ENDIAN);
        for (long block : blocks) output.putLong(block);
        byte[] bytes = output.array();
        int length = bytes.length;
        while (length > 0 && bytes[length - 1] == 0) length--;
        String text = DotNetEncoding.utf8(Arrays.copyOf(bytes, length));
        int end = text.length();
        while (end > 0 && text.charAt(end - 1) == 0) end--;
        return text.substring(0, end);
    }
    private static long[] toLongArray(byte[] data) {
        int count = (data.length + 7) / 8;
        long[] result = new long[count];
        ByteBuffer input = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < count; i++) result[i] = input.getLong(i * 8 <= data.length - 8 ? i * 8 : data.length - 8);
        return result;
    }
    private static void encryptBlocks(long[] blocks, long[] key) {
        int count = blocks.length;
        if (count < 1) return;
        long sum = 0, rounds = 6 + 52 / count, previous = blocks[count - 1];
        while (rounds-- > 0) {
            sum += DELTA;
            int keyIndex = (int) ((sum >> 2) & 3);
            for (int i = 0; i < count - 1; i++) {
                long next = blocks[i + 1];
                long mix = ((previous >> 5) ^ (next << 2)) + ((next >> 3) ^ (previous << 4));
                mix ^= (sum ^ next) + (key[(i & 3) ^ keyIndex] ^ previous);
                previous = blocks[i] += mix;
            }
            long first = blocks[0];
            long mix = ((previous >> 5) ^ (first << 2)) + ((first >> 3) ^ (previous << 4));
            mix ^= (sum ^ first) + (key[((count - 1) & 3) ^ keyIndex] ^ previous);
            blocks[count - 1] += mix;
            previous = blocks[count - 1];
        }
    }
    private static void decryptBlocks(long[] blocks, long[] key) {
        int count = blocks.length;
        if (count < 1) return;
        long sum = (6 + 52 / count) * DELTA, next = blocks[0];
        while (sum != 0) {
            int keyIndex = (int) ((sum >> 2) & 3);
            for (int i = count - 1; i > 0; i--) {
                long previous = blocks[i - 1];
                long mix = ((previous >> 5) ^ (next << 2)) + ((next >> 3) ^ (previous << 4));
                mix ^= (sum ^ next) + (key[(i & 3) ^ keyIndex] ^ previous);
                next = blocks[i] -= mix;
            }
            long previous = blocks[count - 1];
            long mix = ((previous >> 5) ^ (next << 2)) + ((next >> 3) ^ (previous << 4));
            mix ^= (sum ^ next) + (key[keyIndex] ^ previous);
            blocks[0] -= mix;
            next = blocks[0];
            sum -= DELTA;
        }
    }
}
