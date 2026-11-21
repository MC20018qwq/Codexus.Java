package codexus.csharpsup;
import java.nio.*;
import java.nio.charset.*;
public final class DotNetEncoding {
    private DotNetEncoding() {}
    public static byte[] utf8(String text) {
        try {
            ByteBuffer buffer = StandardCharsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE).replaceWith(new byte[]{(byte)0xef,(byte)0xbf,(byte)0xbd}).encode(CharBuffer.wrap(text));
            byte[] bytes = new byte[buffer.remaining()]; buffer.get(bytes); return bytes;
        } catch (CharacterCodingException e) { throw new IllegalArgumentException(e); }
    }
    public static String utf8(byte[] bytes) { return new String(bytes, StandardCharsets.UTF_8); }
    public static byte[] ascii(String text) {
        byte[] bytes = new byte[text.length()];
        for (int i = 0; i < text.length(); i++) bytes[i] = (byte) (text.charAt(i) <= 127 ? text.charAt(i) : '?');
        return bytes;
    }
    public static byte[] latin1(String text) {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            out.write(ch <= 255 ? ch : '?');
        }
        return out.toByteArray();
    }
}
