package codexus.csharpsup;
public final class DotNetUri {
    private static final char[] HEX = "0123456789ABCDEF".toCharArray();
    private DotNetUri() {}
    public static String escapeDataString(String value) {
        StringBuilder out = new StringBuilder();
        for (byte raw : DotNetEncoding.utf8(value)) {
            int b=raw&255;
            if (b>='a'&&b<='z'||b>='A'&&b<='Z'||b>='0'&&b<='9'||b=='-'||b=='_'||b=='.'||b=='~') out.append((char)b);
            else out.append('%').append(HEX[b>>>4]).append(HEX[b&15]);
        }
        return out.toString();
    }
}
