package codexus.csharpsup;
import java.nio.*;
import java.util.UUID;
public final class DotNetGuid {
    public static final UUID EMPTY = new UUID(0, 0);
    private DotNetGuid() {}
    public static byte[] toByteArray(UUID uuid) {
        byte[] bytes = ByteBuffer.allocate(16).putLong(uuid.getMostSignificantBits()).putLong(uuid.getLeastSignificantBits()).array();
        reverse(bytes, 0, 4); reverse(bytes, 4, 6); reverse(bytes, 6, 8); return bytes;
    }
    public static UUID fromByteArray(byte[] input) {
        if (input.length != 16) throw new IllegalArgumentException("Byte array for Guid must be exactly 16 bytes long.");
        byte[] bytes = input.clone(); reverse(bytes, 0, 4); reverse(bytes, 4, 6); reverse(bytes, 6, 8);
        ByteBuffer b = ByteBuffer.wrap(bytes); return new UUID(b.getLong(), b.getLong());
    }
    public static UUID parse(String input) {
        String s = input.trim();
        if (s.startsWith("{") && s.endsWith("}") || s.startsWith("(") && s.endsWith(")")) s = s.substring(1, s.length()-1);
        if (s.length() == 32) s = s.substring(0,8)+"-"+s.substring(8,12)+"-"+s.substring(12,16)+"-"+s.substring(16,20)+"-"+s.substring(20);
        if (!s.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) throw new IllegalArgumentException("Unrecognized Guid format.");
        return UUID.fromString(s);
    }
    public static String format(UUID uuid, String format) {
        return switch (format) {
            case "D", "d" -> uuid.toString();
            case "N", "n" -> uuid.toString().replace("-", "");
            case "B", "b" -> "{" + uuid + "}";
            case "P", "p" -> "(" + uuid + ")";
            default -> throw new IllegalArgumentException("Unsupported Guid format: " + format);
        };
    }
    private static void reverse(byte[] b, int begin, int end) { for(int i=begin,j=end-1;i<j;i++,j--){byte x=b[i];b[i]=b[j];b[j]=x;} }
}
